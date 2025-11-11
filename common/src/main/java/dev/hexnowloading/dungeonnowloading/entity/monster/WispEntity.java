package dev.hexnowloading.dungeonnowloading.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Wisp: a 1 HP, 10 dmg “fire projectile” style mob that tackles players,
 * smelts ores on impact, ignites burnables, ignores most damage, and can possess.
 */
public class WispEntity extends FlyingMob implements Enemy {

    private static final double TACKLE_RANGE = 16.0D;
    private static final double TACKLE_SPEED = 0.35D; // movement per tick toward target (tunable)
    private static final int CONTACT_DAMAGE = 10;

    private static final int    WINDUP_TICKS    = 8;
    private static final double WINDUP_BACKSTEP = 0.28;
    private int windupTicks = 0;

    private boolean lit = false;

    // Post-release despawn
    private static final int RELEASE_LIFETIME_TICKS = 60;
    private int releaseAge = 0;

    // Cruise vector/speed after release (from earlier step)
    private Vec3 releaseDir = Vec3.ZERO;
    private double cruiseSpeed = 0.45; // keep >= MIN_LOCKED_SPEED

    public WispEntity(EntityType<? extends WispEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false; // allow collisions
        this.moveControl = new FlyingMoveControl(this, 16, true);
        this.setRemainingFireTicks(Integer.MAX_VALUE); // visually fiery
        this.setInvulnerable(false);
    }

    // ---------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)      // HP 1
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // No pathing goals; behavior is handled in tick() to feel like a projectile.
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(false);
        return nav;
    }

    @Override
    public boolean isOnFire() {
        return lit; // only show burning once locked / charging
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isSensitiveToWater() {
        // Makes splash water potions / water bottles hurt this, like Blaze/Enderman behavior
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        // Water kill…
        if (this.isInWaterOrBubble()) { discardWithEffect(); return; }

        // Possession first…
        if (tryPossessNearby()) { discardWithEffect(); return; }

        // Targeting with lock & windup
        LivingEntity target = getLockedTargetOrAcquire();
        if (target != null) {
            tackleToward(target);
            if (this.getBoundingBox().inflate(0.1).intersects(target.getBoundingBox())) {
                if (target.hurt(this.damageSources().mobAttack(this), CONTACT_DAMAGE)) {
                    this.gameEvent(GameEvent.ENTITY_DAMAGE, target);
                }
                discardWithEffect();
                return;
            }
        } else {
            // idle drift
            if (this.tickCount % 20 == 0) {
                RandomSource r = this.getRandom();
                this.setDeltaMovement(this.getDeltaMovement().add(
                        (r.nextDouble() - 0.5) * 0.05,
                        (r.nextDouble() - 0.5) * 0.05,
                        (r.nextDouble() - 0.5) * 0.05
                ));
            }
        }

        // Keep constant speed after release so drag doesn’t slow it
        if (released && !releaseDir.equals(Vec3.ZERO)) {
            this.setDeltaMovement(releaseDir.scale(cruiseSpeed));
            // 60-tick fuse after release
            if (++releaseAge >= RELEASE_LIFETIME_TICKS) {
                discardWithEffect();
                return;
            }
        }

        // Block collision behaviors (ore smelt / burnable ignite)
        handleBlockAndEntityImpactThisTick();
    }


    // ---------------------------------------------------------------------
    // Possession (20% chance when there’s a nearby entity targeting someone)
    // ---------------------------------------------------------------------
    private boolean tryPossessNearby() {
        if (this.random.nextFloat() > 0.20f) return false;

        // Search small radius for mobs that have targets
        AABB box = this.getBoundingBox().inflate(6.0, 4.0, 6.0);
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class, box, e -> {
            if (e == this) return false;
            if (!e.isAlive()) return false;
            if (!(e instanceof Mob mob)) return false;
            LivingEntity t = mob.getTarget();
            return t != null && t.isAlive();
        });

        if (candidates.isEmpty()) return false;

        LivingEntity chosen = candidates.get(this.random.nextInt(candidates.size()));

        // Apply Wisp Possession effect and succeed
        // TODO: Replace with your effect registry entry
        // Example: chosen.addEffect(new MobEffectInstance(DNLEffects.WISP_POSSESSION.get(), 20*10, 0), this);
        chosen.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 10, 0), this); // placeholder: visible feedback
        this.gameEvent(GameEvent.ENTITY_INTERACT, chosen);
        return true;
    }

    // ---------------------------------------------------------------------
    // Targeting / tackle
    // ---------------------------------------------------------------------
    @Nullable
    private Player findNearestVisiblePlayer() {
        Player nearest = this.level().getNearestPlayer(this, TACKLE_RANGE);
        if (nearest == null) return null;
        if (!this.hasLineOfSight(nearest)) return null;

        // Optional: prioritize the player if multiple are close
        return nearest;
    }

    private @org.jetbrains.annotations.Nullable LivingEntity getLockedTargetOrAcquire() {
        // Keep current lock if valid
        if (lockedOn && lockedTargetId != -1) {
            Entity e = ((ServerLevel) level()).getEntity(lockedTargetId);
            if (e instanceof LivingEntity le && le.isAlive()) return le;
            lockedOn = false;
            lockedTargetId = -1;
        }

        // Acquire a new lock
        Player p = this.level().getNearestPlayer(this, TACKLE_RANGE);
        if (p != null && this.hasLineOfSight(p)) {
            lockedOn = true;
            lockedTargetId = p.getId();

            // (optional) light/particles when locking
            lit = true;
            this.setRemainingFireTicks(2000000000);
            // spawnLockParticlesAndSound(p); // if you're using this helper

            // ---- WIND-UP INSERT (opposite of wisp→player) ----
            windupTicks = WINDUP_TICKS;

            Vec3 toPlayer = p.position()
                    .add(0, p.getBbHeight() * 0.5, 0)
                    .subtract(this.position());

// opposite direction; normalize so the impulse magnitude is constant
            if (toPlayer.lengthSqr() > 1.0e-6) {
                Vec3 backDir = toPlayer.normalize().scale(-1.0);
                this.setDeltaMovement(backDir.scale(WINDUP_BACKSTEP)); // fixed-magnitude kick
            }
            // -----------------------------------------------

            return p;
        }
        return null;
    }


    private void spawnLockParticlesAndSound(LivingEntity target) {
        if (!level().isClientSide) {
            ((ServerLevel)level()).sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(),
                    12,           // count
                    0.2, 0.2, 0.2,// spread
                    0.01          // speed
            );
        }
        this.playSound(net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE, 0.7f, 1.4f);
    }


    // Speed tuning
    private static final double MIN_LOCKED_SPEED = 0.35;  // never go below this once locked
    private static final double MAX_SPEED        = 0.9;   // cap so it doesn’t explode in speed
    private static final double ACCEL_PER_TICK   = 0.05;  // how hard it steers toward target

    // Lock-on state
    private boolean lockedOn = false;
    private int lockedTargetId = -1; // entity ID; -1 = none

    private static final double RELEASE_DISTANCE = 2.5D; // stop homing when within this range
    private boolean released = false; // whether it has stopped homing

    private void tackleToward(LivingEntity target) {
        this.setNoGravity(true);

        // If already released, cruise straight forever (until timed out)
        if (released) {
            if (!releaseDir.equals(Vec3.ZERO)) {
                this.setDeltaMovement(releaseDir.scale(cruiseSpeed));
                faceVelocity(this.getDeltaMovement());
            }
            return;
        }

        // Wind-up phase: small backward drift; no steering yet
        if (windupTicks > 0) {
            // let existing velocity decay slightly, but keep facing current travel
            Vec3 v = this.getDeltaMovement();
            if (v.lengthSqr() > 1.0e-6) {
                faceVelocity(v);
            }
            windupTicks--;
            return;
        }

        // Normal homing after wind-up
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(this.position());
        double distance = to.length();

        // Close enough: release -> keep straight line, set a 60-tick fuse
        if (distance <= RELEASE_DISTANCE) {
            released = true;
            lockedOn = false;
            lockedTargetId = -1;

            Vec3 current = this.getDeltaMovement();
            Vec3 dir = current.lengthSqr() > 1.0e-6 ? current.normalize() : to.normalize();
            double speed = Math.max(current.length(), MIN_LOCKED_SPEED);
            speed = Mth.clamp(speed, MIN_LOCKED_SPEED, MAX_SPEED);

            releaseDir = dir;
            cruiseSpeed = speed;

            this.setDeltaMovement(releaseDir.scale(cruiseSpeed));
            faceVelocity(this.getDeltaMovement());
            return;
        }

        // Homing steer
        Vec3 dir = to.normalize();
        Vec3 vel = this.getDeltaMovement().add(dir.scale(ACCEL_PER_TICK));
        double speed = vel.length();
        double clamped = Mth.clamp(speed, MIN_LOCKED_SPEED, MAX_SPEED);
        vel = vel.normalize().scale(clamped);
        this.setDeltaMovement(vel);
        faceVelocity(vel);
    }

    private void faceVelocity(Vec3 v) {
        if (v.lengthSqr() < 1.0e-6) return;
        float yaw = (float)(Mth.atan2(v.z, v.x) * (180F / Math.PI)) - 90.0F;
        float pitch = (float)(-(Mth.atan2(v.y, Math.sqrt(v.x * v.x + v.z * v.z)) * (180F / Math.PI)));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
    }


    // ---------------------------------------------------------------------
    // Block impact behaviors
    // ---------------------------------------------------------------------
    private void handleBlockAndEntityImpactThisTick() {
        Vec3 vel   = this.getDeltaMovement();
        if (vel.lengthSqr() < 1.0e-8) return;

        Vec3 start = this.position();
        Vec3 end   = start.add(vel);

        // ---- Block sweep
        BlockHitResult bhr = this.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
        ));

        double travelLen = vel.length();
        double blockHitDist = Double.POSITIVE_INFINITY;
        if (bhr.getType() == HitResult.Type.BLOCK) {
            blockHitDist = bhr.getLocation().distanceTo(start);
            // Ignore “0 distance” grazes caused by starting on/inside a face
            if (blockHitDist < 1.0e-4) {
                bhr = null;
                blockHitDist = Double.POSITIVE_INFINITY;
            }
        } else {
            bhr = null;
        }

        // ---- Entity sweep (simple)
        EntityHitResult ehr = getEntityHitResultOnMove(start, end, travelLen);
        double entityHitDist = (ehr != null) ? ehr.getLocation().distanceTo(start) : Double.POSITIVE_INFINITY;

        // Pick the earliest valid hit within this tick’s travel
        if (blockHitDist <= travelLen || entityHitDist <= travelLen) {
            boolean entityWins = entityHitDist < blockHitDist;

            if (entityWins && ehr != null) {
                Entity hit = ehr.getEntity();
                if (hit instanceof LivingEntity living) {
                    if (living.hurt(this.damageSources().mobAttack(this), CONTACT_DAMAGE)) {
                        this.gameEvent(GameEvent.ENTITY_DAMAGE, living);
                    }
                }
                discardWithEffect();
                return;
            }

            if (bhr != null) {
                BlockPos pos = bhr.getBlockPos();
                BlockState state = this.level().getBlockState(pos);

                if (!state.isAir()) {
                    if (isOre(state)) {
                        smeltOreAndDrop((ServerLevel) this.level(), pos, state);
                        discardWithEffect();
                        return;
                    }

                    if (isBurnable(state, pos)) {
                        this.level().destroyBlock(pos, false);
                        tryPlaceFireAt(pos);
                        this.gameEvent(GameEvent.BLOCK_PLACE);
                        discardWithEffect();
                        return;
                    }

                    // solid hit -> vanish
                    if (state.canOcclude() || state.isSolid()) {
                        discardWithEffect();
                    }
                    return;
                }
            }
        }
    }

    // Minimal entity sweep helper (no external deps)
    private @javax.annotation.Nullable EntityHitResult getEntityHitResultOnMove(Vec3 start, Vec3 end, double travelLen) {
        AABB sweepBox = this.getBoundingBox().expandTowards(end.subtract(start)).inflate(0.25);
        List<Entity> hits = this.level().getEntities(this, sweepBox, e ->
                e.isPickable() && e.isAlive() && e != this
        );

        Entity closest = null;
        Vec3    closestHitPos = null;
        double  closestDist = Double.POSITIVE_INFINITY;

        for (Entity e : hits) {
            AABB box = e.getBoundingBox().inflate(0.01);
            Optional<Vec3> opt = box.clip(start, end);
            if (opt.isPresent()) {
                double d = opt.get().distanceTo(start);
                if (d < closestDist && d <= travelLen + 1.0e-6) {
                    closestDist = d;
                    closest = e;
                    closestHitPos = opt.get();
                }
            }
        }

        return (closest != null) ? new EntityHitResult(closest, closestHitPos) : null;
    }


    private boolean isOre(BlockState state) {
        // Heuristic: treat blocks with vanilla "ores" tag OR name contains "_ore" as ore.
        // TODO: Replace with your own tag check: state.is(ForgeTags.ORES) / your mod tag.
        ResourceKey key = state.getBlockHolder().unwrapKey().get();
        String path = key.location().getPath();
        return state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.ANCIENT_DEBRIS)
                || path.contains("_ore") || path.contains("deepslate_") && path.endsWith("_ore");
    }

    private boolean isBurnable(BlockState state, BlockPos pos) {
        // Generic flammability check in any direction
        for (Direction d : Direction.values()) {
            if (state.ignitedByLava()) return true;
        }
        return false;
    }

    private void tryPlaceFireAt(BlockPos pos) {
        Level level = this.level();
        Block fire = Blocks.FIRE;
        BlockState fireState = fire.defaultBlockState();

        // If the hit block space itself is empty
        if (level.isEmptyBlock(pos)) {
            level.setBlockAndUpdate(pos, fireState);
            return;
        }

        // Otherwise, try to place fire above if it can survive there
        BlockPos above = pos.above();
        if (level.isEmptyBlock(above)) {
            BlockState belowState = level.getBlockState(pos);
            FireBlock fireBlock = (FireBlock) fire;
            if (fireBlock.canSurvive(fireState, level, above)) {
                level.setBlockAndUpdate(above, fireState);
            }
        }
    }

    private void smeltOreAndDrop(ServerLevel server, BlockPos pos, BlockState state) {
        // Build a fake tool-less loot context to get the block drop(s)
        LootParams.Builder loot = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withParameter(LootContextParams.THIS_ENTITY, this);
        List<ItemStack> drops = state.getDrops(loot);

        // Replace each drop with smelted variant if possible
        for (ItemStack drop : drops) {
            ItemStack smelted = findSmeltingResult(server, drop).orElse(drop.copy());
            if (!smelted.isEmpty()) {
                Block.popResource(server, pos, smelted);
            }
        }

        // Destroy ore without vanilla drops (we already spawned ours)
        server.destroyBlock(pos, false);
        this.gameEvent(GameEvent.BLOCK_DESTROY);
        this.playSound(SoundEvents.FURNACE_FIRE_CRACKLE, 1.0f, 1.25f);
    }

    private Optional<ItemStack> findSmeltingResult(ServerLevel level, ItemStack input) {
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(input), level)
                .map(recipe -> {
                    ItemStack result = recipe.getResultItem(level.registryAccess());
                    return result.isEmpty() ? ItemStack.EMPTY : result.copy();
                })
                .filter(stack -> !stack.isEmpty());
    }

    // ---------------------------------------------------------------------
    // Damage handling
    // ---------------------------------------------------------------------
    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        // Weapon with Smite => instant death
        if (attacker instanceof LivingEntity living) {
            ItemStack weapon = living.getMainHandItem();
            int smite = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, weapon);
            if (smite > 0) {
                discardWithEffect();
                return true;
            }
        }

        // Splash water bottle / tipped arrow with WATER potion will damage via isSensitiveToWater==true above.
        // Any other physical/magical source is ignored.
        return false;
    }

    @Override
    protected void onEffectAdded(MobEffectInstance effect, @Nullable Entity source) {
        super.onEffectAdded(effect, source);
        // Instant Health potion kills the Wisp immediately
        if (effect.getEffect() == MobEffects.HEAL) {
            discardWithEffect();
        }
    }

    // ---------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------
    private void discardWithEffect() {
        if (!this.level().isClientSide) {
            // TODO: Play your custom wisp pop sound/particles here
            this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.6f, 1.6f);
            this.gameEvent(GameEvent.ENTITY_DIE);
            this.discard();
        }
    }

    // Make raytracing treat us like a projectile for smoother block hits
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void doPush(Entity pEntity) {
        // no push
    }

    @Override
    public boolean canBeLeashed(Player $$0) {
        return false;
    }
}
