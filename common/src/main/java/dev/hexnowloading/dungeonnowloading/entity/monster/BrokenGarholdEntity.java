package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.BrokenGarholdAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BrokenGarholdEntity extends Monster {

    private static final EntityDataAccessor<BrokenGarholdState> STATE = SynchedEntityData.defineId(BrokenGarholdEntity.class, EntityStates.BROKEN_GARHOLD_STATE);

    private AnimationChainer<BrokenGarholdState> animationChainer = new AnimationChainer<>();

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState fallingStartAnimationState = new AnimationState();
    public final AnimationState fallingAnimationState = new AnimationState();
    public final AnimationState openAnimationState = new AnimationState();

    private int hurtHits = 0;
    private boolean dropping = false;
    private boolean broken = false;
    private boolean releaseInsteadOfDrop = false;

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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, BrokenGarholdState.HANGING);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Chained", this.entityData.get(STATE) == BrokenGarholdState.HANGING || this.entityData.get(STATE) == BrokenGarholdState.OPEN);
        compoundTag.putBoolean("ReleaseInsteadOfDrop", this.releaseInsteadOfDrop);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(STATE, compoundTag.getBoolean("Chained") ? BrokenGarholdState.HANGING : BrokenGarholdState.FALLING);
        this.releaseInsteadOfDrop = compoundTag.getBoolean("ReleaseInsteadOfDrop");
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        if (!broken && !dropping && this.entityData.get(STATE) == BrokenGarholdState.HANGING) {
            if (!hasChainAboveSelf()) {
                this.playFallingAnimation(); // handles FALLING_START -> beginDrop()
            }
        }

        // If we're dropping and touch ground => break
        if (dropping && !broken && this.onGround()) {
            breakOnGround((ServerLevel) level());
        }

        animationChainer.tick(this::transitionTo);
    }

    private boolean isChainBlock(BlockPos pos) {
        return this.level().getBlockState(pos).is(Blocks.CHAIN);
    }

    private boolean hasChainAboveSelf() {
        AABB box = this.getBoundingBox();

        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        double y0 = box.minY;

        BlockPos base = BlockPos.containing(cx, y0, cz);

        // Same offset as your Garhold
        return isChainBlock(base.above(3));
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

            if (releaseInsteadOfDrop) {
                hurtHits = 0;
                this.setHealth(25.0f);
                this.playOpenAnimation();
            } else {
                this.playFallingAnimation();
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

    public void setReleaseInsteadOfDrop(boolean value) {
        this.releaseInsteadOfDrop = value;
    }

    public boolean isHangingOrOpening() {
        return this.entityData.get(STATE).equals(BrokenGarholdState.HANGING) || this.entityData.get(STATE).equals(BrokenGarholdState.OPEN);
    }

    public void playFallingAnimation() {
        if (dropping || broken) return;
        if (this.entityData.get(STATE) == BrokenGarholdState.FALLING_START || this.entityData.get(STATE) == BrokenGarholdState.FALLING) return;
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(BrokenGarholdState.FALLING_START, BrokenGarholdAnimationDuration.FALLING_START, null, this::beginDrop));
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(BrokenGarholdState.FALLING, BrokenGarholdAnimationDuration.FALLING));
    }

    public void playOpenAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(BrokenGarholdState.OPEN, BrokenGarholdAnimationDuration.OPEN, this::forceDismountAtSeat, null));
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.looping(BrokenGarholdState.HANGING, 0.0F));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (STATE.equals(entityDataAccessor)) {
            BrokenGarholdState state = this.entityData.get(STATE);
            switch (state) {
                case FALLING_START -> this.fallingStartAnimationState.startIfStopped(this.tickCount);
                case FALLING -> {
                    this.fallingStartAnimationState.stop();
                    this.fallingAnimationState.startIfStopped(this.tickCount);
                }
                case OPEN -> this.openAnimationState.start(this.tickCount);
            }

        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public BrokenGarholdEntity transitionTo(BrokenGarholdState state) {
        switch (state) {
            case HANGING -> this.entityData.set(STATE, BrokenGarholdState.HANGING);
            case FALLING_START -> this.entityData.set(STATE, BrokenGarholdState.FALLING_START);
            case FALLING -> this.entityData.set(STATE, BrokenGarholdState.FALLING);
            case OPEN -> this.entityData.set(STATE, BrokenGarholdState.OPEN);
        }
        return this;
    }

    public enum BrokenGarholdState {
        HANGING,
        FALLING_START,
        FALLING,
        OPEN
    }
}
