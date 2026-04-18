package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.ReaperSpiderAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.ReaperSpiderMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.ReaperSpiderAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ReaperSpiderEntity extends Spider {

    private static final EntityDataAccessor<ReaperSpiderAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(ReaperSpiderEntity.class, EntityStates.REAPER_SPIDER_ANIMATION_STATE);

    private AnimationChainer<ReaperSpiderAnimationState> animationChainer = new AnimationChainer<>();

    public final AnimationState windUpAnimationState = new AnimationState();
    public final AnimationState tacklingAnimationState = new AnimationState();
    public final AnimationState doubleSlashAnimationState = new AnimationState();
    public final AnimationState recoveryAnimationState = new AnimationState();
    public final AnimationState singleSlashAnimationState = new AnimationState();

    private boolean fastClimb;

    public ReaperSpiderEntity(EntityType<? extends ReaperSpiderEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new ReaperSpiderMoveControl(this,
                3.0D, // sideStrafeSpeed (very fast sidestep)
                0.8D  // backStrafeSpeed
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 19.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_STATE,  ReaperSpiderAnimationState.IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new ReaperSpiderAttackGoal(this));

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            double moveAttr = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            Vec3 vel = this.getDeltaMovement();
            System.out.println("[ReaperSpider] attr=" + moveAttr
                    + " vel=(" + vel.x + ", " + vel.y + ", " + vel.z + ") "
                    + "state=" + this.getAnimationState());
        }
        this.animationChainer.tick(this::transitionTo);
    }

    public void playDoubleSlashAnimation(Runnable damageWindowCallback) {
        this.animationChainer.reset();

        // Tracks if we've already applied damage in this animation's window.
        final boolean[] hasHitInWindow = new boolean[] { false };

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                ReaperSpiderAnimationState.DOUBLE_SLASH,
                ReaperSpiderAnimationDuration.DOUBLE_SLASH,
                null, // onStart
                () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE), // onComplete
                (anim, progress) -> {
                    // damage window [0.4, 0.5]
                    if (!hasHitInWindow[0]
                            && progress >= 0.4f
                            && progress <= 0.5f) {

                        hasHitInWindow[0] = true; // ensure it only fires once

                        if (damageWindowCallback != null && this.isAlive()) {
                            damageWindowCallback.run();
                        }
                    }
                }
        ));
    }

    public void playSingleSlashAnimation(Runnable damageWindowCallback) {
        this.animationChainer.reset();

        // Tracks if we've already applied damage in this animation's window.
        final boolean[] hasHitInWindow = new boolean[] { false };

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                ReaperSpiderAnimationState.SINGLE_SLASH,
                ReaperSpiderAnimationDuration.SINGLE_SLASH,
                null, // onStart
                () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE), // onComplete
                (anim, progress) -> {
                    // damage window [0.4, 0.5]
                    if (!hasHitInWindow[0]
                            && progress >= 0.4f
                            && progress <= 0.5f) {

                        hasHitInWindow[0] = true; // ensure it only fires once

                        if (damageWindowCallback != null && this.isAlive()) {
                            damageWindowCallback.run();
                        }
                    }
                }
        ));
    }
   /* public void playSingleSlashAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(ReaperSpiderAnimationState.SINGLE_SLASH, ReaperSpiderAnimationDuration.SINGLE_SLASH, null, () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE)));
    }*/

    public void playWindUpAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(ReaperSpiderAnimationState.WIND_UP, ReaperSpiderAnimationDuration.WIND_UP, null, () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE)));
    }

    @Override
    public boolean onClimbable() {
        boolean vanilla = super.onClimbable();
        return vanilla || this.horizontalCollision;
    }

    public void setFastClimb(boolean fastClimb) {
        this.fastClimb = fastClimb;
    }

    public boolean isFastClimb() {
        return fastClimb;
    }

    @Override
    public void travel(Vec3 travelVector) {
        // let vanilla / pathfinding handle movement first
        super.travel(travelVector);

        // then post-adjust climb speed if we want it faster
        if (this.fastClimb && this.onClimbable() && !this.isFallFlying()) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.y > 0.0D) {
                // multiply the existing climb speed
                double multiplier = 2.0D; // 2x vanilla climb speed, tweak to taste
                this.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            ReaperSpiderAnimationState animationState = this.getAnimationState();
            this.resetAnimation();
            switch (animationState) {
                case WIND_UP -> this.windUpAnimationState.startIfStopped(this.tickCount);
                case TACKLING -> this.tacklingAnimationState.startIfStopped(this.tickCount);
                case DOUBLE_SLASH -> this.doubleSlashAnimationState.startIfStopped(this.tickCount);
                case RECOVERY -> this.recoveryAnimationState.startIfStopped(this.tickCount);
                case SINGLE_SLASH -> this.singleSlashAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public ReaperSpiderEntity transitionTo(ReaperSpiderAnimationState animationState) {
        switch (animationState) {
            case IDLE:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE);
                break;
            case WIND_UP:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.WIND_UP);
                break;
            case TACKLING:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.TACKLING);
                break;
            case DOUBLE_SLASH:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.DOUBLE_SLASH);
                break;
            case RECOVERY:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.RECOVERY);
                break;
            case SINGLE_SLASH:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.SINGLE_SLASH);
                break;
        }
        return this;
    }

    private void resetAnimation() {
        this.tacklingAnimationState.stop();
        this.doubleSlashAnimationState.stop();
        this.singleSlashAnimationState.stop();
        this.recoveryAnimationState.stop();
        this.windUpAnimationState.stop();
    }

    public boolean isAttackAnimationRunning() {
        return getAnimationState() != ReaperSpiderAnimationState.IDLE;
    }

    public ReaperSpiderAnimationState getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public enum ReaperSpiderAnimationState {
        IDLE,
        WIND_UP,
        TACKLING,
        DOUBLE_SLASH,
        RECOVERY,
        SINGLE_SLASH
    }
}
