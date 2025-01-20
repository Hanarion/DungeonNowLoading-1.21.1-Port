package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.block.entity.ShieldingStonePillarBlockEntity;
import dev.hexnowloading.dungeonnowloading.entity.ai.*;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.FairkeeperOurosMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.FairkeeperSerpentMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.util.*;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import dev.hexnowloading.dungeonnowloading.util.NbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FairkeeperOurosEntity extends Monster implements Boss, Enemy, SlumberingEntity, FairkeeperSerpentEntity {

    private static final EntityDataAccessor<FairkeeperOurosState> STATE = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityStates.FAIRKEEPER_OUROS_STATE);
    private static final EntityDataAccessor<BlockPos> SPAWN_POINT = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> CALLER_UUID = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_ON_CEILING = SynchedEntityData.defineId(FairkeeperOurosEntity.class, EntityDataSerializers.BOOLEAN);

    private MoveSet<FairkeeperOurosState> stateSelector = new MoveSet<>();
    private final Deque<Vec3> positionHistory = new LinkedList<>();

    private int attackTick;
    private float previousTilt = 0.0f;
    private BlockPos awakenEndPos;

    private final ServerBossEvent bossEvent;
    public static final int SEGMENT_COUNT = 14;
    public static int SEGMENT_DELAY_STEP = 7;
    
    public FairkeeperOurosEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        //this.moveControl = new FairkeeperSerpentMoveControl(this, 5.0F);
        this.moveControl = new FairkeeperOurosMoveControl(this);
        this.setMaxUpStep(3.0f);
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
        this.goalSelector.addGoal(1, new BossResetGoal(this, this.getFollowDistance()));
        this.goalSelector.addGoal(2, new FairkeeperOurosAwakenGoal(this));
        //this.goalSelector.addGoal(3, new FairkeeperCircleAroundPlayerGoal(this, 20.0, 1.0, true)); // Clockwise
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        //this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new BossTargetSelectorGoal(this, this.getFollowDistance()));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, FairkeeperOurosState.SLUMBERING);
        this.entityData.define(SPAWN_POINT, BlockPos.ZERO);
        this.entityData.define(CHILD_UUID, Optional.empty());
        this.entityData.define(CALLER_UUID, Optional.empty());
        this.entityData.define(IS_ON_CEILING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("SpawnPoint", NbtHelper.newIntList(this.getSpawnPoint().getX(), this.getSpawnPoint().getY(), this.getSpawnPoint().getZ()));
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
        this.entityData.set(SPAWN_POINT, new BlockPos(compoundTag.getList("SpawnPoint", CompoundTag.TAG_INT).getInt(0), compoundTag.getList("SpawnPoint", CompoundTag.TAG_INT).getInt(1), compoundTag.getList("SpawnPoint", CompoundTag.TAG_INT).getInt(2)));
        this.entityData.set(STATE, compoundTag.getBoolean("Slumbering") ? FairkeeperOurosState.SLUMBERING : FairkeeperOurosState.IDLE);
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
                    FairkeeperOurosPartEntity part = new FairkeeperOurosPartEntity(DNLEntityTypes.FAIRKEEPER_OUROS_PART.get(), partParent, this, i);
                    if (partParent == this) {
                        this.setChildId(part.getUUID());
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

            if (!this.onGround() && this.getDeltaMovement().y > 0.0) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.8, 1.0));

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

    @Override
    public void travel(Vec3 $$0) {
        if (this.isControlledByLocalInstance()) {
            double $$1 = -0.08; // Negative value for upward gravity
            boolean $$2 = this.getDeltaMovement().y >= 0.0; // Checks if the entity is rising

            if ($$2 && this.hasEffect(MobEffects.SLOW_FALLING)) {
                $$1 = -0.01; // Slower upward movement for slow falling effect
            }

            if (this.isFallFlying()) {
                this.checkSlowFallDistance();
                Vec3 $$13 = this.getDeltaMovement();
                Vec3 $$14 = this.getLookAngle();
                float $$15 = this.getXRot() * (float) (Math.PI / 180.0);
                double $$16 = Math.sqrt($$14.x * $$14.x + $$14.z * $$14.z);
                double $$17 = $$13.horizontalDistance();
                double $$18 = $$14.length();
                double $$19 = Math.cos((double) $$15);
                $$19 = $$19 * $$19 * Math.min(1.0, $$18 / 0.4);
                $$13 = this.getDeltaMovement().add(0.0, $$1 * (-1.0 + $$19 * 0.75), 0.0);

                if ($$13.y > 0.0 && $$16 > 0.0) { // Adjust for rising motion
                    double $$20 = $$13.y * 0.1 * $$19;
                    $$13 = $$13.add($$14.x * $$20 / $$16, $$20, $$14.z * $$20 / $$16);
                }

                if ($$15 > 0.0F && $$16 > 0.0) { // Adjust for upward pitch
                    double $$21 = $$17 * (double) (Mth.sin($$15)) * 0.04;
                    $$13 = $$13.add(-$$14.x * $$21 / $$16, $$21 * 3.2, -$$14.z * $$21 / $$16);
                }

                if ($$16 > 0.0) {
                    $$13 = $$13.add(($$14.x / $$16 * $$17 - $$13.x) * 0.1, 0.0, ($$14.z / $$16 * $$17 - $$13.z) * 0.1);
                }

                this.setDeltaMovement($$13.multiply(0.99F, 0.98F, 0.99F));
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.horizontalCollision && !this.level().isClientSide) {
                    double $$22 = this.getDeltaMovement().horizontalDistance();
                    double $$23 = $$17 - $$22;
                    float $$24 = (float) ($$23 * 10.0 - 3.0);
                    if ($$24 > 0.0F) {
                        this.playSound(this.getFallDamageSound((int) $$24), 1.0F, 1.0F);
                        this.hurt(this.damageSources().flyIntoWall(), $$24);
                    }
                }

                if (this.onGround() && !this.level().isClientSide) {
                    this.setSharedFlag(7, false);
                }
            } else {
                BlockPos $$25 = this.getBlockPosBelowThatAffectsMyMovement();
                float $$26 = this.level().getBlockState($$25).getBlock().getFriction();
                float $$27 = this.onGround() ? $$26 * 0.91F : 0.91F;
                Vec3 $$28 = this.handleRelativeFrictionAndCalculateMovement($$0, $$26);
                double $$29 = $$28.y;

                if (this.hasEffect(MobEffects.LEVITATION)) {
                    $$29 += (0.05 * (double) (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - $$28.y) * 0.2;
                } else if (this.level().isClientSide && !this.level().hasChunkAt($$25)) {
                    if (this.getY() > (double) this.level().getMinBuildHeight()) {
                        $$29 = 0.1; // Upward motion when outside chunk area
                    } else {
                        $$29 = 0.0;
                    }
                } else if (!this.isNoGravity()) {
                    $$29 -= $$1; // Apply upward gravity
                }

                if (this.shouldDiscardFriction()) {
                    this.setDeltaMovement($$28.x, $$29, $$28.z);
                } else {
                    this.setDeltaMovement($$28.x * (double) $$27, $$29 * 0.98F, $$28.z * (double) $$27);
                }
            }
        }
    }

    private SoundEvent getFallDamageSound(int $$0) {
        return $$0 > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    @Override
    public boolean onGround() {
        BlockPos aboveBlockPos = this.blockPosition().above(4);
        return this.level().getBlockState(aboveBlockPos).isSolidRender(this.level(), aboveBlockPos);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isState(FairkeeperOurosState.AWAKENING)) this.enableBossBar();
        //this.cielingMovementCalculation();
        this.performContactDamage();
        this.abilitySelectionTick();
        this.blockDestructionTick();
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void cielingMovementCalculation() {
        BlockPos aboveBlockPos = this.blockPosition().above(4);
        boolean isOnCeiling = this.level().getBlockState(aboveBlockPos).isCollisionShapeFullBlock(this.level(), aboveBlockPos);
        this.setIsOnCeiling(isOnCeiling);

        if (isOnCeiling) {
            System.out.println(tickCount + " : On Cieling");
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, 0, motion.z);
            if (!this.isNoGravity()) {
                this.setDeltaMovement(motion.multiply(0.91, 1.0, 0.91));
            }
            this.setPos(this.getX(), aboveBlockPos.getY() - this.getBbHeight(), this.getZ());
        } else if (!this.isNoGravity()) {
            System.out.println(tickCount + " : Inverted Gravity");
            Vec3 motion = this.getDeltaMovement();
            double invertedGravity = 0.08;
            double drag = 0.98;
            double newYMotion = (motion.y + invertedGravity) * drag;
            this.setDeltaMovement(motion.x, newYMotion, motion.z);
        }
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

    private void abilitySelectionTick() {

        stateSelector.tick();

        if (!this.isState(FairkeeperOurosState.IDLE)) {
            return;
        }

        if (this.attackTick > 0) {
            --this.attackTick;
            return;
        }

        this.targetRandomPlayer();

        if (this.getTarget() == null) return;

        if (stateSelector.isEmpty()) {
            stateSelector.addMove(FairkeeperOurosState.CIRCLING, 5, 60, 0);
            stateSelector.addMove(FairkeeperOurosState.GROUND_SMASH, 5, 400, 0);
            stateSelector.addMove(FairkeeperOurosState.STONE_PILLAR, 5, 400, 0);
            stateSelector.addMove(FairkeeperOurosState.SHIELDING_STONE_PILLAR, 4, 800, 200);
            System.out.println(stateSelector);
            //stateSelector.removeMove(FairkeeperOurosState.CIRCLING);
            System.out.println(stateSelector);
        }
        this.setState(stateSelector.selectMove());
    }

    public void stopAttacking(int cooldown) {
        this.setState(FairkeeperOurosState.IDLE);
        this.setTarget(null);
        this.setAttackTick(cooldown);
    }

    @Override
    public void targetRandomPlayer() {
        this.setState(FairkeeperOurosState.TARGET);
    }

    @Override
    public boolean playerTargetingCondition() {
        return this.isState(FairkeeperOurosState.TARGET);
    }

    @Override
    public void postPlayerTargeting() {
        this.setState(FairkeeperOurosState.IDLE);
    }

    @Override
    public BlockPos resetRegionCenter() {
        return this.getSpawnPoint();
    }

    @Override
    public boolean resetCondition() {
        return !this.isSlumbering();
    }

    @Override
    public void resetBoss() {
        this.setHealth(this.getMaxHealth());
        this.disableBossBar();
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(this.getSpawnPoint().getX() + 0.5, this.getSpawnPoint().getY(), this.getSpawnPoint().getZ() + 0.5);
        this.setState(FairkeeperOurosState.SLUMBERING);
        this.setTarget(null);
        this.stateSelector.clear();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.isCreativePlayer()) {
            return super.hurt(damageSource, amount);
        }
        if (this.isSlumbering()) {
            return false;
        }
        if (damageSource.is(DNLTags.FAIRKEEPER_HURTABLE)) {
            return super.hurt(damageSource, amount);
        }

        double RANGE = this.getFollowDistance();

        double maxRangeX = this.getX() + RANGE;
        double minRangeX = this.getX() - RANGE;
        double maxRangeY = this.getY() + RANGE;
        double minRangeY = this.getY() - RANGE;
        double maxRangeZ = this.getZ() + RANGE;
        double minRangeZ = this.getZ() - RANGE;

        Map<BlockPos, BlockEntity> map = new HashMap<>();
        int chunkMinX = SectionPos.blockToSectionCoord(minRangeX);
        int chunkMinZ = SectionPos.blockToSectionCoord(minRangeZ);
        int chunkMaxX = SectionPos.blockToSectionCoord(maxRangeX);
        int chunkMaxZ = SectionPos.blockToSectionCoord(maxRangeZ);
        for (int x = 0; chunkMinX + x <= chunkMaxX; x++) {
            for (int z = 0; chunkMinZ + z <= chunkMaxZ; z++) {
                map.putAll(this.level().getChunk(chunkMinX + x, chunkMinZ + z).getBlockEntities());
            }
        }

        Map<BlockPos, BlockEntity> filtered = map.entrySet()
                .stream()
                .filter(e -> (e.getValue() instanceof ShieldingStonePillarBlockEntity blockEntity && blockEntity.getBlockState().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER))
                .filter(e -> e.getKey().getX() < maxRangeX && e.getKey().getX() >= minRangeX && e.getKey().getY() < maxRangeY && e.getKey().getY() >= minRangeY && e.getKey().getZ() < maxRangeZ && e.getKey().getZ() >= minRangeZ)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!filtered.isEmpty()) {
            List<BlockPos> blockPosList = filtered.keySet().stream().toList();
            for (BlockPos blockPos : blockPosList) {
                this.redstoneBeam(this.level(), this.blockPosition(), blockPos);
            }
            return false;
        }

        return super.hurt(damageSource, amount);
    }

    private void redstoneBeam(Level level, BlockPos originPos, BlockPos targetPos) {
        double d = (double) (targetPos.getX() - originPos.getX());
        double e = (double) (targetPos.getY() - originPos.getY());
        double f = (double) (targetPos.getZ() - originPos.getZ());
        double s = Math.sqrt(d * d + e * e + f * f);
        d /= s;
        e /= s;
        f /= s;
        double r = level.random.nextDouble();
        while (r < s) {
            r += 0.2;
            level.addAlwaysVisibleParticle(DustParticleOptions.REDSTONE, (double) originPos.getX() + 0.5D + d * r, (double) originPos.getY() + 0.5D + e * r, (double) originPos.getZ() + 0.5D + f * r, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        this.setSpawnPoint(this.blockPosition());
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
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
        return super.isInWall();
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

    public void setSpawnPoint(BlockPos blockPos) {
        this.entityData.set(SPAWN_POINT, blockPos);
    }

    public BlockPos getSpawnPoint() {
        return this.entityData.get(SPAWN_POINT);
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

    public BlockPos getAwakenEndPos() {
        return this.awakenEndPos;
    }

    public void setAwakenEndPos(BlockPos blockPos) {
        this.awakenEndPos = blockPos;
    }

    public void setIsOnCeiling(boolean b) { this.entityData.set(IS_ON_CEILING, b); }

    public boolean isOnCeiling() { return this.entityData.get(IS_ON_CEILING); }

    @Override
    public boolean isStationary() {
        return isSlumbering();
    }

    @Override
    public boolean isSlumbering() {
        return isState(FairkeeperOurosState.SLUMBERING);
    }

    public enum FairkeeperOurosState {
        SLUMBERING,
        AWAKENING,
        IDLE,
        CIRCLING,
        TARGET,
        STRAFE,
        GROUND_SMASH,
        OVERHEAT_LANE,
        STONE_PILLAR,
        SHIELDING_STONE_PILLAR,
        DYING;

        private FairkeeperOurosState() {
        }
    }
}
