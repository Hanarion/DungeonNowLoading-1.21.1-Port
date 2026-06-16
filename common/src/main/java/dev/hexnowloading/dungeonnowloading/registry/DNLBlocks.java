package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.block.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

import static dev.hexnowloading.dungeonnowloading.block.GenericExplosiveBarrelBlock.FUSE;

public class DNLBlocks {


    /* Initialization must be done later to avoid conflict with Sodium. */

    // DESIGN BLOCKS
    public static Supplier<Block> COILING_STONE_PILLAR;
    public static Supplier<Block> COILING_STONE_PILLAR_CAPITAL;
    public static Supplier<Block> COILING_STONE_PILLAR_STAIRS;
    public static Supplier<Block> COILING_STONE_PILLAR_SLAB;
    public static Supplier<Block> COILING_STONE_PILLAR_WALL;
    public static Supplier<Block> CHISELED_COILING_STONE_PILLAR;
    public static Supplier<Block> STONE_TILES;
    public static Supplier<Block> CRACKED_STONE_TILES;
    public static Supplier<Block> STONE_TILE_STAIRS;
    public static Supplier<Block> STONE_TILE_SLAB;
    public static Supplier<Block> STONE_TILE_WALL;
    public static Supplier<Block> SIGNALING_STONE_EMBLEM;
    public static Supplier<Block> DUELING_STONE_EMBLEM;
    public static Supplier<Block> PUZZLING_STONE_EMBLEM;
    public static Supplier<Block> POLISHED_STONE;
    public static Supplier<Block> BORDERED_STONE;
    public static Supplier<Block> ACACIA_WOODEN_BOARD;
    public static Supplier<Block> BAMBOO_WOODEN_BOARD;
    public static Supplier<Block> BIRCH_WOODEN_BOARD;
    public static Supplier<Block> CHERRY_WOODEN_BOARD;
    public static Supplier<Block> CRIMSON_WOODEN_BOARD;
    public static Supplier<Block> DARK_OAK_WOODEN_BOARD;
    public static Supplier<Block> JUNGLE_WOODEN_BOARD;
    public static Supplier<Block> MANGROVE_WOODEN_BOARD;
    public static Supplier<Block> OAK_WOODEN_BOARD;
    public static Supplier<Block> PALE_OAK_WOODEN_BOARD;
    public static Supplier<Block> SPRUCE_WOODEN_BOARD;
    public static Supplier<Block> WARPED_WOODEN_BOARD;

    public static Supplier<Block> MOSS;

    // MECHANICAL BLOCKS
    public static Supplier<Block> BOOK_PILE;// = registerBlock("book_pile", () -> new BookPileBlock(BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.WOOL)));
    public static Supplier<Block> EXPLOSIVE_BARREL;// = registerBlock("explosive_barrel", () -> new ExplosiveBarrelBlock(BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.GRASS)));
    public static Supplier<Block> SILVERFISH_BARREL;
    public static Supplier<Block> COBBLESTONE_PEBBLES;// = registerBlock("cobblestone_pebbles", () -> new PebbleBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.STONE)));
    public static Supplier<Block> MOSSY_COBBLESTONE_PEBBLES;// = registerBlock("mossy_cobblestone_pebbles", () -> new PebbleBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.STONE)));
    public static Supplier<Block> IRON_INGOT_PILE;// = registerBlock("iron_ingot_pile", () -> new PileBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.METAL)));
    public static Supplier<Block> GOLD_INGOT_PILE;// = registerBlock("gold_ingot_pile", () -> new PileBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.METAL)));
    public static Supplier<Block> WOODEN_WALL_RACK;// = registerBlock("wooden_wall_rack", () -> new WallRackBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava()));
    public static Supplier<Block> WOODEN_WALL_PLATFORM;// = registerBlock("wooden_wall_platform", () -> new WallPlatformBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava()));
    public static Supplier<Block> SPIKES;// = registerBlock("spikes", () -> new SpikesBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)));
    public static Supplier<Block> DUNGEON_WALL_TORCH;// = registerBlock("dungeon_wall_torch", () -> new DungeonWallTorch(BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(DungeonWallTorch.LIGHT_EMISSION).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)));

    public static Supplier<Block> CHAOS_SPAWNER_EDGE;// = registerBlock("chaos_spawner_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_DIAMOND_EDGE;// = registerBlock("chaos_spawner_diamond_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_DIAMOND_VERTEX;// = registerBlock("chaos_spawner_diamond_vertex", () -> new ChaosSpawnerVertexBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_BROKEN_EDGE;// = registerBlock("chaos_spawner_broken_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_BROKEN_DIAMOND_VERTEX;// = registerBlock("chaos_spawner_broken_diamond_vertex", () -> new ChaosSpawnerVertexBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_BROKEN_DIAMOND_EDGE;// = registerBlock("chaos_spawner_broken_diamond_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_BARRIER_CENTER;// = registerBlock("chaos_spawner_barrier_center", () -> new ChaosSpawnerBarrierCenterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.AMETHYST).lightLevel((lightLevel) -> {return 15;}).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_BARRIER_EDGE;// = registerBlock("chaos_spawner_barrier_edge", () -> new ChaosSpawnerBarrierEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.AMETHYST).lightLevel((lightLevel) -> {return 15;}).noOcclusion()));
    public static Supplier<Block> CHAOS_SPAWNER_BARRIER_VERTEX;// = registerBlock("chaos_spawner_barrier_vertex", () -> new ChaosSpawnerBarrierVertexBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).lightLevel((lightLevel) -> {return 15;}).noOcclusion()));

    public static Supplier<Block> FAIRKEEPER_CHEST;
    public static Supplier<Block> WISE_FAIRKEEPER_CHEST;
    public static Supplier<Block> FIERCE_FAIRKEEPER_CHEST;
    public static Supplier<Block> FAIRKEEEPER_SPAWNER;
    public static Supplier<Block> REDSTONE_LANE_I;
    public static Supplier<Block> REDSTONE_LANE_L;
    public static Supplier<Block> REDSTONE_LANE_T;
    public static Supplier<Block> ROTATOR_PRESSURE_PLATE;
    public static Supplier<Block> STONE_NOTCH;
    public static Supplier<Block> COAL_STONE_NOTCH;
    public static Supplier<Block> COPPER_STONE_NOTCH;
    public static Supplier<Block> IRON_STONE_NOTCH;
    public static Supplier<Block> GOLD_STONE_NOTCH;
    public static Supplier<Block> REDSTONE_STONE_NOTCH;
    public static Supplier<Block> AMETHYST_STONE_NOTCH;
    public static Supplier<Block> LAPIS_STONE_NOTCH;
    public static Supplier<Block> EMERALD_STONE_NOTCH;
    public static Supplier<Block> QUARTZ_STONE_NOTCH;
    public static Supplier<Block> GLOWSTONE_STONE_NOTCH;
    public static Supplier<Block> PRISMARINE_STONE_NOTCH;
    public static Supplier<Block> CHORUS_STONE_NOTCH;
    public static Supplier<Block> ECHO_STONE_NOTCH;
    public static Supplier<Block> DIAMOND_STONE_NOTCH;
    public static Supplier<Block> NETHERITE_STONE_NOTCH;
    public static Supplier<Block> SIGNAL_GATE;
    public static Supplier<Block> SCUTTLE_STATUE;
    public static Supplier<Block> BALLISTA_GOLEM_STATUE;
    public static Supplier<Block> BALLISTA_GOLEM_STATUE_PART;
    public static Supplier<Block> OVERCHARGED_REDSTONE_BLOCK;
    public static Supplier<Block> VERTEX_PILLAR;
    public static Supplier<Block> MENDING_AURA;
    public static Supplier<Block> STONE_PRESERVER;
    public static Supplier<Block> REDSTONE_IDOL;
    public static Supplier<Block> MENDING_TABLE;
    public static Supplier<Block> DURITE_CLUSTER;
    public static Supplier<Block> LARGE_DURITE_BUD;
    public static Supplier<Block> MEDIUM_DURITE_BUD;
    public static Supplier<Block> SMALL_DURITE_BUD;
    public static Supplier<Block> MENDSTONE_CHALK_MARK;
    public static Supplier<Block> DUNGEON_BANNER_SPAWNER_MAGENTA;
    public static Supplier<Block> DUNGEON_BANNER_SPAWNER_BLACK;
    public static Supplier<Block> DUNGEON_BANNER_SPAWNER_BLUE;
    public static Supplier<Block> DUNGEON_BANNER_SPAWNER_PURPLE;
    public static Supplier<Block> DUNGEON_BANNER_SPAWNER_GREEN;
    public static Supplier<Block> DUNGEON_BANNER_HOLLOW;
    public static Supplier<Block> DUNGEON_BANNER_SPAWNER_CARRIER;
    public static Supplier<Block> DUNGEON_BANNER_EXPERIENCE_BOTTLE;
    public static Supplier<Block> DUNGEON_BANNER_CHAOS_SPAWNER;
    public static Supplier<Block> DUNGEON_BANNER_WHIMPER_LANTERN;
    public static Supplier<Block> DUNGEON_BANNER_GARHOLD_UPSIDEDOWN;
    public static Supplier<Block> DUNGEON_BANNER_SKULL_OF_CHAOS;
    public static Supplier<Block> DURITE_QUELLER;
    public static Supplier<Block> DUNGEON_DIRECTOR;
    public static Supplier<Block> SPAWN_NODE;
    public static Supplier<Block> BRITTLESTONE;
    public static Supplier<Block> DEEPSTEEL_BLOCK;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_FRAME;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_FLOATING;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_FLOATING_RAIL;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_FRAME_TOP;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_SUSPENDED;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_SUSPENDED_RAIL;
    public static Supplier<Block> DEEPSTEEL_SLOPED_PLATFORM_FLOATING;
    public static Supplier<Block> DEEPSTEEL_SLOPED_PLATFORM_FLOATING_RAIL;
    public static Supplier<Block> DEEPSTEEL_PLATFORM_ENCLOSED_STAIRS;
    public static Supplier<Block> DEEPSTEEL_MOUNTED_RAIL;
    public static Supplier<Block> DEEPSTEEL_MOUNTED_POWERED_RAIL;
    public static Supplier<Block> DEEPSTEEL_MOUNTED_DETECTOR_RAIL;
    public static Supplier<Block> DEEPSTEEL_MOUNTED_ACTIVATOR_RAIL;
    public static Supplier<Block> WISPWARD_LANTERN;
    public static Supplier<Block> TIMED_WISPWARD_LANTERN;
    public static Supplier<Block> WEB_CARPET;
    public static Supplier<Block> WEBBING_BLOCK;
    public static Supplier<Block> WEBBING_NEST_BLOCK;
    public static Supplier<Block> SUSPENDED_WEB;
    public static Supplier<Block> BURNACLE;
    public static Supplier<Block> WISP_BLOCK;
    public static Supplier<Block> WISPWARD_CHEST;

    // Trophies
    public static Supplier<Block> LABYRINTH_TROPHY;// = registerBlock("labyrinth_trophy", () -> new Block(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CUSTOM_HEAD).strength(1.0f).pushReaction(PushReaction.DESTROY)));
    public static Supplier<Block> TEMPLE_OF_DUALITY_TROPHY;

    public static Supplier<Block> PLAYER_STATUE;

    public static boolean blocksRegistered = false;

    public static void init() {
        // DESIGN BLOCKS
        MOSS = registerBlock("moss", () -> new MossMultifaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).noOcclusion().noCollission().replaceable().sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)));
        COILING_STONE_PILLAR = registerBlock("coiling_stone_pillar", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        COILING_STONE_PILLAR_CAPITAL = registerBlock("coiling_stone_pillar_capital", () -> new PillarCapBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        COILING_STONE_PILLAR_STAIRS = registerBlock("coiling_stone_pillar_stairs", () -> new StairBlock(COILING_STONE_PILLAR.get().defaultBlockState(), BlockBehaviour.Properties.copy(COILING_STONE_PILLAR.get())));
        COILING_STONE_PILLAR_SLAB = registerBlock("coiling_stone_pillar_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        COILING_STONE_PILLAR_WALL = registerBlock("coiling_stone_pillar_wall", () -> new WallBlock(BlockBehaviour.Properties.copy(COILING_STONE_PILLAR.get()).forceSolidOn()));
        CHISELED_COILING_STONE_PILLAR = registerBlock("chiseled_coiling_stone_pillar", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        STONE_TILES = registerBlock("stone_tiles", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        CRACKED_STONE_TILES = registerBlock("cracked_stone_tiles", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        STONE_TILE_STAIRS = registerBlock("stone_tile_stairs", () -> new StairBlock(STONE_TILES.get().defaultBlockState(), BlockBehaviour.Properties.copy(STONE_TILES.get())));
        STONE_TILE_SLAB = registerBlock("stone_tile_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        STONE_TILE_WALL = registerBlock("stone_tile_wall", () -> new WallBlock(BlockBehaviour.Properties.copy(STONE_TILES.get()).forceSolidOn()));
        SIGNALING_STONE_EMBLEM = registerBlock("signaling_stone_emblem", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        DUELING_STONE_EMBLEM = registerBlock("dueling_stone_emblem", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        PUZZLING_STONE_EMBLEM = registerBlock("puzzling_stone_emblem", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        POLISHED_STONE = registerBlock("polished_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        BORDERED_STONE = registerBlock("bordered_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        ACACIA_WOODEN_BOARD = registerBlock("acacia_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        BAMBOO_WOODEN_BOARD = registerBlock("bamboo_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        BIRCH_WOODEN_BOARD = registerBlock("birch_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        CHERRY_WOODEN_BOARD = registerBlock("cherry_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        CRIMSON_WOODEN_BOARD = registerBlock("crimson_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        DARK_OAK_WOODEN_BOARD = registerBlock("dark_oak_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        JUNGLE_WOODEN_BOARD = registerBlock("jungle_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        MANGROVE_WOODEN_BOARD = registerBlock("mangrove_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        OAK_WOODEN_BOARD = registerBlock("oak_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        PALE_OAK_WOODEN_BOARD = registerBlock("pale_oak_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        SPRUCE_WOODEN_BOARD = registerBlock("spruce_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));
        WARPED_WOODEN_BOARD = registerBlock("warped_wooden_board", () -> new WoodenBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.0F).ignitedByLava().noOcclusion()));


        // MECHANICAL BLOCKS
        BOOK_PILE = registerBlock("book_pile", () -> new BookPileBlock(BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.WOOL)));
        EXPLOSIVE_BARREL = registerBlock("explosive_barrel", () -> new ExplosiveBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().sound(SoundType.GRASS)
                .lightLevel(state -> state.hasProperty(FUSE) && state.getValue(FUSE) > 0 ? 12 : 0)
                .emissiveRendering((state, getter, pos) -> state.hasProperty(FUSE) && state.getValue(FUSE) > 0)
        ));
        SILVERFISH_BARREL = registerBlock("silverfish_barrel", () -> new SilverfishBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().sound(SoundType.GRASS).noLootTable()));
        COBBLESTONE_PEBBLES = registerBlock("cobblestone_pebbles", () -> new PebbleBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.STONE)));
        MOSSY_COBBLESTONE_PEBBLES = registerBlock("mossy_cobblestone_pebbles", () -> new PebbleBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.STONE)));
        IRON_INGOT_PILE = registerBlock("iron_ingot_pile", () -> new PileBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.METAL)));
        GOLD_INGOT_PILE = registerBlock("gold_ingot_pile", () -> new PileBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.METAL)));
        WOODEN_WALL_RACK = registerBlock("wooden_wall_rack", () -> new WallRackBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava()));
        WOODEN_WALL_PLATFORM = registerBlock("wooden_wall_platform", () -> new WallPlatformBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava()));
        SPIKES = registerBlock("spikes", () -> new SpikesBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion().sound(SoundType.METAL).pushReaction(PushReaction.DESTROY).noLootTable()));
        DUNGEON_WALL_TORCH = registerBlock("dungeon_wall_torch", () -> new DungeonWallTorch(BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(DungeonWallTorch.LIGHT_EMISSION).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)));
        DUNGEON_BANNER_SPAWNER_MAGENTA = registerBlock("dungeon_banner_spawner_magenta", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.SPAWNER_MAGENTA, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_SPAWNER_BLACK = registerBlock("dungeon_banner_spawner_black", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.SPAWNER_BLACK, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_SPAWNER_BLUE = registerBlock("dungeon_banner_spawner_blue", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.SPAWNER_BLUE, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_SPAWNER_PURPLE = registerBlock("dungeon_banner_spawner_purple", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.SPAWNER_PURPLE, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_SPAWNER_GREEN = registerBlock("dungeon_banner_spawner_green", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.SPAWNER_GREEN, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_HOLLOW = registerBlock("dungeon_banner_hollow", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.HOLLOW, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_SPAWNER_CARRIER = registerBlock("dungeon_banner_spawner_carrier", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.SPAWNER_CARRIER, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_EXPERIENCE_BOTTLE = registerBlock("dungeon_banner_experience_bottle", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.EXPERIENCE_BOTTLE, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_CHAOS_SPAWNER = registerBlock("dungeon_banner_chaos_spawner", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.CHAOS_SPAWNER, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_WHIMPER_LANTERN = registerBlock("dungeon_banner_whimper_lantern", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.WHIMPER_LANTERN, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_GARHOLD_UPSIDEDOWN = registerBlock("dungeon_banner_garhold_upsidedown", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.GARHOLD_UPSIDEDOWN, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));
        DUNGEON_BANNER_SKULL_OF_CHAOS = registerBlock("dungeon_banner_skull_of_chaos", () -> new DungeonBannerBlock(DungeonBannerBlock.DungeonBannerVariant.SKULL_OF_CHAOS, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0f).sound(SoundType.WOOD).ignitedByLava()));

        CHAOS_SPAWNER_EDGE = registerBlock("chaos_spawner_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_DIAMOND_EDGE = registerBlock("chaos_spawner_diamond_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_DIAMOND_VERTEX = registerBlock("chaos_spawner_diamond_vertex", () -> new ChaosSpawnerVertexBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_BROKEN_EDGE = registerBlock("chaos_spawner_broken_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_BROKEN_DIAMOND_VERTEX = registerBlock("chaos_spawner_broken_diamond_vertex", () -> new ChaosSpawnerVertexBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_BROKEN_DIAMOND_EDGE = registerBlock("chaos_spawner_broken_diamond_edge", () -> new ChaosSpawnerEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_BARRIER_CENTER = registerBlock("chaos_spawner_barrier_center", () -> new ChaosSpawnerBarrierCenterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.AMETHYST).lightLevel((lightLevel) -> {return 15;}).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_BARRIER_EDGE = registerBlock("chaos_spawner_barrier_edge", () -> new ChaosSpawnerBarrierEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.AMETHYST).lightLevel((lightLevel) -> {return 15;}).noOcclusion().noLootTable()));
        CHAOS_SPAWNER_BARRIER_VERTEX = registerBlock("chaos_spawner_barrier_vertex", () -> new ChaosSpawnerBarrierVertexBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).lightLevel((lightLevel) -> {return 15;}).noOcclusion().noLootTable()));

        FAIRKEEPER_CHEST = registerBlock("fairkeeper_chest", () -> new FairkeeperChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 2.5F).noOcclusion().sound(SoundType.WOOD).lightLevel((lightLevel) -> 7)));
        WISE_FAIRKEEPER_CHEST = registerBlock("wise_fairkeeper_chest", () -> new DisabledFairkeeperChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 2.5F).noOcclusion().sound(SoundType.WOOD).lightLevel((lightLevel) -> 7)));
        FIERCE_FAIRKEEPER_CHEST = registerBlock("fierce_fairkeeper_chest", () -> new DisabledFairkeeperChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 2.5F).noOcclusion().sound(SoundType.WOOD).lightLevel((lightLevel) -> 7)));
        FAIRKEEEPER_SPAWNER = registerBlock("fairkeeper_spawner", () -> new FairkeeperSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).noOcclusion().sound(SoundType.WOOD).noLootTable()));
        REDSTONE_LANE_I = registerBlock("redstone_lane_i", () -> new RedstoneLaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).strength(1.5F, 6.0F).sound(SoundType.METAL).lightLevel(DNLBlocks::laneLight).requiresCorrectToolForDrops().pushReaction(PushReaction.PUSH_ONLY)));
        REDSTONE_LANE_L = registerBlock("redstone_lane_l", () -> new RedstoneLaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).strength(1.5F, 6.0F).sound(SoundType.METAL).lightLevel(DNLBlocks::laneLight).requiresCorrectToolForDrops().pushReaction(PushReaction.PUSH_ONLY)));
        REDSTONE_LANE_T = registerBlock("redstone_lane_t", () -> new RedstoneLaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).strength(1.5F, 6.0F).sound(SoundType.METAL).lightLevel(DNLBlocks::laneLight).requiresCorrectToolForDrops().pushReaction(PushReaction.PUSH_ONLY)));
        ROTATOR_PRESSURE_PLATE = registerBlock("rotator_pressure_plate", () -> new RotatorPressurePlate(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).forceSolidOn().instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY).lightLevel(blockState -> blockState.getValue(PressurePlateBlock.POWERED) ? 9 : 0), BlockSetType.STONE));
        STONE_NOTCH = registerBlock("stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.NONE));
        COAL_STONE_NOTCH = registerBlock("coal_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.COAL));
        COPPER_STONE_NOTCH = registerBlock("copper_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.COPPER));
        IRON_STONE_NOTCH = registerBlock("iron_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.IRON));
        GOLD_STONE_NOTCH = registerBlock("gold_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.GOLD));
        REDSTONE_STONE_NOTCH = registerBlock("redstone_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.REDSTONE));
        AMETHYST_STONE_NOTCH = registerBlock("amethyst_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.AMETHYST));
        LAPIS_STONE_NOTCH = registerBlock("lapis_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.LAPIS));
        EMERALD_STONE_NOTCH = registerBlock("emerald_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.EMERALD));
        QUARTZ_STONE_NOTCH = registerBlock("quartz_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.QUARTZ));
        GLOWSTONE_STONE_NOTCH = registerBlock("glowstone_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.GLOWSTONE));
        PRISMARINE_STONE_NOTCH = registerBlock("prismarine_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.PRISMARINE));
        CHORUS_STONE_NOTCH = registerBlock("chorus_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.CHORUS));
        ECHO_STONE_NOTCH = registerBlock("echo_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.ECHO));
        DIAMOND_STONE_NOTCH = registerBlock("diamond_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.DIAMOND));
        NETHERITE_STONE_NOTCH = registerBlock("netherite_stone_notch", () -> new StoneNotchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F), StoneNotchBlock.StoneNotchMaterialSignalStrength.NETHERITE));
        SIGNAL_GATE = registerBlock("signal_gate", () -> new SignalGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).isRedstoneConductor(DNLBlocks::never).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        SCUTTLE_STATUE = registerBlock("scuttle_statue", () -> new ScuttleStatueBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).noOcclusion().pushReaction(PushReaction.IGNORE).sound(SoundType.METAL)));
        BALLISTA_GOLEM_STATUE = registerBlock("ballista_golem_statue", () -> new BallistaGolemStatueBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).noOcclusion().pushReaction(PushReaction.IGNORE).sound(SoundType.METAL)));
        BALLISTA_GOLEM_STATUE_PART = registerBlock("ballista_golem_statue_part", () -> new BallistaGolemStatuePartBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).noOcclusion().pushReaction(PushReaction.IGNORE).sound(SoundType.EMPTY)));
        OVERCHARGED_REDSTONE_BLOCK = registerBlock("overcharged_redstone_block", () -> new OverchargedRedstoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL).isRedstoneConductor(DNLBlocks::never).lightLevel((lightLevel) -> 15)));
        VERTEX_PILLAR = registerBlock("vertex_pillar", () -> new VertexPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).noOcclusion().noLootTable().pushReaction(PushReaction.IGNORE).sound(SoundType.STONE)));
        MENDING_AURA = registerBlock("mending_aura", () -> new MendingAuraBlock(BlockBehaviour.Properties.of().mapColor(MapColor.LAPIS).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0F, 3600000.0F).noOcclusion().dynamicShape().pushReaction(PushReaction.IGNORE).noLootTable().emissiveRendering(DNLBlocks::always).lightLevel(lightLevel -> 7)));
        STONE_PRESERVER = registerBlock("stone_preserver", () -> new PreserverBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0F, 3600000.0F).noOcclusion().pushReaction(PushReaction.IGNORE).noLootTable().sound(SoundType.STONE).emissiveRendering(DNLBlocks::always)));
        DURITE_QUELLER = registerBlock("durite_queller", () -> new DuriteQuellerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).strength(50F, 1200.0F).noOcclusion().pushReaction(PushReaction.IGNORE).sound(SoundType.STONE).requiresCorrectToolForDrops().emissiveRendering(DNLBlocks::always).lightLevel(lightLevel -> 10)));
        REDSTONE_IDOL = registerBlock("redstone_idol", () -> new RedstoneIdolBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(DNLBlocks::never).noOcclusion()));
        MENDING_TABLE = registerBlock("mending_table", () -> new MendingTableBlock(BlockBehaviour.Properties.of().strength(5.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));
        DURITE_CLUSTER = registerBlock("durite_cluster", () -> new DuriteClusterBlock(DuriteClusterBlock.HitboxPreset.CLUSTER, BlockBehaviour.Properties.of().strength(1.5f, 1.5f).mapColor(MapColor.COLOR_BLUE).forceSolidOn().forceSolidOn().lightLevel(blockState -> 4).pushReaction(PushReaction.DESTROY)));
        LARGE_DURITE_BUD = registerBlock("large_durite_bud", () -> new DuriteClusterBlock(DuriteClusterBlock.HitboxPreset.LARGE, BlockBehaviour.Properties.of().strength(1.5f, 1.5f).mapColor(MapColor.COLOR_BLUE).forceSolidOn().forceSolidOn().lightLevel(blockState -> 3).pushReaction(PushReaction.DESTROY)));
        MEDIUM_DURITE_BUD = registerBlock("medium_durite_bud", () -> new DuriteClusterBlock(DuriteClusterBlock.HitboxPreset.MEDIUM, BlockBehaviour.Properties.of().strength(1.5f, 1.5f).mapColor(MapColor.COLOR_BLUE).forceSolidOn().forceSolidOn().lightLevel(blockState -> 2).pushReaction(PushReaction.DESTROY)));
        SMALL_DURITE_BUD = registerBlock("small_durite_bud", () -> new DuriteClusterBlock(DuriteClusterBlock.HitboxPreset.SMALL, BlockBehaviour.Properties.of().strength(1.5f, 1.5f).mapColor(MapColor.COLOR_BLUE).forceSolidOn().forceSolidOn().lightLevel(blockState -> 1).pushReaction(PushReaction.DESTROY)));
        MENDSTONE_CHALK_MARK = registerBlock("mendstone_chalk_mark", () -> new MendstoneChalkMarkBlock(BlockBehaviour.Properties.of().strength(0.0F, 3600000.0F).noOcclusion().pushReaction(PushReaction.IGNORE).noLootTable().emissiveRendering(DNLBlocks::always).lightLevel(lightLevel -> 7)));
        SPAWN_NODE = registerBlock("spawn_node", () -> new SpawnNodeBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion().noCollission().noLootTable()));
        DUNGEON_DIRECTOR = registerBlock("dungeon_director", () -> new DungeonDirectorBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion().noCollission().noLootTable()));

        BRITTLESTONE = registerBlock("brittlestone", () -> new BrittlestoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0f, 6.0f).noOcclusion()));
        DEEPSTEEL_BLOCK = registerBlock("deepsteel_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F, 1200.0F)));
        DEEPSTEEL_PLATFORM_FRAME = registerBlock("deepsteel_platform_frame", () -> axisDeepsteelPlatformBlock(deepsteelFrameShape()));
        DEEPSTEEL_PLATFORM_FLOATING = registerBlock("deepsteel_platform_floating", () -> deepsteelPlatformBlock(deepsteelFloatingShape()));
        DEEPSTEEL_PLATFORM_FLOATING_RAIL = registerBlock("deepsteel_platform_floating_rail", () -> axisDeepsteelPlatformBlock(deepsteelFloatingRailShape()));
        DEEPSTEEL_PLATFORM_FRAME_TOP = registerBlock("deepsteel_platform_frame_top", () -> axisDeepsteelPlatformBlock(deepsteelFrameTopShape()));
        DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL = registerBlock("deepsteel_platform_frame_top_rail", () -> axisDeepsteelPlatformBlock(deepsteelFrameShape()));
        DEEPSTEEL_PLATFORM_SUSPENDED = registerBlock("deepsteel_platform_suspended", () -> suspendedDeepsteelPlatformBlock(deepsteelFloatingShape()));
        DEEPSTEEL_PLATFORM_SUSPENDED_RAIL = registerBlock("deepsteel_platform_suspended_rail", () -> suspendedDeepsteelPlatformBlock(deepsteelFloatingRailShape()));
        DEEPSTEEL_SLOPED_PLATFORM_FLOATING = registerBlock("deepsteel_sloped_platform_floating", () -> directionalDeepsteelPlatformBlock(deepsteelStairsShape()));
        DEEPSTEEL_SLOPED_PLATFORM_FLOATING_RAIL = registerBlock("deepsteel_sloped_platform_floating_rail", () -> directionalDeepsteelPlatformBlock(deepsteelStairsRailShape()));
        DEEPSTEEL_PLATFORM_ENCLOSED_STAIRS = registerBlock("deepsteel_platform_enclosed_stairs", () -> directionalDeepsteelPlatformBlock(deepsteelEnclosedStairsShape()));
        DEEPSTEEL_MOUNTED_RAIL = registerBlock("deepsteel_mounted_rail", () -> new DeepsteelMountedRailBlock(DeepsteelMountedRailBlock.properties(), Items.RAIL));
        DEEPSTEEL_MOUNTED_POWERED_RAIL = registerBlock("deepsteel_mounted_powered_rail", () -> new DeepsteelMountedPoweredRailBlock(DeepsteelMountedRailBlock.properties(), Items.POWERED_RAIL));
        DEEPSTEEL_MOUNTED_DETECTOR_RAIL = registerBlock("deepsteel_mounted_detector_rail", () -> new DeepsteelMountedDetectorRailBlock(DeepsteelMountedRailBlock.properties(), Items.DETECTOR_RAIL));
        DEEPSTEEL_MOUNTED_ACTIVATOR_RAIL = registerBlock("deepsteel_mounted_activator_rail", () -> new DeepsteelMountedPoweredRailBlock(DeepsteelMountedRailBlock.properties(), Items.ACTIVATOR_RAIL));
        WISPWARD_LANTERN = registerBlock("wispward_lantern", () -> new WispwardLanternBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F, 1200.0F).sound(SoundType.LANTERN).noCollission().noOcclusion().lightLevel(WispwardLanternBlock::lightEmission)));
        TIMED_WISPWARD_LANTERN = registerBlock("timed_wispward_lantern", () -> new WispwardLanternBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F, 1200.0F).sound(SoundType.LANTERN).noCollission().noOcclusion().lightLevel(WispwardLanternBlock::lightEmission), true));
        WEB_CARPET = registerBlock("web_carpet", () -> new WebCarpetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).forceSolidOn().noCollission().requiresCorrectToolForDrops().strength(4.0F).pushReaction(PushReaction.DESTROY)));
        WEBBING_BLOCK = registerBlock("webbing_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(4.0F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));
        WEBBING_NEST_BLOCK = registerBlock("webbing_nest_block", () -> new WebbingNestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(4.0F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));
        SUSPENDED_WEB = registerBlock("suspended_web", () -> new SuspendedWebBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).noCollission().noOcclusion().strength(4.0F).pushReaction(PushReaction.DESTROY)));
        BURNACLE = registerBlock("burnacle", () -> new BurnacleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F, 1200.0F).pushReaction(PushReaction.DESTROY).noOcclusion()));
        WISP_BLOCK = registerBlock("wisp_block", () -> new WispBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instabreak().noCollission().noOcclusion().noLootTable().sound(SoundType.EMPTY).lightLevel(state -> 14).pushReaction(PushReaction.DESTROY)));
        WISPWARD_CHEST = registerBlock("wispward_chest", () -> new WispwardChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F, 1200.0F).sound(SoundType.METAL).noOcclusion()));
        // Trophies
        LABYRINTH_TROPHY = registerBlock("labyrinth_trophy", () -> new TrophyBlock(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CUSTOM_HEAD).strength(1.0f).noOcclusion().pushReaction(PushReaction.DESTROY)));
        TEMPLE_OF_DUALITY_TROPHY = registerBlock("temple_of_duality_trophy", () -> new TrophyBlock(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CUSTOM_HEAD).strength(1.0f).noOcclusion().pushReaction(PushReaction.DESTROY)));

        // Patron
        PLAYER_STATUE = registerBlock("player_statue", () -> new PlayerStatueBlock(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CUSTOM_HEAD).strength(1.5f, 6.0f).noOcclusion().pushReaction(PushReaction.IGNORE)));

        blocksRegistered = true;
    }

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> blockSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.BLOCK, name, blockSupplier);
    }

    private static Block deepsteelPlatformBlock(VoxelShape shape) {
        return new DeepsteelStaticPlatformBlock(deepsteelPlatformProperties(), shape);
    }

    private static Block directionalDeepsteelPlatformBlock(VoxelShape shape) {
        return new DeepsteelPlatformBlock(deepsteelPlatformProperties(), shape, DeepsteelPlatformBlock.ShapeRotation.FULL);
    }

    private static Block axisDeepsteelPlatformBlock(VoxelShape shape) {
        return new DeepsteelPlatformBlock(deepsteelPlatformProperties(), shape, DeepsteelPlatformBlock.ShapeRotation.AXIS);
    }

    private static Block suspendedDeepsteelPlatformBlock(VoxelShape shape) {
        return new DeepsteelSuspendedPlatformBlock(deepsteelPlatformProperties(), shape);
    }

    private static BlockBehaviour.Properties deepsteelPlatformProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F, 1200.0F).noOcclusion();
    }

    private static VoxelShape deepsteelFrameShape() {
        return Shapes.or(
                Block.box(0, 0, 0, 2, 16, 16),
                Block.box(14, 0, 0, 16, 16, 16)
        );
    }

    private static VoxelShape deepsteelFloatingShape() {
        return Block.box(0, 13, 0, 16, 16, 16);
    }

    private static VoxelShape deepsteelFloatingRailShape() {
        return Shapes.or(
                Block.box(0, 13, 0, 2, 16, 16),
                Block.box(14, 13, 0, 16, 16, 16)
        );
    }

    private static VoxelShape deepsteelFrameTopShape() {
        return Shapes.or(
                Block.box(0, 0, 0, 2, 13, 16),
                Block.box(14, 0, 0, 16, 13, 16),
                Block.box(0, 13, 0, 16, 16, 16)
        );
    }

    private static VoxelShape deepsteelStairsShape() {
        return Shapes.or(
                Block.box(0, 0, 0, 16, 8, 8),
                Block.box(0, 8, 8, 16, 16, 16)
        );
    }

    private static VoxelShape deepsteelEnclosedStairsShape() {
        return Shapes.or(
                deepsteelStairsShape(),
                deepsteelFrameShape()
        );
    }

    private static VoxelShape deepsteelStairsRailShape() {
        return Shapes.or(
                Block.box(0, 0, 0, 2, 8, 8),
                Block.box(14, 0, 0, 16, 8, 8),
                Block.box(0, 8, 8, 2, 16, 16),
                Block.box(14, 8, 8, 16, 16, 16)
        );
    }

    private static int laneLight(BlockState blockState) {
        return switch (blockState.getValue(DNLProperties.REDSTONE_LANE_MODE)) {
            default -> 0;
            case POWERED -> 3;
            case OVERPOWERED -> 15;
        };
    }

    public static boolean always(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    private static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }
}
