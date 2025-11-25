package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class MimicartEntity extends AbstractMinecart {

    private static final float MAX_HEALTH = 40.0F; // or whatever you want
    private float health = MAX_HEALTH;

    private static final double SUCK_RADIUS = 5.0D;
    private static final int SUCK_WINDUP_TICKS = 20;    // 2 seconds at 20 tps
    private static final int SUCK_COOLDOWN_TICKS = 80;  // 4 seconds cooldown (tweak)

    private int suckTimer;          // counts down during windup
    private int suckCooldown;       // delay between attempts
    private java.util.UUID suckTargetId;
    private boolean suckingActive;  // we already applied motion, waiting for collision/mount
    private int suckingTicks;

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

        // If dead or already carrying somebody, don't try to suck in players
        if (!this.isAlive() || this.isVehicle()) {
            resetSuckState();
            return;
        }

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

        // --- Windup phase: counting down before applying pull ---
        if (suckTimer > 0 && target != null) {
            // Abort if they leave the radius during windup
            if (target.distanceToSqr(this) > SUCK_RADIUS * SUCK_RADIUS || !canBePulled(target)) {
                resetSuckState();
            } else {
                suckTimer--;

                // Windup finished → apply pull if still in range
                if (suckTimer == 0) {
                    if (!target.isPassenger() && target.distanceToSqr(this) <= SUCK_RADIUS * SUCK_RADIUS) {
                        // timer just finished and pull is valid → play “grab” particles
                        spawnPullParticles();
                        this.playSound(SoundEvents.MINECART_RIDING, 1.0F, this.getSoundPitch()); // optional

                        applyPullTowardsSelf(target);
                        suckingActive = true;
                        suckingTicks = 20; // up to 1 second window to collide + mount
                    } else {
                        resetSuckState();
                    }
                }

            }
        }
        // --- Try to start a new windup (no active target, no cooldown) ---
        else if (suckTimer == 0 && suckTargetId == null && suckCooldown <= 0) {
            Player nearest = this.level().getNearestPlayer(this, SUCK_RADIUS);
            if (nearest != null && canBePulled(nearest)) {
                suckTargetId = nearest.getUUID();
                suckTimer = SUCK_WINDUP_TICKS;

                // first time it “locks on” to a player
                spawnDetectParticles();
                this.playSound(SoundEvents.MINECART_INSIDE_UNDERWATER, 0.7F, this.getSoundPitch()); // optional
            }
        }

        // --- After pull applied: check for collision to force mount ---
        if (suckingActive && target != null) {
            suckingTicks--;
            if (suckingTicks <= 0 || !canBePulled(target)) {
                resetSuckState();
            } else {
                // If their bounding boxes intersect, force mount
                if (!target.isPassenger() &&
                        this.getBoundingBox().inflate(0.2D).intersects(target.getBoundingBox())) {

                    target.startRiding(this, true);
                    suckCooldown = SUCK_COOLDOWN_TICKS;
                    resetSuckState();
                }
            }
        }
    }

    private void resetSuckState() {
        this.suckTimer = 0;
        this.suckCooldown = Math.max(this.suckCooldown, 0); // keep current CD if set
        this.suckTargetId = null;
        this.suckingActive = false;
        this.suckingTicks = 0;
    }

    private boolean canBePulled(Player player) {
        // tweak as you like
        return !player.isSpectator()
                && !player.isCreative()
                && player.isAlive();
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
        // point we want to throw them toward (slightly above the cart)
        net.minecraft.world.phys.Vec3 cartPos = this.position().add(0.0D, 0.5D, 0.0D);
        net.minecraft.world.phys.Vec3 delta = cartPos.subtract(target.position());

        // horizontal component (XZ)
        net.minecraft.world.phys.Vec3 deltaXZ = new net.minecraft.world.phys.Vec3(delta.x, 0.0D, delta.z);
        double distSq = deltaXZ.lengthSqr();
        if (distSq < 1.0E-4D) {
            return; // already basically on top
        }

        double dist = Math.sqrt(distSq);
        net.minecraft.world.phys.Vec3 dirXZ = deltaXZ.scale(1.0D / dist); // normalize

        // ---- Strength tuning ----
        // stronger pull the further away they are (within reason)
        double horizontalStrength = Mth.clamp(0.7D + dist * 0.15D, 0.7D, 2.5D);
        double verticalStrength   = Mth.clamp(0.4D + dist * 0.08D, 0.4D, 2.0D);

        double vx = dirXZ.x * horizontalStrength;
        double vz = dirXZ.z * horizontalStrength;
        double vy = verticalStrength;

        // blend with existing velocity so it feels like a grab, not a hard teleport of motion
        net.minecraft.world.phys.Vec3 current = target.getDeltaMovement();
        net.minecraft.world.phys.Vec3 newVel = current.scale(0.2D).add(vx, vy, vz);

        // final safety clamp so speed isn't completely insane
        double maxSpeedSq = 4.0D; // sqrt(4) = 2 blocks/tick max
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
                    ParticleTypes.ANGRY_VILLAGER, // detection effect
                    cx + ox, cy + oy, cz + oz,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }

    private void spawnPullParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // stronger central burst when the pull actually triggers
        double cx = this.getX();
        double cy = this.getY() + 0.5D;
        double cz = this.getZ();

        serverLevel.sendParticles(
                ParticleTypes.PORTAL, // different effect for the grab
                cx, cy, cz,
                20,        // count
                0.4D, 0.3D, 0.4D, // spread
                0.1D       // speed
        );
    }



    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true; // client trusts server
        }

        if (this.isInvulnerableTo(source)) {
            return false;
        }

        // vanilla minecart damage visuals
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

        // --- SFX: hurt ---
        this.playSound(SoundEvents.VILLAGER_HURT, 1.0F, this.getSoundPitch());

        // apply health damage
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

        // ensure the entity reports as dead
        this.setRemoved(RemovalReason.KILLED);

        // death sound
        this.playSound(SoundEvents.VILLAGER_DEATH, 1.0F, getSoundPitch());

        this.ejectPassengers();

        boolean creativeInstabuild =
                source.getEntity() instanceof Player player && player.getAbilities().instabuild;

        if (creativeInstabuild && !this.hasCustomName()) {
            // insta-delete without drops
            this.discard();
        } else {
            // force drop behavior
            this.spawnAtLocation(this.getDropItem());
            this.discard(); // instead of destroy()
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
        // swap to your custom minecart item later if you want
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
}
