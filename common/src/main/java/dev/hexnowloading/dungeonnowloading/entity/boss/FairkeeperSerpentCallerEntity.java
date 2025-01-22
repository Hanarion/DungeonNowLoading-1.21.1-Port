package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.config.BossConfig;
import dev.hexnowloading.dungeonnowloading.entity.misc.SpecialItemEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
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
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.MixinEnvironment;

import javax.annotation.Nullable;
import java.util.*;

public class FairkeeperSerpentCallerEntity extends Entity {

    private static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> BOROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> OUROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> HORIZONTAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VERTICAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);

    private final int ARENA_SIZE = 20;
    private DamageSource lastDamageSource;

    private int activationTick;
    private Set<UUID> playerUUIDs;

    public FairkeeperSerpentCallerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.playerUUIDs = new HashSet<>();
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
        this.setActivated(true);
        this.setOffsets(8, 9);
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
                        return;
                    }
                    this.summonBosses();
                    this.setPhase(1);
                    this.activationTick = 60;
                    return;
                case 1:
                    if (this.activationTick > 0) {
                        this.activationTick--;
                        return;
                    }

                    if (this.getBoros() == null && this.getOuros() == null) {
                        this.defeatedBosses();
                        this.setPhase(2);
                        return;
                    }

                    if (this.isAllPlayersInBound()) {
                        this.activationTick = 10;
                        return;
                    }

                    this.resetBosses();
                case 2:
                    break;
            }
        }
    }

    private void defeatedBosses() {
        if (BossConfig.TOGGLE_MULTIPLAYER_LOOT.get() && !this.playerUUIDs.isEmpty()) {
            for (UUID playerUUID : this.playerUUIDs) {
                this.spawnLootTableItems(this.lastDamageSource, true, true, playerUUID);
            }
        } else {
            this.spawnLootTableItems(this.lastDamageSource, true, false, null);
        }
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

        FairkeeperBorosEntity boros = new FairkeeperBorosEntity(DNLEntityTypes.FAIRKEEPER.get(), this.level());
        if (boros != null) {
            boros.moveTo(centeredCounterClockWiseTargetPosition.x, centeredCounterClockWiseTargetPosition.y - boros.getBoundingBox().getYsize() / 2, centeredCounterClockWiseTargetPosition.z);
            boros.setCallerId(this.getUUID());
            boros.setState(FairkeeperBorosEntity.FairkeeperState.AWAKENING);
            boros.setYRot(clockWiseDirection.toYRot());
            boros.yBodyRot = boros.getYRot();
            boros.yHeadRot = boros.getYRot();
            this.level().addFreshEntity(boros);
            this.setBorosId(boros.getUUID());
            EntityScale.scaleBossHealth(boros, playerCount);
            EntityScale.scaleBossAttack(boros, playerCount);
        }

        FairkeeperOurosEntity ouros = new FairkeeperOurosEntity(DNLEntityTypes.FAIRKEEPER_OUROS.get(), this.level());
        if (ouros != null) {
            ouros.moveTo(centeredClockWiseTargetPosition.x, centeredClockWiseTargetPosition.y - ouros.getBoundingBox().getYsize() / 2, centeredClockWiseTargetPosition.z);
            ouros.setCallerId(this.getUUID());
            ouros.setState(FairkeeperOurosEntity.FairkeeperOurosState.AWAKENING);
            ouros.setYRot(counterClockWiseDirection.toYRot());
            ouros.yBodyRot = ouros.getYRot();
            ouros.yHeadRot = ouros.getYRot();
            this.level().addFreshEntity(ouros);
            this.setOurosId(ouros.getUUID());
            EntityScale.scaleBossHealth(ouros, playerCount);
            EntityScale.scaleBossAttack(ouros, playerCount);
        }
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(DNLItems.FAIRKEEPER_SERPENT_CALLER.get());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        System.out.println("hello");
        if (this.getPhase() < 1) {
            player.displayClientMessage(Component.translatable("entity.dungeonnowloading.fairkeeper_serpent_caller.right_click"), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.interact(player, hand);
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

}
