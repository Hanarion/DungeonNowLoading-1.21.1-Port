package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.entity.ai.*;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.*;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FairkeeperBorosEntity extends Monster implements Boss, Enemy, SlumberingEntity, FairkeeperSerpentEntity, FairkeeperSerpentHeadEntity {

    //private static final EntityDataAccessor<FairkeeperState> STATE = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityStates.FAIRKEEPER_STATE);
    private static final EntityDataAccessor<FairkeeperBorosState> STATE = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityStates.FAIRKEEPER_BOROS_STATE);
    private static final EntityDataAccessor<FairkeeperBorosAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityStates.FAIRKEEPER_BOROS_ANIMATION_STATE);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> CALLER_UUID = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> HAS_ARMOR = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState pursueOpenMouthAnimationState = new AnimationState();
    public final AnimationState pursueOpenedMouthAnimationState = new AnimationState();
    public final AnimationState pursueCloseMouthAnimationState = new AnimationState();

    /*private static final byte TRIGGER_IDLE_ANIMATION_BYTE = 70;
    private static final byte TRIGGER_PURSUE_OPEN_MOUTH_ANIMATION_BYTE = 71;
    private static final byte TRIGGER_PURSUE_OPENED_MOUTH_ANIMATION_BYTE = 72;
    private static final byte TRIGGER_PURSUE_CLOSE_MOUTH_ANIMATION_BYTE = 73;*/


    private TickBaseMoveSet<FairkeeperBorosState> stateSelector = new TickBaseMoveSet<>();
    private final Deque<Vec3> positionHistory = new LinkedList<>();
    private Set<UUID> partList;

    private int attackTick;
    private float previousTilt = 0.0f;
    private Vec3 awakenEndPos;
    private boolean targetRandomPlayer;
    private float armorHealth;
    private boolean damageFromOtherSegment;


    private final ServerBossEvent bossEvent;
    public static final int SEGMENT_COUNT = 14;
    public static int SEGMENT_DELAY_STEP = 11;
    private static final float MAX_ARMOR_HEALTH = 150F;

    private int mouthOpenAnimationTimeOut;
    private static final int MOUTH_OPEN_ANIMATION_DURATION = 19;


    public FairkeeperBorosEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(0.0f);
        this.setPersistenceRequired();
        this.setArmor(true);
        this.setArmorHealth(150f);
        this.xpReward = 0;
        this.bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new FairkeeperBorosAwakenGoal(this));
        this.goalSelector.addGoal(3, new FairkeeperBorosPursuePlayerGoal(FairkeeperBorosState.FLAME_PURSUING, this, 1.3));
        this.goalSelector.addGoal(3, new FairkeeperBorosFlameThrowerGoal(FairkeeperBorosState.FLAME_PURSUING, this, 20, true));
        this.goalSelector.addGoal(3, new FairkeeperBorosTackleGoal(FairkeeperBorosState.TACKLE, this, 1.3, 6.0F, 0.5F));
        this.goalSelector.addGoal(3, new FairkeeperBorosShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_LINE, this, 1.5, FairkeeperBorosShootArrowGoal.PATTERN_LINE));
        this.goalSelector.addGoal(3, new FairkeeperBorosShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_SLITHER, this, 1.5, FairkeeperBorosShootArrowGoal.PATTERN_SLITHER));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAndShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_SMALL_CIRCLE, this, 1.5f, FairkeeperBorosCircleAndShootArrowGoal.PATTERN_SMALL_CIRLCE));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAndShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_LARGE_CIRCLE, this, 1.5f, FairkeeperBorosCircleAndShootArrowGoal.PATTERN_LARGE_CIRCLE));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAndShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_PLAYER_LARGE_CRICLE, this, 1.5f, FairkeeperBorosCircleAndShootArrowGoal.PATTERN_PLAYER_LARGE_CIRCLE));
        this.goalSelector.addGoal(3, new FairkeeperBorosPursueAndShootArrowGoal(FairkeeperBorosState.PURSUE_AND_SHOOT_TRIPLE_ARROW, this, 1.3f, 3.0F, 60, FairkeeperBorosPursueAndShootArrowGoal.PATTERN_TRIPLE));
        this.goalSelector.addGoal(3, new FairkeeperBorosPursueAndShootArrowGoal(FairkeeperBorosState.PURSUE_AND_SHOOT_SINGLE_ARROW, this, 1.3f, 3.0F, 60, FairkeeperBorosPursueAndShootArrowGoal.PATTERN_SINGLE));
        this.goalSelector.addGoal(3, new FairkeeperBorosShootArrowAboveGoal(FairkeeperBorosState.SHOOT_ARROW_ABOVE, this, 1.5f, FairkeeperBorosShootArrowAboveGoal.PATTERN_PLAYER_LARGE_CIRCLE));
        this.goalSelector.addGoal(3, new FairkeeperBorosFlameThrowerGoal(FairkeeperBorosState.DESPERATE, this, 20, false));
        this.goalSelector.addGoal(3, new FairkeeperBorosPursueAndShootArrowGoal(FairkeeperBorosState.DESPERATE, this, 1.5F, 2.0F, 30, FairkeeperBorosPursueAndShootArrowGoal.PATTERN_DESPERATE));
        this.goalSelector.addGoal(3, new FairkeeperBorosEatVertexProjectilesGoal(FairkeeperBorosState.EAT_VERTEX_PROJECTILES, this, 1.5f));
        this.goalSelector.addGoal(4, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.IDLE, this, 20.0, 1.5, true, true));
        this.goalSelector.addGoal(5, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.IDLE, this, 20.0, 1.5, true, false));
        this.targetSelector.addGoal(2, new BossTargetSelectorGoal(this, this.getFollowDistance()));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHILD_UUID, Optional.empty());
        this.entityData.define(CALLER_UUID, Optional.empty());
        this.entityData.define(STATE, FairkeeperBorosState.IDLE);
        this.entityData.define(ANIMATION_STATE, FairkeeperBorosAnimationState.IDLE);
        this.entityData.define(HAS_ARMOR, true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.getChildId() != null) {
            compoundTag.putUUID("ChildUUID", this.getChildId());
        }
        if (this.getCallerId() != null) {
            compoundTag.putUUID("CallerUUID", this.getCallerId());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (this.hasCustomName()) this.bossEvent.setName(this.getDisplayName());
        if (compoundTag.hasUUID("ChildUUID")) {
            this.setChildId(compoundTag.getUUID("ChildUUID"));
        }
        if (compoundTag.hasUUID("CallerUUID")) {
            this.setCallerId(compoundTag.getUUID("CallerUUID"));
        }
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossEvent.addPlayer(serverPlayer);
        if (this.isSlumbering()) this.disableBossBar();
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossEvent.removePlayer(serverPlayer);
    }

    /*@Override
    public LookControl getLookControl() {
        return new FairkeeperLookControl(this, 0.1F, 0.05F);
    }*/

    @Override
    public void tick() {
        super.tick();
        this.segmentControl();
        this.animationControl();
    }

    private void animationControl() {
        if (!this.level().isClientSide) {
            return;
        }

        if (this.mouthOpenAnimationTimeOut-- > 0) {
            if (this.mouthOpenAnimationTimeOut <= 0) {
                this.transitionTo(FairkeeperBorosAnimationState.MOUTH_OPENED);
            }
        }

        if (this.pursueOpenMouthAnimationState.isStarted() && this.mouthOpenAnimationTimeOut <= 0) {
            this.mouthOpenAnimationTimeOut = MOUTH_OPEN_ANIMATION_DURATION;
        }

    }

    private void segmentControl() {
        if (!this.level().isClientSide) {
            Entity child = getChild();
            if (child == null) {
                LivingEntity partParent = this;
                int segments = SEGMENT_COUNT;
                for (int i = 0; i < segments; i++) {
                    FairkeeperBorosPartEntity part = new FairkeeperBorosPartEntity(DNLEntityTypes.FAIRKEEPER_BOROS_PART.get(), partParent, this, i);
                    if (partParent == this) {
                        this.setChildId(part.getUUID());
                    } else if (partParent instanceof FairkeeperBorosPartEntity bodyPartParent && !bodyPartParent.isTail()) {
                        bodyPartParent.setChild(part);
                    }
                    //double offsetX = partParent.getX() - (i + 1) * 1.0; // Offset by 3 blocks for each segment
                    part.setPos(partParent.getX(), partParent.getY(), partParent.getZ());
                    part.setYRot(partParent.getYRot());
                    part.yBodyRot = partParent.getYRot();
                    part.yHeadRot = partParent.getYRot();
                    part.setRotatable(false);
                    partParent = part;
                    if (i == segments - 1) {
                        part.setTail(true);
                    }
                    this.level().addFreshEntity(part);
                }
            }

            if (this.getDeltaMovement().lengthSqr() > 0.01) {
                synchronized (positionHistory) {
                    positionHistory.addFirst(new Vec3(this.getX(), this.getY(), this.getZ()));

                    int maxHistorySize = (SEGMENT_COUNT + 1) * SEGMENT_DELAY_STEP;
                    if (positionHistory.size() > maxHistorySize) {
                        positionHistory.pollLast();
                    }
                }
            }

            if (!this.onGround() && this.getDeltaMovement().y < 0.0) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.8, 1.0));
            }

            if (!this.isState(FairkeeperBorosState.AWAKENING)) {
                this.lookTowardTarget();
            }
        }
    }

    private void lookTowardTarget() {
        double directionX = this.getMoveControl().getWantedX() - this.getX();
        double directionZ = this.getMoveControl().getWantedZ() - this.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0;

        this.setYRot((float) yaw);
        this.yBodyRot = (float) yaw;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isState(FairkeeperBorosState.AWAKENING)) this.enableBossBar();
        this.performContactDamage();
        this.abilityCooldown();
        this.blockDestructionTick();
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void performContactDamage() {
        this.level().getEntities(this, this.getBoundingBox(), this::canPerformContactDamageTo)
                .forEach(entity -> {
                    entity.push(this);
                    entity.hurt(entity.level().damageSources().mobAttack(this), this.getContactDamage());
                });
    }

    private float getContactDamage() {
        return (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * (this.isState(FairkeeperBorosState.TACKLE) ? 1.0F : 0.5F));
    }

    private boolean canPerformContactDamageTo(Entity entity) {
        if (entity instanceof FairkeeperBorosPartEntity part) {
            return !this.getUUID().equals(part.getHeadId());
        }

        return !(entity instanceof VertexOrbProjectileEntity) && !(entity instanceof VertexDomainProjectileEntity);
    }

    private void vertexTransmissionEffectImmunity() {
        this.removeEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
    }

    private void blockDestructionTick() {
        int DESTRUCTION_RANGE = 4;
        int y = 0;
        if (this.getMoveControl().hasWanted()) {
            y = Mth.floor(this.getMoveControl().getWantedY()) - this.getBlockY();
        }
        if (y < -1) {
            this.destroyContactBlocks(-DESTRUCTION_RANGE, DESTRUCTION_RANGE, -1, 3, -DESTRUCTION_RANGE, DESTRUCTION_RANGE);
            return;
        }
        if (this.getDeltaMovement().lengthSqr() > 0.01) {
            return;
        }
        if (y > 1) {
            this.setPos(this.getX(), this.getY() + 1, this.getZ());
            this.destroyContactBlocks(-DESTRUCTION_RANGE, DESTRUCTION_RANGE, 0, 4, -DESTRUCTION_RANGE, DESTRUCTION_RANGE);
            return;
        }
        this.destroyContactBlocks(-DESTRUCTION_RANGE, DESTRUCTION_RANGE, 0, 3, -DESTRUCTION_RANGE, DESTRUCTION_RANGE);
    }

    private void destroyContactBlocks(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int ix = minX; ix <= maxX; ix++) {
            for (int iz = minZ; iz <= maxZ; iz++) {
                for (int iy = minY; iy <= maxY; iy++) {
                    int dx = this.getBlockX() + ix;
                    int dy = this.getBlockY() + iy;
                    int dz = this.getBlockZ() + iz;
                    BlockPos blockPos = new BlockPos(dx, dy, dz);
                    BlockState blockState = this.level().getBlockState(blockPos);
                    if (!blockState.isAir()) {
                        if (!blockState.is(BlockTags.WITHER_IMMUNE)) {
                            this.level().destroyBlock(blockPos, true, this);
                        }
                    }
                }
            }
        }
    }

    private void abilityCooldown() {

        if (!this.isState(FairkeeperBorosState.IDLE)) {
            return;
        }

        if (this.attackTick > 0) {
            --this.attackTick;
            return;
        }

        this.targetRandomPlayer();

        ((FairkeeperSerpentCallerEntity) this.getCaller()).setBorosWaitingForCommand(true);
    }

    public void stopAttacking(int cooldown) {
        this.setState(FairkeeperBorosState.IDLE);
        this.setAttackTick(cooldown);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.getEntity() instanceof FairkeeperSerpentEntity) {
            if (damageSource.getDirectEntity() instanceof AbstractArrow arrow) {
                arrow.remove(RemovalReason.DISCARDED);
            }
            return false;
        }

        if (this.isDamageFromOtherSegment()) {
            this.setDamageFromOtherSegment(false);
            return super.hurt(damageSource, amount);
        }

        if (!this.hasArmor() || damageSource.isCreativePlayer()) {
            return super.hurt(damageSource, amount);
        }

        if (damageSource.is(DamageTypes.EXPLOSION) || (damageSource.getDirectEntity() instanceof LivingEntity livingEntity && livingEntity.canDisableShield() && amount > 6)) {
            boolean penetratesArmor = this.getArmorHealth() - amount <= 0;
            float nonKillableDamage = penetratesArmor ? 0 : amount;
            if (penetratesArmor) {
                this.setArmor(false);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
                return super.hurt(damageSource, 0);
            } else {
                this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);
                this.setArmorHealth(this.getArmorHealth() - nonKillableDamage);
                return super.hurt(damageSource, 0);
            }
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 1.0F);
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.getCaller();
        if (caller != null) {
            caller.setLastDamageSource(damageSource);
        }
        super.die(damageSource);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        MobEffect effect = mobEffectInstance.getEffect();
        if (effect == MobEffects.POISON || effect == DNLMobEffects.VERTEX_TRANSMISSION.get()) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            FairkeeperBorosAnimationState animationState = this.getAnimationState();
            this.resetAnimations();
            switch (animationState) {
                case IDLE -> this.idleAnimationState.startIfStopped(this.tickCount);
                case MOUTH_OPEN -> this.pursueOpenMouthAnimationState.startIfStopped(this.tickCount);
                case MOUTH_OPENED -> this.pursueOpenedMouthAnimationState.startIfStopped(this.tickCount);
                case MOUTH_CLOSE -> this.pursueCloseMouthAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetAnimations() {
        this.idleAnimationState.stop();
        this.pursueOpenMouthAnimationState.stop();
        this.pursueOpenedMouthAnimationState.stop();
        this.pursueCloseMouthAnimationState.stop();
    }

    public FairkeeperBorosEntity transitionTo(FairkeeperBorosAnimationState state) {
        switch (state) {
            case IDLE:
                this.setAnimationState(FairkeeperBorosAnimationState.IDLE);
                break;
            case MOUTH_OPEN:
                this.setAnimationState(FairkeeperBorosAnimationState.MOUTH_OPEN);
                break;
            case MOUTH_OPENED:
                this.setAnimationState(FairkeeperBorosAnimationState.MOUTH_OPENED);
                break;
            case MOUTH_CLOSE:
                this.setAnimationState(FairkeeperBorosAnimationState.MOUTH_CLOSE);
                break;
        }

        return this;
    }


    @Override
    public void targetRandomPlayer() {
        this.targetRandomPlayer = true;
    }

    @Override
    public boolean playerTargetingCondition() {
        return this.targetRandomPlayer;
    }

    @Override
    public void postPlayerTargeting() {
        this.targetRandomPlayer = false;
    }

    @Override
    public BlockPos resetRegionCenter() {
        return this.getCaller().blockPosition();
    }

    @Override
    public boolean resetCondition() {
        return false;
    }

    @Override
    public void resetBoss() {
        this.remove(RemovalReason.DISCARDED);
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
    public boolean isInWall() {
        return false;
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
    }

    @Override
    protected int calculateFallDamage(float $$0, float $$1) {
        return 0;
    }

    @Override
    public boolean causeFallDamage(float v, float v1, DamageSource damageSource) {
        return false;
    }

    public void playFlameShootingSound(double x, double y, double z) {
        this.level().playSound(null, x, y, z, DNLSounds.SCUTTLE_SHOOTING_FLAME.get(), this.getSoundSource(), 3.0F, 1.0F);
    }

    public void playBeamSound(double x, double y, double z) {
        this.level().playSound(null, x, y, z, DNLSounds.FAIRKEEPER_BOROS_BEAM.get(), this.getSoundSource(), 3.0F, 2.0F);
    }

    public void playArrowSound(double x, double y, double z) {
        this.level().playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_BLAST_FAR, this.getSoundSource(), 3.0F, 0.1F);
    }

    public void playHealSound(double x, double y, double z) {
        this.level().playSound(null, x, y, z, DNLSounds.FAIRKEEPER_BOROS_HEAL.get(), this.getSoundSource(), 3.0F, 1.0F);
    }
    @Override
    protected SoundEvent getDeathSound() {
        return super.getDeathSound();
    }

    public Entity getChild() {
        UUID id = getChildId();
        if (id != null && !this.level().isClientSide) {
            return ((ServerLevel) this.level()).getEntity(id);
        }
        return null;
    }

    public Entity getCaller() {
        UUID id = getCallerId();
        if (id != null && !this.level().isClientSide) {
            return ((ServerLevel) this.level()).getEntity(id);
        }
        return null;
    }

    public void enableBossBar() { this.bossEvent.setVisible(true); }
    public void disableBossBar() { this.bossEvent.setVisible(false); }
    public int getAttackTick() { return this.attackTick; }
    public void setAttackTick(int i) { this.attackTick = i; }
    public double getAttackDamage() { return this.getAttributeValue(Attributes.ATTACK_DAMAGE); }
    public double getFollowDistance() { return this.getAttributeValue(Attributes.FOLLOW_RANGE); }
    public void setState(FairkeeperBorosState fairkeeperState) { this.entityData.set(STATE, fairkeeperState); }
    public FairkeeperBorosState getState() { return this.entityData.get(STATE); }
    public void setAnimationState(FairkeeperBorosAnimationState animationState) { this.entityData.set(ANIMATION_STATE, animationState); }
    public FairkeeperBorosAnimationState getAnimationState() { return this.entityData.get(ANIMATION_STATE); }
    public boolean isState(FairkeeperBorosState fairkeeperState) { return this.getState().equals(fairkeeperState); }
    public UUID getChildId() { return this.entityData.get(CHILD_UUID).orElse(null); }
    public void setChildId(@Nullable UUID uniqueId) { this.entityData.set(CHILD_UUID, Optional.ofNullable(uniqueId)); }
    public UUID getCallerId() { return this.entityData.get(CALLER_UUID).orElse(null); }
    public void setCallerId(@Nullable UUID uniqueId) { this.entityData.set(CALLER_UUID, Optional.ofNullable(uniqueId)); }
    public Queue<Vec3> getPositionHistory() { return this.positionHistory; }
    public float getPreviousTilt() { return this.previousTilt; }
    public void setPreviousTilt(float tilt) { this.previousTilt = tilt; }
    public Vec3 getAwakenEndPos() { return this.awakenEndPos; }
    public void setAwakenEndPos(Vec3 blockPos) { this.awakenEndPos = blockPos; }
    public boolean hasArmor() { return this.entityData.get(HAS_ARMOR); }
    public void setArmor(boolean b) { this.entityData.set(HAS_ARMOR, b); }
    public float getArmorHealth() { return armorHealth; }
    public void setArmorHealth(float f) { this.armorHealth = f; }
    public void setDamageFromOtherSegment(boolean b) { this.damageFromOtherSegment = b; }
    public boolean isDamageFromOtherSegment() { return this.damageFromOtherSegment; }

    @Override
    public boolean isStationary() {
        return false;
    }

    @Override
    public boolean isSlumbering() {
        return false;
    }

    public enum FairkeeperBorosAnimationState {
        IDLE,
        MOUTH_OPEN,
        MOUTH_OPENED,
        MOUTH_CLOSE
    }

    public enum FairkeeperBorosState {
        AWAKENING,
        IDLE,
        TACKLE,
        PURSUE_RANDOM,
        FLAME_PURSUING,
        SHOOT_ARROW_RANDOM,
        SHOOT_ARROW_LINE,
        SHOOT_ARROW_SLITHER,
        SHOOT_ARROW_SMALL_CIRCLE,
        SHOOT_ARROW_LARGE_CIRCLE,
        SHOOT_ARROW_PLAYER_LARGE_CRICLE,
        PURSUE_AND_SHOOT_TRIPLE_ARROW,
        PURSUE_AND_SHOOT_SINGLE_ARROW,
        SHOOT_ARROW_ABOVE,
        EAT_VERTEX_PROJECTILES,
        DESPERATE,
        DYING;

        private FairkeeperBorosState() {}
    }
}
