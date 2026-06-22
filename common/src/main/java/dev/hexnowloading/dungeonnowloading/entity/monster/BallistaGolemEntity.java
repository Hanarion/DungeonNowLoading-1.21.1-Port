package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.BallistaGolemArrowAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.BallistaGolemMeleeAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.BallistaGolemReloadGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.SlumberingEntityPlayerTargetGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.BallistaGolemMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.pathfinding.BallistaGolemPathNavigation;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStartTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStopTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

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
        this.setMaxUpStep(1.0F);
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.moveControl = new BallistaGolemMoveControl(this);
        this.navigation = new BallistaGolemPathNavigation(this, this.level());
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0F)
                .add(Attributes.ATTACK_DAMAGE, 19.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 32.0F)
                .add(Attributes.ARMOR, 15.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BallistaGolemReloadGoal(this));
        this.goalSelector.addGoal(2, new BallistaGolemArrowAttackGoal(this));
        this.goalSelector.addGoal(3, new BallistaGolemMeleeAttackGoal(this, 1.0, true, 1.1F));
        //this.goalSelector.addGoal(6, new SlumberingEntityRandomStrollGoal(this, 0.5));
        //this.goalSelector.addGoal(7, new SlumberingEntityLookAtPlayerGoal(this, Player.class, 6.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SlumberingEntityPlayerTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, BallistaGolemState.SLUMBERING);
        builder.define(ARROW_COUNT, 6);
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
            if (aiTick == 153 - 6) {
                this.playBallsitaGolemSound(DNLSounds.BALLISTA_GOLEM_WAKING.get());
            }
            if (aiTick > 0) {
                aiTick--;
            } else {
                this.setState(BallistaGolemState.IDLE);
                this.triggerIdleAnimation();
            }
        }

        if (this.horizontalCollision && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            boolean brokeLeaves = false;
            AABB box = this.getBoundingBox().inflate(0.2);

            for (BlockPos pos : BlockPos.betweenClosed(
                    Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                    Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {

                BlockState state = this.level().getBlockState(pos);
                if (state.getBlock() instanceof LeavesBlock) {
                    boolean destroyed = this.level().destroyBlock(pos, true, this);
                    if (destroyed) {
                        brokeLeaves = true;
                    }
                }
            }

            if (!brokeLeaves && this.onGround()) {
                this.jumpFromGround();
            }
        }
        super.customServerAiStep();
    }

    @Override
    public double getMeleeAttackRangeSqr(LivingEntity livingEntity) {
        return (double) (this.getBbWidth() * this.getBbWidth());
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(2.0F);
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

    public void playBallsitaGolemSound(SoundEvent soundEvent) {
        float radius = 32.0f;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        for (ServerPlayer player : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), soundEvent.getLocation(), SoundSource.HOSTILE), player);
        }
    }

    public void stopBallsitaGolemSounds() {
        float radius = 32.0f;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        List<ResourceLocation> soundsToStop = new ArrayList<>(List.of());
        soundsToStop.add(DNLSounds.BALLISTA_GOLEM_WAKING.get().getLocation());
        soundsToStop.add(DNLSounds.BALLISTA_GOLEM_RELOAD.get().getLocation());
        for (ServerPlayer player : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStop) {
                Services.NETWORK.sendToPlayer(new S2CStopTickingSoundPacket(this.getId(), sound, 20, true), player);
            }
        }
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.85F;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(DNLSounds.BALLISTA_GOLEM_STEP.get(), 1.5F, 1.0F);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.BALLISTA_GOLEM_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return DNLSounds.BALLISTA_GOLEM_HURT.get();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.getDirectEntity() instanceof AbstractArrow) {
            return false;
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide) {
            this.stopBallsitaGolemSounds();
        }
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
