package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class MimicartEntity extends AbstractMinecart {

    private static final float MAX_HEALTH = 40.0F;
    private float health = MAX_HEALTH;

    private static final double DETECT_RADIUS = 7.0D;  // start charging here
    private static final double SNATCH_MAX_DISTANCE = 15.0D;

    private static final int BEAM_DRAW_TICKS = 10; // 0.5s at 20 TPS
    private static final int SUCK_WINDUP_TICKS = 20;    // 1 second a
    private static final int FAIL_COOLDOWN_TICKS = 40;// t 20 tps (comment was 2s before)
    private static final int SUCK_COOLDOWN_TICKS = 80;  // 4 seconds cooldown
    private static final int TRAP_DURATION_TICKS = 600; // 10 seconds
    private static final int POST_TRAP_COOLDOWN_TICKS = 100; // 5 seconds
    private static final float SPEED_MODIFIER = 2.5F;
    private static final float ATTACK_DAMAGE = 1.0F;
    private static final float RIDING_OFFSET_EASE = 0.92F;


    // suck / pull state
    private int suckTimer;
    private int suckCooldown;
    private java.util.UUID suckTargetId;
    private Vec3 suckLockedPos;

    // trap state
    private java.util.UUID trappedPassengerId;
    private int trappedTicks;

    // post-trap immunity
    private java.util.UUID lastReleasedPlayerId;
    private int lastReleasedCooldown;

    private boolean wasOnRail;

    private double xPush;
    private double zPush;

    private Vec3 snatchOffset = Vec3.ZERO;
    private Vec3 snatchStartOffset = Vec3.ZERO;
    private int snatchDurationTicks = 15;   // total ticks to fully pull in (tweak)
    private int snatchTicksElapsed = 0;

    private int collideGrabCooldown;

    public MimicartEntity(EntityType<? extends MimicartEntity> type, Level level) {
        super(type, level);
        this.health = MAX_HEALTH;
    }

    public MimicartEntity(Level level, double x, double y, double z) {
        super(DNLEntityTypes.MIMICART.get(), level, x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        boolean onRailNow = this.isOnRails();
        if (this.wasOnRail && !onRailNow && this.isVehicle()) {
            dismountTrappedPassengerWithCooldown();
        }
        this.wasOnRail = onRailNow;

        // --- post-trap cooldown ticking ---
        if (lastReleasedCooldown > 0) {
            lastReleasedCooldown--;
            if (lastReleasedCooldown == 0) {
                lastReleasedPlayerId = null;
            }
        }

        // --- trap ticking / auto-eject after 10s ---
        // --- trap ticking / auto-eject after 10s ---
        if (trappedPassengerId != null) {
            Entity passenger = this.getFirstPassenger();

            if (passenger != null
                    && passenger.getUUID().equals(trappedPassengerId)
                    && passenger instanceof net.minecraft.world.entity.LivingEntity living) {

                trappedTicks++;

                if (trappedTicks % 60 == 0 && trappedTicks > 0) {
                    living.hurt(this.damageSources().generic(), ATTACK_DAMAGE);
                    this.playSound(SoundEvents.MINECART_RIDING, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                }

                if (trappedTicks >= TRAP_DURATION_TICKS) {
                    dismountTrappedPassengerWithCooldown();
                }

            } else {
                // passenger left or changed
                trappedPassengerId = null;
                trappedTicks = 0;
                snatchOffset = Vec3.ZERO;
            }
        }


        // If dead or already carrying somebody, don't try to suck in players
        if (!this.isAlive() || this.isVehicle()) {
            resetSuckState(true);
            return;
        }

        // --- suck logic ---

        // Cooldown ticking
        if (suckCooldown > 0) {
            suckCooldown--;
        }

        if (collideGrabCooldown > 0) {
            collideGrabCooldown--;
        }

        // Resolve current target (if we have one)
        Player target = getSuckTarget();

        // If we had a target UUID but that player no longer exists, reset
        if (suckTargetId != null && target == null) {
            resetSuckState(true);
            return;
        }

        // Windup / beam phase
        if (suckTimer > 0) {
            // PRE-LOCK: still tracking the live player
            if (this.suckLockedPos == null) {
                // we haven't locked yet → need a valid target in range & LOS
                if (target == null) {
                    resetSuckState(true);
                    return;
                }

                boolean outOfDetection = target.distanceToSqr(this) > DETECT_RADIUS * DETECT_RADIUS;
                boolean noPull = !canBePulled(target) || !hasLineOfSightTo(target);

                if (outOfDetection || noPull) {
                    resetSuckState(true);
                    return;
                }

            } else {
                // POST-LOCK: we've already memorized suckLockedPos
                // now ignore where the player is; only care if the cart strays too far
                double distCartToLockSq = this.position().distanceToSqr(this.suckLockedPos);
                if (distCartToLockSq > SNATCH_MAX_DISTANCE * SNATCH_MAX_DISTANCE) {
                    // cart drifted too far from the telegraphed point → cancel
                    resetSuckState(true);
                    return;
                }
            }

            // tick down after checks
            suckTimer--;

            // optional “beam start” moment (sound/extra particles)
            if (this.suckTimer == BEAM_DRAW_TICKS) {
                spawnDetectParticles();
                this.playSound(SoundEvents.WITCH_CELEBRATE, 0.8F, 1.2F);
            }

            // draw beam ONLY if we have a locked position
            if (this.suckLockedPos != null
                    && this.level() instanceof ServerLevel server
                    && this.suckTimer <= BEAM_DRAW_TICKS
                    && this.suckTimer >= 0) {

                int elapsed = BEAM_DRAW_TICKS - this.suckTimer; // 0..BEAM_DRAW_TICKS
                double progress = Mth.clamp(
                        (double) elapsed / (double) BEAM_DRAW_TICKS,
                        0.0D, 1.0D
                );

                Vec3 start = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
                Vec3 end   = this.suckLockedPos;
                Vec3 delta = end.subtract(start);

                int segments = 8;
                for (int i = 0; i <= segments; i++) {
                    double tSeg = (double) i / (double) segments;
                    double t = tSeg * progress;

                    Vec3 point = start.add(delta.scale(t));

                    server.sendParticles(
                            ParticleTypes.POOF,
                            point.x, point.y, point.z,
                            1,
                            0.01D, 0.01D, 0.01D,
                            0.0D
                    );
                }
            }

            // resolve snatch when timer finishes (your existing code)
            // resolve snatch when timer finishes
            if (suckTimer == 0) {
                if (this.suckLockedPos != null) {
                    double distCartToLockSq = this.position().distanceToSqr(this.suckLockedPos);
                    if (distCartToLockSq <= SNATCH_MAX_DISTANCE * SNATCH_MAX_DISTANCE) {

                        double grabRadius = 1.5D;

                        // 1) Prefer grabbing a player near the locked position
                        Player grabbedPlayer = this.level().getNearestPlayer(
                                this.suckLockedPos.x,
                                this.suckLockedPos.y,
                                this.suckLockedPos.z,
                                grabRadius,
                                (p) -> p instanceof Player player &&canBePulled(player)
                        );

                        Entity grabbedEntity = grabbedPlayer;

                        // 2) If no player, try any other living entity in the radius (optional)
                        if (grabbedEntity == null) {
                            var others = this.level().getEntitiesOfClass(
                                    net.minecraft.world.entity.LivingEntity.class,
                                    new net.minecraft.world.phys.AABB(
                                            this.suckLockedPos.x - grabRadius,
                                            this.suckLockedPos.y - grabRadius,
                                            this.suckLockedPos.z - grabRadius,
                                            this.suckLockedPos.x + grabRadius,
                                            this.suckLockedPos.y + grabRadius,
                                            this.suckLockedPos.z + grabRadius
                                    ),
                                    e -> e.isAlive()
                                            && !e.isPassenger()
                            );

                            if (!others.isEmpty()) {
                                grabbedEntity = others.get(0);
                            }
                        }

                        // 3) If we found *any* target, grab it
                        if (grabbedEntity != null && !grabbedEntity.isPassenger()) {
                            spawnPullParticles();
                            this.playSound(SoundEvents.MINECART_RIDING, 1.0F, this.getSoundPitch());

                            grabbedEntity.startRiding(this, true);
                            beginTrap(grabbedEntity);

                            suckCooldown = SUCK_COOLDOWN_TICKS;
                            resetSuckState(false);
                        } else {
                            // tongue reached the spot, but nobody was there
                            resetSuckState(true);
                        }

                    } else {
                        // cart drifted too far away from its telegraphed point
                        resetSuckState(true);
                    }
                } else {
                    // somehow no locked position at resolution time
                    resetSuckState(true);
                }
            }
        }
        else if (suckTimer == 0 && suckTargetId == null && suckCooldown <= 0) {
            Player nearest = this.level().getNearestPlayer(this, DETECT_RADIUS);
            if (nearest != null && canBePulled(nearest) && hasLineOfSightTo(nearest)) {
                this.suckTargetId = nearest.getUUID();
                this.suckTimer = SUCK_WINDUP_TICKS;

                // Lock the body position immediately when it enters detect radius
                double bodyY = nearest.getY() + nearest.getBbHeight() * 0.55D;
                this.suckLockedPos = new Vec3(nearest.getX(), bodyY, nearest.getZ());

                // initial tell
                spawnDetectParticles();
                this.playSound(SoundEvents.MINECART_INSIDE_UNDERWATER, 0.7F, this.getSoundPitch());
            }
        }

        if (this.suckTimer <= 0 && this.suckTargetId == null) {
            tryCollideGrab();
        }
    }

    private void resetSuckState(boolean failed) {
        this.suckTimer = 0;
        this.suckTargetId = null;
        this.suckLockedPos = null;

        if (failed) {
            // enforce cooldown after a miss
            this.suckCooldown = FAIL_COOLDOWN_TICKS;
        }
    }


    private boolean canBePulled(Player player) {
        if (player.isSpectator()
                || player.isCreative()
                || !player.isAlive()) {
            return false;
        }

        // 5s cooldown after being forcibly dismounted by this Mimicart
        if (lastReleasedPlayerId != null
                && lastReleasedCooldown > 0
                && player.getUUID().equals(lastReleasedPlayerId)) {
            return false;
        }

        return true;
    }

    private boolean hasLineOfSightTo(Player player) {
        var start = this.position().add(0, this.getBbHeight() * 0.5, 0); // cart "eyes"
        var end = player.getEyePosition(); // player eyes

        var ctx = new net.minecraft.world.level.ClipContext(
                start,
                end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, // stop on blocks
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        );

        var result = this.level().clip(ctx);

        // If result type is MISS → nothing blocks the sight line
        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }


    @org.jetbrains.annotations.Nullable
    private Player getSuckTarget() {
        if (this.suckTargetId == null) {
            return null;
        }
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.suckTargetId);
    }


    private void spawnDetectParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // small “alert” burst around the cart
        double cx = this.getX();
        double cy = this.getY() + 0.5D;
        double cz = this.getZ();

        for (int i = 0; i < 12; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 0.6D;
            double oy = this.random.nextDouble() * 0.4D;
            double oz = (this.random.nextDouble() - 0.5D) * 0.6D;
            serverLevel.sendParticles(
                    ParticleTypes.ANGRY_VILLAGER,
                    cx + ox, cy + oy, cz + oz,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }

    private void spawnPullParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double cx = this.getX();
        double cy = this.getY() + 0.5D;
        double cz = this.getZ();

        serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                cx, cy, cz,
                20,
                0.4D, 0.3D, 0.4D,
                0.1D
        );
    }

    private void beginTrap(Entity entity) {
        this.trappedPassengerId = entity.getUUID();
        this.trappedTicks = 0;

        double dx = this.getX() - entity.getX();
        double dz = this.getZ() - entity.getZ();

        double lenSq = dx * dx + dz * dz;
        if (lenSq < 1.0E-4D) {
            var vel = this.getDeltaMovement();
            dx = vel.x;
            dz = vel.z;
            lenSq = dx * dx + dz * dz;
            if (lenSq < 1.0E-4D) {
                dx = 0.01D;
                dz = 0.0D;
            }
        }

        this.xPush = dx;
        this.zPush = dz;
    }



    private void dismountTrappedPassengerWithCooldown() {
        if (!this.isVehicle()) {
            return;
        }

        if (this.getFirstPassenger() instanceof Player p
                && this.trappedPassengerId != null
                && p.getUUID().equals(this.trappedPassengerId)) {

            this.ejectPassengers();

            this.trappedPassengerId = null;
            this.trappedTicks = 0;
            this.snatchOffset = Vec3.ZERO; // reset

            this.lastReleasedPlayerId = p.getUUID();
            this.lastReleasedCooldown = POST_TRAP_COOLDOWN_TICKS;
        } else {
            this.ejectPassengers();
            this.snatchOffset = Vec3.ZERO;
        }
    }

    private void tryCollideGrab() {
        if (this.level().isClientSide) return;
        if (!this.isAlive()) return;
        if (this.isVehicle()) return;               // already carrying someone
        if (this.suckTimer > 0) return;             // currently doing tongue attack
        if (this.collideGrabCooldown > 0) return;   // still on collision cooldown

        // optional: only grab if actually moving a little
        Vec3 motion = this.getDeltaMovement();
        if (motion.horizontalDistanceSqr() < 0.01D) {
            return;
        }

        double radius = 1.0D; // 1 block grab range around the cart
        var bbox = this.getBoundingBox().inflate(radius);

        // look for any nearby living entity to grab
        var candidates = this.level().getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                bbox,
                e -> e.isAlive() && !e.isPassenger()
        );

        if (candidates.isEmpty()) {
            return;
        }

        net.minecraft.world.entity.LivingEntity target = candidates.get(0);

        // For players, respect your existing immunity rules
        if (target instanceof Player player) {
            if (!canBePulled(player)) {
                return;
            }
        }

        // Mount immediately
        target.startRiding(this, true);

        // Reuse your trap logic for players
        if (target instanceof Player player) {
            beginTrap(player);
        } else {
            // non-player trapped if you want them to take damage too
            this.trappedPassengerId = target.getUUID();
            this.trappedTicks = 0;
        }

        // feedback
        spawnPullParticles();
        this.playSound(SoundEvents.MINECART_RIDING, 1.0F, this.getSoundPitch());

        // 2s collision grab cooldown
        this.collideGrabCooldown = 40;
    }

    @Override
    protected void moveAlongTrack(BlockPos pos, BlockState state) {
        double minPush = 1.0E-4D;
        double minSpeed = 0.001D;

        // vanilla rail / collision handling
        super.moveAlongTrack(pos, state);

        // only do furnace-like push while trapping someone
        if (this.trappedPassengerId == null || !this.isVehicle()) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        double speedSq = motion.horizontalDistanceSqr();
        double pushSq = this.xPush * this.xPush + this.zPush * this.zPush;

        // same logic as MinecartFurnace: align push vector with current motion direction
        if (pushSq > minPush && speedSq > minSpeed) {
            double speed = Math.sqrt(speedSq);
            double pushLen = Math.sqrt(pushSq);
            this.xPush = motion.x / speed * pushLen;
            this.zPush = motion.z / speed * pushLen;
        }
    }

    @Override
    protected void applyNaturalSlowdown() {
        // only self-propel while trapping a passenger
        if (this.trappedPassengerId != null && this.isVehicle()) {
            double pushSq = this.xPush * this.xPush + this.zPush * this.zPush;
            if (pushSq > 1.0E-7D) {
                double len = Math.sqrt(pushSq);
                this.xPush /= len;
                this.zPush /= len;

                Vec3 vec3 = this.getDeltaMovement()
                        .multiply(0.8D, 0.0D, 0.8D)
                        .add(this.xPush * SPEED_MODIFIER, 0.0D, this.zPush * SPEED_MODIFIER);

                if (this.isInWater()) {
                    vec3 = vec3.scale(0.1D);
                }

                this.setDeltaMovement(vec3);
            } else {
                // fallback: vanilla friction on rails
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.98D, 0.0D, 0.98D));
            }

            // keep vanilla minecart friction bits (drag, etc.)
            super.applyNaturalSlowdown();
        } else {
            // not trapping → behave like a normal minecart
            super.applyNaturalSlowdown();
        }
    }

    @Override
    protected double getMaxSpeed() {
        // tweak to taste; furnace uses (isInWater ? 3 : 4) / 20.0
        return (this.isInWater() ? 3.0D : 8.0D) / 20.0D;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if (passenger instanceof LivingEntity living) {
            double seatY = this.getY() + this.getPassengersRidingOffset() + passenger.getMyRidingOffset();

            // initial full offset at the moment they get grabbed
            Vec3 offset = living.position().subtract(this.getX(), seatY, this.getZ());

            this.snatchStartOffset = offset;
            this.snatchOffset = offset;

            // reset easing timer
            this.snatchTicksElapsed = 0;
            this.snatchDurationTicks = 15; // or scale with distance if you want
        }

        super.addPassenger(passenger);
    }


    @Override
    protected void positionRider(Entity passenger, MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }

        double baseY = this.getY() + this.getPassengersRidingOffset() + passenger.getMyRidingOffset();
        double x = this.getX();
        double y = baseY;
        double z = this.getZ();

        if (passenger instanceof LivingEntity && !this.snatchStartOffset.equals(Vec3.ZERO)) {
            if (snatchTicksElapsed < snatchDurationTicks) {
                double t = (double) snatchTicksElapsed / (double) snatchDurationTicks; // 0 → 1 over time

                // Time easing (slow start, faster later)
                double eased = t * t; // you can try t*t*t for even softer start

                // --- Decompose the start offset into horizontal + vertical ---
                double startX = this.snatchStartOffset.x;
                double startY = this.snatchStartOffset.y;
                double startZ = this.snatchStartOffset.z;

                // Horizontal: just lerp down to 0 using eased factor
                double horizontalFactor = 1.0D - eased; // 1 → 0
                double offX = startX * horizontalFactor;
                double offZ = startZ * horizontalFactor;

                // Baseline vertical: also lerp down to 0
                double baseYOffset = startY * horizontalFactor;

                // Arc bump: 0 at t=0 and t=1, peak at t=0.5
                // 4 * t * (1 - t) is a nice simple parabola in [0,1]
                double arcPeak = 4.0D * t * (1.0D - t); // 0 → 1 → 0
                double arcHeight = 3.0D; // tweak this for how "high" the scoop looks

                double offY = baseYOffset + arcHeight * arcPeak;

                this.snatchOffset = new Vec3(offX, offY, offZ);
                snatchTicksElapsed++;
            } else {
                this.snatchOffset = Vec3.ZERO;
                this.snatchStartOffset = Vec3.ZERO;
            }

            x += this.snatchOffset.x;
            y += this.snatchOffset.y;
            z += this.snatchOffset.z;
        }

        move.accept(passenger, x, y, z);
    }



    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true; // client trusts server
        }

        if (source.getDirectEntity() instanceof AbstractArrow) {
            this.playSound(SoundEvents.SHIELD_BLOCK, 0.6F, 1.2F);
            return false;
        }

        if (this.isInvulnerableTo(source)) {
            return false;
        }

        // vanilla minecart damage visuals
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

        // SFX: hurt
        this.playSound(SoundEvents.VILLAGER_HURT, 1.0F, this.getSoundPitch());

        this.setHealth(this.getHealth() - amount);

        if (this.getHealth() <= 0.0F) {
            this.die(source);
        }

        return true;
    }

    private float getSoundPitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    private void die(DamageSource source) {
        if (this.isRemoved()) {
            return;
        }

        this.setHealth(0.0F);
        this.setRemoved(RemovalReason.KILLED);

        this.playSound(SoundEvents.VILLAGER_DEATH, 1.0F, getSoundPitch());

        // eject riders on death
        this.ejectPassengers();

        // clear trap state
        this.trappedPassengerId = null;
        this.trappedTicks = 0;
        this.snatchOffset = Vec3.ZERO;


        boolean creativeInstabuild =
                source.getEntity() instanceof Player player && player.getAbilities().instabuild;

        if (creativeInstabuild && !this.hasCustomName()) {
            this.discard();
        } else {
            this.spawnAtLocation(this.getDropItem());
            this.discard();
        }
    }


    @Override
    public void activateMinecart(int x, int y, int z, boolean powered) {
        super.activateMinecart(x, y, z, powered);

        if (!this.level().isClientSide && powered && this.isVehicle()) {
            dismountTrappedPassengerWithCooldown();
        }
    }


    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Health", this.health);
        tag.putInt("SuckTimer", suckTimer);
        tag.putInt("SuckCooldown", suckCooldown);
        if (suckTargetId != null) {
            tag.putUUID("SuckTarget", suckTargetId);
        }
        if (trappedPassengerId != null) {
            tag.putUUID("TrappedPassenger", trappedPassengerId);
            tag.putInt("TrappedTicks", trappedTicks);
        }
        if (lastReleasedPlayerId != null && lastReleasedCooldown > 0) {
            tag.putUUID("LastReleased", lastReleasedPlayerId);
            tag.putInt("LastReleasedCD", lastReleasedCooldown);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Health", Tag.TAG_FLOAT)) {
            this.health = tag.getFloat("Health");
        } else {
            this.health = MAX_HEALTH;
        }
        suckTimer = tag.getInt("SuckTimer");
        suckCooldown = tag.getInt("SuckCooldown");
        suckTargetId = tag.hasUUID("SuckTarget") ? tag.getUUID("SuckTarget") : null;
        if (tag.hasUUID("TrappedPassenger")) {
            trappedPassengerId = tag.getUUID("TrappedPassenger");
            trappedTicks = tag.getInt("TrappedTicks");
        } else {
            trappedPassengerId = null;
            trappedTicks = 0;
        }
        if (tag.hasUUID("LastReleased")) {
            lastReleasedPlayerId = tag.getUUID("LastReleased");
            lastReleasedCooldown = tag.getInt("LastReleasedCD");
        } else {
            lastReleasedPlayerId = null;
            lastReleasedCooldown = 0;
        }
    }

    public float getHealth() {
        return this.health;
    }

    public void setHealth(float health) {
        this.health = Mth.clamp(health, 0.0F, MAX_HEALTH);
    }

    public void heal(float amount) {
        this.setHealth(this.health + amount);
    }

    public float getMaxHealth() {
        return MAX_HEALTH;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && this.getHealth() > 0.0F;
    }

    @Override
    public Item getDropItem() {
        return DNLItems.MIMICART.get();
    }

    @Override
    public Type getMinecartType() {
        return Type.RIDEABLE;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(DNLItems.MIMICART.get());
    }
    @Override
    public boolean fireImmune() {
        return true; // full vanilla fire immunity
    }

    @Override
    public void setSecondsOnFire(int seconds) {
        // block being lit on fire
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

}
