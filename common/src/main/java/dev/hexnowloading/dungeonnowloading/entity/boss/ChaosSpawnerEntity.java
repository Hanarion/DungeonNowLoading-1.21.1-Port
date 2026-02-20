package dev.hexnowloading.dungeonnowloading.entity.boss;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.*;
import dev.hexnowloading.dungeonnowloading.config.BossConfig;
import dev.hexnowloading.dungeonnowloading.entity.ai.*;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import dev.hexnowloading.dungeonnowloading.entity.misc.SpecialItemEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.*;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CFadeInTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CFadeOutBackgroundMusicSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStartTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStopTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.*;
import dev.hexnowloading.dungeonnowloading.sound.TickingSoundTarget;
import dev.hexnowloading.dungeonnowloading.util.WeightedRandomBag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class ChaosSpawnerEntity extends Monster implements Enemy, UniqueDeathAnimationEntity, WeightedTargetProvider {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Boolean> DATA_FLAGS_ID = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<State> DATA_STATE;
    private static final EntityDataAccessor<BlockPos> SPAWN_POINT = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AWAKENING_TICKS = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICK = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTIVE_RANGE = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PLAYER_COUNT = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARRIER_NORTH_TICK = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARRIER_EAST_TICK = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARRIER_SOUTH_TICK = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARRIER_WEST_TICK = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARRIER_UP_TICK = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARRIER_DOWN_TICK = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final byte TRIGGER_SLEEP_ANIMATION_BYTE = 70;
    private static final byte TRIGGER_WAKE_UP_ANIMATION_BYTE = 71;
    private static final byte TRIGGER_SMASH_ATTACK_ANIMATION_BYTE = 72;
    private static final byte TRIGGER_RANGE_ATTACK_ANIMATION_BYTE = 73;
    private static final byte TRIGGER_RANGE_BURST_ATTACK_ANIMATION_BYTE = 74;
    private static final byte TRIGGER_SUMMON_ANIMATION_BYTE = 75;
    private static final byte TRIGGER_DEATH_ANIMATION_BYTE = 76;
    private static final byte TRIGGER_ANIMATION_STOP_BYTE = 77;

    private static final UUID RECALL_ATTACK_MOD_UUID = UUID.fromString("1f3d0f6b-2c9e-4d4f-9c2d-2a91c8f2b8d7");
    private static final UUID RECALL_HEALTH_MOD_UUID = UUID.fromString("e41f6c2a-77b1-4c52-9f89-0a3e8c6d21f9");
    private static final int RECALL_POSITION_OFFSET_Y = 1;

    private int musicTick;
    protected int attackTickCount;
    private int contactAttackTickCount;
    private int deathAnimationTickCount;
    private int barrierCheckTickCount;
    private int defeatedCount = 0;
    private int modifiedDefeatedCount = 0;
    private Set<UUID> playerUUIDs;
    private UUID currentPlayerUUID;
    private List<Player> pushTargets;
    private Set<UUID> playerDefeatedUUIDs;

    public final AnimationState awakeningAnimationState = new AnimationState();
    public final AnimationState sleepingAnimationState = new AnimationState();
    public final AnimationState smashAttackAnimationState = new AnimationState();
    public final AnimationState rangeAttackAnimationState = new AnimationState();
    public final AnimationState summonAnimationState = new AnimationState();
    public final AnimationState rangeBurstAttackAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private final ServerBossEvent bossEvent;
    private DamageSource killedDamageSource;
    private final Map<UUID, Double> damageMap = new HashMap<>();
    private final Map<UUID, LivingEntity> attackers = new HashMap<>();
    private final Map<UUID, Double> threatScores = new HashMap<>();

    public ChaosSpawnerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
        this.xpReward = 500;
        this.playerUUIDs = Sets.newHashSet();
        this.currentPlayerUUID = UUID.randomUUID();
        this.playerDefeatedUUIDs = Sets.newHashSet();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.FLYING_SPEED)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new ChaosSpawnerResetGoal(this, this.getFollowDistance()));
        this.goalSelector.addGoal(2, new ChaosSpawnerSummonMobGoal(this));
        this.goalSelector.addGoal(2, new ChaosSpawnerShootGhostBulletGoal(this));
        this.goalSelector.addGoal(2, new ChaosSpawnerPushGoal(this));
        this.goalSelector.addGoal(2, new ChaosSpawnerLookAtTargetGoal(this, false));
        this.goalSelector.addGoal(8, new ChaosSpawnerRandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new BossTargetSelectorGoal(this));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return super.createNavigation(level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPAWN_POINT, BlockPos.ZERO);
        this.entityData.define(DATA_STATE, State.IDLE);
        this.entityData.define(PHASE, 0);
        this.entityData.define(DATA_FLAGS_ID, false);
        this.entityData.define(AWAKENING_TICKS, 0);
        this.entityData.define(ATTACK_TICK, 0);
        this.entityData.define(PLAYER_COUNT, 0);
        this.entityData.define(BARRIER_NORTH_TICK, -1);
        this.entityData.define(BARRIER_EAST_TICK, -1);
        this.entityData.define(BARRIER_SOUTH_TICK, -1);
        this.entityData.define(BARRIER_WEST_TICK, -1);
        this.entityData.define(BARRIER_UP_TICK, -1);
        this.entityData.define(BARRIER_DOWN_TICK, -1);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("SpawnPointX", this.getSpawnPointPos().getX());
        compoundTag.putInt("SpawnPointY", this.getSpawnPointPos().getY());
        compoundTag.putInt("SpawnPointZ", this.getSpawnPointPos().getZ());
        compoundTag.putInt("Phase", this.entityData.get(PHASE));
        compoundTag.putInt("AwakeningTicks", this.entityData.get(AWAKENING_TICKS));
        compoundTag.putInt("AttackTicks", this.attackTickCount);
        compoundTag.putInt("PlayerCount", this.entityData.get(PLAYER_COUNT));
        compoundTag.putInt("BarrierNorthTicks", this.entityData.get(BARRIER_NORTH_TICK));
        compoundTag.putInt("BarrierEastTicks,", this.entityData.get(BARRIER_EAST_TICK));
        compoundTag.putInt("BarrierSouthTicks,", this.entityData.get(BARRIER_SOUTH_TICK));
        compoundTag.putInt("BarrierWestTicks,", this.entityData.get(BARRIER_WEST_TICK));
        compoundTag.putInt("BarrierUpTicks,", this.entityData.get(BARRIER_UP_TICK));
        compoundTag.putInt("BarrierDownTicks,", this.entityData.get(BARRIER_DOWN_TICK));
        ListTag listTag = new ListTag();
        for (UUID id : this.playerUUIDs) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Id", id);
            listTag.add(t);
        }
        compoundTag.put("PlayerUUIDs", listTag);

        SeepingSoulEntity.writeRecallNBT(compoundTag, this.playerDefeatedUUIDs, this.defeatedCount, this.modifiedDefeatedCount);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        int i = compoundTag.getInt("SpawnPointX");
        int j = compoundTag.getInt("SpawnPointY");
        int k = compoundTag.getInt("SpawnPointZ");
        this.entityData.set(SPAWN_POINT, new BlockPos(i, j, k));
        this.entityData.set(AWAKENING_TICKS, compoundTag.getInt("AwakeningTicks"));
        this.entityData.set(PLAYER_COUNT, compoundTag.getInt("PlayerCount"));
        this.entityData.set(BARRIER_NORTH_TICK, compoundTag.getInt("BarrierNorthTicks"));
        this.entityData.set(BARRIER_EAST_TICK, compoundTag.getInt("BarrierEastTicks"));
        this.entityData.set(BARRIER_SOUTH_TICK, compoundTag.getInt("BarrierSouthTicks"));
        this.entityData.set(BARRIER_WEST_TICK, compoundTag.getInt("BarrierWestTicks"));
        this.entityData.set(BARRIER_UP_TICK, compoundTag.getInt("BarrierUpTicks"));
        this.entityData.set(BARRIER_DOWN_TICK, compoundTag.getInt("BarrierDownTicks"));
        this.attackTickCount = compoundTag.getInt("AttackTicks");
        int phase = compoundTag.getInt("Phase");
        if (phase < 1) {
            this.entityData.set(DATA_STATE, State.SLEEPING);
            this.entityData.set(PHASE, 0);
        } else {
            this.entityData.set(PHASE, phase);
        }
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        if (compoundTag.contains("PlayerUUIDs", CompoundTag.TAG_LIST)) {
            ListTag listTag = compoundTag.getList("PlayerUUIDs", CompoundTag.TAG_COMPOUND);
            for (int a = 0; a < listTag.size(); ++a) {
                this.playerUUIDs.add(listTag.getCompound(a).getUUID("Id"));
            }
        }
        SeepingSoulEntity.RecallData data = SeepingSoulEntity.readRecallNBT(compoundTag);

        this.playerDefeatedUUIDs.clear();
        this.playerDefeatedUUIDs.addAll(data.playerDefeatedUUIDs());

        this.defeatedCount = data.defeatedCount();
        this.modifiedDefeatedCount = data.modifiedDefeatedCount();
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.yBodyRot = 0.0F;
        this.yBodyRotO = 0.0F;
    }

    protected @NotNull BodyRotationControl createBodyControl() {
        return new EntityBodyRotationControl(this);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    // Called when entity starts rendering
    public void startSeenByPlayer(@NotNull ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
        if (this.entityData.get(PHASE) == 0) {
            disableBossBar();
        }
    }

    @Override
    // Called when entity stops rendering
    public void stopSeenByPlayer(@NotNull ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    public void startBossFight(ItemStack recallItemStack) {
        if (this.isAlive() && this.entityData.get(PHASE) < 1 && this.getState() != State.AWAKENING) {
            AABB bossArena = new AABB(this.blockPosition()).inflate(getFollowDistance());
            List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, bossArena);
            for (ServerPlayer p : players) {
                playerUUIDs.add(p.getUUID());
            }
            int playerCount = players.size();
            this.modifiedDefeatedCount = SeepingSoulEntity.getModifiedDefeatedCount(this.defeatedCount, recallItemStack);
            EntityScale.scaleBossHealth(this, playerCount, this.modifiedDefeatedCount);
            EntityScale.scaleBossAttack(this, playerCount, this.modifiedDefeatedCount);
            this.entityData.set(PLAYER_COUNT, playerCount);
            this.entityData.set(AWAKENING_TICKS, 160);
            this.entityData.set(DATA_STATE, State.AWAKENING);
            if (modifiedDefeatedCount > 0) {
                this.setCustomName(RecallUtil.recalledName(
                        Component.translatable("entity.dungeonnowloading.chaos_spawner"),
                        modifiedDefeatedCount
                ));
            } else {
                this.setCustomName(Component.translatable("entity.dungeonnowloading.chaos_spawner"));
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (this.isAlive() && this.entityData.get(PHASE) < 1 && this.getState() != State.AWAKENING) {
            player.displayClientMessage(Component.translatable("entity.dungeonnowloading.chaos_spawner.right_click"), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(player, interactionHand);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.getAwakeningTick() > 0) {
            int k1 = this.getAwakeningTick() - 1;
            if (k1 == 99) {
                this.triggerWakeUpAnimation();
                this.playSound(DNLSounds.CHAOS_SPAWNER_CHAIN_BREAK.get(), 3.0F, 1.0F);
            }
            if (k1 == 60) {
                this.playSound(DNLSounds.CHAOS_SPAWNER_LAUGHTER.get(), 3.0F, 1.0F);
            }
            if (k1 <= 0) {
                this.setPhase(1);
                this.setDataState(State.IDLE);
            }
            this.setAwakeningTick(k1);
            if (k1 == 0) {
                for (int i = 0; i < 50; ++i) {
                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.FLAME, this.getRandomX(0.9D), this.getRandomY(), this.getRandomZ(0.9D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                this.playBossMusic();
                this.fillBarriers();
                this.fillFrames();
                this.enableBossBar();
            }
        }
        if (this.getPhase() > 0 && !this.getState().equals(State.DEATH)) {
            this.musicTick();
            this.abilitySelectionTick();
            this.damageContactEntity();
            this.checkBarrierTick();
            this.phaseUpdateTick();
        }
        if (this.getState().equals(State.DEATH)) {
            this.deathAnimationTickCount++;
            if (this.deathAnimationTickCount >= 160) {
                this.die(this.damageSources().generic());
            }
        }
        // Temporary fix to boss reset (boss does not reset when player dies in a middle of an attack)
        if (this.getPhase() == 0 && this.getState().equals(State.IDLE)) {
            this.setDataState(State.SLEEPING);
        }
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void musicTick() {
        musicTick++;
        if (this.musicTick >= 2560) {
            this.musicTick = 0;
            this.playLoopMusic();
        }
    }

    private void phaseUpdateTick() {
        if (this.getPhase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5) {
            this.fadeInOverlayMusic();
            this.setPhase(2);
        }
    }

    private void damageContactEntity() {
        if (contactAttackTickCount > 0) {
            this.contactAttackTickCount--;
        } else {
            this.contactAttackTickCount = 20;
            AABB aabb = (new AABB(this.blockPosition())).inflate(2);
            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
            for (LivingEntity livingEntity : targets) {
                if (livingEntity == this) {
                    continue;
                }
                this.doHurtTarget(livingEntity);
            }
        }
    }

    private void abilitySelectionTick() {
        if (this.getTarget() != null) {
            if (attackTickCount > 0) {
                --this.attackTickCount;
            } else {
                if (this.entityData.get(DATA_STATE) == State.IDLE) {
                    BossTargetSelectorGoal.changeTarget(this);
                    WeightedRandomBag<State> attackPool = new WeightedRandomBag<>();
                    AABB aabb = (new AABB(this.blockPosition())).inflate(10);
                    pushTargets = this.level().getEntitiesOfClass(Player.class, aabb);
                    if (!pushTargets.isEmpty()) {
                        attackPool.addEntry(State.PUSH, 3);
                        attackPool.addEntry(State.SHOOT_GHOST_BULLET_SINGLE, 3);
                        attackPool.addEntry(State.SHOOT_GHOST_BULLET_BURST, 2);
                        attackPool.addEntry(State.SUMMON_MOB, 1);
                    } else {
                        attackPool.addEntry(State.SHOOT_GHOST_BULLET_SINGLE, 3);
                        attackPool.addEntry(State.SHOOT_GHOST_BULLET_BURST, 2);
                        attackPool.addEntry(State.SUMMON_MOB, 1);
                    }
                    this.entityData.set(DATA_STATE, attackPool.getRandom());
                }
            }
        }
    }

    private void checkBarrierTick() {
        if (barrierCheckTickCount > 0) {
            barrierCheckTickCount--;
        } else {
            barrierCheckTickCount = 20;
            BlockPos cageCenterPos = this.entityData.get(SPAWN_POINT).above(1);
            checkBarrierIsBroken(cageCenterPos.offset(0, 0, -2), BARRIER_NORTH_TICK);
            checkBarrierIsBroken(cageCenterPos.offset(2, 0, 0), BARRIER_EAST_TICK);
            checkBarrierIsBroken(cageCenterPos.offset(0, 0, 2), BARRIER_SOUTH_TICK);
            checkBarrierIsBroken(cageCenterPos.offset(-2, 0, 0), BARRIER_WEST_TICK);
            checkBarrierIsBroken(cageCenterPos.offset(0, 2, 0), BARRIER_UP_TICK);
            checkBarrierIsBroken(cageCenterPos.offset(0, -2, 0), BARRIER_DOWN_TICK);
        }
        regenerateBarrierTick(FRAME_POS_NORTH, BARRIER_NORTH_TICK, this.entityData.get(SPAWN_POINT).above(1).offset(0, 0, -2), 0);
        regenerateBarrierTick(FRAME_POS_EAST, BARRIER_EAST_TICK, this.entityData.get(SPAWN_POINT).above(1).offset(2, 0, 0), 1);
        regenerateBarrierTick(FRAME_POS_SOUTH, BARRIER_SOUTH_TICK, this.entityData.get(SPAWN_POINT).above(1).offset(0, 0, 2), 2);
        regenerateBarrierTick(FRAME_POS_WEST, BARRIER_WEST_TICK, this.entityData.get(SPAWN_POINT).above(1).offset(-2, 0, 0), 3);
        regenerateBarrierTick(FRAME_POS_UP, BARRIER_UP_TICK, this.entityData.get(SPAWN_POINT).above(1).offset(0, 2, 0), 4);
        regenerateBarrierTick(FRAME_POS_DOWN, BARRIER_DOWN_TICK, this.entityData.get(SPAWN_POINT).above(1).offset(0, -2, 0), 5);
    }

    private void checkBarrierIsBroken(BlockPos blockPos, EntityDataAccessor<Integer> entityDataAccessor) {
        if (!this.level().getBlockState(blockPos).is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get()) && this.entityData.get(entityDataAccessor) < 0) {
            this.entityData.set(entityDataAccessor, 100);
        }
    }

    private void regenerateBarrierTick(ImmutableList<BlockPos> framePositions , EntityDataAccessor<Integer> entityDataAccessor, BlockPos barrierCenterPos, int barrierDirection) {
        if (this.entityData.get(entityDataAccessor) > 0) {
            int barrierRegenerateTickCount = this.entityData.get(entityDataAccessor) - 1;
            this.entityData.set(entityDataAccessor, barrierRegenerateTickCount);
        } else if (this.entityData.get(entityDataAccessor) != -1) {
            BlockPos cageCenterPos = this.entityData.get(SPAWN_POINT).above(1);
            Iterator<BlockPos> iterator = framePositions.iterator();
            int fixedFrame = 0;
            while(iterator.hasNext()) {
                BlockPos framePos = cageCenterPos.offset(iterator.next());
                BlockState frameState = this.level().getBlockState(framePos);
                if (frameState.is(DNLBlocks.CHAOS_SPAWNER_EDGE.get()) || frameState.is(DNLBlocks.CHAOS_SPAWNER_DIAMOND_EDGE.get()) || frameState.is(DNLBlocks.CHAOS_SPAWNER_DIAMOND_VERTEX.get())) {
                    fixedFrame++;
                } else {
                    if (frameState.getBlock() instanceof ChaosSpawnerEdgeBlock) {
                        this.level().playSound(null, framePos, DNLSounds.CHAOS_SPAWNER_DIAMOND_NOTCH_REGENERATE.get(), this.getSoundSource(), 3.0F, 0.8F);
                        ChaosSpawnerEdgeBlock.fixFrame(this.level(), framePos, frameState);
                    } else if (frameState.getBlock() instanceof  ChaosSpawnerVertexBlock) {
                        this.level().playSound(null, framePos, DNLSounds.CHAOS_SPAWNER_DIAMOND_NOTCH_REGENERATE.get(), this.getSoundSource(), 3.0F, 0.8F);
                        ChaosSpawnerVertexBlock.fixFrame(this.level(), framePos, frameState);
                    }
                    this.entityData.set(entityDataAccessor, 20);
                    break;
                }
            }
            if (fixedFrame == 16) {
                this.entityData.set(entityDataAccessor, -1);
                this.placeFullBarrier(barrierCenterPos, barrierDirection);
            }
        }
    }

    private void placeFullBarrier(BlockPos barrierCenterPos, int barrierDirection) {
        this.level().playSound(null, barrierCenterPos, DNLSounds.CHAOS_SPAWNER_BARRIER_REGENERATE.get(), this.getSoundSource(), 3.0F, 1.0F);
        ChaosSpawnerBarrierCenterBlock.placeBarrier(this.level(), barrierCenterPos, barrierDirection);
        ChaosSpawnerBarrierEdgeBlock.placeBarrier(this.level(), barrierCenterPos, barrierDirection);
        ChaosSpawnerBarrierVertexBlock.placeBarrier(this.level(), barrierCenterPos, barrierDirection);
    }

    private void fillFrames() {
        Iterator<BlockPos> iterator = FRAME_POSITIONS.iterator();
        BlockPos cageCenterPos = this.entityData.get(SPAWN_POINT).above(1);
        while (iterator.hasNext()) {
            BlockPos framePos = cageCenterPos.offset((BlockPos) iterator.next());
            BlockState frameState = this.level().getBlockState(framePos);
            if (frameState.is(DNLBlocks.CHAOS_SPAWNER_BROKEN_DIAMOND_EDGE.get()) || frameState.is(DNLBlocks.CHAOS_SPAWNER_BROKEN_EDGE.get())) {
                ChaosSpawnerEdgeBlock.fixFrame(this.level(), framePos, frameState);
            } else if (frameState.is(DNLBlocks.CHAOS_SPAWNER_BROKEN_DIAMOND_VERTEX.get())) {
                ChaosSpawnerVertexBlock.fixFrame(this.level(), framePos, frameState);
            }
        }
    }

    private void fillBarriers() {
        BlockPos cageCenterPos = this.entityData.get(SPAWN_POINT).above(1);
        Level level = this.level();
        if (!level.getBlockState(cageCenterPos.offset(0, 0, -2)).is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get())) { this.placeFullBarrier(cageCenterPos.offset(0, 0, -2), 0); }
        if (!level.getBlockState(cageCenterPos.offset(2, 0, 0)).is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get())) { this.placeFullBarrier(cageCenterPos.offset(2, 0, 0), 1); }
        if (!level.getBlockState(cageCenterPos.offset(0, 0, 2)).is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get())) { this.placeFullBarrier(cageCenterPos.offset(0, 0, 2), 2); }
        if (!level.getBlockState(cageCenterPos.offset(-2, 0, 0)).is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get())) { this.placeFullBarrier(cageCenterPos.offset(-2, 0, 0), 3); }
        if (!level.getBlockState(cageCenterPos.offset(0, 2, 0)).is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get())) { this.placeFullBarrier(cageCenterPos.offset(0, 2, 0), 4); }
        if (!level.getBlockState(cageCenterPos.offset(0, -2, 0)).is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get())) { this.placeFullBarrier(cageCenterPos.offset(0, -2, 0), 5); }
    }

    @Override
    public void handleEntityEvent(byte b) {
        this.resetAnimations();
        switch (b) {
            case EntityEvent.DEATH:
                SoundEvent soundEvent = this.getDeathSound();
                if (soundEvent != null) {
                    this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }
                this.deathAnimationState.start(this.tickCount);
                this.setHealth(0.0F);
                this.setDataState(State.DEATH);

            case TRIGGER_WAKE_UP_ANIMATION_BYTE:
                this.sleepingAnimationState.stop();
                this.awakeningAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SMASH_ATTACK_ANIMATION_BYTE:
                this.smashAttackAnimationState.start(this.tickCount);
                break;
            case TRIGGER_RANGE_ATTACK_ANIMATION_BYTE:
                this.rangeAttackAnimationState.start(this.tickCount);
                break;
            case TRIGGER_RANGE_BURST_ATTACK_ANIMATION_BYTE:
                this.rangeBurstAttackAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SUMMON_ANIMATION_BYTE:
                this.summonAnimationState.start(this.tickCount);
                break;
            case TRIGGER_DEATH_ANIMATION_BYTE:
                this.deathAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SLEEP_ANIMATION_BYTE:
                this.sleepingAnimationState.start(this.tickCount);
            case TRIGGER_ANIMATION_STOP_BYTE:
                this.resetAnimations();
            default:
                super.handleEntityEvent(b);
        }
    }

    private void resetAnimations() {
        this.sleepingAnimationState.stop();
        this.awakeningAnimationState.stop();
        this.smashAttackAnimationState.stop();
        this.summonAnimationState.stop();
        this.rangeAttackAnimationState.stop();
        this.rangeBurstAttackAnimationState.stop();
        this.deathAnimationState.stop();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, SpawnGroupData spawnGroupData, CompoundTag compoundTag) {
        this.entityData.set(SPAWN_POINT, this.blockPosition());
        this.entityData.set(DATA_STATE, State.SLEEPING);
        this.triggerSleepAnimation();
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.isCreativePlayer() && (this.entityData.get(DATA_STATE) == State.SLEEPING || this.entityData.get(DATA_STATE) == State.AWAKENING)) {
            return false;
        }
        return hurtAndTrackAttackers(damageSource, damage);
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 1) {
            this.setXRot(0.0F);
        }
        if (this.deathTime >= 160 && !this.level().isClientSide() && !this.isRemoved()) {
            Level level = this.level();
            if (this.killedDamageSource == null) {
                this.killedDamageSource = this.level().damageSources().generic();
            }
            Entity entity = killedDamageSource.getEntity();
            LivingEntity livingEntity = this.getKillCredit();
            if (this.deathScore >= 0 && livingEntity != null) {
                livingEntity.awardKillScore(this, this.deathScore, this.killedDamageSource);
            }
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                if (entity == null || entity.killedEntity(serverLevel, this)) {
                    this.dropAllDeathLoot(killedDamageSource);
                    this.createWitherRose(livingEntity);
                }
                this.level().broadcastEntityEvent(this, (byte)3);
            }
            spawnSeepingSoul((ServerLevel) level);
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    private void spawnSeepingSoul(ServerLevel level) {
        // Spawn at boss *starting position*
        BlockPos start = this.getSpawnPointPos();

        SeepingSoulEntity soul = DNLEntityTypes.SEEPING_SOUL.get().create(level);
        if (soul == null) return;

        soul.moveTo(start.getX() + 0.5, start.getY() + RECALL_POSITION_OFFSET_Y, start.getZ() + 0.5, 0, 0);

        // bossId should match the recall registry id you use for Chaos Spawner
        soul.setBossId(new ResourceLocation(DungeonNowLoading.MOD_ID, "chaos_spawner"));

        int count = SeepingSoulEntity.getRecallCountForSeepingSoul(this.defeatedCount, this.modifiedDefeatedCount);

        soul.setPlayerDefeatedUUIDs(this.playerDefeatedUUIDs);
        // next run is +1
        soul.setDefeatedCount(Mth.clamp(count, 0, 100));

        level.addFreshEntity(soul);

        soul.playSpawnAnimation();
    }


    @Override
    public void die(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        this.killedDamageSource = damageSource;
        Entity entity = this.killedDamageSource.getEntity();
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        if (!this.level().isClientSide && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getCombatTracker().getDeathMessage().getString());
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (entity == null || entity.killedEntity(serverLevel, this)) {
                this.gameEvent(GameEvent.ENTITY_DIE);
            }
            this.level().broadcastEntityEvent(this, (byte)3);
        }
        this.dead = true;
        this.getCombatTracker().recheckStatus();
        this.setPose(Pose.DYING);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int a, boolean b) {
        if (!BossConfig.TOGGLE_MULTIPLAYER_LOOT.get()) {
            super.dropCustomDeathLoot(damageSource, a, b);
            return;
        }

        if (this.playerUUIDs.isEmpty()) return;

        // If recalled enough times, ignore gating entirely
        boolean ignoreGate = this.modifiedDefeatedCount > 0;

        // How many times to roll the loot table per eligible player
        int drops = Math.max(1, (this.modifiedDefeatedCount - this.defeatedCount) + 1);

        for (UUID playerUUID : this.playerUUIDs) {

            dropGreatXpBottlesForPlayer(playerUUID);

            if (this.modifiedDefeatedCount >= 4) {
                dropLabyrinthTrophyForPlayer(playerUUID);
            }

            if (this.modifiedDefeatedCount >= 1) { // Recall I+
                dropRecallEnchantedBookForPlayer(playerUUID);
            }

            // Gate: only first-clear gets loot (only on non-ignored runs)
            if (!ignoreGate) {
                if (this.playerDefeatedUUIDs.contains(playerUUID)) {
                    continue;
                }
            }

            this.currentPlayerUUID = playerUUID;

            // Drop multiple times for this player
            for (int i = 0; i < drops; i++) {
                this.spawnLootTableItems(damageSource, true);
            }

            // Mark claimed only for gated runs
            if (!ignoreGate) {
                this.playerDefeatedUUIDs.add(playerUUID);
            }
        }
    }

    private void dropLabyrinthTrophyForPlayer(UUID playerUuid) {
        if (this.level().isClientSide) return;

        ItemStack banner = new ItemStack(DNLBlocks.DUNGEON_BANNER_CHAOS_SPAWNER.get());
        this.spawnSpecialItemEntity(banner, 0.0F, playerUuid);
    }

    private void dropGreatXpBottlesForPlayer(UUID playerUuid) {
        if (this.level().isClientSide) return;

        int recallCount = Math.max(0, this.modifiedDefeatedCount);; // 0,1,2,...
        int total = 5 * (recallCount + 1);

        Item greatBottleItem = DNLItems.GREAT_EXPERIENCE_BOTTLE.get();

        if (greatBottleItem == null) return;

        // Split into max stack sizes
        int max = greatBottleItem.getMaxStackSize(); // typically 64
        while (total > 0) {
            int n = Math.min(total, max);
            ItemStack stack = new ItemStack(greatBottleItem, n);
            this.spawnSpecialItemEntity(stack, 0.0F, playerUuid);
            total -= n;
        }
    }

    private void dropRecallEnchantedBookForPlayer(UUID playerUuid) {
        if (this.level().isClientSide) return;

        Enchantment ench = this.random.nextBoolean()
                ? DNLEnchantments.ARC_SHOT.get()
                : DNLEnchantments.PULSE_SHOT.get();

        int level = 1;

        ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ench, level));
        // (Optional) sanity check: book.getTag() should contain "StoredEnchantments"

        this.spawnSpecialItemEntity(book, 0.0F, playerUuid);
    }

    @Override
    protected void dropFromLootTable(DamageSource $$0, boolean $$1) {
        if (!BossConfig.TOGGLE_MULTIPLAYER_LOOT.get()) {
            super.dropFromLootTable($$0, $$1);
        }
    }

    public void spawnLootTableItems(DamageSource damageSource, boolean b) {
        ResourceLocation resourceLocation = this.getLootTable();
        LootTable lootTable = this.level().getServer().getLootData().getLootTable(resourceLocation);
        LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel) this.level())).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damageSource).withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());
        if (b && this.lastHurtByPlayer != null) {
            lootparams$builder = lootparams$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
        }
        LootParams lootParams = lootparams$builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(lootParams, this.getLootTableSeed(), this::spawnSpecialItemAtLocation);
    }

    public SpecialItemEntity spawnSpecialItemAtLocation(ItemStack itemStack) {
        return spawnSpecialItemEntity(itemStack, 0.0F, this.currentPlayerUUID);
    }

    public SpecialItemEntity spawnSpecialItemEntity(ItemStack itemStack, float i, UUID uuid) {
        if (itemStack.isEmpty()) {
            return null;
        } else if (this.level().isClientSide) {
            return null;
        } else {
            SpecialItemEntity specialItemEntity = new SpecialItemEntity(this.level(), this.getX(), this.getY() + i, this.getZ(), itemStack);
            specialItemEntity.setPickerUUID(uuid);
            specialItemEntity.setDefaultPickUpDelay();
            this.level().addFreshEntity(specialItemEntity);
            return specialItemEntity;
        }
    }

    private void playBossMusic() {
        float radius = (float) this.getFollowDistance();
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        List<ResourceLocation> soundsToStart = new ArrayList<>(List.of());
        soundsToStart.add(DNLSounds.MUSIC_HELLSPAWN_BASE.get().getLocation());
        soundsToStart.add(DNLSounds.MUSIC_HELLSPAWN_OVERLAY.get().getLocation());
        for (ServerPlayer player : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStart) {
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), sound, SoundSource.MUSIC, 0, 1.0f, false, radius, radius), player);
            }
            Services.NETWORK.sendToPlayer(new S2CFadeInTickingSoundPacket(this.getId(), DNLSounds.MUSIC_HELLSPAWN_BASE.get().getLocation(), TickingSoundTarget.NEWEST, 1.0f, 60), player);
            Services.NETWORK.sendToPlayer(new S2CFadeOutBackgroundMusicSoundPacket(60), player);
        }
    }

    private void playLoopMusic() {
        float radius = (float) this.getFollowDistance();
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        for (ServerPlayer player : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_HELLSPAWN_BASE.get().getLocation(), SoundSource.MUSIC, 0, 1.0f, false, radius, radius), player);
            Services.NETWORK.sendToPlayer(new S2CFadeInTickingSoundPacket(this.getId(), DNLSounds.MUSIC_HELLSPAWN_BASE.get().getLocation(), TickingSoundTarget.NEWEST, 1.0f, 60), player);
            if (this.getPhase() > 1) {
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_HELLSPAWN_OVERLAY.get().getLocation(), SoundSource.MUSIC, 1, 1.0f, false, radius, radius), player);
            }else {
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_HELLSPAWN_OVERLAY.get().getLocation(), SoundSource.MUSIC, 0, 1.0f, false, radius, radius), player);
            }
        }
    }

    public void stopAllBossMusic() {
        float radius = (float) (this.getFollowDistance() * 2);
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );

        List<ResourceLocation> soundsToStop = new ArrayList<>(List.of());
        soundsToStop.add(DNLSounds.MUSIC_HELLSPAWN_BASE.get().getLocation());
        soundsToStop.add(DNLSounds.MUSIC_HELLSPAWN_OVERLAY.get().getLocation());

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStop) {
                Services.NETWORK.sendToPlayer(new S2CStopTickingSoundPacket(this.getId(), sound, TickingSoundTarget.ALL, 60, true), otherPlayer);
            }
        }
    }

    private void fadeInOverlayMusic() {
        float radius = (float) this.getFollowDistance();
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CFadeInTickingSoundPacket(this.getId(), DNLSounds.MUSIC_HELLSPAWN_OVERLAY.get().getLocation(), TickingSoundTarget.NEWEST, 1.0f, 20), otherPlayer);
        }
    }

    /*@Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }*/

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
    protected float getStandingEyeHeight(Pose $$0, EntityDimensions $$1) {
        return 1.0F;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return DNLSounds.CHAOS_SPAWNER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.CHAOS_SPAWNER_DEATH.get();
    }

    private SoundEvent getChainBreakSound() {
        return DNLSounds.CHAOS_SPAWNER_CHAIN_BREAK.get();
    }

    public SoundEvent getScreechSound() {
        return DNLSounds.CHAOS_SPAWNER_LAUGHTER.get();
    }

    public boolean isAwakening() { return this.entityData.get(DATA_STATE) == State.AWAKENING; }

    public boolean isSleeping() { return this.entityData.get(DATA_STATE) == State.SLEEPING; }

    public boolean isAttacking(State attackState) {
        return this.entityData.get(DATA_STATE) == attackState;
    }

    public State getState() {
        return this.entityData.get(DATA_STATE);
    }

    public void resetMusicTick() {
        this.musicTick = 0;
    }

    public int getAwakeningTick() { return this.entityData.get(AWAKENING_TICKS); }

    public int getAttackTick() { return this.attackTickCount; }

    public int getPhase() { return this.entityData.get(PHASE); }

    public BlockPos getSpawnPointPos() {
        return this.entityData.get(SPAWN_POINT);
    }

    public Set<UUID> getParticipatingPlayerUUIDs() { return this.playerUUIDs; }

    public int getParticipatingPlayerCount() { return this.playerUUIDs.size(); }

    public List<Player> getPushTargets() { return this.pushTargets; }

    public double getAttackDamage() { return this.getAttributeValue(Attributes.ATTACK_DAMAGE); }

    public double getFollowDistance() { return this.getAttributeValue(Attributes.FOLLOW_RANGE); }

    public void triggerWakeUpAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_WAKE_UP_ANIMATION_BYTE); }

    public void triggerRangeAttackAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_RANGE_ATTACK_ANIMATION_BYTE); }

    public void triggerRangeBurstAttackAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_RANGE_BURST_ATTACK_ANIMATION_BYTE); }

    public void triggerSmashAttackAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_SMASH_ATTACK_ANIMATION_BYTE); }

    public void triggerSummonAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_SUMMON_ANIMATION_BYTE); }

    public void triggerDeathAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_DEATH_ANIMATION_BYTE); }

    public void triggerSleepAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_SLEEP_ANIMATION_BYTE); }

    public void triggerAnimationStop() { this.level().broadcastEntityEvent(this, TRIGGER_ANIMATION_STOP_BYTE); }

    public void clearParticipatingPlayers() { this.playerUUIDs.clear(); }

    public void setDataState(State state) { this.entityData.set(DATA_STATE, state); }

    public void setAwakeningTick(int tick) { this.entityData.set(AWAKENING_TICKS, tick); }

    public void setAttackTick(int tick) { this.attackTickCount = tick; }

    public void setPhase(int phase) { this.entityData.set(PHASE, phase); }

    public void setBarrierNorthTick(int tick) { this.entityData.set(BARRIER_NORTH_TICK, tick); }

    public void setBarrierEastTick(int tick) { this.entityData.set(BARRIER_EAST_TICK, tick); }

    public void setBarrierSouthTick(int tick) { this.entityData.set(BARRIER_SOUTH_TICK, tick); }

    public void setBarrierWestTick(int tick) { this.entityData.set(BARRIER_WEST_TICK, tick); }

    public void setBarrierUpTick(int tick) { this.entityData.set(BARRIER_UP_TICK, tick); }

    public void setBarrierDownTick(int tick) { this.entityData.set(BARRIER_DOWN_TICK, tick); }

    public void enableBossBar() { this.bossEvent.setVisible(true); }

    public void disableBossBar() { this.bossEvent.setVisible(false); }

    public void setPlayerDefeatedUUIDs(Set<UUID> uuids) { this.playerDefeatedUUIDs = uuids; }

    public void stopAttacking(int cooldown) {
        this.entityData.set(DATA_STATE, State.IDLE);
        this.setAttackTick(cooldown);
    }

    static {
        DATA_STATE = SynchedEntityData.defineId(ChaosSpawnerEntity.class, EntityStates.CHAOS_SPAWNER_STATE);
    }

    private boolean hurtAndTrackAttackers(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        Entity attacker = source.getEntity();

        if (attacker instanceof LivingEntity livingEntity) {
            this.recordDamage(livingEntity, amount);
        }

        return result;
    }

    @Override
    public Map<UUID, Double> getDamageMap() {
        return damageMap;
    }

    @Override
    public Map<UUID, LivingEntity> getAttackers() {
        return attackers;
    }

    @Override
    public Map<UUID, Double> getThreatScoreMap() {
        return threatScores;
    }

    @Override
    public BlockPos getArenaCenter() {
        return this.blockPosition();
    }

    @Override
    public int getArenaSize() {
        return (int) this.getFollowDistance();
    }

    public static void spawnRecalledStatic(ServerLevel level, SeepingSoulEntity soul, int defeatedCount) {
        ChaosSpawnerEntity boss = DNLEntityTypes.CHAOS_SPAWNER.get().create(level);
        if (boss == null) return;
        BlockPos pos = soul.blockPosition();
        boss.initRecalled(soul, defeatedCount);
        boss.moveTo(pos.getX() + 0.5f, pos.getY() - RECALL_POSITION_OFFSET_Y, pos.getZ() + 0.5f, soul.getYRot(), 0);
        level.addFreshEntity(boss);
        spawnRecallFxTight(level, boss.position());
    }

    public void initRecalled(SeepingSoulEntity soul, int defeatedCount) {
        this.entityData.set(SPAWN_POINT, soul.blockPosition().below(RECALL_POSITION_OFFSET_Y));
        this.defeatedCount = defeatedCount;
        this.setPlayerDefeatedUUIDs(soul.getPlayerDefeatedUUIDs());
    }

    public static void spawnRecallFxTight(ServerLevel level, Vec3 center) {
        double x0 = center.x;
        double y0 = center.y + 1.8F;
        double z0 = center.z;

        // 1.5 x 1.5 area => x/z in [-0.75 .. 0.75]
        double radius = 1.7;

        // Poof burst (fast)
        for (int i = 0; i < 25; i++) {
            double x = x0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;
            double z = z0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;
            double y = y0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;

            double vx = (level.random.nextDouble() * 2.0 - 1.0) * 0.10;
            double vy = (level.random.nextDouble() * 2.0 - 1.0) * 0.10;
            double vz = (level.random.nextDouble() * 2.0 - 1.0) * 0.10;

            level.sendParticles(ParticleTypes.POOF, x, y, z, 1, vx, vy, vz, 0.0);
        }

        // Soul particles (floaty)
        for (int i = 0; i < 18; i++) {
            double x = x0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;
            double z = z0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;
            double y = y0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;

            double vx = (level.random.nextDouble() * 2.0 - 1.0) * 0.025;
            double vy = (level.random.nextDouble() * 2.0 - 1.0) * 0.025;
            double vz = (level.random.nextDouble() * 2.0 - 1.0) * 0.025;

            level.sendParticles(ParticleTypes.SOUL, x, y, z, 1, vx, vy, vz, 0.0);
        }
    }


    public static void disperseStatic(ServerLevel level, SeepingSoulEntity soul, int defeatedCount) {
        // 5x5x5 centered on the soul (radius 2)
        final BlockPos center = soul.blockPosition();
        final int radius = 2;

        // Destroy preserved blocks only within the 5x5x5 cube
        BlockPos.betweenClosedStream(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        ).forEach(pos -> {
            if (isChaosSpawnerPreservedBlock(level, pos)) {
                level.destroyBlock(pos, false);
            }
        });

        BlockPos.betweenClosedStream(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        ).forEach(pos -> {
            if (isChaosSpawnerPreservedBlock(level, pos)) {
                level.destroyBlock(pos, false);
            }
        });

        // Particle + sound feedback at center (nice "disperse" feel)
        playDisperseEffects(level, center);
    }

    private static void playDisperseEffects(ServerLevel level, BlockPos center) {
        double x = center.getX() + 0.5;
        double y = center.getY() + 0.5;
        double z = center.getZ() + 0.5;

        // A little sound helps sell it (optional)
        level.playSound(null, x, y, z, DNLSounds.CHAOS_SPAWNER_SHOCKWAVE.get(), SoundSource.BLOCKS, 1.0F, 1.2F);

        // Explosion-like burst
        level.sendParticles(ParticleTypes.EXPLOSION, x, y, z,
                1, 0.0, 0.0, 0.0, 0.0);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z,
                1, 0.0, 0.0, 0.0, 0.0);

        // Mixed soul + smoke-ish particles
        level.sendParticles(ParticleTypes.SOUL, x, y, z,
                60, 0.9, 0.9, 0.9, 0.05);

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z,
                25, 0.6, 0.6, 0.6, 0.03);

        level.sendParticles(ParticleTypes.POOF, x, y, z,
                30, 0.8, 0.8, 0.8, 0.03);

        // Optional: block break event (vanilla "pop")
        // level.levelEvent(2001, center, 0);
    }

    private static boolean isChaosSpawnerPreservedBlock(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);

        return state.is(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_BARRIER_EDGE.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_BARRIER_VERTEX.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_EDGE.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_DIAMOND_EDGE.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_DIAMOND_VERTEX.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_BROKEN_EDGE.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_BROKEN_DIAMOND_EDGE.get())
                || state.is(DNLBlocks.CHAOS_SPAWNER_BROKEN_DIAMOND_VERTEX.get());
    }


    public enum State {
        SLEEPING,
        AWAKENING,
        IDLE,
        SUMMON_MOB,
        SHOOT_GHOST_BULLET_SINGLE,
        SHOOT_GHOST_BULLET_BURST,
        PUSH,
        DEATH;

        private State() {}
    }

    private static final ImmutableList<BlockPos> FRAME_POSITIONS = ImmutableList.of(
            new BlockPos(2, 2, 2),
            new BlockPos(2, 2, 1),
            new BlockPos(2, 2, 0),
            new BlockPos(2, 2, -1),
            new BlockPos(2, 2, -2),
            new BlockPos(-2, 2, 2),
            new BlockPos(-2, 2, 1),
            new BlockPos(-2, 2, 0),
            new BlockPos(-2, 2, -1),
            new BlockPos(-2, 2, -2),
            new BlockPos(1, 2, 2),
            new BlockPos(0, 2, 2),
            new BlockPos(-1, 2, 2),
            new BlockPos(1, 2, -2),
            new BlockPos(0, 2, -2),
            new BlockPos(-1, 2, -2),
            new BlockPos(2, -2, 2),
            new BlockPos(2, -2, 1),
            new BlockPos(2, -2, 0),
            new BlockPos(2, -2, -1),
            new BlockPos(2, -2, -2),
            new BlockPos(-2, -2, 2),
            new BlockPos(-2, -2, 1),
            new BlockPos(-2, -2, 0),
            new BlockPos(-2, -2, -1),
            new BlockPos(-2, -2, -2),
            new BlockPos(1, -2, 2),
            new BlockPos(0, -2, 2),
            new BlockPos(-1, -2, 2),
            new BlockPos(1, -2, -2),
            new BlockPos(0, -2, -2),
            new BlockPos(-1, -2, -2),
            new BlockPos(2, 1, 2),
            new BlockPos(2, 0, 2),
            new BlockPos(2, -1, 2),
            new BlockPos(2, 1, -2),
            new BlockPos(2, 0, -2),
            new BlockPos(2, -1, -2),
            new BlockPos(-2, 1, -2),
            new BlockPos(-2, 0, -2),
            new BlockPos(-2, -1, -2),
            new BlockPos(-2, -1, 2),
            new BlockPos(-2, 0, 2),
            new BlockPos(-2, -1, 2)
    );

    private ImmutableList<BlockPos> FRAME_POS_NORTH = ImmutableList.of(
            new BlockPos(0, 2, -2),
            new BlockPos(-1, 2, -2),
            new BlockPos(-2, 2, -2),
            new BlockPos(-2, 1, -2),
            new BlockPos(-2, 0, -2),
            new BlockPos(-2, -1, -2),
            new BlockPos(-2, -2, -2),
            new BlockPos(-1, -2, -2),
            new BlockPos(0, -2, -2),
            new BlockPos(1, -2, -2),
            new BlockPos(2, -2, -2),
            new BlockPos(2, -1, -2),
            new BlockPos(2, 0, -2),
            new BlockPos(2, 1, -2),
            new BlockPos(2, 2, -2),
            new BlockPos(1, 2, -2)
    );

    private ImmutableList<BlockPos> FRAME_POS_EAST = ImmutableList.of(
            new BlockPos(2, 2, 0),
            new BlockPos(2, 2, -1),
            new BlockPos(2, 2, -2),
            new BlockPos(2, 1, -2),
            new BlockPos(2, 0, -2),
            new BlockPos(2, -1, -2),
            new BlockPos(2, -2, -2),
            new BlockPos(2, -2, -1),
            new BlockPos(2, -2, 0),
            new BlockPos(2, -2, 1),
            new BlockPos(2, -2, 2),
            new BlockPos(2, -1, 2),
            new BlockPos(2, 0, 2),
            new BlockPos(2, 1, 2),
            new BlockPos(2, 2, 2),
            new BlockPos(2, 2, 1)
    );

    private ImmutableList<BlockPos> FRAME_POS_SOUTH = ImmutableList.of(
            new BlockPos(0, 2, 2),
            new BlockPos(1, 2, 2),
            new BlockPos(2, 2, 2),
            new BlockPos(2, 1, 2),
            new BlockPos(2, 0, 2),
            new BlockPos(2, -1, 2),
            new BlockPos(2, -2, 2),
            new BlockPos(1, -2, 2),
            new BlockPos(0, -2, 2),
            new BlockPos(-1, -2, 2),
            new BlockPos(-2, -2, 2),
            new BlockPos(-2, -1, 2),
            new BlockPos(-2, 0, 2),
            new BlockPos(-2, 1, 2),
            new BlockPos(-2, 2, 2),
            new BlockPos(-1, 2, 2)
    );

    private ImmutableList<BlockPos> FRAME_POS_WEST = ImmutableList.of(
            new BlockPos(-2, 2, 0),
            new BlockPos(-2, 2, 1),
            new BlockPos(-2, 2, 2),
            new BlockPos(-2, 1, 2),
            new BlockPos(-2, 0, 2),
            new BlockPos(-2, -1, 2),
            new BlockPos(-2, -2, 2),
            new BlockPos(-2, -2, 1),
            new BlockPos(-2, -2, 0),
            new BlockPos(-2, -2, -1),
            new BlockPos(-2, -2, -2),
            new BlockPos(-2, -1, -2),
            new BlockPos(-2, 0, -2),
            new BlockPos(-2, 1, -2),
            new BlockPos(-2, 2, -2),
            new BlockPos(-2, 2, -1)
    );

    private ImmutableList<BlockPos> FRAME_POS_UP = ImmutableList.of(
            new BlockPos(0, 2, -2),
            new BlockPos(1, 2, -2),
            new BlockPos(2, 2, -2),
            new BlockPos(2, 2, -1),
            new BlockPos(2, 2, 0),
            new BlockPos(2, 2, 1),
            new BlockPos(2, 2, 2),
            new BlockPos(1, 2, 2),
            new BlockPos(0, 2, 2),
            new BlockPos(-1, 2, 2),
            new BlockPos(-2, 2, 2),
            new BlockPos(-2, 2, 1),
            new BlockPos(-2, 2, 0),
            new BlockPos(-2, 2, -1),
            new BlockPos(-2, 2, -2),
            new BlockPos(-1, 2, -2)
    );

    private ImmutableList<BlockPos> FRAME_POS_DOWN = ImmutableList.of(
            new BlockPos(0, -2, -2),
            new BlockPos(1, -2, -2),
            new BlockPos(2, -2, -2),
            new BlockPos(2, -2, -1),
            new BlockPos(2, -2, 0),
            new BlockPos(2, -2, 1),
            new BlockPos(2, -2, 2),
            new BlockPos(1, -2, 2),
            new BlockPos(0, -2, 2),
            new BlockPos(-1, -2, 2),
            new BlockPos(-2, -2, 2),
            new BlockPos(-2, -2, 1),
            new BlockPos(-2, -2, 0),
            new BlockPos(-2, -2, -1),
            new BlockPos(-2, -2, -2),
            new BlockPos(-1, -2, -2)
    );
}
