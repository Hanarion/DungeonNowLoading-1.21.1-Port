package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.block.DungeonWallTorch;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class GasCloudEntity extends Entity {

    public enum GasState {
        DEFAULT,
        LIT,
        IGNITING,
        EXPLODING
    }

    // Synced fields
    private static final EntityDataAccessor<Integer> DATA_GAS_SIZE =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GROWTH_TIME =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_GAS_SPREAD =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_GAS_SPEED =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_LIFE =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_EXPLOSION_MULTIPLIER =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_AIR_RESISTANCE =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(GasCloudEntity.class, EntityDataSerializers.INT);

    private static final float INFLATE_RANGE = 0.4f;

    // Timers (not necessarily synced)
    private int growthTicks;
    private int ignitionTimer;   // ~10 ticks before it becomes IGNITING
    private int explosionTimer;  // ~25 ticks before it explodes once lit/igniting

    //Note: Only for testing, change it to constants afterwards.
    private int baseChainedDelay = 5;     // default: 5 ticks
    private int baseIgnitionDelay = 10;   // default: 10 ticks
    private int baseExplosionDelay = 25;  // default: 25 ticks

    // Poison stacking tracking
    // (cleared when entity despawns; no need to persist)
    private final Object2IntMap<UUID> timeInGasTicks = new Object2IntOpenHashMap<>();

    public GasCloudEntity(EntityType<? extends GasCloudEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_GAS_SIZE, 1);
        this.entityData.define(DATA_GROWTH_TIME, 0);
        this.entityData.define(DATA_GAS_SPREAD, 3.0F);
        this.entityData.define(DATA_GAS_SPEED, 0.02F);
        this.entityData.define(DATA_LIFE, 200);
        this.entityData.define(DATA_EXPLOSION_MULTIPLIER, 0.4F);
        this.entityData.define(DATA_AIR_RESISTANCE, 0.05F);
        this.entityData.define(DATA_STATE, GasState.DEFAULT.ordinal());
    }

    @Override
    public void tick() {
        super.tick();

        // handle growth (both sides)
        int growthTime = this.entityData.get(DATA_GROWTH_TIME);
        if (growthTime > 0 && growthTicks < growthTime) {
            growthTicks++;
            this.refreshDimensions();
        }

        // SERVER LOGIC
        if (!this.level().isClientSide) {
            // life timer
            int life = this.entityData.get(DATA_LIFE);
            if (life-- <= 0 && getGasState() != GasState.IGNITING && getGasState() != GasState.EXPLODING) {
                this.discard();
                return;
            }
            this.entityData.set(DATA_LIFE, life);

            // environment checks
            handleUnderwaterBehavior();
            handleGasRepulsion();
            handleGasEffects();
            handleTorchInteraction();
            handleProjectileIgnition();
            checkForFireBlocks();
            checkForLitNeighborGas();

            // state-specific behavior
            switch (getGasState()) {
                case DEFAULT -> tickDefault();
                case LIT -> tickLit();
                case IGNITING -> tickIgniting();
                case EXPLODING -> tickExploding();
            }

            // movement + drag
            this.move(MoverType.SELF, this.getDeltaMovement());

            float drag = this.entityData.get(DATA_AIR_RESISTANCE);
            drag = Mth.clamp(drag, 0.0F, 1.0F);

            if (drag > 0.0F) {
                Vec3 vel = this.getDeltaMovement();
                double factor = 1.0D - drag;
                this.setDeltaMovement(vel.x * factor, vel.y * factor, vel.z * factor);
            }
        }

        // CLIENT VISUALS (runs after state updates)
        if (this.level().isClientSide) {
            spawnParticlesClient();
        }
    }


    private void spawnParticlesClient() {
        GasState state = getGasState();
        Vec3 c = getGasCenter();
        double cx = c.x;
        double cy = c.y;
        double cz = c.z;

        double radius = getPoisonRadius();
        int particleCount = (int) Math.max(4, radius * radius * 3);

        // One cheap check: is the gas hitbox intersecting water at all?
        boolean hitWater = this.isInWaterOrBubble(); // or isInWater() if you prefer

        for (int i = 0; i < particleCount; i++) {
            double rx = (this.random.nextDouble() * 2.0D - 1.0D);
            double ry = (this.random.nextDouble() * 2.0D - 1.0D);
            double rz = (this.random.nextDouble() * 2.0D - 1.0D);
            double lenSq = rx * rx + ry * ry + rz * rz;
            if (lenSq > 1.0D) {
                i--;
                continue;
            }

            double px = cx + rx * radius;
            double py = cy + ry * radius;
            double pz = cz + rz * radius;

            double mx = (this.random.nextDouble() - 0.5D) * 0.02D;
            double my = (this.random.nextDouble() - 0.5D) * 0.02D;
            double mz = (this.random.nextDouble() - 0.5D) * 0.02D;

            if (hitWater) {
                // Only do the heavier check if we know we're intersecting water somewhere
                BlockPos particlePos = BlockPos.containing(px, py, pz);
                boolean inWater = this.level().getFluidState(particlePos).is(FluidTags.WATER);

                if (inWater) {
                    // Underwater behavior
                    if (state == GasState.DEFAULT) {
                        // Default gas underwater: just bubbles
                        this.level().addParticle(
                                ParticleTypes.BUBBLE,
                                px, py, pz,
                                mx * 0.1D,
                                0.02D + Math.abs(my) * 0.05D,
                                mz * 0.1D
                        );
                    } else if (state == GasState.LIT || state == GasState.IGNITING) {
                        // 🔥 Lit / igniting underwater: bubbles + cloud (steam-ish look)
                        this.level().addParticle(
                                ParticleTypes.BUBBLE,
                                px, py, pz,
                                mx * 0.1D,
                                0.02D + Math.abs(my) * 0.05D,
                                mz * 0.1D
                        );
                        this.level().addParticle(
                                ParticleTypes.CLOUD,
                                px, py, pz,
                                mx * 0.2D,
                                0.02D + Math.abs(my) * 0.08D,
                                mz * 0.2D
                        );
                    }
                    continue; // we already did underwater behavior for this particle
                }
                // if not inWater, fall through to normal air behavior below
            }

            // Air behavior (not in water at this particle position)
            if (state == GasState.DEFAULT) {
                this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, mx, my, mz);
            } else if (state == GasState.LIT || state == GasState.IGNITING) {
                this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, mx, my, mz);
                this.level().addParticle(ParticleTypes.FLAME, px, py, pz, mx, my, mz);
            }
        }
    }

    private void tickDefault() {
        // maybe slight drifting / idle motion
    }

    private void tickLit() {
        if (this.ignitionTimer > 0) {
            this.ignitionTimer--;
        }

        if (this.ignitionTimer == 0) {
            setGasState(GasState.IGNITING);
            //igniteNearbyGas();
        }

        if (this.explosionTimer > 0) {
            this.explosionTimer--;
        }

        if (this.explosionTimer == 0) {
            setGasState(GasState.EXPLODING);
        }
    }

    private void tickIgniting() {
        if (this.explosionTimer > 0) {
            this.explosionTimer--;
        }

        if (this.explosionTimer == 0) {
            setGasState(GasState.EXPLODING);
        }
    }

    private void tickExploding() {
        if (this.level().isClientSide) return;

        if (shouldFizzleInWater()) {
            Vec3 center = this.getGasCenter();

            this.level().playSound(
                    null,
                    center.x, center.y, center.z,
                    net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.8F,
                    0.9F + this.random.nextFloat() * 0.2F
            );

            this.discard();
        } else {
            explodeAndRemove();
        }
    }



    private void handleUnderwaterBehavior() {
        BlockPos pos = this.blockPosition();
        boolean inWater = this.level().getFluidState(pos).is(FluidTags.WATER);

        if (inWater) {
            // bubbles, rising
            Vec3 motion = this.getDeltaMovement();
            double riseSpeed = 0.1D;
            this.setDeltaMovement(motion.x * 0.8D, Math.max(motion.y, riseSpeed), motion.z * 0.8D);

            // you could set a flag to switch particle type on the client
            // e.g. isUnderwater = true;
        } else {
            // Check if we just emerged from water: below water, here air
            BlockPos below = pos.below();
            if (this.level().getFluidState(below).is(FluidTags.WATER) && this.level().isEmptyBlock(pos)) {
                // revert to gas visuals
                // isUnderwater = false;
            }
        }
    }


    private void handleGasRepulsion() {
        // This should only matter on the server
        if (this.level().isClientSide) return;

        float selfGrowth = getGrowthFactor();
        if (selfGrowth <= 0.0F) return; // not grown at all yet

        float selfSpreadBase = this.entityData.get(DATA_GAS_SPREAD);
        float selfSpeedBase  = this.entityData.get(DATA_GAS_SPEED);

        // Make detection range & push scale with how "grown" the gas is
        float selfSpread = selfSpreadBase * selfGrowth;
        float selfSpeed  = selfSpeedBase * selfGrowth;

        if (selfSpread <= 0.0F || selfSpeed <= 0.0F) return;

        AABB box = this.getBoundingBox().inflate(selfSpread);
        List<GasCloudEntity> nearby = this.level().getEntitiesOfClass(
                GasCloudEntity.class,
                box,
                other -> other != this
        );

        if (nearby.isEmpty()) return;

        Vec3 selfCenter = this.getGasCenter();
        Vec3 push = Vec3.ZERO;

        for (GasCloudEntity other : nearby) {
            float otherGrowth = other.getGrowthFactor();
            if (otherGrowth <= 0.0F) continue; // not formed yet

            float otherSpreadBase = other.entityData.get(DATA_GAS_SPREAD);
            float otherSpread     = otherSpreadBase * otherGrowth;

            Vec3 otherCenter = other.getGasCenter();
            Vec3 diff = selfCenter.subtract(otherCenter);
            double distSq = diff.lengthSqr();
            if (distSq < 1.0E-4D) continue;

            double dist = Math.sqrt(distSq);

            // Only repel strongly when their "gas spheres" are overlapping / close
            double effectiveRange = selfSpread + otherSpread;
            if (dist > effectiveRange) continue;

            // Overlap factor: 0 (just touching) → 1 (fully on top of each other)
            double overlap = 1.0D - (dist / effectiveRange);
            double weight = overlap * overlap; // soften falloff

            push = push.add(diff.normalize().scale(weight));
        }

        if (push.lengthSqr() > 1.0E-6D) {
            // Normalize and scale by gas speed (also scaled by growth)
            push = push.normalize().scale(selfSpeed);

            Vec3 newMotion = this.getDeltaMovement().add(push);

            // Dampening so they don't rocket away
            this.setDeltaMovement(
                    newMotion.x * 0.9D,
                    newMotion.y * 0.9D,
                    newMotion.z * 0.9D
            );
        }
    }

    private void handleGasEffects() {
        if (this.level().isClientSide) return;

        GasState state = getGasState();
        boolean burningGas = (state == GasState.LIT || state == GasState.IGNITING);

        double radius = getPoisonRadius();
        Vec3 c = getGasCenter();
        double cx = c.x;
        double cy = c.y;
        double cz = c.z;

        AABB aabb = new AABB(
                cx - radius, cy - radius, cz - radius,
                cx + radius, cy + radius, cz + radius
        );

        List<LivingEntity> list = this.level().getEntitiesOfClass(
                LivingEntity.class,
                aabb,
                e -> e.isAlive() && !e.isInvulnerable()
        );

        if (burningGas) {
            // 🔥 burn mode (unchanged)
            for (LivingEntity entity : list) {
                if (entity.fireImmune()) continue;
                entity.setSecondsOnFire(1);
                float burnDamagePerTick = 0.5F;
                entity.hurt(this.level().damageSources().inFire(), burnDamagePerTick);
            }

            // clear poison tracking
            timeInGasTicks.keySet().removeIf(uuid ->
                    list.stream().noneMatch(e -> e.getUUID().equals(uuid)));
            return;
        }

        // 🧪 DEFAULT gas: poison mode
        for (LivingEntity entity : list) {
            UUID id = entity.getUUID();
            boolean wasInGas = timeInGasTicks.containsKey(id);
            int time = wasInGas ? timeInGasTicks.getInt(id) + 1 : 1;
            timeInGasTicks.put(id, time);

            // NEW: first tick inside gas → apply Poison I immediately
            if (!wasInGas) {
                entity.addEffect(new MobEffectInstance(
                        MobEffects.POISON,
                        100,          // duration (ticks)
                        0,            // amplifier 0 = Poison I
                        true,
                        true,
                        true
                ));
                continue; // next entity; stacking will start next ticks
            }

            // After that, upgrade every 2 seconds in gas
            if (time % 40 == 0) {
                int steps = time / 40;                         // 1,2,3...
                int amplifier = Mth.clamp(steps, 0, 4);        // 0..4 → Poison I..V

                MobEffectInstance current = entity.getEffect(MobEffects.POISON);
                if (current == null || current.getAmplifier() < amplifier) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.POISON,
                            100,
                            amplifier,
                            true,
                            true,
                            true
                    ));
                }
            }
        }

        // cleanup: remove entities that left the gas
        timeInGasTicks.keySet().removeIf(uuid ->
                list.stream().noneMatch(e -> e.getUUID().equals(uuid)));
    }



    private void handleTorchInteraction() {
        // Run less often to save perf
        if (this.tickCount % 5 != 0) return;

        int radius = this.entityData.get(DATA_GAS_SIZE);
        BlockPos gasPos = this.blockPosition();

        BlockPos.betweenClosedStream(
                gasPos.offset(-radius, -radius, -radius),
                gasPos.offset( radius,  radius,  radius)
        ).forEach(pos -> {
            BlockState state = this.level().getBlockState(pos);

            if (isBurningTorch(state)) {
                // 🎇 Spark particles
                double x = pos.getX() + 0.5D;
                double y = pos.getY() + 0.7D;
                double z = pos.getZ() + 0.5D;

                if (state.getBlock() instanceof WallTorchBlock) {
                    Direction dir = state.getValue(WallTorchBlock.FACING).getOpposite();
                    double offset = 0.27D;
                    x += offset * dir.getStepX();
                    z += offset * dir.getStepZ();
                    y = pos.getY() + 0.8D;
                }

                if (state.getBlock() instanceof DungeonWallTorch && DungeonWallTorch.isLit(state)) {
                    Direction dir = state.getValue(DungeonWallTorch.FACING).getOpposite();
                    double offset = 0.27D;
                    x += offset * dir.getStepX();
                    z += offset * dir.getStepZ();
                    y = pos.getY() + 0.85D;
                }

                ((ServerLevel)this.level()).sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        x, y, z,
                        3,
                        0.1D, 0.1D, 0.1D,
                        0.01D
                );
            }
        });
    }


    private void handleProjectileIgnition() {
        // Only care while still gas
        if (getGasState() != GasState.DEFAULT) {
            return;
        }

        // Small expanded box around the cloud
        AABB box = this.getBoundingBox().inflate(0.1D);

        List<Entity> hits = this.level().getEntities(
                this,
                box,
                e ->
                        e instanceof AbstractArrow
                                || e instanceof Fireball
                                || e instanceof SmallFireball
                                || e instanceof DragonFireball
                                || e instanceof FlameProjectileEntity // your custom one
                                || e instanceof WispProjectileEntity
        );

        if (hits.isEmpty()) {
            return;
        }

        for (Entity e : hits) {
            // Flame arrows
            if (e instanceof AbstractArrow arrow) {
                if (arrow.isOnFire()) {
                    this.ignite();
                    return;
                }
            }

            // Fireballs always count as fire
            if (e instanceof Fireball
                    || e instanceof SmallFireball
                    || e instanceof DragonFireball
                    || e instanceof FlameProjectileEntity
                    || e instanceof WispProjectileEntity) {
                this.ignite();
                return;
            }
        }
    }


    private void checkForFireBlocks() {
        // Lava immersion is easy:
        if (this.isInLava()) {
            this.ignite();
            return;
        }

        // Small inflated box around the gas
        AABB box = this.getBoundingBox().inflate(0.1D);
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.ceil(box.maxX);
        int maxY = Mth.ceil(box.maxY);
        int maxZ = Mth.ceil(box.maxZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);

                    BlockState state = this.level().getBlockState(pos);

                    if (isFireBlock(state, pos)) {
                        this.ignite();
                        return;
                    }
                }
            }
        }
    }

    private boolean isFireBlock(BlockState state, BlockPos pos) {
        Block block = state.getBlock();

        // Normal & soul fire
        if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE) {
            return true;
        }

        // Lit campfires
        if (block instanceof CampfireBlock campfire) {
            if (state.getValue(CampfireBlock.LIT)) {
                return true;
            }
        }

        // Lava as fluid (even if not full-depth)
        if (this.level().getFluidState(pos).is(FluidTags.LAVA)) {
            return true;
        }

        // Magma blocks: we treat standing on / intersecting them as "hot"
        if (block == Blocks.MAGMA_BLOCK) {
            return true;
        }

        // Add any of your custom flame blocks here

        return false;
    }

    private void checkForLitNeighborGas() {
        // Only DEFAULT gas can be newly ignited
        if (this.getGasState() != GasState.DEFAULT) {
            return;
        }

        AABB box = this.getBoundingBox().inflate(INFLATE_RANGE);

        List<GasCloudEntity> neighbors = this.level().getEntitiesOfClass(
                GasCloudEntity.class,
                box,
                other -> other != this && isIgniting(other)
        );

        if (!neighbors.isEmpty()) {
            // You can use ignite() or delayIgnite(...) if you want staggered propagation
            this.igniteChain();
        }
    }

    private static boolean isIgniting(GasCloudEntity gas) {
        return gas.getGasState() == GasState.IGNITING;
    }



    private boolean isBurningTorch(BlockState state) {
        // handle regular torch, wall torch, soul torches, etc
        Block b = state.getBlock();
        return b == Blocks.TORCH
                || b == Blocks.WALL_TORCH
                || b == Blocks.SOUL_TORCH
                || b == Blocks.SOUL_WALL_TORCH
                || (b == DNLBlocks.DUNGEON_WALL_TORCH.get() && DungeonWallTorch.isLit(state))
                // your custom torches...
                ;
    }

    private Vec3 getGasCenter() {
        // Center of the current hitbox
        return this.getBoundingBox().getCenter();
    }

    private float getGrowthFactor() {
        int growthTime = this.entityData.get(DATA_GROWTH_TIME);
        if (growthTime <= 0) {
            return 1.0F; // instant full size
        }
        if (growthTicks >= growthTime) {
            return 1.0F;
        }
        return (float) growthTicks / (float) growthTime;
    }

    private double getPoisonRadius() {
        int maxSize = this.entityData.get(DATA_GAS_SIZE);
        float factor = getGrowthFactor();

        // whatever mapping you want; keeping previous example:
        double maxRadius = 1.0D + maxSize * 0.5D;
        return maxRadius * factor;
    }

    public void ignite() {
        if (getGasState() == GasState.DEFAULT) {
            setGasState(GasState.LIT);

            int igniteDelay   = Math.max(0, this.baseIgnitionDelay);
            int explodeDelay  = Math.max(0, this.baseExplosionDelay);

            this.ignitionTimer  = igniteDelay;
            this.explosionTimer = igniteDelay + explodeDelay; // LIT → IGNITING → EXPLODING
        }
    }

    public void igniteChain() {
        if (getGasState() == GasState.DEFAULT) {
            setGasState(GasState.LIT);

            int chainDelay    = Math.max(0, this.baseChainedDelay);
            int explodeDelay  = Math.max(0, this.baseExplosionDelay);

            this.ignitionTimer  = chainDelay;
            this.explosionTimer = chainDelay + explodeDelay;
        }
    }

    private void igniteNearbyGas() {
        // Very small link radius: only directly touching gas blobs ignite each other
        AABB box = this.getBoundingBox().inflate(0.2D);

        List<GasCloudEntity> clouds = this.level().getEntitiesOfClass(
                GasCloudEntity.class,
                box,
                e -> e != this && e.getGasState() == GasState.DEFAULT
        );

        for (GasCloudEntity cloud : clouds) {
            // Use that cloud's own ignition/explosion timing (from its timers / NBT)
            cloud.ignite();
        }
    }

    private void explodeAndRemove() {
        if (this.level().isClientSide) {
            this.discard();
            return;
        }

        float size = this.entityData.get(DATA_GAS_SIZE);
        float multiplier = this.entityData.get(DATA_EXPLOSION_MULTIPLIER);

        // Use gas center, not feet
        Vec3 center = this.getGasCenter();
        double cx = center.x;
        double cy = center.y;
        double cz = center.z;

        // If you want explosion to also respect growth, you can multiply by getGrowthFactor()
        float growth = getGrowthFactor();
        double radius = size * 2.0D * growth; // or just size * 2.0D if you want full power even when small
        if (radius <= 0.0D) {
            radius = 0.5D; // safety minimum
        }

        this.level().explode(
                this,
                cx, cy, cz,
                (float) radius * multiplier,
                Level.ExplosionInteraction.TNT
        );

        this.discard();
    }


    private boolean shouldFizzleInWater() {
        // "Touching water" – uses the bounding box vs water (cheap)
        boolean touchingWater = this.isInWaterOrBubble(); // or isInWater()

        if (!touchingWater) {
            return false;
        }

        // "Center is in water" – sample fluid at gas center
        Vec3 center = this.getGasCenter();
        BlockPos centerPos = BlockPos.containing(center);
        boolean centerInWater = this.level().getFluidState(centerPos).is(FluidTags.WATER);

        return centerInWater;
    }

    public GasState getGasState() {
        return GasState.values()[this.entityData.get(DATA_STATE)];
    }

    public void setGasState(GasState state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    @Override
    public boolean isOnFire() {
        return false;
    }


    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int maxSize = this.entityData.get(DATA_GAS_SIZE);
        float factor = getGrowthFactor();

        // base from registry, or just compute directly
        float base = 0.5F + maxSize * 1.0F; // tune as you like
        float actual = base * factor;

        return EntityDimensions.scalable(actual, actual);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

        tag.putInt("GasSize", this.entityData.get(DATA_GAS_SIZE));
        tag.putInt("GrowthTime", this.entityData.get(DATA_GROWTH_TIME));
        tag.putFloat("GasSpread", this.entityData.get(DATA_GAS_SPREAD));
        tag.putFloat("GasSpeed", this.entityData.get(DATA_GAS_SPEED));
        tag.putInt("Life", this.entityData.get(DATA_LIFE));
        tag.putFloat("ExplosionMultiplier", this.entityData.get(DATA_EXPLOSION_MULTIPLIER));
        tag.putFloat("AirResistance", this.entityData.get(DATA_AIR_RESISTANCE));

        tag.putInt("GasState", this.entityData.get(DATA_STATE));
        tag.putInt("GrowthTicks", this.growthTicks);
        //Note: Only for testing
        tag.putInt("ChainDelay", this.baseChainedDelay);
        //Note: Only for testing
        tag.putInt("IgnitionDelay", this.baseIgnitionDelay);
        //Note: Only for testing
        tag.putInt("ExplosionDelay", this.baseExplosionDelay);

        tag.putInt("IgnitionTimer", this.ignitionTimer);
        tag.putInt("ExplosionTimer", this.explosionTimer);

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        if (tag.contains("GasSize")) {
            this.entityData.set(DATA_GAS_SIZE, tag.getInt("GasSize"));
        }
        if (tag.contains("GrowthTime")) {
            this.entityData.set(DATA_GROWTH_TIME, tag.getInt("GrowthTime"));
        }
        if (tag.contains("GasSpread")) {
            this.entityData.set(DATA_GAS_SPREAD, tag.getFloat("GasSpread"));
        }
        if (tag.contains("GasSpeed")) {
            this.entityData.set(DATA_GAS_SPEED, tag.getFloat("GasSpeed"));
        }
        if (tag.contains("Life")) {
            this.entityData.set(DATA_LIFE, tag.getInt("Life"));
        }
        if (tag.contains("ExplosionMultiplier")) {
            this.entityData.set(DATA_EXPLOSION_MULTIPLIER, tag.getFloat("ExplosionMultiplier"));
        }
        if (tag.contains("AirResistance")) {
            this.entityData.set(DATA_AIR_RESISTANCE, tag.getFloat("AirResistance"));
        }

        if (tag.contains("GasState")) {
            int ordinal = tag.getInt("GasState");
            ordinal = Mth.clamp(ordinal, 0, GasState.values().length - 1);
            this.entityData.set(DATA_STATE, ordinal);
        }
        //Note: Only for testing
        if (tag.contains("ChainDelay")) {
            this.baseChainedDelay = tag.getInt("ChainDelay");
        }
        //Note: Only for testing
        if (tag.contains("IgnitionDelay")) {
            this.baseIgnitionDelay = tag.getInt("IgnitionDelay");
        }
        //Note: Only for testing
        if (tag.contains("ExplosionDelay")) {
            this.baseExplosionDelay = tag.getInt("ExplosionDelay");
        }

        this.growthTicks = tag.getInt("GrowthTicks");
        this.ignitionTimer = tag.getInt("IgnitionTimer");
        this.explosionTimer = tag.getInt("ExplosionTimer");

    }

    public void configureFromBurnacle(
            int gasSize,
            int growthTime,
            float gasSpread,
            float gasSpeed,
            float airResistance,
            int life,
            float explosionMultiplier,
            int chainDelay,
            int ignitionDelay,
            int explosionDelay
    ) {
        // Values stored via entityData and NBT
        this.entityData.set(DATA_GAS_SIZE, gasSize);
        this.entityData.set(DATA_GROWTH_TIME, growthTime);
        this.entityData.set(DATA_GAS_SPREAD, gasSpread);
        this.entityData.set(DATA_GAS_SPEED, gasSpeed);
        this.entityData.set(DATA_LIFE, life);
        this.entityData.set(DATA_EXPLOSION_MULTIPLIER, explosionMultiplier);
        this.entityData.set(DATA_AIR_RESISTANCE, airResistance);

        // These are the "Note: Only for testing" base delays you persist
        this.baseChainedDelay = chainDelay;
        this.baseIgnitionDelay = ignitionDelay;
        this.baseExplosionDelay = explosionDelay;

        // Reset timers so the new values take effect cleanly
        this.growthTicks = 0;
        this.ignitionTimer = 0;
        this.explosionTimer = 0;
    }


}
