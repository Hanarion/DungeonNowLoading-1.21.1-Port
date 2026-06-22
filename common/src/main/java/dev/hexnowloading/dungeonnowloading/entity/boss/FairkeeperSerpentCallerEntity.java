package dev.hexnowloading.dungeonnowloading.entity.boss;

import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.entity.PreserverBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.VertexPillarBlockEntity;
import dev.hexnowloading.dungeonnowloading.config.BossConfig;
import dev.hexnowloading.dungeonnowloading.entity.ai.BossTargetSelectorGoal;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import dev.hexnowloading.dungeonnowloading.entity.misc.SpecialItemEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.*;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CFadeInTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CFadeOutBackgroundMusicSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStartTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStopTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.sound.TickingSoundTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;


public class FairkeeperSerpentCallerEntity extends Entity {

    private static final EntityDataAccessor<FairkeeperSerpentCallerAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityStates.FAIRKEEPER_SERPENT_CALLER_ANIMATION_STATE);
    private static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> BOROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> OUROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> HORIZONTAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VERTICAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState activeAnimationState = new AnimationState();

    private static final int SPAWN_OFFSET_X = 20;
    private static final int SPAWN_OFFSET_Y = 15;
    private final int BEHIND_BLOCK_SPAWN_OFFSET = 5;
    private static final int ARENA_SIZE = 36;
    private final int MAX_SCUTTLE_COUNT = 6;
    private DamageSource lastDamageSource;
    private FairkeeperBorosEntity boros;
    private FairkeeperOurosEntity ouros;
    private boolean hasAddedDesperateMove;

    private boolean burrowing;
    private boolean borosWaitingForCommand;
    private boolean ourosWaitingForCommand;
    private int isBorosDefeated;
    private int isOurosDefeated;
    private UUID pendingBorosUUID;
    private UUID pendingOurosUUID;
    private int activationTick;
    private int musicTick;
    private Set<UUID> playerUUIDs;
    private Set<UUID> minionUUIDs;
    private Set<UUID> playerDefeatedUUIDs;
    private int defeatedCount = 0;
    private int modifiedDefeatedCount = 0;
    private static final UUID RECALL_ATTACK_MOD_UUID = UUID.fromString("3f9c1e2b-8a64-4d0a-9e2c-6d7f1c4a9a12");
    private static final UUID RECALL_HEALTH_UUID = UUID.fromString("b27a4d91-1c58-4a6e-9b0f-2f8e6c3d5a44");
    private ItemStack recallItemStack;


    private WeightBaseMoveSet<Pair<FairkeeperBorosEntity.FairkeeperBorosState, FairkeeperOurosEntity.FairkeeperOurosState>> introMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<Pair<FairkeeperBorosEntity.FairkeeperBorosState, FairkeeperOurosEntity.FairkeeperOurosState>> comboMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<Pair<FairkeeperBorosEntity.FairkeeperBorosState, FairkeeperOurosEntity.FairkeeperOurosState>> directMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperOurosEntity.FairkeeperOurosState> ourosMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperOurosEntity.FairkeeperOurosState> ourosPillarMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperBorosEntity.FairkeeperBorosState> borosMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperBorosEntity.FairkeeperBorosState> borosArrowMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperBorosEntity.FairkeeperBorosState> borosPursueMoveSet = new WeightBaseMoveSet<>();

    private ExhaustionTracker ourosExhaustion = new ExhaustionTracker(10F);
    private ExhaustionTracker borosExhaustion = new ExhaustionTracker(10F);

    private static final Map<FairkeeperOurosEntity.FairkeeperOurosState, Float> OUROS_EXHAUSTION_MAP = Map.of(
            FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE, 2F,
            FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_MORE_SCUTTLES, 2F,
            FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB, 1F,
            FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_TRIPLE_VERTEX_ORB, 2F,
            FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_VERTEX_DOMAIN, 5F,
            FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_CENTER, 2F,
            FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_INNER, 2F,
            FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_OUTER, 2F,
            FairkeeperOurosEntity.FairkeeperOurosState.DESPERATE, 8F
    );

    private static final Map<FairkeeperBorosEntity.FairkeeperBorosState, Float> BOROS_EXHAUSTION_MAP = Map.of(
            FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_LINE_FAST, 2F,
            FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_SMALL_CIRCLE, 2F,
            FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_LARGE_CIRCLE, 2F,
            FairkeeperBorosEntity.FairkeeperBorosState.TACKLE, 2F,
            FairkeeperBorosEntity.FairkeeperBorosState.TACKLE_FAST, 2F,
            FairkeeperBorosEntity.FairkeeperBorosState.FLAME_TACKLE, 4F,
            FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_AND_SHOOT_TRIPLE_ARROW, 2F,
            FairkeeperBorosEntity.FairkeeperBorosState.DESPERATE, 8F
    );

    public FairkeeperSerpentCallerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.playerUUIDs = new HashSet<>();
        this.minionUUIDs = new HashSet<>();
        this.playerDefeatedUUIDs = new HashSet<>();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ANIMATION_STATE, FairkeeperSerpentCallerAnimationState.NONE);
        builder.define(ACTIVATED, false);
        builder.define(BOROS_UUID, Optional.empty());
        builder.define(OUROS_UUID, Optional.empty());
        builder.define(HORIZONTAL_OFFSET, 0);
        builder.define(VERTICAL_OFFSET, 0);
        builder.define(PHASE, 0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("Activated", isActivated());
        if (this.getBorosId() != null) {
            compoundTag.putUUID("BorosUUID", this.getBorosId());
        }
        if (this.getOurosId() != null) {
            compoundTag.putUUID("OurosUUID", this.getOurosId());
        }
        compoundTag.putInt("IsBorosDefeated", this.isBorosDefeated);
        compoundTag.putInt("IsOurosDefeated", this.isOurosDefeated);
        compoundTag.putInt("HorizontalOffset", this.getHorizontalOffset());
        compoundTag.putInt("VerticalOffset", this.getVerticalOffset());
        compoundTag.putInt("Phase", this.getPhase());
        ListTag listTag = new ListTag();
        CompoundTag uuidCompoundTag = new CompoundTag();
        Iterator<UUID> var = this.playerUUIDs.iterator();
        for (int i = 0; var.hasNext(); i++) {
            listTag.add(uuidCompoundTag);
            uuidCompoundTag.putUUID("PlayerUUID" + i, var.next());
        }
        compoundTag.put("PlayerUUIDs", listTag);
        SeepingSoulEntity.writeRecallNBT(compoundTag, this.playerDefeatedUUIDs, this.defeatedCount, this.modifiedDefeatedCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.entityData.set(ACTIVATED, compoundTag.getBoolean("Activated"));
        if (compoundTag.hasUUID("BorosUUID")) {
            this.setBorosId(compoundTag.getUUID("BorosUUID"));
            this.pendingBorosUUID = this.getBorosId();
        }
        if (compoundTag.hasUUID("OurosUUID")) {
            this.setOurosId(compoundTag.getUUID("OurosUUID"));
            this.pendingOurosUUID = this.getOurosId();
        }
        this.isOurosDefeated = compoundTag.getInt("IsOurosDefeated");
        this.isBorosDefeated = compoundTag.getInt("IsBorosDefeated");
        this.entityData.set(HORIZONTAL_OFFSET, compoundTag.getInt("HorizontalOffset"));
        this.entityData.set(VERTICAL_OFFSET, compoundTag.getInt("VerticalOffset"));
        this.entityData.set(PHASE, compoundTag.getInt("Phase"));
        if (compoundTag.contains("PlayerUUIDs", CompoundTag.TAG_LIST)) {
            ListTag listTag = compoundTag.getList("PlayerUUIDs", CompoundTag.TAG_COMPOUND);
            for (int a = 0; a < listTag.size(); ++a) {
                CompoundTag compoundTag1 = listTag.getCompound(a);
                this.playerUUIDs.add(compoundTag1.getUUID("PlayerUUID" + a));
            }
        }
        SeepingSoulEntity.RecallData data = SeepingSoulEntity.readRecallNBT(compoundTag);

        this.playerDefeatedUUIDs.clear();
        this.playerDefeatedUUIDs.addAll(data.playerDefeatedUUIDs());

        this.defeatedCount = data.defeatedCount();
        this.modifiedDefeatedCount = data.modifiedDefeatedCount();
    }

    public void startBossFight(ItemStack recallItemStack) {
        this.recallItemStack = recallItemStack;
        this.modifiedDefeatedCount = SeepingSoulEntity.getModifiedDefeatedCount(this.defeatedCount, recallItemStack);
        this.activationTick = 60;
        this.isBorosDefeated = 0;
        this.isOurosDefeated = 0;
        this.ourosExhaustion.resetExhaustion();
        this.borosExhaustion.resetExhaustion();
        this.playSound(DNLSounds.FAIRKEEPER_SERPENT_CALLER_ACTIVATED.get(), 3.0F, 1.0F);
        this.playBossMusic();
        this.playSound(DNLSounds.FAIRKEEPERS_INTRO.get(), 3.0F, 1.0F);
        this.clearAllMoveSet();
        this.setActivated(true);
        this.setOffsets(SPAWN_OFFSET_X, SPAWN_OFFSET_Y);
        this.transitionTo(FairkeeperSerpentCallerAnimationState.ACTIVE);
        AABB bossArena = new AABB(this.blockPosition()).inflate(ARENA_SIZE);
        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, bossArena);
        for (ServerPlayer p : players) {
            playerUUIDs.add(p.getUUID());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isBorosDefeated <= 0 && this.boros == null && this.pendingBorosUUID != null) {
            Entity entity = ((ServerLevel) this.level()).getEntity(pendingBorosUUID);
            if (entity instanceof FairkeeperBorosEntity borosEntity) {
                this.boros = borosEntity;
                this.pendingBorosUUID = null;
            }
        }
        if (this.isOurosDefeated <= 0 && this.ouros == null && this.pendingOurosUUID != null) {
            Entity entity = ((ServerLevel) this.level()).getEntity(pendingOurosUUID);
            if (entity instanceof FairkeeperOurosEntity ourosEntity) {
                this.ouros = ourosEntity;
                this.pendingOurosUUID = null;
            }
        }
        if (!this.level().isClientSide) {
            if (this.entityData.get(ANIMATION_STATE) == FairkeeperSerpentCallerAnimationState.NONE) {
                this.transitionTo(FairkeeperSerpentCallerAnimationState.IDLE);
            }
        }
        if (this.isActivated() && !this.level().isClientSide) {
            musicTick++;
            if (this.musicTick >= 3242) {
                this.musicTick = 0;
                this.playLoopMusic();
            }
            switch (this.getPhase()) {
                case 0:
                    if (this.activationTick > 0) {
                        this.activationTick--;
                        break;
                    }
                    this.summonBosses();
                    this.setPhase(1);
                    this.burrowing = true;
                    spawnEmergingParticles();
                    //this.activationTick = 60;
                    break;
                case 1:
                    if (burrowing) {
                        spawnBurrowingParticles();
                    }
                    if (this.isBorosWaitingForCommand() && this.isOurosWaitingForCommand()) {
                        this.introMoveSet();
                        this.burrowing = false;
                    }

                    if (this.activationTick > 0) {
                        this.activationTick--;
                        break;
                    }

                    if (this.getHealthRatio() < 0.75F) {
                        this.setPhase(2);
                        this.clearAllMoveSet();
                        break;
                    }

                    if (this.isAllPlayersInBound()) {
                        this.activationTick = 10;
                        break;
                    }

                    this.resetBosses();
                    break;
                case 2:

                    if (this.isBorosWaitingForCommand()) {
                        this.commandBorosPhase2();
                    }
                    if (this.isOurosWaitingForCommand()) {
                        this.commandOurosPhase2();
                    }

                    if (this.activationTick > 0) {
                        this.activationTick--;
                        break;
                    }

                    if (this.isOurosDefeated > 2 || this.isBorosDefeated > 2) {
                        if (this.isBorosDefeated > 2) {
                            fadeInOurosMusic();
                        } else {
                            fadeInBorosMusic();
                        }
                        this.setPhase(3);
                        this.clearAllMoveSet();
                        break;
                    }

                    if (this.isAllPlayersInBound()) {
                        this.activationTick = 10;
                        break;
                    }

                    this.resetBosses();
                    break;
                case 3:

                    if (this.isBorosWaitingForCommand()) {
                        this.commandBorosPhase2();
                    }
                    if (this.isOurosWaitingForCommand()) {
                        this.commandOurosPhase2();
                    }

                    if (this.activationTick > 0) {
                        this.activationTick--;
                        break;
                    }

                    if (this.isBorosDefeated > 2 && this.isOurosDefeated > 2) {
                        this.defeatedBosses();
                        this.setPhase(4);
                        break;
                    }

                    if (this.isAllPlayersInBound()) {
                        this.activationTick = 10;
                        break;
                    }

                    this.resetBosses();
                    break;
                case 4:
            }
        }
    }

    private float getHealthRatio() {
        if (this.boros == null || this.ouros == null) {
            return 0.0F;
        }
        return (float) ((this.ouros.getHealth() + this.boros.getHealth()) / (this.ouros.getAttributeValue(Attributes.MAX_HEALTH) + this.boros.getAttributeValue(Attributes.MAX_HEALTH)));
    }

    private static final ImmutableList<FairkeeperBorosEntity.FairkeeperBorosState> BOROS_WAITING_STATES = ImmutableList.of(
            FairkeeperBorosEntity.FairkeeperBorosState.IDLE
    );

    private static final ImmutableList<FairkeeperOurosEntity.FairkeeperOurosState> OUROS_WAITING_STATES = ImmutableList.of(
            FairkeeperOurosEntity.FairkeeperOurosState.IDLE
    );

    private void introMoveSet() {

        BossTargetSelectorGoal.changeTarget(this.boros);
        BossTargetSelectorGoal.changeTarget(this.ouros);

        if (this.boros.getTarget() == null && this.ouros.getTarget() == null) {
            return;
        }

        if (introMoveSet.isEmpty()) {
            introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.IDLE, FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE), 1, 2, 2);
            introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.IDLE, FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB), 1, 0, 0);
            //introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.IDLE, FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_TRIPLE_VERTEX_ORB), 1, 1, 0);
            introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.IDLE, FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_RANDOM), 1, 2, 0);
            introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_LINE, FairkeeperOurosEntity.FairkeeperOurosState.IDLE), 1, 2, 1);
            introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.TACKLE, FairkeeperOurosEntity.FairkeeperOurosState.IDLE), 1, 2, 0);
            introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_AND_SHOOT_SINGLE_ARROW, FairkeeperOurosEntity.FairkeeperOurosState.IDLE), 1, 1, 0);
            //introMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.FLAME_PURSUING, FairkeeperOurosEntity.FairkeeperOurosState.IDLE), 1, 2, 0);
        }

        if (directMoveSet.isEmpty()) {
            directMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.IDLE, FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB), 1, 0, 0);
            directMoveSet.addMove(new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_ABOVE, FairkeeperOurosEntity.FairkeeperOurosState.IDLE), 1, 0, 0);
        }

        if (ourosPillarMoveSet.isEmpty()) {
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_OUTER), 1, 0, 0);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_CENTER), 1, 0, 1);
        }

        ourosBorosAssignState();
    }

    private void commandBorosPhase2() {

        BossTargetSelectorGoal.changeTarget(this.boros);

        if (this.boros.getTarget() == null) {
            return;
        }

        if (this.borosMoveSet.isEmpty()) {
            borosMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_RANDOM, 1, 0, 1);
            borosMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_RANDOM, 2, 0, 0);
        }

        if (this.borosArrowMoveSet.isEmpty()) {
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_LINE_FAST, 1, 1, 2);
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_SMALL_CIRCLE, 1, 1, 0);
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_LARGE_CIRCLE, 1, 1, 0);
        }

        if (this.borosPursueMoveSet.isEmpty()) {
            borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.TACKLE, 3, 1, 1);
            borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.FLAME_TACKLE, 2, 1, 0);
            borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_AND_SHOOT_TRIPLE_ARROW, 3 , 0, 1);
        }

        borosAssignState();
    }

    private void commandOurosPhase2() {

        BossTargetSelectorGoal.changeTarget(this.ouros);

        if (this.ouros.getTarget() == null) {
            return;
        }

        if (this.ourosMoveSet.isEmpty()) {
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE, 3, 2, 3);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB, 1, 0, 3);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_TRIPLE_VERTEX_ORB, 3, 2, 2);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_VERTEX_DOMAIN, 6, 4, 1);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_RANDOM, 3, 0);
        }

        if (ourosPillarMoveSet.isEmpty()) {
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_CENTER), 1, 1, 0);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_INNER), 1, 1, 1);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LINE_OUTER), 1, 1, 0);
        }

        ourosAssignState();
    }

    private void ourosBorosAssignState() {
        Pair<FairkeeperBorosEntity.FairkeeperBorosState, FairkeeperOurosEntity.FairkeeperOurosState> statePair;

        cleanMinionList();

        if (isAboveBoros()) {
            statePair = directMoveSet.selectMove();
        } else {
            statePair = introMoveSet.selectMove();
        }

        if (statePair.getB().equals(FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_RANDOM)) {
            statePair = new Pair<>(statePair.getA(), ourosPillarMoveSet.selectMove());
        }

        if (statePair.getB().equals(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE)) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            long scuttleCount = this.minionUUIDs.stream()
                    .map(serverLevel::getEntity)
                    .filter(entity -> entity instanceof ScuttleEntity)
                    .count();

            if (scuttleCount > MAX_SCUTTLE_COUNT) {
                statePair = introMoveSet.selectMoveWithoutCooldownReduction();
            }
        }

        if (this.boros.getTarget() == null) {
            statePair = new Pair<>(FairkeeperBorosEntity.FairkeeperBorosState.IDLE, statePair.getB());
        } else if (this.ouros.getTarget() == null) {
            statePair = new Pair<>(statePair.getA(), FairkeeperOurosEntity.FairkeeperOurosState.IDLE);
        }


        //introMoveSet.displayAllStats();
        Entity boros = this.getBoros();
        Entity ouros = this.getOuros();
        boolean isBorosWaiting = false;
        boolean isOurosWaiting = false;
        if (boros != null && ouros != null) {
            ((FairkeeperBorosEntity) boros).setState(statePair.getA());
            ((FairkeeperOurosEntity) ouros).setState(statePair.getB());
            if (BOROS_WAITING_STATES.contains(statePair.getA())) {
                isBorosWaiting = true;
            }
            if (OUROS_WAITING_STATES.contains(statePair.getB())) {
                isOurosWaiting = true;
            }
        }
        this.setBorosWaitingForCommand(isBorosWaiting);
        this.setOurosWaitingForCommand(isOurosWaiting);
    }

    private void borosAssignState() {
        if (this.boros == null) {
            return;
        }

        FairkeeperBorosEntity.FairkeeperBorosState state;

        cleanMinionList();

        float exhaustionPercent = this.borosExhaustion.getExhaustionPercent();
        float random = this.random.nextFloat();

        if (isAboveBoros()) {
            state = FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_ABOVE;
        } else if (exhaustionPercent > 0.5F && exhaustionPercent > random) {
            state = FairkeeperBorosEntity.FairkeeperBorosState.EXHAUSTED;
        } else {
            state = borosMoveSet.selectMove();
        }

        if (state == null) {
            state = FairkeeperBorosEntity.FairkeeperBorosState.IDLE;
        }

        if (state.equals(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_RANDOM)) {
            state = borosArrowMoveSet.selectMove();
        }

        if (state.equals(FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_RANDOM)) {
            state = borosPursueMoveSet.selectMove();
        }

        if (this.getPhase() > 2 && !this.hasAddedDesperateMove) {
            state = FairkeeperBorosEntity.FairkeeperBorosState.DESPERATE;
            this.hasAddedDesperateMove = true;
            this.borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.DESPERATE, 2, 3, 3);
            this.borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.TACKLE_FAST, 3, 1, 1);
            this.borosPursueMoveSet.removeMove(FairkeeperBorosEntity.FairkeeperBorosState.TACKLE);
        }

        Entity boros = this.getBoros();
        if (boros instanceof FairkeeperBorosEntity borosEntity) {
            if (state == null) {
                borosEntity.setState(FairkeeperBorosEntity.FairkeeperBorosState.IDLE);
                borosEntity.setAttackTick(60);
                this.ourosMoveSet.reduceAllCooldown();
            } else if (state == FairkeeperBorosEntity.FairkeeperBorosState.EXHAUSTED) {
                borosEntity.setState(FairkeeperBorosEntity.FairkeeperBorosState.IDLE);
                borosEntity.setAttackTick((int) (this.borosExhaustion.getExhaustionPercent() * 200));
                this.borosExhaustion.resetExhaustion();
            } else {
                ((FairkeeperBorosEntity) boros).setState(state);
                float exhaustionCost = BOROS_EXHAUSTION_MAP.getOrDefault(state, 0F);
                this.borosExhaustion.addExhaustion(exhaustionCost);
            }
        }
        this.setBorosWaitingForCommand(false);
    }

    private void ourosAssignState() {
        if (this.ouros == null) {
            return;
        }

        FairkeeperOurosEntity.FairkeeperOurosState state;

        cleanMinionList();

        float exhaustionPercent = this.ourosExhaustion.getExhaustionPercent();

        if (isAboveBoros()) {
            state = FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB;
        } else if (exhaustionPercent > 0.5F && exhaustionPercent > this.random.nextFloat()) {
            state = FairkeeperOurosEntity.FairkeeperOurosState.EXHAUSTED;
        } else {
            state = ourosMoveSet.selectMove();
        }

        if (state == null) {
            state = FairkeeperOurosEntity.FairkeeperOurosState.IDLE;
        }

        if (state.equals(FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_RANDOM)) {
            state = ourosPillarMoveSet.selectMove();
        }

        if (state.equals(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE) || state.equals(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_MORE_SCUTTLES)) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            long scuttleCount = this.minionUUIDs.stream()
                    .map(serverLevel::getEntity)
                    .filter(entity -> entity instanceof ScuttleEntity)
                    .count();

            if (scuttleCount > MAX_SCUTTLE_COUNT) {
                state = ourosMoveSet.selectMoveWithoutCooldownReduction();
            }
        }

        if (this.getPhase() > 2 && !this.hasAddedDesperateMove) {
            state = FairkeeperOurosEntity.FairkeeperOurosState.DESPERATE;
            this.hasAddedDesperateMove = true;
            this.ourosMoveSet.increaseAllCooldown();
            int previousTotalWeight = ourosMoveSet.getTotalWeight();
            this.ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.DESPERATE, previousTotalWeight * 2, 3, 3);
            this.ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_MORE_SCUTTLES, 3, 3, 0);
            this.ourosMoveSet.removeMove(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE);
        }

        Entity ouros = this.getOuros();
        if (ouros instanceof FairkeeperOurosEntity ourosEntity) {
            if (state == null) {
                ourosEntity.setState(FairkeeperOurosEntity.FairkeeperOurosState.IDLE);
                ourosEntity.setAttackTick(60);
                this.ourosMoveSet.reduceAllCooldown();
            } else if (state == FairkeeperOurosEntity.FairkeeperOurosState.EXHAUSTED) {
                ourosEntity.setState(FairkeeperOurosEntity.FairkeeperOurosState.IDLE);
                ourosEntity.setAttackTick((int) (this.ourosExhaustion.getExhaustionPercent() * 200));
                this.ourosExhaustion.resetExhaustion();
            } else {
                ((FairkeeperOurosEntity) ouros).setState(state);
                float exhaustionCost = OUROS_EXHAUSTION_MAP.getOrDefault(state, 0F);
                this.ourosExhaustion.addExhaustion(exhaustionCost);
            }
        }
        this.setOurosWaitingForCommand(false);
    }

    private void clearAllMoveSet() {
        introMoveSet.clear();
        comboMoveSet.clear();
        directMoveSet.clear();
        ourosMoveSet.clear();
        ourosPillarMoveSet.clear();
        borosMoveSet.clear();
        borosArrowMoveSet.clear();
        borosPursueMoveSet.clear();
        hasAddedDesperateMove = false;
    }

    private boolean isAboveBoros() {
        return this.boros != null && this.boros.getTarget() != null && this.boros.getTarget().getY() > this.boros.getY() + 3;
    }

    private void defeatedBosses() {
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), 1.0F, Level.ExplosionInteraction.BLOCK);
        this.dropDeathLoot();
        this.removeAllMinions();
        this.removePillars();
        this.spawnSeepingSoul();
        this.remove(RemovalReason.KILLED);
    }

    private void dropDeathLoot() {
        if (!BossConfig.TOGGLE_MULTIPLAYER_LOOT.get()) {
            this.spawnLootTableItems(this.lastDamageSource, true, false, null);
        }

        if (this.playerUUIDs.isEmpty()) return;

        boolean ignoreGate = this.modifiedDefeatedCount > 0;

        int drops = Math.max(1, (this.modifiedDefeatedCount - this.defeatedCount) + 1);

        for (UUID playerUUID : this.playerUUIDs) {

            dropGreatXpBottlesForPlayer(playerUUID);

            if (this.modifiedDefeatedCount >= 4) {
                dropTempleOfDualityTrophyForPlayer(playerUUID);
            }

            // Gate: only first-clear gets loot (only on non-ignored runs)
            if (!ignoreGate) {
                if (this.playerDefeatedUUIDs.contains(playerUUID)) {
                    continue;
                }
            }

            // Drop multiple times for this player
            for (int i = 0; i < drops; i++) {
                this.spawnLootTableItems(this.lastDamageSource, true, true, playerUUID);
            }

            // Mark claimed only for gated runs
            if (!ignoreGate) {
                this.playerDefeatedUUIDs.add(playerUUID);
            }
        }
    }

    private void dropTempleOfDualityTrophyForPlayer(UUID playerUuid) {
        if (this.level().isClientSide) return;

        ItemStack trophy = new ItemStack(DNLBlocks.TEMPLE_OF_DUALITY_TROPHY.get());
        this.spawnSpecialItemEntity(trophy, 0.0F, playerUuid);
    }

    private void dropGreatXpBottlesForPlayer(UUID playerUuid) {
        if (this.level().isClientSide) return;

        int recallCount = Math.max(0, this.modifiedDefeatedCount);
        int total = 15 * (recallCount + 1);

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

    private void spawnSeepingSoul() {
        // Spawn at boss *starting position*
        BlockPos start = this.blockPosition();

        SeepingSoulEntity soul = DNLEntityTypes.SEEPING_SOUL.get().create(this.level());
        if (soul == null) return;

        soul.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, this.getYRot(), 0);

        // bossId should match the recall registry id you use for Chaos Spawner
        soul.setBossId(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "fairkeeper_serpent_caller"));

        int count = SeepingSoulEntity.getRecallCountForSeepingSoul(this.defeatedCount, this.modifiedDefeatedCount);

        soul.setPlayerDefeatedUUIDs(this.playerDefeatedUUIDs);

        // next run is +1
        soul.setDefeatedCount(Mth.clamp(count, 0, 100));

        this.level().addFreshEntity(soul);

        soul.playSpawnAnimation();
    }

    private static void removePreservers(ServerLevel level, BlockPos soulPos) {
        BlockPos center = soulPos;
        int includeWallPreserver = 1;
        int arenaSize = ARENA_SIZE + includeWallPreserver;

        Map<BlockPos, BlockEntity> map = new HashMap<>();

        int minX = center.getX() - arenaSize;
        int minZ = center.getZ() - arenaSize;
        int maxX = center.getX() + arenaSize;
        int maxZ = center.getZ() + arenaSize;

        int chunkMinX = SectionPos.blockToSectionCoord(minX);
        int chunkMinZ = SectionPos.blockToSectionCoord(minZ);
        int chunkMaxX = SectionPos.blockToSectionCoord(maxX);
        int chunkMaxZ = SectionPos.blockToSectionCoord(maxZ);

        for (int x = chunkMinX; x <= chunkMaxX; x++) {
            for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                map.putAll(level.getChunk(x, z).getBlockEntities());
            }
        }

        Map<BlockPos, BlockEntity> preserver = map.entrySet().stream()
                .filter(e -> e.getValue() instanceof PreserverBlockEntity)
                .filter(e -> {
                    BlockPos pos = e.getKey();
                    return pos.getX() >= minX && pos.getX() <= maxX &&
                            pos.getZ() >= minZ && pos.getZ() <= maxZ &&
                            Math.abs(pos.getY() - center.getY()) <= arenaSize;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        preserver.keySet().forEach(pos -> level.setBlock(pos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL));
    }

    private void spawnLootTableItems(DamageSource damageSource, boolean b, boolean multiplayer, @Nullable UUID uuid) {
        if (damageSource == null) {
            return;
        }
        ResourceLocation baseResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(DNLEntityTypes.FAIRKEEPER_SERPENT_CALLER.get()).withPrefix("entities/");
        ResourceLocation lootTableResourceLocation = this.isBorosDefeated > this.isOurosDefeated ? baseResourceLocation.withSuffix("/boros") : baseResourceLocation.withSuffix("/ouros");
        LootTable lootTable = this.level().getServer().getLootData().getLootTable(lootTableResourceLocation);
        LootParams.Builder builder = (new LootParams.Builder((ServerLevel) this.level()))
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());
        if (b && damageSource.getEntity() instanceof Player player) {
            builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());
        }
        LootParams lootParams = builder.create(LootContextParamSets.ENTITY);
        if (multiplayer) {
            lootTable.getRandomItems(lootParams, itemStack -> spawnSpecialItemEntity(itemStack, 0.0F, uuid));
        } else {
            lootTable.getRandomItems(lootParams, this::spawnAtLocation);
        }
    }

    private void spawnSpecialItemEntity(ItemStack itemStack, float i, UUID uuid) {
        if (!itemStack.isEmpty() && !this.level().isClientSide && uuid != null) {
            SpecialItemEntity specialItemEntity = new SpecialItemEntity(this.level(), this.getX(), this.getY() + i, this.getZ(), itemStack);
            specialItemEntity.setPickerUUID(uuid);
            specialItemEntity.setDefaultPickUpDelay();
            this.level().addFreshEntity(specialItemEntity);
        }
    }

    private void resetBosses() {
        FairkeeperBorosEntity boros = (FairkeeperBorosEntity) this.getBoros();
        FairkeeperOurosEntity ouros = (FairkeeperOurosEntity) this.getOuros();
        if (boros != null) {
            boros.resetBoss();
        }
        if (ouros != null) {
            ouros.resetBoss();
        }
        this.setActivated(false);
        this.entityData.set(ANIMATION_STATE, FairkeeperSerpentCallerAnimationState.IDLE);
        this.setPhase(0);
        this.activationTick = 0;
        this.musicTick = 0;
        this.playerUUIDs.clear();
        this.removeAllMinions();
        this.removePillars();
        this.stopAllBossMusic();
    }

    public void removePillars() {
        Level level = this.level();
        BlockPos center = this.blockPosition();
        int arenaSize = this.getArenaSize();

        Map<BlockPos, BlockEntity> map = new HashMap<>();

        int minX = center.getX() - arenaSize;
        int minZ = center.getZ() - arenaSize;
        int maxX = center.getX() + arenaSize;
        int maxZ = center.getZ() + arenaSize;

        int chunkMinX = SectionPos.blockToSectionCoord(minX);
        int chunkMinZ = SectionPos.blockToSectionCoord(minZ);
        int chunkMaxX = SectionPos.blockToSectionCoord(maxX);
        int chunkMaxZ = SectionPos.blockToSectionCoord(maxZ);

        for (int x = chunkMinX; x <= chunkMaxX; x++) {
            for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                map.putAll(level.getChunk(x, z).getBlockEntities());
            }
        }

        Map<BlockPos, BlockEntity> filtered = map.entrySet().stream()
                .filter(e -> e.getValue() instanceof VertexPillarBlockEntity)
                .filter(e -> {
                    BlockPos pos = e.getKey();
                    return pos.getX() >= minX && pos.getX() <= maxX &&
                            pos.getZ() >= minZ && pos.getZ() <= maxZ &&
                            Math.abs(pos.getY() - center.getY()) <= arenaSize;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        filtered.keySet().forEach(pos -> level.removeBlock(pos, false));
    }

    private void removeAllMinions() {
        ServerLevel serverLevel = ((ServerLevel) this.level());
        for (UUID minionUUID : this.minionUUIDs) {
            Entity minion = serverLevel.getEntity(minionUUID);
            if (minion != null) {
                SpawnMobUtil.createPoofParticle(serverLevel, minion);
                minion.discard();
            }
        }
        this.minionUUIDs.clear();
    }

    private boolean isAllPlayersInBound() {
        if (!BossConfig.TOGGLE_BOSS_RESET.get()) {
            return false;
        }

        AABB aabb = new AABB(this.blockPosition()).inflate(ARENA_SIZE);
        List<Player> list = this.level().getEntitiesOfClass(Player.class, aabb);
        list.removeIf(player -> !player.isAlive());
        return !list.isEmpty();
    }

    private void spawnEmergingParticles() {
        BlockPos currentPosition = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
        Direction direction = Direction.fromYRot(this.getYRot());
        Direction clockWiseDirection = direction.getClockWise();
        Direction counterClockWiseDirection = direction.getCounterClockWise();

        BlockPos clockWiseTargetPosition = currentPosition
                .relative(clockWiseDirection, this.getHorizontalOffset())
                .below(this.getVerticalOffset());

        BlockPos counterClockWiseTargetPosition = currentPosition
                .relative(counterClockWiseDirection, this.getHorizontalOffset())
                .above(this.getVerticalOffset());

        Vec3 centeredClockWiseTargetPosition = clockWiseTargetPosition.getCenter();

        Vec3 centeredCounterClockWiseTargetPosition = counterClockWiseTargetPosition.getCenter();

        ((ServerLevel) this.level()).sendParticles(
                ParticleTypes.EXPLOSION,
                centeredCounterClockWiseTargetPosition.x, centeredCounterClockWiseTargetPosition.y, centeredCounterClockWiseTargetPosition.z,
                5,
                3.0, 0.0, 3.0, // offset (for spread)
                0 // speed
        );
        ((ServerLevel) this.level()).sendParticles(
                ParticleTypes.EXPLOSION,
                centeredClockWiseTargetPosition.x, centeredClockWiseTargetPosition.y, centeredClockWiseTargetPosition.z,
                5,
                3.0, 0.0, 3.0, // offset (for spread)
                0 // speed
        );
    }

    private void spawnBurrowingParticles() {
        BlockPos currentPosition = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
        Direction direction = Direction.fromYRot(this.getYRot());
        Direction clockWiseDirection = direction.getClockWise();
        Direction counterClockWiseDirection = direction.getCounterClockWise();

        BlockPos clockWiseTargetPosition = currentPosition
                .relative(clockWiseDirection, this.getHorizontalOffset())
                .below(this.getVerticalOffset());

        BlockPos counterClockWiseTargetPosition = currentPosition
                .relative(counterClockWiseDirection, this.getHorizontalOffset())
                .above(this.getVerticalOffset());

        Vec3 centeredClockWiseTargetPosition = clockWiseTargetPosition.getCenter();

        Vec3 centeredCounterClockWiseTargetPosition = counterClockWiseTargetPosition.getCenter();

        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        ((ServerLevel) this.level()).sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, stoneBricks),
                centeredCounterClockWiseTargetPosition.x, centeredCounterClockWiseTargetPosition.y, centeredCounterClockWiseTargetPosition.z,
                10,
                3.0, 3.0, 3.0, // offset (for spread)
                0.05 // speed
        );
        ((ServerLevel) this.level()).sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, stoneBricks),
                centeredClockWiseTargetPosition.x, centeredClockWiseTargetPosition.y, centeredClockWiseTargetPosition.z,
                10,
                3.0, 3.0, 3.0, // offset (for spread)
                0.05 // speed
        );

    }

    private void summonBosses() {
        BlockPos currentPosition = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
        Direction direction = Direction.fromYRot(this.getYRot());
        Direction clockWiseDirection = direction.getClockWise();
        Direction counterClockWiseDirection = direction.getCounterClockWise();

        BlockPos clockWiseTargetPosition = currentPosition
                .relative(clockWiseDirection, this.getHorizontalOffset())
                .below(this.getVerticalOffset());

        BlockPos counterClockWiseTargetPosition = currentPosition
                .relative(counterClockWiseDirection, this.getHorizontalOffset())
                .above(this.getVerticalOffset());

        Vec3 centeredClockWiseTargetPosition = clockWiseTargetPosition.getCenter();

        Vec3 centeredCounterClockWiseTargetPosition = counterClockWiseTargetPosition.getCenter();

        int playerCount = playerUUIDs.size();

        FairkeeperBorosEntity boros = new FairkeeperBorosEntity(this.level(), this);
        if (boros != null) {
            boros.moveTo(centeredCounterClockWiseTargetPosition.x, centeredCounterClockWiseTargetPosition.y - boros.getBoundingBox().getYsize() / 2 + BEHIND_BLOCK_SPAWN_OFFSET, centeredCounterClockWiseTargetPosition.z);
            boros.setState(FairkeeperBorosEntity.FairkeeperBorosState.AWAKENING);
            boros.setYRot(clockWiseDirection.toYRot());
            boros.yBodyRot = boros.getYRot();
            boros.yHeadRot = boros.getYRot();
            boros.setCanDestroyBlocks(false);
            this.level().addFreshEntity(boros);
            boros.transitionTo(FairkeeperBorosEntity.FairkeeperBorosAnimationState.IDLE);
            this.setBorosId(boros.getUUID());
            this.setBorosWaitingForCommand(false);
            EntityScale.scaleBossHealth(boros, playerCount, modifiedDefeatedCount);
            EntityScale.scaleBossAttack(boros, playerCount, modifiedDefeatedCount);
            EntityScale.scaleBossExhaustion(boros, playerCount, this.borosExhaustion, modifiedDefeatedCount);
            if (modifiedDefeatedCount > 0) {
                boros.setCustomName(RecallUtil.recalledName(Component.translatable("entity.dungeonnowloading.fairkeeper_boros"), modifiedDefeatedCount));
            } else {
                boros.setCustomName(Component.translatable("entity.dungeonnowloading.fairkeeper_boros"));
            }
            this.boros = boros;
        }

        FairkeeperOurosEntity ouros = new FairkeeperOurosEntity(this.level(), this);
        if (ouros != null) {
            ouros.moveTo(centeredClockWiseTargetPosition.x, centeredClockWiseTargetPosition.y - ouros.getBoundingBox().getYsize() / 2 - BEHIND_BLOCK_SPAWN_OFFSET, centeredClockWiseTargetPosition.z);
            ouros.setState(FairkeeperOurosEntity.FairkeeperOurosState.AWAKENING);
            ouros.setYRot(counterClockWiseDirection.toYRot());
            ouros.yBodyRot = ouros.getYRot();
            ouros.yHeadRot = ouros.getYRot();
            ouros.setCanDestroyBlocks(false);
            this.level().addFreshEntity(ouros);
            ouros.transitionTo(FairkeeperOurosEntity.FairkeeperOurosAnimationState.IDLE);
            this.setOurosId(ouros.getUUID());
            this.setOurosWaitingForCommand(false);
            EntityScale.scaleBossHealth(ouros, playerCount, modifiedDefeatedCount);
            EntityScale.scaleBossAttack(ouros, playerCount, modifiedDefeatedCount);
            EntityScale.scaleBossExhaustion(ouros, playerCount, this.ourosExhaustion, modifiedDefeatedCount);
            if (modifiedDefeatedCount > 0) {
                ouros.setCustomName(RecallUtil.recalledName(Component.translatable("entity.dungeonnowloading.fairkeeper_ouros"), modifiedDefeatedCount));
            } else {
                ouros.setCustomName(Component.translatable("entity.dungeonnowloading.fairkeeper_ouros"));
            }
            this.ouros = ouros;
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            FairkeeperSerpentCallerAnimationState animationState = this.entityData.get(ANIMATION_STATE);
            switch (animationState) {
                case IDLE -> this.idleAnimationState.startIfStopped(this.tickCount);
                case ACTIVE -> {
                    this.activeAnimationState.stop();
                    this.activeAnimationState.start(this.tickCount);
                }
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public void transitionTo(FairkeeperSerpentCallerAnimationState state) {
        this.entityData.set(ANIMATION_STATE, state);
    }

    @Override
    public boolean isPickable() {
        return !this.isActivated() && !this.isRemoved();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(DNLItems.FAIRKEEPER_SERPENT_CALLER.get());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.getPhase() < 1) {
            player.displayClientMessage(Component.translatable("entity.dungeonnowloading.fairkeeper_serpent_caller.right_click"), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.level().isClientSide && !this.isRemoved() && damageSource.isCreativePlayer()) {
            this.kill();
            return true;
        }
        return super.hurt(damageSource, v);
    }

    private void playBossMusic() {
        float radius = ARENA_SIZE;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        List<ResourceLocation> soundsToStart = new ArrayList<>(List.of());
        soundsToStart.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_BASE.get().getLocation());
        soundsToStart.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_BOROS.get().getLocation());
        soundsToStart.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_OUROS.get().getLocation());
        for (ServerPlayer player : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStart) {
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), sound, SoundSource.MUSIC, 0, 1.0f, false, ARENA_SIZE, ARENA_SIZE), player);
            }
            Services.NETWORK.sendToPlayer(new S2CFadeInTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_BASE.get().getLocation(), TickingSoundTarget.NEWEST, 1.0f, 60), player);
            Services.NETWORK.sendToPlayer(new S2CFadeOutBackgroundMusicSoundPacket(60), player);
        }
    }

    private void playLoopMusic() {
        float radius = ARENA_SIZE;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        for (ServerPlayer player : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_BASE.get().getLocation(), SoundSource.MUSIC, 1, 1.0f, false, ARENA_SIZE, ARENA_SIZE), player);
            if (this.isBorosDefeated > 2) {
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_OUROS.get().getLocation(), SoundSource.MUSIC, 1, 1.0f, false, ARENA_SIZE, ARENA_SIZE), player);
            } else if (this.isOurosDefeated > 2) {
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_BOROS.get().getLocation(), SoundSource.MUSIC, 1, 1.0f, false, ARENA_SIZE, ARENA_SIZE), player);
            } else {
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_OUROS.get().getLocation(), SoundSource.MUSIC, 0, 1.0f, false, ARENA_SIZE, ARENA_SIZE), player);
                Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_BOROS.get().getLocation(), SoundSource.MUSIC, 0, 1.0f, false, ARENA_SIZE, ARENA_SIZE), player);
            }
        }
    }

    public void stopAllBossMusic() {
        float radius = ARENA_SIZE * 2;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );

        List<ResourceLocation> soundsToStop = new ArrayList<>(List.of());
        soundsToStop.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_BASE.get().getLocation());
        soundsToStop.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_BOROS.get().getLocation());
        soundsToStop.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_OUROS.get().getLocation());

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStop) {
                Services.NETWORK.sendToPlayer(new S2CStopTickingSoundPacket(this.getId(), sound, TickingSoundTarget.ALL, 60, true), otherPlayer);
            }
        }
    }

    public void stopBossMusic() {
        float radius = ARENA_SIZE * 2;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );

        List<ResourceLocation> soundsToStop = new ArrayList<>(List.of());
        soundsToStop.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_BASE.get().getLocation());
        soundsToStop.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_BOROS.get().getLocation());
        soundsToStop.add(DNLSounds.MUSIC_CLASH_OF_DUALITY_OUROS.get().getLocation());

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStop) {
                Services.NETWORK.sendToPlayer(new S2CStopTickingSoundPacket(this.getId(), sound, 60, true), otherPlayer);
            }
        }
    }

    private void fadeInBorosMusic() {
        float radius = ARENA_SIZE;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CFadeInTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_BOROS.get().getLocation(), TickingSoundTarget.NEWEST, 1.0f, 20), otherPlayer);
        }
    }

    private void fadeInOurosMusic() {
        float radius = ARENA_SIZE;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CFadeInTickingSoundPacket(this.getId(), DNLSounds.MUSIC_CLASH_OF_DUALITY_OUROS.get().getLocation(), TickingSoundTarget.NEWEST, 1.0f, 20), otherPlayer);
        }
    }

    public static void disperse(ServerLevel level, SeepingSoulEntity soul, int defeatedCount) {
        removePreservers(level, soul.blockPosition());
    }

    public static void spawnRecalled(ServerLevel level, SeepingSoulEntity soul, int defeatedCount) {
        FairkeeperSerpentCallerEntity boss = DNLEntityTypes.FAIRKEEPER_SERPENT_CALLER.get().create(level);
        if (boss == null) return;
        BlockPos pos = soul.blockPosition();
        boss.initRecalled(soul, defeatedCount);
        boss.moveTo(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f, soul.getYRot(), 0);
        level.addFreshEntity(boss);
        spawnRecallFxTight(level, boss.position());
    }

    public void initRecalled(SeepingSoulEntity soul,  int defeatedCount) {
        this.defeatedCount = defeatedCount;
        this.playerDefeatedUUIDs = soul.getPlayerDefeatedUUIDs();
    }

    public static void spawnRecallFxTight(ServerLevel level, Vec3 center) {
        double x0 = center.x;
        double y0 = center.y + 0.5F;
        double z0 = center.z;

        // 1.5 x 1.5 area => x/z in [-0.75 .. 0.75]
        double radius = 0.75;

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

    public Entity getBoros() {
        UUID id = getBorosId();
        if (id != null && !this.level().isClientSide) {
            return ((ServerLevel) this.level()).getEntity(id);
        }
        return null;
    }

    @Nullable
    public UUID getBorosId() { return this.entityData.get(BOROS_UUID).orElse(null); }

    public void setBorosId(@Nullable UUID uniqueId) {
        this.entityData.set(BOROS_UUID, Optional.ofNullable(uniqueId));
    }

    public Entity getOuros() {
        UUID id = getOurosId();
        if (id != null && !this.level().isClientSide) {
            return ((ServerLevel) this.level()).getEntity(id);
        }
        return null;
    }

    @Nullable
    public UUID getOurosId() { return this.entityData.get(OUROS_UUID).orElse(null); }

    public void setOurosId(@Nullable UUID uniqueId) {
        this.entityData.set(OUROS_UUID, Optional.ofNullable(uniqueId));
    }

    public void setActivated(boolean activated) { this.entityData.set(ACTIVATED, activated); }

    public boolean isActivated() { return this.entityData.get(ACTIVATED); }

    public void setOffsets(int x, int y) {
        this.entityData.set(HORIZONTAL_OFFSET, x);
        this.entityData.set(VERTICAL_OFFSET, y);
    }

    public int getHorizontalOffset() { return this.entityData.get(HORIZONTAL_OFFSET); }

    public int getVerticalOffset() { return this.entityData.get(VERTICAL_OFFSET); }

    public int getPhase() { return this.entityData.get(PHASE); }

    public void setPhase(int phase) { this.entityData.set(PHASE, phase); }

    public void setLastDamageSource(DamageSource damageSource) { this.lastDamageSource = damageSource; }

    public int getArenaSize() { return ARENA_SIZE; }

    public boolean isBorosWaitingForCommand() { return this.borosWaitingForCommand && this.boros != null; }

    public void setBorosWaitingForCommand(boolean waitingForCommand) {
        this.borosWaitingForCommand = waitingForCommand;
    }

    public boolean isOurosWaitingForCommand() { return this.ourosWaitingForCommand && this.ouros != null; }

    public void setOurosWaitingForCommand(boolean waitingForCommand) {
        this.ourosWaitingForCommand = waitingForCommand;
    }

    public int getParticipatingPlayerCount() {
        return this.playerUUIDs.size();
    }

    public Set<UUID> getParticipatingPlayerUUIDs() {
        return this.playerUUIDs;
    }

    public void removeMinion(UUID uuid) {
        this.minionUUIDs.remove(uuid);
    }

    public void addMinion(UUID uuid) {
        this.minionUUIDs.add(uuid);
    }

    public Set<UUID> getMinionUUIDs() {
        return this.minionUUIDs;
    }

    public boolean isBorosDefeated() {
        return this.isBorosDefeated > 0;
    }

    public boolean isOurosDefeated() {
        return this.isOurosDefeated > 0;
    }

    public void dyingBoros() {
        this.isBorosDefeated = this.isOurosDefeated > 0 ? 2 : 1;
    }

    public void dyingOuros() {
        this.isOurosDefeated = this.isBorosDefeated > 0 ? 2 : 1;
    }

    public void defeatedBoros() {
        this.isBorosDefeated = this.isBorosDefeated == 2 ? 4 : 3;
    }

    public void defeatedOuros() {
        this.isOurosDefeated = this.isOurosDefeated == 2 ? 4 : 3;
    }

    public void cleanMinionList() {
        ServerLevel serverLevel = (ServerLevel) this.level();

        this.minionUUIDs = this.getMinionUUIDs().stream()
                .map(serverLevel::getEntity) // Map UUIDs to entities
                .filter(Objects::nonNull) // Keep only non-null entities
                .map(Entity::getUUID) // Convert back to UUIDs
                .collect(Collectors.toSet());
    }

    public enum FairkeeperSerpentCallerAnimationState {
        NONE,
        IDLE,
        ACTIVE
    }
}
