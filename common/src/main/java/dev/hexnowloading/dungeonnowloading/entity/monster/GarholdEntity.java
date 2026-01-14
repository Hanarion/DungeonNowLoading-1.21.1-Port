package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.garhold.GarholdDiveCaptureGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.garhold.GarholdHoverAboveTargetGoal;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.GarholdAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;

public class GarholdEntity extends Monster {

    private static final EntityDataAccessor<GarholdState> STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_STATE);
    private static final EntityDataAccessor<GarholdAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_ANIMATION_STATE);
    private static final EntityDataAccessor<GarholdBottomGateAnimationState> BOTTOM_GATE_ANIMATION_STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_GATE_ANIMATION_STATE);
    private static final EntityDataAccessor<GarholdSideGateAnimationState> SIDE_GATE_ANIMATION_STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_SIDE_GATE_ANIMATION_STATE);

    private AnimationChainer<GarholdAnimationState> animationChainer = new AnimationChainer<>();
    private AnimationChainer<GarholdBottomGateAnimationState> bottomGateAnimationChainer = new AnimationChainer<>();
    private AnimationChainer<GarholdSideGateAnimationState> sideGateAnimationChainer = new AnimationChainer<>();

    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState detachAnimationState = new AnimationState();
    public final AnimationState chargeDiveAnimationState = new AnimationState();
    public final AnimationState landDiveAnimationState = new AnimationState();
    public final AnimationState closingGateAnimationState = new AnimationState();
    public final AnimationState sideCaptureAnimationState = new AnimationState();
    public final AnimationState ascendAnimationState = new AnimationState();
    public final AnimationState idleHangAnimationState = new AnimationState();
    public final AnimationState reattachAnimationState = new AnimationState();

    public final AnimationState bottomOpenAnimationState = new AnimationState();
    public final AnimationState bottomOpenedAnimationState = new AnimationState();
    public final AnimationState bottomClosedAnimationState = new AnimationState();
    public final AnimationState sideOpenedAnimationState = new AnimationState();
    public final AnimationState sideClosedAnimationState = new AnimationState();
    public final AnimationState sideOpenAnimationState = new AnimationState();

    // --- Ascend movement (server tick based) ---
    private boolean ascendLiftActive = false;
    private int ascendLiftTicksLeft = 0;

    private static final float ASCEND_LIFT_START_PROGRESS = 0.25f;
    private static final int ASCEND_LIFT_TICKS = 10;
    private static final double ASCEND_LIFT_BLOCKS = 3.0;
    private static final double ASCEND_LIFT_PER_TICK = ASCEND_LIFT_BLOCKS / ASCEND_LIFT_TICKS;

    public GarholdEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.moveControl = new FlyingMoveControl(this, 16, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GarholdDiveCaptureGoal(this));
        this.goalSelector.addGoal(2, new GarholdHoverAboveTargetGoal(this, 1.5F, 6.0, 3.0, 14.0));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));

        super.registerGoals();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 35.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.2) // Required for slowing down the initial speed on ground.
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ARMOR, 15.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, GarholdState.FLYING);
        this.entityData.define(ANIMATION_STATE, GarholdAnimationState.NONE);
        this.entityData.define(BOTTOM_GATE_ANIMATION_STATE, GarholdBottomGateAnimationState.BOTTOM_CLOSED);
        this.entityData.define(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.SIDE_CLOSED);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Chained", this.isGarholdState(GarholdState.CHAINED));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setGarholdState(compoundTag.getBoolean("Chained") ? GarholdState.CHAINED : GarholdState.FLYING);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.entityData.get(ANIMATION_STATE).equals(GarholdAnimationState.NONE)) {
            this.playFlyAnimation();
            this.playOpenedBottomAnimation();
            this.playOpenedSideAnimation();
        }

        animationChainer.tick(this::transitionTo);
        bottomGateAnimationChainer.tick(this::transitionTo);
        sideGateAnimationChainer.tick(this::transitionTo);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (ascendLiftTicksLeft > 0) {
            // keep it stable during the lift
            this.setDeltaMovement(0.0, 0.0, 0.0);

            // move up smoothly
            this.setPos(this.getX(), this.getY() + ASCEND_LIFT_PER_TICK, this.getZ());

            ascendLiftTicksLeft--;

            if (ascendLiftTicksLeft <= 0) {
                ascendLiftTicksLeft = 0;
                ascendLiftActive = false;
            }
        }
    }


    public boolean hasCapturedPlayer() {
        return !this.getPassengers().isEmpty();
    }

    public void beginCapture(Player player) {

        // Kick them off whatever they were riding
        if (player.isPassenger()) {
            player.stopRiding();
        }

        player.startRiding(this, true);
        playClosingGateAnimation();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) return;

        float seatY;

        GarholdAnimationState anim = this.entityData.get(ANIMATION_STATE);

        if (anim == GarholdAnimationState.FLY) {
            seatY = 0.4f;
        } else if (anim == GarholdAnimationState.CLOSING_GATE) {
            seatY = 0.1f;
        } else if (anim == GarholdAnimationState.ASCEND) {
            // before 25% progress (lift not started yet) -> 0.1
            if (!ascendLiftActive) {
                seatY = 0.1f;
            } else {
                // lift is 10 ticks: ticksLeft goes 10..0
                float t = 1.0f - (ascendLiftTicksLeft / (float) ASCEND_LIFT_TICKS); // 0 -> 1
                seatY = 0.1f + (0.4f - 0.1f) * t; // lerp 0.1 -> 0.3
            }
        } else {
            seatY = 0.1f; // default
        }

        Vec3 seat = new Vec3(this.getX(), this.getY() + seatY, this.getZ());
        moveFunction.accept(passenger, seat.x, seat.y, seat.z);
    }


    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    public GarholdState getGarholdState() {
        return this.entityData.get(STATE);
    }

    public void setGarholdState(GarholdState state) {
        this.entityData.set(STATE, state);
    }

    public boolean isGarholdState(GarholdState state) {
        return this.entityData.get(STATE).equals(state);
    }

    public boolean isNoAnimation() {
        return this.entityData.get(ANIMATION_STATE).equals(GarholdAnimationState.NONE);
    }

    private void playIdleOrIdleBreak() {
            this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdAnimationState.FLY, GarholdAnimationDuration.FLY, null, this::playIdleOrIdleBreak));
    }

    public void playFlyAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdAnimationState.FLY, GarholdAnimationDuration.FLY, null, this::playIdleOrIdleBreak));
    }

    public void playIdleHangingAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.looping(GarholdAnimationState.IDLE_HANGING, GarholdAnimationDuration.IDLE_HANGING));
    }

    public void playChargeDiveWithProgress(BiConsumer<GarholdAnimationState, Float> onProgress, Runnable onComplete) {
        this.playCloseBottomAnimation();
        this.playClosedSideAnimation();
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdAnimationState.CHARGE_DIVE, GarholdAnimationDuration.CHARGE_DIVE, null, onComplete, onProgress));
    }

    public void playLandDiveAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdAnimationState.LAND_DIVE, GarholdAnimationDuration.LAND_DIVE, null,
                        () -> {
                            if (this.hasCapturedPlayer()) {
                                this.playClosingGateAnimation();
                            } else {
                                this.playAscendAnimation();
                            }
                        }
                )
        );
    }

    public void playClosingGateAnimation() {
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdAnimationState.CLOSING_GATE, GarholdAnimationDuration.CLOSING_GATE, null, this::playAscendAnimation));
    }


    public void playAscendAnimation() {
        this.playOpeningSideAnimation();
        this.playOpenBottomAnimation();
        this.animationChainer.enqueue(
                AnimationChainer.AnimationStep.of(
                        GarholdAnimationState.ASCEND,
                        GarholdAnimationDuration.ASCEND,
                        () -> { // onStart
                            ascendLiftActive = false;
                            ascendLiftTicksLeft = 0;
                        },
                        () -> { // onComplete
                            ascendLiftActive = false;
                            ascendLiftTicksLeft = 0;

                            this.setGarholdState(GarholdState.FLYING);
                            this.playFlyAnimation(); // optional but usually what you want
                        },
                        (anim, progress) -> { // onProgress
                            if (!ascendLiftActive && progress >= ASCEND_LIFT_START_PROGRESS) {
                                ascendLiftActive = true;
                                ascendLiftTicksLeft = ASCEND_LIFT_TICKS;
                            }
                        }
                )
        );
    }

    // Bottom Animations

    public void playCloseBottomAnimation() {
        this.bottomGateAnimationChainer.reset();
        this.bottomGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdBottomGateAnimationState.BOTTOM_CLOSED, GarholdAnimationDuration.BOTTOM_CLOSED));
    }

    public void playOpenBottomAnimation() {
        this.bottomGateAnimationChainer.reset();
        this.bottomGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdBottomGateAnimationState.BOTTOM_OPEN, GarholdAnimationDuration.BOTTOM_OPEN));
        this.bottomGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.looping(GarholdBottomGateAnimationState.BOTTOM_OPENED, GarholdAnimationDuration.BOTTOM_OPENED));
    }

    public void playOpenedBottomAnimation() {
        this.bottomGateAnimationChainer.reset();
        this.bottomGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.looping(GarholdBottomGateAnimationState.BOTTOM_OPENED, GarholdAnimationDuration.BOTTOM_OPENED));
    }

    // Side Animations

    public void playOpeningSideAnimation() {
        this.sideGateAnimationChainer.reset();
        this.sideGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdSideGateAnimationState.SIDE_OPEN, GarholdAnimationDuration.SIDE_OPENING));
        this.sideGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.looping(GarholdSideGateAnimationState.SIDE_OPENED, GarholdAnimationDuration.SIDE_OPENED));
    }

    public void playOpenedSideAnimation() {
        this.sideGateAnimationChainer.reset();
        this.sideGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.looping(GarholdSideGateAnimationState.SIDE_OPENED, GarholdAnimationDuration.SIDE_OPENED));
    }

    public void playClosedSideAnimation() {
        this.sideGateAnimationChainer.reset();
        this.sideGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.looping(GarholdSideGateAnimationState.SIDE_CLOSED, GarholdAnimationDuration.SIDE_CLOSED));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            GarholdAnimationState animationState = this.entityData.get(ANIMATION_STATE);
            this.resetAnimation();
            switch (animationState) {
                case FLY -> this.flyAnimationState.startIfStopped(this.tickCount);
                case DETACH -> this.detachAnimationState.startIfStopped(this.tickCount);
                case CHARGE_DIVE -> this.chargeDiveAnimationState.startIfStopped(this.tickCount);
                case LAND_DIVE -> this.landDiveAnimationState.startIfStopped(this.tickCount);
                case CLOSING_GATE -> this.closingGateAnimationState.startIfStopped(this.tickCount);
                case SIDE_CAPTURE -> this.sideCaptureAnimationState.startIfStopped(this.tickCount);
                case ASCEND -> this.ascendAnimationState.startIfStopped(this.tickCount);
                case IDLE_HANGING -> this.idleHangAnimationState.startIfStopped(this.tickCount);
                case REATTACH -> this.reattachAnimationState.startIfStopped(this.tickCount);
            }
        }
        if (BOTTOM_GATE_ANIMATION_STATE.equals(entityDataAccessor)) {
            GarholdBottomGateAnimationState gateAnimationState = this.entityData.get(BOTTOM_GATE_ANIMATION_STATE);
            this.resetBottomGateAnimation();
            switch (gateAnimationState) {
                case BOTTOM_CLOSED -> this.bottomClosedAnimationState.startIfStopped(this.tickCount);
                case BOTTOM_OPEN -> this.bottomOpenAnimationState.startIfStopped(this.tickCount);
                case BOTTOM_OPENED -> this.bottomOpenedAnimationState.startIfStopped(this.tickCount);
            }
        }
        if (SIDE_GATE_ANIMATION_STATE.equals(entityDataAccessor)) {
            GarholdSideGateAnimationState sideGateAnimationState = this.entityData.get(SIDE_GATE_ANIMATION_STATE);
            this.resetSideGateAnimation();
            switch (sideGateAnimationState) {
                case SIDE_CLOSED -> this.sideClosedAnimationState.startIfStopped(this.tickCount);
                case SIDE_OPEN -> this.sideOpenAnimationState.startIfStopped(this.tickCount);
                case SIDE_OPENED -> this.sideOpenedAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetBottomGateAnimation() {
        this.bottomOpenAnimationState.stop();
        this.bottomOpenedAnimationState.stop();
        this.bottomClosedAnimationState.stop();
    }

    private void resetAnimation() {
        this.flyAnimationState.stop();
        this.detachAnimationState.stop();
        this.chargeDiveAnimationState.stop();
        this.landDiveAnimationState.stop();
        this.closingGateAnimationState.stop();
        this.sideCaptureAnimationState.stop();
        this.ascendAnimationState.stop();
        this.idleHangAnimationState.stop();
        this.reattachAnimationState.stop();
    }

    private void resetSideGateAnimation() {
        this.sideOpenedAnimationState.stop();
        this.sideOpenAnimationState.stop();
        this.sideClosedAnimationState.stop();
    }

    public GarholdEntity transitionTo(GarholdAnimationState state) {
        switch (state) {
            case FLY -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.FLY);
            case DETACH -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.DETACH);
            case CHARGE_DIVE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.CHARGE_DIVE);
            case LAND_DIVE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.LAND_DIVE);
            case CLOSING_GATE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.CLOSING_GATE);
            case SIDE_CAPTURE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.SIDE_CAPTURE);
            case ASCEND -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.ASCEND);
            case IDLE_HANGING -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.IDLE_HANGING);
            case REATTACH -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.REATTACH);
        }
        return this;
    }

    public GarholdEntity transitionTo(GarholdBottomGateAnimationState state) {
        switch (state) {
            case BOTTOM_CLOSED -> this.entityData.set(BOTTOM_GATE_ANIMATION_STATE, GarholdBottomGateAnimationState.BOTTOM_CLOSED);
            case BOTTOM_OPEN -> this.entityData.set(BOTTOM_GATE_ANIMATION_STATE, GarholdBottomGateAnimationState.BOTTOM_OPEN);
            case BOTTOM_OPENED -> this.entityData.set(BOTTOM_GATE_ANIMATION_STATE, GarholdBottomGateAnimationState.BOTTOM_OPENED);
        }
        return this;
    }

    public GarholdEntity transitionTo(GarholdSideGateAnimationState state) {
        switch (state) {
            case SIDE_CLOSED -> this.entityData.set(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.SIDE_CLOSED);
            case SIDE_OPEN -> this.entityData.set(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.SIDE_OPEN);
            case SIDE_OPENED -> this.entityData.set(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.SIDE_OPENED);
        }
        return this;
    }

    public enum GarholdState {
        CHAINED,
        DETACH,
        FLYING,
        DIVE
    }

    public enum GarholdBottomGateAnimationState {
        BOTTOM_OPEN,
        BOTTOM_OPENED,
        BOTTOM_CLOSED,
    }

    public enum GarholdSideGateAnimationState {
        SIDE_OPENED,
        SIDE_OPEN,
        SIDE_CLOSED
    }

    public enum GarholdAnimationState {
        NONE,
        FLY,
        DETACH,
        CHARGE_DIVE,
        LAND_DIVE,
        CLOSING_GATE,
        SIDE_CAPTURE,
        ASCEND,
        IDLE_HANGING,
        REATTACH
    }
}
