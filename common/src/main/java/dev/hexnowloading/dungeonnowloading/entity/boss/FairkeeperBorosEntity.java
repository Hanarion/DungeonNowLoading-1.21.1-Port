package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.block.entity.ShieldingStonePillarBlockEntity;
import dev.hexnowloading.dungeonnowloading.entity.ai.*;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.SmoothBodyRotationControl;
import dev.hexnowloading.dungeonnowloading.entity.util.Boss;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.MoveSet;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FairkeeperBorosEntity extends Monster implements Boss, Enemy, SlumberingEntity {

    private static final EntityDataAccessor<FairkeeperState> STATE = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityStates.FAIRKEEPER_STATE);
    private static final EntityDataAccessor<BlockPos> SPAWN_POINT = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID = SynchedEntityData.defineId(FairkeeperBorosEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private MoveSet<FairkeeperState> stateSelector = new MoveSet<>();
    private final Deque<Vec3> positionHistory = new LinkedList<>();

    private int attackTick;

    private final ServerBossEvent bossEvent;
    public static final int SEGMENT_COUNT = 13;
    public static int SEGMENT_DELAY_STEP = 7;

    public FairkeeperBorosEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(3.0f);
        this.bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BossResetGoal(this, this.getFollowDistance()));
        this.goalSelector.addGoal(2, new FairkeeperCircleAroundPlayerGoal(this, 20.0, 1.0, true)); // Clockwise
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        //this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        //this.targetSelector.addGoal(2, new BossTargetSelectorGoal(this, this.getFollowDistance()));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, FairkeeperState.SLUMBERING);
        this.entityData.define(SPAWN_POINT, BlockPos.ZERO);
        this.entityData.define(CHILD_UUID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("SpawnPoint", NbtHelper.newIntList(this.getSpawnPoint().getX(), this.getSpawnPoint().getY(), this.getSpawnPoint().getZ()));
        compoundTag.putBoolean("Slumbering", isSlumbering());
        if (this.getChildId() != null) {
            compoundTag.putUUID("ChildUUID", this.getChildId());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(SPAWN_POINT, new BlockPos(compoundTag.getList("SpawnPoint", CompoundTag.TAG_INT).getInt(0), compoundTag.getList("SpawnPoint", CompoundTag.TAG_INT).getInt(1), compoundTag.getList("SpawnPoint", CompoundTag.TAG_INT).getInt(2)));
        this.entityData.set(STATE, compoundTag.getBoolean("Slumbering") ? FairkeeperState.SLUMBERING : FairkeeperState.IDLE);
        if (this.hasCustomName()) this.bossEvent.setName(this.getDisplayName());
        if (compoundTag.hasUUID("ChildUUID")) {
            this.setChildId(compoundTag.getUUID("ChildUUID"));
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

    public void startBossFight() {
        this.entityData.set(STATE, FairkeeperState.AWAKENING);
    }

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
                    }
                    //double offsetX = partParent.getX() - (i + 1) * 1.0; // Offset by 3 blocks for each segment
                    part.setPos(partParent.getX(), partParent.getY(), partParent.getZ());
                    partParent = part;
                    if (i == segments - 1) {
                        part.setTail(true);
                    }
                    this.level().addFreshEntity(part);
                }
            }

            if (this.getDeltaMovement().lengthSqr() > 0.01) {
                positionHistory.addFirst(new Vec3(this.getX(), this.getY(), this.getZ()));

                int maxHistorySize = (SEGMENT_COUNT + 1) * SEGMENT_DELAY_STEP;
                if (positionHistory.size() > maxHistorySize) {
                    positionHistory.pollLast();
                }
                //System.out.println(positionHistory);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        //System.out.println(this.getState());
        if (this.isState(FairkeeperState.AWAKENING)) this.enableBossBar();
        //this.abilitySelectionTick();
        this.blockDestructionTick();
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void blockDestructionTick() {
        int DESTRUCTION_RANGE = 2;
        Entity target = this.getTarget();
        int y = 0;
        if (target != null) {
            y = target.getBlockY() - this.getBlockY();
            System.out.println(y);
        }
        if (y < -2) {
            this.destroyContactBlocks(-DESTRUCTION_RANGE, DESTRUCTION_RANGE, -1, 3, -DESTRUCTION_RANGE, DESTRUCTION_RANGE);
            return;
        }
        if (this.getDeltaMovement().lengthSqr() > 0.01) {
            return;
        }
        if (y > 2) {
            System.out.println("UP");
            this.setPos(this.getX(), this.getY() + 1, this.getZ());
            //this.destroyContactBlocks(-DESTRUCTION_RANGE, DESTRUCTION_RANGE, 1, 5, -DESTRUCTION_RANGE, DESTRUCTION_RANGE);
            //return;
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

        //System.out.println(this.getState());

        if (!this.isState(FairkeeperState.IDLE)) {
            return;
        }

        if (this.attackTick > 0) {
            --this.attackTick;
            return;
        }

        this.targetRandomPlayer();

        if (this.getTarget() == null) return;

        if (stateSelector.isEmpty()) {
            stateSelector.addMove(FairkeeperState.STRAFE, 5, 60, 0);
            stateSelector.addMove(FairkeeperState.GROUND_SMASH, 5, 400, 0);
            stateSelector.addMove(FairkeeperState.STONE_PILLAR, 5, 400, 0);
            stateSelector.addMove(FairkeeperState.SHIELDING_STONE_PILLAR, 4, 800, 200);
        }
        this.setState(stateSelector.selectMove());
    }

    public void stopAttacking(int cooldown) {
        this.setState(FairkeeperState.IDLE);
        this.setTarget(null);
        this.setAttackTick(cooldown);
    }

    @Override
    public void targetRandomPlayer() {
        this.setState(FairkeeperState.TARGET);
    }

    @Override
    public boolean playerTargetingCondition() {
        return this.isState(FairkeeperState.TARGET);
    }

    @Override
    public void postPlayerTargeting() {
        this.setState(FairkeeperState.IDLE);
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
        this.setState(FairkeeperState.SLUMBERING);
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

        //System.out.println(this.level() + " / " + filtered.isEmpty());

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

    public void enableBossBar() { this.bossEvent.setVisible(true); }
    public void disableBossBar() { this.bossEvent.setVisible(false); }
    public int getAttackTick() { return this.attackTick; }
    public void setAttackTick(int i) { this.attackTick = i; }
    public double getAttackDamage() { return this.getAttributeValue(Attributes.ATTACK_DAMAGE); }
    public double getFollowDistance() { return this.getAttributeValue(Attributes.FOLLOW_RANGE); }
    public void setSpawnPoint(BlockPos blockPos) { this.entityData.set(SPAWN_POINT, blockPos); }
    public BlockPos getSpawnPoint() { return this.entityData.get(SPAWN_POINT); }
    public void setState(FairkeeperState fairkeeperState) { this.entityData.set(STATE, fairkeeperState); }
    public FairkeeperState getState() { return this.entityData.get(STATE); }
    public boolean isState(FairkeeperState fairkeeperState) { return this.getState().equals(fairkeeperState); }
    public UUID getChildId() { return this.entityData.get(CHILD_UUID).orElse(null); }
    public void setChildId(@Nullable UUID uniqueId) { this.entityData.set(CHILD_UUID, Optional.ofNullable(uniqueId)); }
    public Queue<Vec3> getPositionHistory() { return this.positionHistory; }

    @Override
    public boolean isStationary() {
        return isSlumbering();
    }

    @Override
    public boolean isSlumbering() {
        return isState(FairkeeperState.SLUMBERING);
    }

    public enum FairkeeperState {
        SLUMBERING,
        AWAKENING,
        IDLE,
        TARGET,
        STRAFE,
        GROUND_SMASH,
        OVERHEAT_LANE,
        STONE_PILLAR,
        SHIELDING_STONE_PILLAR,
        DYING;

        private FairkeeperState() {}
    }

    public class FacingMoveControl extends MoveControl {
        private final Mob mob;

        public FacingMoveControl(Mob mob) {
            super(mob);
            this.mob = mob;
        }

        @Override
        public void tick() {
            // Get the mob's current yaw (facing direction)
            float yaw = this.mob.getYRot();

            // Calculate the forward movement direction based on yaw
            double xSpeed = -Math.sin(Math.toRadians(yaw)) * this.speedModifier;
            double zSpeed = Math.cos(Math.toRadians(yaw)) * this.speedModifier;

            // Set the mob's velocity to move in the facing direction
            this.mob.setDeltaMovement(new Vec3(xSpeed, this.mob.getDeltaMovement().y, zSpeed));

            // Update the speed for the mob
            this.mob.setSpeed((float) this.speedModifier);
        }
    }
}
