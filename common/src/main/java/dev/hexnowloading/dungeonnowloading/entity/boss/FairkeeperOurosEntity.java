package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.entity.ai.*;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.FairkeeperOurosMoveControl;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FairkeeperOurosEntity extends Monster implements Boss, Enemy, SlumberingEntity, FairkeeperSerpentEntity {

    private static final EntityDataAccessor<FairkeeperOurosState> STATE = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityStates.FAIRKEEPER_OUROS_STATE);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> CALLER_UUID = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_ON_CEILING = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityDataSerializers.BOOLEAN);

    private TickBaseMoveSet<FairkeeperOurosState> stateSelector = new TickBaseMoveSet<>();
    private final Deque<Vec3> positionHistory = new LinkedList<>();

    private int attackTick;
    private float previousTilt = 0.0f;
    private Vec3 awakenEndPos;
    private boolean targetRandomPlayer;

    private final ServerBossEvent bossEvent;
    public static final int SEGMENT_COUNT = 14;
    public static int SEGMENT_DELAY_STEP = 7;
    
    public FairkeeperOurosEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        //this.moveControl = new FairkeeperSerpentMoveControl(this, 5.0F);
        this.moveControl = new FairkeeperOurosMoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.setMaxUpStep(0.0f);
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
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new FairkeeperOurosAwakenGoal(this));
        this.goalSelector.addGoal(3, new FairkeeperOurosCircleAroundGoal(FairkeeperOurosState.CIRCLING, this, 20.0, 1.3, false));
        this.goalSelector.addGoal(3, new FairkeeperOurosCircleAroundGoal(FairkeeperOurosState.SHOOT_VERTEX_ARROW_DIRECT, this, 20.0, 1.3, false));
        this.goalSelector.addGoal(3, new FairkeeperOurosShootVertexArrowGoal(FairkeeperOurosState.SHOOT_VERTEX_ARROW_DIRECT, this));
        this.goalSelector.addGoal(3, new FairkeeperOurosCircleAroundGoal(FairkeeperOurosState.SUMMON_SCUTTLE, this, 20.0, 1.3, false));
        this.goalSelector.addGoal(3, new FairkeeperOurosDropScuttleGoal(FairkeeperOurosState.SUMMON_SCUTTLE, this));
        this.goalSelector.addGoal(3, new FairkeeperOurosVertexPillarDropGoal(FairkeeperOurosState.DROP_PILLAR_SMALL_SQUARE, this, 1.3, FairkeeperOurosVertexPillarDropGoal.PATTERN_SMALL_SQUARE));
        this.goalSelector.addGoal(3, new FairkeeperOurosVertexPillarDropGoal(FairkeeperOurosState.DROP_PILLAR_SINGLE_LINE, this, 1.3, FairkeeperOurosVertexPillarDropGoal.PATTERN_SINGLE_LINE));
        this.goalSelector.addGoal(3, new FairkeeperOurosVertexPillarDropGoal(FairkeeperOurosState.DROP_PILLAR_CROSS, this, 1.3, FairkeeperOurosVertexPillarDropGoal.PATTERN_CROSS));
        this.goalSelector.addGoal(3, new FairkeeperOurosVertexPillarDropGoal(FairkeeperOurosState.DROP_PILLAR_LARGE_SQUARE, this, 1.3, FairkeeperOurosVertexPillarDropGoal.PATTERN_LARGE_SQUARE));
        this.goalSelector.addGoal(3, new FairkeeperOurosVertexPillarDropGoal(FairkeeperOurosState.DROP_PILLAR_DOUBLE_LINE, this, 1.3, FairkeeperOurosVertexPillarDropGoal.PATTERN_DOUBLE_LINE));
        this.goalSelector.addGoal(4, new FairkeeperOurosCircleAroundGoal(FairkeeperOurosState.IDLE, this, 20.0F, 1.3, false));
        //this.goalSelector.addGoal(3, new FairkeeperCircleAroundPlayerGoal(this, 20.0, 1.0, true)); // Clockwise
        //this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        //this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        //this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new BossTargetSelectorGoal(this, this.getFollowDistance()));
    }

    private void setMoveSets() {
        stateSelector.addMove(FairkeeperOurosState.SHOOT_VERTEX_ARROW_DIRECT, 4, 200, 0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHILD_UUID, Optional.empty());
        this.entityData.define(CALLER_UUID, Optional.empty());
        this.entityData.define(IS_ON_CEILING, false);
        this.entityData.define(STATE, FairkeeperOurosState.IDLE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Slumbering", isSlumbering());
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
                    FairkeeperOurosPartEntity part = new FairkeeperOurosPartEntity(DNLEntityTypes.FAIRKEEPER_OUROS_PART.get(), partParent, this, i);
                    if (partParent == this) {
                        this.setChildId(part.getUUID());
                    } else if (partParent instanceof FairkeeperOurosPartEntity bodyPartParent && !bodyPartParent.isTail()) {
                        if (!bodyPartParent.isTail()) {
                            bodyPartParent.setChildId(part.getUUID());
                        } else {
                            bodyPartParent.setChildId(null);
                        }
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

            if (!this.onCieling() && this.getDeltaMovement().y > 0.0) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.8, 1.0));
            }

            if (!this.isState(FairkeeperOurosState.AWAKENING)) {
                this.lookTowardTarget();
            }
        }

        /*FairkeeperLookControl lookControl = (FairkeeperLookControl) this.getLookControl();

        // Always update pitch based on motion
        lookControl.tick();

        // Update yaw only if there's a target
        if (this.getTarget() != null) {
            lookControl.setLookAt(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
        }*/
    }

    private void lookTowardTarget() {
        double directionX = this.getMoveControl().getWantedX() - this.getX();
        double directionZ = this.getMoveControl().getWantedZ() - this.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0;

        this.setYRot((float) yaw);
        this.yBodyRot = (float) yaw;
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (this.isControlledByLocalInstance()) {
            double upwardGravity = -0.08; // Negative value for upward gravity
            boolean isRising = this.getDeltaMovement().y >= 0.0; // Checks if the entity is rising

            if (isRising && this.hasEffect(MobEffects.SLOW_FALLING)) {
                upwardGravity = -0.01; // Slower upward movement for slow falling effect
            }

            if (this.isFallFlying()) {
                this.checkSlowFallDistance();
                Vec3 currentVelocity = this.getDeltaMovement();
                Vec3 lookDirection = this.getLookAngle();
                float pitchRadians = this.getXRot() * (float) (Math.PI / 180.0);
                double horizontalLookDistance = Math.sqrt(lookDirection.x * lookDirection.x + lookDirection.z * lookDirection.z);
                double horizontalVelocity = currentVelocity.horizontalDistance();
                double lookMagnitude = lookDirection.length();
                double adjustedCosPitch = Math.cos(pitchRadians);
                adjustedCosPitch = adjustedCosPitch * adjustedCosPitch * Math.min(1.0, lookMagnitude / 0.4);

                currentVelocity = this.getDeltaMovement().add(0.0, upwardGravity * (-1.0 + adjustedCosPitch * 0.75), 0.0);

                if (currentVelocity.y > 0.0 && horizontalLookDistance > 0.0) { // Adjust for rising motion
                    double risingAdjustment = currentVelocity.y * 0.1 * adjustedCosPitch;
                    currentVelocity = currentVelocity.add(
                            lookDirection.x * risingAdjustment / horizontalLookDistance,
                            risingAdjustment,
                            lookDirection.z * risingAdjustment / horizontalLookDistance
                    );
                }

                if (pitchRadians > 0.0F && horizontalLookDistance > 0.0) { // Adjust for upward pitch
                    double upwardAdjustment = horizontalVelocity * (double) (Mth.sin(pitchRadians)) * 0.04;
                    currentVelocity = currentVelocity.add(
                            -lookDirection.x * upwardAdjustment / horizontalLookDistance,
                            upwardAdjustment * 3.2,
                            -lookDirection.z * upwardAdjustment / horizontalLookDistance
                    );
                }

                if (horizontalLookDistance > 0.0) {
                    currentVelocity = currentVelocity.add(
                            (lookDirection.x / horizontalLookDistance * horizontalVelocity - currentVelocity.x) * 0.1,
                            0.0,
                            (lookDirection.z / horizontalLookDistance * horizontalVelocity - currentVelocity.z) * 0.1
                    );
                }

                this.setDeltaMovement(currentVelocity.multiply(0.99F, 0.98F, 0.99F));
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.horizontalCollision && !this.level().isClientSide) {
                    double collisionHorizontalVelocity = this.getDeltaMovement().horizontalDistance();
                    double collisionVelocityDifference = horizontalVelocity - collisionHorizontalVelocity;
                    float collisionDamage = (float) (collisionVelocityDifference * 10.0 - 3.0);

                    if (collisionDamage > 0.0F) {
                        this.playSound(this.getFallDamageSound((int) collisionDamage), 1.0F, 1.0F);
                        this.hurt(this.damageSources().flyIntoWall(), collisionDamage);
                    }
                }

                if (this.onCieling() && !this.level().isClientSide) {
                    this.setSharedFlag(7, false);
                }
            } else {

                BlockPos blockBelow = this.getBlockPosBelowThatAffectsMyMovement();
                float blockFriction = this.level().getBlockState(blockBelow).getBlock().getFriction();
                float groundFriction = this.onCieling() ? blockFriction * 0.91F : 0.91F;

                this.moveRelative(this.getFrictionInfluencedSpeed(blockFriction), movementInput);
                this.setDeltaMovement(this.getDeltaMovement());
                this.move(MoverType.SELF, this.getDeltaMovement());
                //Vec3 adjustedMovement = this.handleRelativeFrictionAndCalculateMovement(movementInput, blockFriction);
                Vec3 adjustedMovement = this.getDeltaMovement();
                if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.getFeetBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
                    adjustedMovement = new Vec3(adjustedMovement.x, -0.2, adjustedMovement.z);
                }
                double adjustedYVelocity = adjustedMovement.y;

                if (this.hasEffect(MobEffects.LEVITATION)) {
                    adjustedYVelocity += (0.05 * (double) (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - adjustedMovement.y) * 0.2;
                } else if (this.level().isClientSide && !this.level().hasChunkAt(blockBelow)) {
                    if (this.getY() > (double) this.level().getMinBuildHeight()) {
                        adjustedYVelocity = -0.1; // Upward motion when outside chunk area
                    } else {
                        adjustedYVelocity = 0.0;
                    }
                } else if (!this.isNoGravity()) {
                    adjustedYVelocity -= upwardGravity; // Apply upward gravity
                }

                if (this.shouldDiscardFriction()) {
                    this.setDeltaMovement(adjustedMovement.x, adjustedYVelocity, adjustedMovement.z);
                } else {
                    this.setDeltaMovement(
                            adjustedMovement.x * (double) groundFriction,
                            adjustedYVelocity * 0.98F,
                            adjustedMovement.z * (double) groundFriction
                    );
                }
            }
        }
    }

    private float getFrictionInfluencedSpeed(float $$0) {
        return this.onCieling() ? this.getSpeed() * (0.21600002F / ($$0 * $$0 * $$0)) : this.getFlyingSpeed();
    }

    @Override
    protected BlockPos getOnPos(float $$0) {
        if (this.mainSupportingBlockPos.isPresent()) {
            BlockPos $$1 = this.mainSupportingBlockPos.get();
            if (!($$0 > 1.0E-5F)) {
                return $$1;
            } else {
                BlockState $$2 = this.level().getBlockState($$1);
                return (!((double) $$0 <= 0.5) || !$$2.is(BlockTags.FENCES)) && !$$2.is(BlockTags.WALLS) && !($$2.getBlock() instanceof FenceGateBlock)
                        ? $$1.atY(Mth.floor(this.position().y + (double) $$0)) // Adjust for ceiling (add $$0 to y)
                        : $$1;
            }
        } else {
            int $$3 = Mth.floor(this.position().x);
            int $$4 = Mth.floor(this.position().y + (double) $$0); // Adjust for ceiling (add $$0 to y)
            int $$5 = Mth.floor(this.position().z);
            return new BlockPos($$3, $$4, $$5);
        }
    }

    private SoundEvent getFallDamageSound(int $$0) {
        return $$0 > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public boolean onCieling() {
        return this.verticalCollision && !this.verticalCollisionBelow;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isState(FairkeeperOurosState.AWAKENING)) this.enableBossBar();
        //this.cielingMovementCalculation();
        this.performContactDamage();
        //this.abilitySelectionTick();
        this.blockDestructionTick();
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void performContactDamage() {
        this.level().getEntities(this, this.getBoundingBox(), this::canPerformContactDamageTo)
                .forEach(entity -> {
                    entity.push(this);
                    entity.hurt(entity.level().damageSources().mobAttack(this), (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F));
                });
    }

    private boolean canPerformContactDamageTo(Entity entity) {
        if (entity instanceof FairkeeperOurosPartEntity part) {
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
            y = (int) (Mth.floor(this.getMoveControl().getWantedY()) - this.getBoundingBox().maxY);
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
        this.destroyContactBlocks(-DESTRUCTION_RANGE, DESTRUCTION_RANGE, -1, 3, -DESTRUCTION_RANGE, DESTRUCTION_RANGE);
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

    private void abilitySelectionTick() {

        stateSelector.tick();

        if (!this.isState(FairkeeperOurosState.IDLE)) {
            return;
        }

        if (this.attackTick > 0) {
            --this.attackTick;
            return;
        }

        if (this.getTarget() == null) return;

        if (stateSelector.isEmpty()) {
            this.setMoveSets();
        }
        FairkeeperOurosState state = stateSelector.selectMove();
        this.setState(state);
    }

    public void stopAttacking(int cooldown) {
        this.setState(FairkeeperOurosState.IDLE);
        this.targetRandomPlayer();
        this.setAttackTick(cooldown);
        if (this.getTarget() != null) {
            this.getTarget().sendSystemMessage(Component.literal("Ouros : Stopped"));
        }
        ((FairkeeperSerpentCallerEntity) this.getCaller()).setOurosWaitingForCommand(true);
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
    public boolean isInWall() {
        return false;
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

    public void enableBossBar() {
        this.bossEvent.setVisible(true);
    }

    public void disableBossBar() {
        this.bossEvent.setVisible(false);
    }

    public int getAttackTick() {
        return this.attackTick;
    }

    public void setAttackTick(int i) {
        this.attackTick = i;
    }

    public double getAttackDamage() {
        return this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public double getFollowDistance() {
        return this.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    public void setState(FairkeeperOurosState FairkeeperOurosState) {
        this.entityData.set(STATE, FairkeeperOurosState);
    }

    public FairkeeperOurosState getState() {
        return this.entityData.get(STATE);
    }

    public boolean isState(FairkeeperOurosState FairkeeperOurosState) {
        return this.getState().equals(FairkeeperOurosState);
    }

    public UUID getChildId() {
        return this.entityData.get(CHILD_UUID).orElse(null);
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.entityData.set(CHILD_UUID, Optional.ofNullable(uniqueId));
    }

    public UUID getCallerId() {
        return this.entityData.get(CALLER_UUID).orElse(null);
    }

    public void setCallerId(@Nullable UUID uniqueId) {
        this.entityData.set(CALLER_UUID, Optional.ofNullable(uniqueId));
    }

    public Queue<Vec3> getPositionHistory() {
        return this.positionHistory;
    }

    public float getPreviousTilt() {
        return this.previousTilt;
    }

    public void setPreviousTilt(float tilt) {
        this.previousTilt = tilt;
    }

    public Vec3 getAwakenEndPos() {
        return this.awakenEndPos;
    }

    public void setAwakenEndPos(Vec3 blockPos) {
        this.awakenEndPos = blockPos;
    }

    public void setIsOnCeiling(boolean b) { this.entityData.set(IS_ON_CEILING, b); }

    public boolean isOnCeiling() { return this.entityData.get(IS_ON_CEILING); }

    @Override
    public boolean isStationary() {
        return false;
    }

    @Override
    public boolean isSlumbering() {
        return false;
    }

    public enum FairkeeperOurosState {
        AWAKENING,
        IDLE,
        CIRCLING,
        SHOOT_VERTEX_ARROW_DIRECT,
        SUMMON_SCUTTLE,
        DROP_PILLAR_SMALL_SQUARE,
        DROP_PILLAR_SINGLE_LINE,
        DROP_PILLAR_LARGE_SQUARE,
        DROP_PILLAR_CROSS,
        DROP_PILLAR_DOUBLE_LINE,
        DYING;

        private FairkeeperOurosState() {
        }
    }
}
