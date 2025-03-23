package dev.hexnowloading.dungeonnowloading.entity.boss;

import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.config.BossConfig;
import dev.hexnowloading.dungeonnowloading.entity.misc.SpecialItemEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import dev.hexnowloading.dungeonnowloading.entity.util.SpawnMobUtil;
import dev.hexnowloading.dungeonnowloading.entity.util.WeightBaseMoveSet;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    private static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> BOROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> OUROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> HORIZONTAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VERTICAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);

    private static final int SPAWN_OFFSET_X = 20;
    private static final int SPAWN_OFFSET_Y = 15;
    private final int ARENA_SIZE = 36;
    private final int MAX_SCUTTLE_COUNT = 6;
    private DamageSource lastDamageSource;
    private FairkeeperBorosEntity boros;
    private FairkeeperOurosEntity ouros;
    private boolean hasAddedDesperateMove;

    private boolean borosWaitingForCommand;
    private boolean ourosWaitingForCommand;
    private int activationTick;
    private Set<UUID> playerUUIDs;
    private Set<UUID> minionUUIDs;
    private WeightBaseMoveSet<Pair<FairkeeperBorosEntity.FairkeeperBorosState, FairkeeperOurosEntity.FairkeeperOurosState>> introMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<Pair<FairkeeperBorosEntity.FairkeeperBorosState, FairkeeperOurosEntity.FairkeeperOurosState>> comboMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<Pair<FairkeeperBorosEntity.FairkeeperBorosState, FairkeeperOurosEntity.FairkeeperOurosState>> directMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperOurosEntity.FairkeeperOurosState> ourosMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperOurosEntity.FairkeeperOurosState> ourosPillarMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperBorosEntity.FairkeeperBorosState> borosMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperBorosEntity.FairkeeperBorosState> borosArrowMoveSet = new WeightBaseMoveSet<>();
    private WeightBaseMoveSet<FairkeeperBorosEntity.FairkeeperBorosState> borosPursueMoveSet = new WeightBaseMoveSet<>();


    public FairkeeperSerpentCallerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.playerUUIDs = new HashSet<>();
        this.minionUUIDs = new HashSet<>();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ACTIVATED, false);
        this.entityData.define(BOROS_UUID, Optional.empty());
        this.entityData.define(OUROS_UUID, Optional.empty());
        this.entityData.define(HORIZONTAL_OFFSET, 0);
        this.entityData.define(VERTICAL_OFFSET, 0);
        this.entityData.define(PHASE, 0);
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
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.entityData.set(ACTIVATED, compoundTag.getBoolean("Activated"));
        if (compoundTag.hasUUID("BorosUUID")) {
            this.setBorosId(compoundTag.getUUID("BorosUUID"));
        }
        if (compoundTag.hasUUID("OurosUUID")) {
            this.setOurosId(compoundTag.getUUID("OurosUUID"));
        }
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
    }

    public void startBossFight() {
        this.activationTick = 60;
        this.clearAllMoveSet();
        this.setActivated(true);
        this.setOffsets(SPAWN_OFFSET_X, SPAWN_OFFSET_Y);
        AABB bossArena = new AABB(this.blockPosition()).inflate(ARENA_SIZE);
        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, bossArena);
        for (ServerPlayer p : players) {
            playerUUIDs.add(p.getUUID());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isActivated() && !this.level().isClientSide) {
            switch (this.getPhase()) {
                case 0:
                    if (this.activationTick > 0) {
                        this.activationTick--;
                        break;
                    }
                    this.summonBosses();
                    this.setPhase(1);
                    //this.activationTick = 60;
                    break;
                case 1:
                    if (this.isBorosWaitingForCommand() && this.isOurosWaitingForCommand()) {
                        this.introMoveSet();
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

                    if (this.getBoros() == null || this.getOuros() == null) {
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

                    if (this.getBoros() == null && this.getOuros() == null) {
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

        if (this.boros.getTarget() == null || this.ouros.getTarget() == null) {
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
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_SMALL_SQUARE), 1, 0, 0);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_SINGLE_LINE), 1, 0, 1);
        }

        ourosBorosAssignState();
    }

    private void commandBorosPhase2() {

        if (this.boros.getTarget() == null) {
            return;
        }

        if (this.borosMoveSet.isEmpty()) {
            borosMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_RANDOM, 1, 0, 1);
            borosMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_RANDOM, 1, 0, 0);
        }

        if (this.borosArrowMoveSet.isEmpty()) {
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_LINE, 1, 3, 3);
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_SMALL_CIRCLE, 1, 3, 3);
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_LARGE_CIRCLE, 1, 3, 0);
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_SLITHER, 1, 3, 0);
            borosArrowMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_PLAYER_LARGE_CRICLE, 1, 3, 0);
        }

        if (this.borosPursueMoveSet.isEmpty()) {
            borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.TACKLE, 1, 2, 2);
            borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.FLAME_PURSUING, 1, 1, 0);
            borosPursueMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_AND_SHOOT_TRIPLE_ARROW, 1, 1, 1);
        }

        borosAssignState();
    }

    private void commandOurosPhase2() {

        if (this.ouros.getTarget() == null) {
            return;
        }

        if (this.ourosMoveSet.isEmpty()) {
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE, 3, 2, 3);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB, 1, 0, 3);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_TRIPLE_VERTEX_ORB, 3, 2, 2);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_VERTEX_DOMAIN, 6, 4, 1);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_RANDOM, 3, 2, 0);
        }

        if (ourosPillarMoveSet.isEmpty()) {
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_SMALL_SQUARE), 1, 3, 3);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_SINGLE_LINE), 1, 3, 3);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_DOUBLE_LINE), 1, 3, 1);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_CROSS), 1, 3, 0);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LARGE_SQUARE), 1, 3, 1);
        }

        ourosAssignState();
    }

    private void commandBorosPhase3() {

    }


    private void commandOurosPhase3() {
        if (this.ourosMoveSet.isEmpty()) {
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SUMMON_SCUTTLE, 3, 2, 1);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB, 1, 0, 1);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_TRIPLE_VERTEX_ORB, 3, 2, 1);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_VERTEX_DOMAIN, 6, 4, 0);
            ourosMoveSet.addMove(FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_RANDOM, 3, 2, 1);
        }

        if (ourosPillarMoveSet.isEmpty()) {
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_SMALL_SQUARE), 1, 0, 2);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_SINGLE_LINE), 1, 0, 2);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_DOUBLE_LINE), 3, 0, 0);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_CROSS), 3, 0, 0);
            ourosPillarMoveSet.addMove((FairkeeperOurosEntity.FairkeeperOurosState.DROP_PILLAR_LARGE_SQUARE), 3, 0, 0);
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


        introMoveSet.displayAllStats();
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

        if (isAboveBoros()) {
            state = FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_ABOVE;
        } else {
            state = borosMoveSet.selectMove();
        }

        if (state.equals(FairkeeperBorosEntity.FairkeeperBorosState.SHOOT_ARROW_RANDOM)) {
            state = borosArrowMoveSet.selectMove();
        }

        if (state.equals(FairkeeperBorosEntity.FairkeeperBorosState.PURSUE_RANDOM)) {
            state = borosPursueMoveSet.selectMove();
        }

        int halfSize = this.getArenaSize();
        AABB arenaAABB = new AABB(
                this.getX() - halfSize, this.getY() - halfSize, this.getZ() - halfSize,
                this.getX() + halfSize, this.getY() + halfSize, this.getZ() + halfSize
        );

        List<Entity> targetProjectile = boros.level().getEntities((Entity) null, arenaAABB, entity ->
                        entity instanceof VertexOrbProjectileEntity || entity instanceof VertexDomainProjectileEntity
                ).stream()
                .filter(entity -> entity.getY() < boros.getY() + 3) // Filter out unreachable projectiles
                .toList();

        if (targetProjectile.size() > 4 * (this.boros.getHealth() / this.boros.getMaxHealth()) && this.boros.getRandom().nextFloat() < 1.0F) {
            state = FairkeeperBorosEntity.FairkeeperBorosState.EAT_VERTEX_PROJECTILES;
        }

        if (this.getPhase() > 2 && !this.hasAddedDesperateMove) {
            state = FairkeeperBorosEntity.FairkeeperBorosState.DESPERATE;
            this.hasAddedDesperateMove = true;
            this.borosMoveSet.addMove(FairkeeperBorosEntity.FairkeeperBorosState.DESPERATE, borosMoveSet.getTotalWeight() * 2, 3);
        }

        Entity boros = this.getBoros();
        if (boros instanceof FairkeeperBorosEntity borosEntity) {
            if (state == null) {
                borosEntity.setState(FairkeeperBorosEntity.FairkeeperBorosState.IDLE);
                borosEntity.setAttackTick(60);
                this.ourosMoveSet.reduceAllCooldown();
            } else {
                ((FairkeeperBorosEntity) boros).setState(state);
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

        if (isAboveBoros()) {
            state = FairkeeperOurosEntity.FairkeeperOurosState.SHOOT_SINGLE_VERTEX_ORB;
        } else {
            state = ourosMoveSet.selectMove();
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
            } else {
                ((FairkeeperOurosEntity) ouros).setState(state);
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
        if (BossConfig.TOGGLE_MULTIPLAYER_LOOT.get() && !this.playerUUIDs.isEmpty()) {
            for (UUID playerUUID : this.playerUUIDs) {
                this.spawnLootTableItems(this.lastDamageSource, true, true, playerUUID);
            }
        } else {
            this.spawnLootTableItems(this.lastDamageSource, true, false, null);
        }
        this.removeAllMinions();
        this.remove(RemovalReason.KILLED);
    }

    private void spawnLootTableItems(DamageSource damageSource, boolean b, boolean multiplayer, @Nullable UUID uuid) {
        ResourceLocation resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(DNLEntityTypes.FAIRKEEPER_SERPENT_CALLER.get());
        ResourceLocation lootTableResourceLocation = resourceLocation.withPrefix("entities/");
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
        this.setPhase(0);
        this.activationTick = 0;
        this.playerUUIDs.clear();
        this.removeAllMinions();
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

        FairkeeperBorosEntity boros = new FairkeeperBorosEntity(DNLEntityTypes.FAIRKEEPER_BOROS.get(), this.level());
        if (boros != null) {
            boros.moveTo(centeredCounterClockWiseTargetPosition.x, centeredCounterClockWiseTargetPosition.y - boros.getBoundingBox().getYsize() / 2, centeredCounterClockWiseTargetPosition.z);
            boros.setCallerId(this.getUUID());
            boros.setState(FairkeeperBorosEntity.FairkeeperBorosState.AWAKENING);
            boros.setYRot(clockWiseDirection.toYRot());
            boros.yBodyRot = boros.getYRot();
            boros.yHeadRot = boros.getYRot();
            boros.setCanDestroyBlocks(false);
            this.level().addFreshEntity(boros);
            boros.transitionTo(FairkeeperBorosEntity.FairkeeperBorosAnimationState.IDLE);
            this.setBorosId(boros.getUUID());
            this.setBorosWaitingForCommand(false);
            EntityScale.scaleBossHealth(boros, playerCount);
            EntityScale.scaleBossAttack(boros, playerCount);
            this.boros = boros;
        }

        FairkeeperOurosEntity ouros = new FairkeeperOurosEntity(DNLEntityTypes.FAIRKEEPER_OUROS.get(), this.level());
        if (ouros != null) {
            ouros.moveTo(centeredClockWiseTargetPosition.x, centeredClockWiseTargetPosition.y - ouros.getBoundingBox().getYsize() / 2, centeredClockWiseTargetPosition.z);
            ouros.setCallerId(this.getUUID());
            ouros.setState(FairkeeperOurosEntity.FairkeeperOurosState.AWAKENING);
            ouros.setYRot(counterClockWiseDirection.toYRot());
            ouros.yBodyRot = ouros.getYRot();
            ouros.yHeadRot = ouros.getYRot();
            ouros.setCanDestroyBlocks(false);
            this.level().addFreshEntity(ouros);
            ouros.transitionTo(FairkeeperOurosEntity.FairkeeperOurosAnimationState.IDLE);
            this.setOurosId(ouros.getUUID());
            this.setOurosWaitingForCommand(false);
            EntityScale.scaleBossHealth(ouros, playerCount);
            EntityScale.scaleBossAttack(ouros, playerCount);
            this.ouros = ouros;
        }
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

    public void cleanMinionList() {
        ServerLevel serverLevel = (ServerLevel) this.level();

        this.minionUUIDs = this.getMinionUUIDs().stream()
                .map(serverLevel::getEntity) // Map UUIDs to entities
                .filter(Objects::nonNull) // Keep only non-null entities
                .map(Entity::getUUID) // Convert back to UUIDs
                .collect(Collectors.toSet());
    }

    public enum FairkeeperSerpentCallerState {
        ;

        private FairkeeperSerpentCallerState() {}
    }
}
