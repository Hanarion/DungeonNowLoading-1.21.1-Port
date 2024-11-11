package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.*;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.SmoothBodyRotationControl;
import dev.hexnowloading.dungeonnowloading.entity.projectile.FlameProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BallistaGolemEntity extends Monster implements Enemy, SlumberingEntity {

    private static final EntityDataAccessor<BallistaGolemState> STATE = SynchedEntityData.defineId(BallistaGolemEntity.class, EntityStates.BALLISTA_GOLEM_STATE);
    private static final EntityDataAccessor<Integer> ARROW_COUNT = SynchedEntityData.defineId(BallistaGolemEntity.class, EntityDataSerializers.INT);
    public static final int BALLISTA_GOLEM_MELEE_RANGE = 7;
    private int aiTick = 0;

    public final AnimationState wakeUpAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState reloadAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();

    private static final byte TRIGGER_WAKING_UP_ANIMATION_BYTE = 70;
    private static final byte TRIGGER_IDLE_ANIMATION_BYTE = 71;
    private static final byte TRIGGER_RELOAD_ANIMATION_BYTE = 72;
    private static final byte TRIGGER_SHOOT_ANIMATION_BYTE = 73;

    public BallistaGolemEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setState(BallistaGolemState.SLUMBERING);
        this.setBallistaArrowCount(6);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 115.0F)
                .add(Attributes.ATTACK_DAMAGE, 19.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 32.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BallistaGolemReloadGoal(this));
        this.goalSelector.addGoal(2, new BallistaGolemArrowAttackGoal(this));
        this.goalSelector.addGoal(3, new BallistaGolemMeleeAttackGoal(this, 1.0, true, 1.1F));
        this.goalSelector.addGoal(4, new AllRangeMeleeAttackGoal(this, 0.5, true, 1.1F));
        this.goalSelector.addGoal(5, new SlumberingEntityRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(6, new SlumberingEntityLookAtPlayerGoal(this, Player.class, 6.0F));
        this.targetSelector.addGoal(1, new SlumberingEntityPlayerTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, BallistaGolemState.SLUMBERING);
        this.entityData.define(ARROW_COUNT, 6);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Slumbering", isSlumbering());
        compoundTag.putInt("ArrowCount", this.entityData.get(ARROW_COUNT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        boolean isSlumbering = compoundTag.getBoolean("Slumbering");
        this.entityData.set(STATE, isSlumbering ? BallistaGolemState.SLUMBERING : BallistaGolemState.IDLE);
        this.entityData.set(ARROW_COUNT, compoundTag.getInt("ArrowCount"));
    }

    @Override
    protected @NotNull BodyRotationControl createBodyControl() {
        return new SmoothBodyRotationControl(this, 5.0f);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isSlumbering()) {
            this.setState(BallistaGolemState.AWAKENING);
            this.triggerWakingUpAnimation();
            this.aiTick = 153;
        }
        if (this.isState(BallistaGolemState.AWAKENING)) {
            if (aiTick > 0) {
                aiTick--;
            } else {
                this.setState(BallistaGolemState.IDLE);
                this.triggerIdleAnimation();
            }
        }
        super.customServerAiStep();
    }

    @Override
    public double getMeleeAttackRangeSqr(LivingEntity livingEntity) {
        return (double) (this.getBbWidth() * this.getBbWidth());
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 2.0F;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isSlumbering();
    }

    @Override
    public void handleEntityEvent(byte b) {
        switch (b) {
            case TRIGGER_WAKING_UP_ANIMATION_BYTE:
                this.wakeUpAnimationState.start(this.tickCount);
                break;
            case TRIGGER_IDLE_ANIMATION_BYTE:
                this.idleAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SHOOT_ANIMATION_BYTE:
                this.idleAnimationState.stop();
                this.shootAnimationState.start(this.tickCount);
                break;
            case TRIGGER_RELOAD_ANIMATION_BYTE:
                this.idleAnimationState.stop();
                this.reloadAnimationState.start(this.tickCount);
                break;
            default:
                super.handleEntityEvent(b);
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.is(DNLTags.BALLISTA_GOLEM_HURTABLE)) {
            return false;
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    public boolean isSlumbering() {
        return this.isState(BallistaGolemState.SLUMBERING);
    }

    @Override
    public boolean isStationary() {
        return this.isSlumbering() || this.isState(BallistaGolemState.AWAKENING) || this.isState(BallistaGolemState.SHOOT) || this.isState(BallistaGolemState.RELOAD);
    }

    public void setBallistaArrowCount(int arrowCount) { this.entityData.set(ARROW_COUNT, arrowCount); }
    public int getBallistaArrowCount() { return this.entityData.get(ARROW_COUNT); }
    public double getFollowDistance() { return this.getAttributeValue(Attributes.FOLLOW_RANGE); }
    public void setState(BallistaGolemState ballistaGolemState) { this.entityData.set(STATE, ballistaGolemState); }
    public BallistaGolemState getState() { return this.entityData.get(STATE); }
    public boolean isState(BallistaGolemEntity.BallistaGolemState ballistaGolemState) { return this.getState().equals(ballistaGolemState); }

    public void triggerWakingUpAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_WAKING_UP_ANIMATION_BYTE); }
    public void triggerIdleAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_IDLE_ANIMATION_BYTE); }
    public void triggerShootAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_SHOOT_ANIMATION_BYTE); }
    public void triggerReloadAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_RELOAD_ANIMATION_BYTE); }


    public enum BallistaGolemState {
        SLUMBERING,
        AWAKENING,
        IDLE,
        SHOOT,
        RELOAD;

        private BallistaGolemState() {}
    }
}
