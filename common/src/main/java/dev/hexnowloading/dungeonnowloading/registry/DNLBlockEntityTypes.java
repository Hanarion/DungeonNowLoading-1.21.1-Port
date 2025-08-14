package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.block.MendingTableBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class DNLBlockEntityTypes {
    public static final Supplier<BlockEntityType<FairkeeperChestBlockEntity>> FAIRKEEPER_CHEST = register("fairkeeper_chest", () -> BlockEntityType.Builder.of(FairkeeperChestBlockEntity::new, DNLBlocks.FAIRKEEPER_CHEST.get()).build(null));
    public static final Supplier<BlockEntityType<FairkeeperSpawnerBlockEntity>> FAIRKEEPER_SPAWNER = register("fairkeeper_spawner", () -> BlockEntityType.Builder.of(FairkeeperSpawnerBlockEntity::new, DNLBlocks.FAIRKEEEPER_SPAWNER.get()).build(null));
    public static final Supplier<BlockEntityType<ScuttleStatueBlockEntity>> SCUTTLE_STATUE = register("scuttle_statue", () -> BlockEntityType.Builder.of(ScuttleStatueBlockEntity::new, DNLBlocks.SCUTTLE_STATUE.get()).build(null));
    public static final Supplier<BlockEntityType<BallistaGolemStatueBlockEntity>> BALLISTA_GOLEM_STATUE = register("ballista_golem_statue", () -> BlockEntityType.Builder.of(BallistaGolemStatueBlockEntity::new, DNLBlocks.BALLISTA_GOLEM_STATUE.get()).build(null));
    public static final Supplier<BlockEntityType<VertexPillarBlockEntity>> VERTEX_PILLAR = register("vertex_pillar", () -> BlockEntityType.Builder.of(VertexPillarBlockEntity::new, DNLBlocks.VERTEX_PILLAR.get()).build(null));
    public static final Supplier<BlockEntityType<DisabledFairkeeperChestBlockEntity>> DISABLED_FAIRKEEPER_CHEST = register("disabled_fairkeeper_chest", () -> BlockEntityType.Builder.of(DisabledFairkeeperChestBlockEntity::new, DNLBlocks.WISE_FAIRKEEPER_CHEST.get(), DNLBlocks.FIERCE_FAIRKEEPER_CHEST.get()).build(null));
    public static final Supplier<BlockEntityType<PreserverBlockEntity>> PRESERVER_BLOCK = register("preserver", () -> BlockEntityType.Builder.of(PreserverBlockEntity::new, DNLBlocks.STONE_PRESERVER.get()).build(null));
    public static final Supplier<BlockEntityType<MendingAuraBlockEntity>> MENDING_AURA = register("mending_aura", () -> BlockEntityType.Builder.of(MendingAuraBlockEntity::new, DNLBlocks.MENDING_AURA.get(), DNLBlocks.MENDING_AURA_STAIRS.get(), DNLBlocks.MENDING_AURA_SLAB.get(), DNLBlocks.MENDING_AURA_FENCE.get(), DNLBlocks.MENDING_AURA_WALL.get(), DNLBlocks.MENDING_AURA_PATH.get(), DNLBlocks.MENDING_AURA_PANE.get()).build(null));
    public static final Supplier<BlockEntityType<MendingTableBlockEntity>> MENDING_TABLE = register("mending_table", () -> BlockEntityType.Builder.of(MendingTableBlockEntity::new, DNLBlocks.MENDING_TABLE.get()).build(null));
    //public static final RegistryObject<BlockEntityType<WindAlterBlockEntity>> WIND_ALTER = BLOCK_ENTITY_TYPES.register("wind_alter", () -> BlockEntityType.Builder.of(WindAlterBlockEntity::new, SkyislandBlocks.WIND_ALTER.get()).build(null));

    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String name, Supplier<BlockEntityType<T>> type) {
        return Services.REGISTRY.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, type);
    }

    public static void init() {}
}
