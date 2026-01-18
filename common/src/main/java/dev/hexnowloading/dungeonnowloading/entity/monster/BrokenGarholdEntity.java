package dev.hexnowloading.dungeonnowloading.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BrokenGarholdEntity extends Monster {

    public final AnimationState idleAnimationState = new AnimationState();

    private int hurtHits = 0;
    private boolean dropping = false;
    private boolean broken = false;

    public BrokenGarholdEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.setHealth(25.0f);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 35.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        // If we're dropping and touch ground => break
        if (dropping && !broken && this.onGround()) {
            breakOnGround((ServerLevel) level());
        }
    }

    private boolean hasPlayerPassenger() {
        if (getPassengers().isEmpty()) return false;
        return getPassengers().get(0) instanceof Player;
    }

    private void beginDrop() {
        dropping = true;

        this.level().playSound(
                null, // everyone hears
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.CHAIN_BREAK,
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );

        // enable gravity to fall naturally
        this.setNoGravity(false);

        // optional: kill horizontal motion so it drops straight down
        Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(0.0, v.y, 0.0);
        this.hurtMarked = true;
    }

    private void breakOnGround(ServerLevel level) {
        broken = true;

        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.METAL_BREAK,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        // Spawn "spawner-ish" particles (poof + smoke works well)
        spawnBreakFx(level);

        // Dismount passenger safely onto ground
        forceDismountAtSeat();

        // Remove entity
        this.discard();
    }

    private void spawnBreakFx(ServerLevel level) {
        Vec3 p = this.position().add(0, 0.5, 0);

        ParticleOptions spawnerDust = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPAWNER.defaultBlockState());

        level.sendParticles(
                spawnerDust,
                p.x, p.y, p.z,
                100,          // count
                0.7, 2.3, 0.7,
                0.15         // speed
        );
    }

    private void forceDismountAtSeat() {
        if (getPassengers().isEmpty()) return;

        Entity rider = getPassengers().get(0);

        // Compute seat position exactly like positionRider()
        float seatY = 0.5F;
        Vec3 seat = new Vec3(this.getX(), this.getY() + seatY, this.getZ());

        rider.stopRiding();

        rider.teleportTo(seat.x, seat.y, seat.z);

        if (rider instanceof Player p) {
            p.setDeltaMovement(Vec3.ZERO);
            p.hurtMarked = true;
        }
    }


    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) return;

        float seatY = 0.5F;

        Vec3 seat = new Vec3(this.getX(), this.getY() + seatY, this.getZ());
        moveFunction.accept(passenger, seat.x, seat.y, seat.z);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) {
            return super.hurt(source, amount);
        }

        boolean ok = super.hurt(source, amount);
        if (!ok) return false;

        // Only count real damage events
        if (!broken && !dropping && this.hasPlayerPassenger()) {
            hurtHits++;

            // First 2 hits: never drop
            if (hurtHits <= 2) {
                return true;
            }

            if (this.random.nextFloat() < 0.5f) {
                beginDrop();
            }
        }

        return true;
    }

    // Optional: if killed while holding player, also break + release
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide && !broken) {
            spawnBreakFx((ServerLevel) level());
            forceDismountAtSeat();
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }
}
