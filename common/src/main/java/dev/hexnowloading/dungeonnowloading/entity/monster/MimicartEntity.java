package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class MimicartEntity extends AbstractMinecart {

    private static final float MAX_HEALTH = 40.0F;
    private float health = MAX_HEALTH;

    private static final double DETECT_RADIUS = 7.0D;  // start charging here
    private static final double SUCK_RADIUS   = 5.0D;  // actually pull here

    private static final int SUCK_WINDUP_TICKS = 20;    // 1 second at 20 tps (comment was 2s before)
    private static final int SUCK_COOLDOWN_TICKS = 80;  // 4 seconds cooldown
    private static final int TRAP_DURATION_TICKS = 600; // 10 seconds
    private static final int POST_TRAP_COOLDOWN_TICKS = 100; // 5 seconds
    private static final float SPEED_MODIFIER = 1.5F;
    private static final float ATTACK_DAMAGE = 1.0F;

    // suck / pull state
    private int suckTimer;
    private int suckCooldown;
    private java.util.UUID suckTargetId;
    private boolean suckingActive;
    private int suckingTicks;

    // trap state
    private java.util.UUID trappedPassengerId;
    private int trappedTicks;

    // post-trap immunity
    private java.util.UUID lastReleasedPlayerId;
    private int lastReleasedCooldown;

    private boolean wasOnRail;

    private double xPush;
    private double zPush;

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
        if (trappedPassengerId != null) {
            if (this.getFirstPassenger() instanceof Player p && p.getUUID().equals(trappedPassengerId)) {
                trappedTicks++;

                if (trappedTicks % 60 == 0 && trappedTicks > 0) {
                    p.hurt(this.damageSources().generic(), ATTACK_DAMAGE); // 1 heart (2 damage)
                    this.playSound(SoundEvents.MINECART_RIDING, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                }

                if (trappedTicks >= TRAP_DURATION_TICKS) {
                    dismountTrappedPassengerWithCooldown();
                }

            } else {
                // passenger left some other way (death, teleport, etc.)
                trappedPassengerId = null;
                trappedTicks = 0;
            }
        }

        // If dead or already carrying somebody, don't try to suck in players
        if (!this.isAlive() || this.isVehicle()) {
            resetSuckState();
            return;
        }

        // --- suck logic ---

        // Cooldown ticking
        if (suckCooldown > 0) {
            suckCooldown--;
        }

        // Resolve current target (if we have one)
        Player target = getSuckTarget();

        // If we had a target UUID but that player no longer exists, reset
        if (suckTargetId != null && target == null) {
            resetSuckState();
            return;
        }

        // Windup phase: counting down before applying pull
        // Windup phase: counting down before applying pull
        if (suckTimer > 0 && target != null) {
            // Abort if they leave the detection radius during windup
            if (target.distanceToSqr(this) > DETECT_RADIUS * DETECT_RADIUS
                    || !canBePulled(target)
                    || !hasLineOfSightTo(target)) {
                resetSuckState();
            } else {
                suckTimer--;

                // Windup finished → apply pull if still in range
                if (suckTimer == 0) {
                    if (!target.isPassenger()
                            // must be within the *suck* radius now
                            && target.distanceToSqr(this) <= SUCK_RADIUS * SUCK_RADIUS
                            && hasLineOfSightTo(target)) {

                        spawnPullParticles();
                        this.playSound(SoundEvents.MINECART_RIDING, 1.0F, this.getSoundPitch());

                        applyPullTowardsSelf(target);
                        suckingActive = true;
                        suckingTicks = 20; // up to 1 second window to collide + mount
                    } else {
                        resetSuckState();
                    }
                }
            }
        }

        // Try to start a new windup (no active target, no cooldown)
        else if (suckTimer == 0 && suckTargetId == null && suckCooldown <= 0) {
            Player nearest = this.level().getNearestPlayer(this, DETECT_RADIUS);
            if (nearest != null && canBePulled(nearest) && hasLineOfSightTo(nearest)) {
                suckTargetId = nearest.getUUID();
                suckTimer = SUCK_WINDUP_TICKS;

                // first time it “locks on” to a player
                spawnDetectParticles();
                this.playSound(SoundEvents.MINECART_INSIDE_UNDERWATER, 0.7F, this.getSoundPitch());
            }
        }


        // After pull applied: check for collision to force mount
        if (suckingActive && target != null) {
            suckingTicks--;
            if (suckingTicks <= 0 || !canBePulled(target)) {
                resetSuckState();
            } else {
                // If their bounding boxes intersect, force mount
                if (!target.isPassenger() &&
                        this.getBoundingBox().inflate(0.2D).intersects(target.getBoundingBox())) {

                    target.startRiding(this, true);
                    beginTrap(target);
                    suckCooldown = SUCK_COOLDOWN_TICKS;
                    resetSuckState();
                }
            }
        }
    }

    private void resetSuckState() {
        this.suckTimer = 0;
        this.suckCooldown = Math.max(this.suckCooldown, 0);
        this.suckTargetId = null;
        this.suckingActive = false;
        this.suckingTicks = 0;
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

    private void applyPullTowardsSelf(Player target) {
        // Base target: slightly above the cart
        net.minecraft.world.phys.Vec3 cartPos = this.position().add(0.0D, 0.5D, 0.0D);

        // If the cart is moving, shift the target a bit *in front* of its motion
        net.minecraft.world.phys.Vec3 motion = this.getDeltaMovement();
        double speedSq = motion.x * motion.x + motion.z * motion.z; // horizontal speed²
        if (speedSq > 1.0E-4D) {
            net.minecraft.world.phys.Vec3 dir = new net.minecraft.world.phys.Vec3(motion.x, 0.0D, motion.z).normalize();

            // how far ahead of the cart we aim; tweak to taste
            double aheadDistance = 2.0D; // ~1.5 blocks in front
            cartPos = cartPos.add(dir.scale(aheadDistance));
        }

        // --- original logic from your "perfect" version, but using cartPos ---

        net.minecraft.world.phys.Vec3 delta = cartPos.subtract(target.position());

        // horizontal component (XZ)
        net.minecraft.world.phys.Vec3 deltaXZ = new net.minecraft.world.phys.Vec3(delta.x, 0.0D, delta.z);
        double distSq = deltaXZ.lengthSqr();
        if (distSq < 1.0E-4D) {
            return; // already basically on top
        }

        double dist = Math.sqrt(distSq);
        net.minecraft.world.phys.Vec3 dirXZ = deltaXZ.scale(1.0D / dist); // normalize

        // ---- Strength tuning (unchanged) ----
        double horizontalStrength = Mth.clamp(0.7D + dist * 0.15D, 0.7D, 2.5D);
        double verticalStrength   = Mth.clamp(0.4D + dist * 0.08D, 0.4D, 2.0D);

        double vx = dirXZ.x * horizontalStrength;
        double vz = dirXZ.z * horizontalStrength;
        double vy = verticalStrength;

        // blend with existing velocity so it feels like a grab, not a hard teleport of motion
        net.minecraft.world.phys.Vec3 current = target.getDeltaMovement();
        net.minecraft.world.phys.Vec3 newVel = current.scale(0.2D).add(vx, vy, vz);

        // final safety clamp so speed isn't completely insane
        double maxSpeedSq = 6.0D; // sqrt(4) = 2 blocks/tick max
        if (newVel.lengthSqr() > maxSpeedSq) {
            newVel = newVel.normalize().scale(Math.sqrt(maxSpeedSq));
        }

        target.setDeltaMovement(newVel);
        target.hurtMarked = true; // update on client immediately
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

    private void beginTrap(Player player) {
        this.trappedPassengerId = player.getUUID();
        this.trappedTicks = 0;

        // pick a push direction similar to furnace minecart: away from where the player was
        double dx = this.getX() - player.getX();
        double dz = this.getZ() - player.getZ();

        // if too tiny (they're almost exactly on top), fall back to current motion or a default
        double lenSq = dx * dx + dz * dz;
        if (lenSq < 1.0E-4D) {
            var vel = this.getDeltaMovement();
            dx = vel.x;
            dz = vel.z;
            lenSq = dx * dx + dz * dz;
            if (lenSq < 1.0E-4D) {
                // final fallback: arbitrary small push so furnace logic can align it to the track
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

            // 5s immunity
            this.lastReleasedPlayerId = p.getUUID();
            this.lastReleasedCooldown = POST_TRAP_COOLDOWN_TICKS;
        } else {
            // Fallback: if somehow not marked as trapped, just eject everyone.
            this.ejectPassengers();
        }
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
        return (this.isInWater() ? 3.0D : 4.0D) / 20.0D;
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
        tag.putBoolean("SuckingActive", suckingActive);
        tag.putInt("SuckingTicks", suckingTicks);
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
        suckingActive = tag.getBoolean("SuckingActive");
        suckingTicks = tag.getInt("SuckingTicks");
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
        return Items.MINECART;
    }

    @Override
    public Type getMinecartType() {
        return Type.RIDEABLE;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
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
