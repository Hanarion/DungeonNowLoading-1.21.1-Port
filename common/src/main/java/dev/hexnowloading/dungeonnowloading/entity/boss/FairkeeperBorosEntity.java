package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.entity.ai.*;
import dev.hexnowloading.dungeonnowloading.entity.util.*;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
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

public class FairkeeperBorosEntity extends Monster implements Boss, Enemy, SlumberingEntity, FairkeeperSerpentEntity {

    //private static final EntityDataAccessor<FairkeeperState> STATE = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityStates.FAIRKEEPER_STATE);
    private static final EntityDataAccessor<FairkeeperBorosState> STATE = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityStates.FAIRKEEPER_BOROS_STATE);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> CALLER_UUID = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private TickBaseMoveSet<FairkeeperBorosState> stateSelector = new TickBaseMoveSet<>();
    private final Deque<Vec3> positionHistory = new LinkedList<>();

    private int attackTick;
    private float previousTilt = 0.0f;
    private Vec3 awakenEndPos;
    private boolean targetRandomPlayer;

    private final ServerBossEvent bossEvent;
    public static final int SEGMENT_COUNT = 14;
    public static int SEGMENT_DELAY_STEP = 7;

    public FairkeeperBorosEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(3.0f);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new FairkeeperBorosAwakenGoal(this));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.CIRCLING, this, 20.0, 1.5, true));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.SHOOT_POISON_ARROW, this, 10.0, 1.5, true));
        this.goalSelector.addGoal(3, new FairkeeperBorosShootPoisonArrowGoal(FairkeeperBorosState.SHOOT_POISON_ARROW, this));
        this.goalSelector.addGoal(3, new FairkeeperBorosPursuePlayerGoal(FairkeeperBorosState.FLAME_PURSUING, this, 1.3));
        this.goalSelector.addGoal(3, new FairkeeperBorosFlameThrowerGoal(FairkeeperBorosState.FLAME_PURSUING, this, 20));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.FLAME_CIRCLING, this, 5.0, 1.5, true));
        this.goalSelector.addGoal(3, new FairkeeperBorosFlameThrowerGoal(FairkeeperBorosState.FLAME_CIRCLING, this, 40));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.FLAME_PULSATING, this, 8.0, 1.6, true));
        this.goalSelector.addGoal(3, new FairkeeperBorosPulsatingFlameThrowerGoal(FairkeeperBorosState.FLAME_PULSATING, this, 40));
        this.goalSelector.addGoal(3, new FairkeeperBorosTackleGoal(FairkeeperBorosState.TACKLE, this, 1.3, 6.0F, 0.5F));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.SHOOT_POISON_POTION, this, 7.0, 1.5, true));
        this.goalSelector.addGoal(3, new FairkeeperBorosPoisonPotionGoal(FairkeeperBorosState.SHOOT_POISON_POTION, this));
        this.goalSelector.addGoal(3, new FairkeeperBorosPursuePlayerGoal(FairkeeperBorosState.FOLLOW, this, 1.1));
        this.goalSelector.addGoal(3, new FairkeeperBorosShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_LINE, this, 1.5, FairkeeperBorosShootArrowGoal.PATTERN_LINE));
        this.goalSelector.addGoal(3, new FairkeeperBorosShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_SLITHER, this, 1.5, FairkeeperBorosShootArrowGoal.PATTERN_SLITHER));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAndShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_SMALL_CIRCLE, this, 1.5f, FairkeeperBorosCircleAndShootArrowGoal.PATTERN_SMALL_CIRLCE));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAndShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_LARGE_CIRCLE, this, 1.5f, FairkeeperBorosCircleAndShootArrowGoal.PATTERN_LARGE_CIRCLE));
        this.goalSelector.addGoal(3, new FairkeeperBorosCircleAndShootArrowGoal(FairkeeperBorosState.SHOOT_ARROW_PLAYER_LARGE_CRICLE, this, 1.5f, FairkeeperBorosCircleAndShootArrowGoal.PATTERN_PLAYER_LARGE_CIRCLE));
        this.goalSelector.addGoal(3, new FairkeeperBorosChaseAndShootArrowGoal(FairkeeperBorosState.CHASE_AND_SHOOT_ARROW, this, 1.3f, 3.0F, FairkeeperBorosChaseAndShootArrowGoal.PATTERN_TRIPLE));
        this.goalSelector.addGoal(4, new FairkeeperBorosCircleAroundPlayerGoal(FairkeeperBorosState.IDLE, this, 20.0, 1.5, true));
        //this.goalSelector.addGoal(3, new FairkeeperBorosShootPoisonArrowGoal());
        //this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        //this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        //this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new BossTargetSelectorGoal(this, this.getFollowDistance()));
    }

    private void setMoveSets() {
        stateSelector.addMove(FairkeeperBorosState.FOLLOW, 4, 200, 0);
        stateSelector.addMove(FairkeeperBorosState.TACKLE, 4, 200, 0);
        stateSelector.addMove(FairkeeperBorosState.FLAME_CIRCLING, 4, 200, 0);
        stateSelector.addMove(FairkeeperBorosState.FLAME_PURSUING, 4, 200, 0);
        stateSelector.addMove(FairkeeperBorosState.SHOOT_POISON_ARROW, 4, 200, 0);
        stateSelector.addMove(FairkeeperBorosState.SHOOT_POISON_POTION, 4, 60, 0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHILD_UUID, Optional.empty());
        this.entityData.define(CALLER_UUID, Optional.empty());
        this.entityData.define(STATE, FairkeeperBorosState.IDLE);
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
        return true;
    }

    private void vertexTransmissionEffectImmunity() {
        this.removeEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
    }

    private void blockDestructionTick() {
        int DESTRUCTION_RANGE = 2;
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

        if (this.getTarget() == null) return;

        this.targetRandomPlayer();
        if (this.getTarget() != null) {
            this.getTarget().sendSystemMessage(Component.literal("Boros : Stopped"));
        }
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
        return super.hurt(damageSource, amount);
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
    public void targetRandomPlayer() {
        this.targetRandomPlayer = true;
    }

    @Override
    public boolean playerTargetingCondition() {
        return this.targetRandomPlayer || this.getTarget() != null;
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


    @Override
    public boolean isStationary() {
        return false;
    }

    @Override
    public boolean isSlumbering() {
        return false;
    }

    public enum FairkeeperBorosState {
        AWAKENING,
        IDLE,
        CIRCLING,
        SHOOT_POISON_ARROW,
        FLAME_PURSUING,
        FLAME_CIRCLING,
        FLAME_PULSATING,
        SHOOT_ARROW_LINE,
        SHOOT_ARROW_SLITHER,
        SHOOT_ARROW_SMALL_CIRCLE,
        SHOOT_ARROW_LARGE_CIRCLE,
        SHOOT_ARROW_PLAYER_LARGE_CRICLE,
        CHASE_AND_SHOOT_ARROW,
        TACKLE,
        FOLLOW,
        SHOOT_POISON_POTION,
        DYING;

        private FairkeeperBorosState() {}
    }
}
