package dev.hexnowloading.dungeonnowloading.datagen.provider;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.*;
import dev.hexnowloading.dungeonnowloading.block.property.RedstoneLaneMode;
import dev.hexnowloading.dungeonnowloading.datagen.provider.blockitemstategenerators.BannerBlockItemGen;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLProperties;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DNLForgeBlockStateProvider extends BlockStateProvider {
    public DNLForgeBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), DungeonNowLoading.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(DNLBlocks.STONE_TILES.get());
        simpleBlockWithItem(DNLBlocks.CRACKED_STONE_TILES.get());
        simpleBlockWithItem(DNLBlocks.SIGNALING_STONE_EMBLEM.get());
        simpleBlockWithItem(DNLBlocks.DUELING_STONE_EMBLEM.get());
        simpleBlockWithItem(DNLBlocks.PUZZLING_STONE_EMBLEM.get());
        simpleBlockWithItem(DNLBlocks.POLISHED_STONE.get());
        simpleBlockWithItem(DNLBlocks.BORDERED_STONE.get());
        simpleBlockWithItem(DNLBlocks.STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.COAL_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.COPPER_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.IRON_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.GOLD_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.REDSTONE_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.AMETHYST_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.LAPIS_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.EMERALD_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.QUARTZ_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.GLOWSTONE_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.PRISMARINE_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.CHORUS_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.ECHO_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.DIAMOND_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.NETHERITE_STONE_NOTCH.get());
        simpleBlockWithItem(DNLBlocks.OVERCHARGED_REDSTONE_BLOCK.get());
        simpleBlockWithItem(DNLBlocks.SPAWN_NODE.get());
        anyModelBlockWithItem(DNLBlocks.DURITE_QUELLER.get(), models().cubeBottomTop(ForgeRegistries.BLOCKS.getKey(DNLBlocks.DURITE_QUELLER.get()).getPath(), modLoc("block/durite_queller_side"), modLoc("block/durite_queller_bottom"), modLoc("block/durite_queller_top")));
        simpleBlockWithItem(DNLBlocks.BRITTLESTONE.get());
        simpleBlockWithItem(DNLBlocks.DEEPSTEEL_BLOCK.get());

        dungeonDirectorBlock(DNLBlocks.DUNGEON_DIRECTOR.get());
        fullyRotatedVarientBlock(DNLBlocks.MENDING_AURA.get());
        fullyRotatedVarientStairsLikeBlockWithItem(DNLBlocks.MENDING_AURA_STAIRS.get(), DNLBlocks.MENDING_AURA.get());
        fullyRotatedVarientSlabLikeBlockWithItem(DNLBlocks.MENDING_AURA_SLAB.get(), DNLBlocks.MENDING_AURA.get());
        fullyRotatedVarientFenceLikeBlockWithItem(DNLBlocks.MENDING_AURA_FENCE.get(), DNLBlocks.MENDING_AURA.get());
        fullyRotatedVarientWallLikeBlockWithItem(DNLBlocks.MENDING_AURA_WALL.get(), DNLBlocks.MENDING_AURA.get());
        fullyRotatedVarientPathLikeBlockWithItem(DNLBlocks.MENDING_AURA_PATH.get(), DNLBlocks.MENDING_AURA.get());
        fullyRotatedPaneLikeBlockWithItem(DNLBlocks.MENDING_AURA_PANE.get(), DNLBlocks.MENDING_AURA.get());
        chestLikeRandomTexturedBlock(DNLBlocks.MENDING_AURA_CHEST.get(), DNLBlocks.MENDING_AURA.get(), MendingAuraChestBlock.FACING, MendingAuraChestBlock.CHEST_TYPE, BlockStateProperties.WATERLOGGED);
        stairsBlockWithItem((StairBlock) DNLBlocks.STONE_TILE_STAIRS.get(), DNLBlocks.STONE_TILES.get());
        slabBlockWithItems((SlabBlock) DNLBlocks.STONE_TILE_SLAB.get(), DNLBlocks.STONE_TILES.get());
        wallBlockWithItem((WallBlock) DNLBlocks.STONE_TILE_WALL.get(), DNLBlocks.STONE_TILES.get());
        rotatedPillarBlockWithItem((RotatedPillarBlock) DNLBlocks.COILING_STONE_PILLAR.get());
        rotatedPillarBlockWithItem((RotatedPillarBlock) DNLBlocks.CHISELED_COILING_STONE_PILLAR.get());
        rotatedPillarCapBlockWithItem((PillarCapBlock) DNLBlocks.COILING_STONE_PILLAR_CAPITAL.get());
        slabBlockWithItems((SlabBlock) DNLBlocks.COILING_STONE_PILLAR_SLAB.get());
        stairsBlockWithItem((StairBlock) DNLBlocks.COILING_STONE_PILLAR_STAIRS.get());
        wallBlockWithItem((WallBlock) DNLBlocks.COILING_STONE_PILLAR_WALL.get());
        redstoneLaneWithItem((RedstoneLaneBlock) DNLBlocks.REDSTONE_LANE_I.get());
        redstoneLaneWithItem((RedstoneLaneBlock) DNLBlocks.REDSTONE_LANE_L.get());
        redstoneLaneWithItem((RedstoneLaneBlock) DNLBlocks.REDSTONE_LANE_T.get());
        signalGateWithItem((SignalGateBlock) DNLBlocks.SIGNAL_GATE.get());
        preserverWithItem((PreserverBlock) DNLBlocks.STONE_PRESERVER.get());
        particleOnlyModel(DNLBlocks.PLAYER_STATUE.get());
        facingSixWayWithExistingModel(DNLBlocks.DURITE_CLUSTER.get(), "durite_cluster");
        facingSixWayWithExistingModel(DNLBlocks.LARGE_DURITE_BUD.get(), "large_durite_bud");
        facingSixWayWithExistingModel(DNLBlocks.MEDIUM_DURITE_BUD.get(), "medium_durite_bud");
        facingSixWayWithExistingModel(DNLBlocks.SMALL_DURITE_BUD.get(), "small_durite_bud");
        horizontalModelFromParent(DNLBlocks.ACACIA_WOODEN_BOARD.get(),    "wooden_board", "wooden_board", modLoc("block/acacia_wooden_board"),    modLoc("item/acacia_wooden_board"));
        horizontalModelFromParent(DNLBlocks.BAMBOO_WOODEN_BOARD.get(),    "wooden_board", "wooden_board", modLoc("block/bamboo_wooden_board"),    modLoc("item/bamboo_wooden_board"));
        horizontalModelFromParent(DNLBlocks.BIRCH_WOODEN_BOARD.get(),     "wooden_board", "wooden_board", modLoc("block/birch_wooden_board"),     modLoc("item/birch_wooden_board"));
        horizontalModelFromParent(DNLBlocks.CHERRY_WOODEN_BOARD.get(),    "wooden_board", "wooden_board", modLoc("block/cherry_wooden_board"),    modLoc("item/cherry_wooden_board"));
        horizontalModelFromParent(DNLBlocks.CRIMSON_WOODEN_BOARD.get(),   "wooden_board", "wooden_board", modLoc("block/crimson_wooden_board"),   modLoc("item/crimson_wooden_board"));
        horizontalModelFromParent(DNLBlocks.DARK_OAK_WOODEN_BOARD.get(),  "wooden_board", "wooden_board", modLoc("block/dark_oak_wooden_board"),  modLoc("item/dark_oak_wooden_board"));
        horizontalModelFromParent(DNLBlocks.JUNGLE_WOODEN_BOARD.get(),    "wooden_board", "wooden_board", modLoc("block/jungle_wooden_board"),    modLoc("item/jungle_wooden_board"));
        horizontalModelFromParent(DNLBlocks.MANGROVE_WOODEN_BOARD.get(),  "wooden_board", "wooden_board", modLoc("block/mangrove_wooden_board"),  modLoc("item/mangrove_wooden_board"));
        horizontalModelFromParent(DNLBlocks.OAK_WOODEN_BOARD.get(),       "wooden_board", "wooden_board", modLoc("block/oak_wooden_board"),       modLoc("item/oak_wooden_board"));
        horizontalModelFromParent(DNLBlocks.PALE_OAK_WOODEN_BOARD.get(),  "wooden_board", "wooden_board", modLoc("block/pale_oak_wooden_board"),  modLoc("item/pale_oak_wooden_board"));
        horizontalModelFromParent(DNLBlocks.SPRUCE_WOODEN_BOARD.get(),    "wooden_board", "wooden_board", modLoc("block/spruce_wooden_board"),    modLoc("item/spruce_wooden_board"));
        horizontalModelFromParent(DNLBlocks.WARPED_WOODEN_BOARD.get(),    "wooden_board", "wooden_board", modLoc("block/warped_wooden_board"),    modLoc("item/warped_wooden_board"));
        generateMendstoneChalkMarkModels((MendstoneChalkMarkBlock) DNLBlocks.MENDSTONE_CHALK_MARK.get(), DNLItems.MENDSTONE_CHALK_MARK.get(), MendstoneChalkMarkBlock.OUTLINE);
        //faceBlockWithItem((MendstoneChalkMarkBlock) DNLBlocks.MENDSTONE_CHALK_MARK.get(), DNLItems.MENDSTONE_CHALK.get());

        //fairkeeperSpawnerWithItem((FairkeeperSpawnerBlock) DNLBlocks.FAIRKEEEPER_SPAWNER.get());
        //simpleRandomBlockWithItem(DNLBlocks.MOSS.get(), 5);
        BannerBlockItemGen gen = new BannerBlockItemGen(this);

        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_SPAWNER_MAGENTA.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_SPAWNER_BLACK.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_SPAWNER_BLUE.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_SPAWNER_PURPLE.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_SPAWNER_GREEN.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_HOLLOW.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_SPAWNER_CARRIER.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_EXPERIENCE_BOTTLE.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_CHAOS_SPAWNER.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_WHIMPER_LANTERN.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_GARHOLD_UPSIDEDOWN.get());
        gen.dungeonBanner(DNLBlocks.DUNGEON_BANNER_SKULL_OF_CHAOS.get());
        railPlatformStates(DNLBlocks.RAIL_PLATFORM.get());
        multifaceWebCarpet(DNLBlocks.WEB_CARPET.get());
        burnacleSixWayWithStages(DNLBlocks.BURNACLE.get());
    }

    private void fenceGateBlockWithItem(FenceGateBlock block, Block parent) {
        fenceGateBlock(block, blockTexture(parent));
        itemModels()
                .withExistingParent(name(block), mcLoc("block/template_fence_gate"))
                .renderType("cutout")
                .texture("texture", blockTexture(parent));
    }

//    private void doorBlockWithItem(DoorBlock block, ResourceLocation bottom, ResourceLocation top) {
//        doorBlock(block, bottom, top);
//        itemModels()
//                .withExistingParent(name(block), mcLoc("item/generated"))
//                .texture("layer0", bottom)
//                .texture("layer1", top)
//                .renderType("cutout");
//    }

    private void simplePressurePlateBlockWithItem(PressurePlateBlock block, Block parent) {
        pressurePlateBlock(block, blockTexture(parent));
        itemModels().withExistingParent(name(block),  modLoc("block/" + name(block)));
    }

    private void buttonBlockWithItem(ButtonBlock block, Block parent) {
        buttonBlock(block, blockTexture(parent));
        itemModels()
                .withExistingParent(name(block), mcLoc("block/button_inventory"))
                .texture("texture", blockTexture(parent));
    }

    private void fenceBlockWithItem(FenceBlock block, Block parent) {
        fenceBlock(block, blockTexture(parent));

        itemModels().withExistingParent(
                        ForgeRegistries.BLOCKS.getKey(block).getPath(),
                        mcLoc("block/fence_inventory"))
                .texture("texture", blockTexture(parent));
    }

    private void simpleBlockWithItem(Block block) {
        simpleBlock(block);
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block).getPath())));
    }

    private void anyModelBlockWithItem(Block block, ModelFile model) {
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    private void simpleItem(Block block) {
        String name = ForgeRegistries.BLOCKS.getKey(block).getPath();
        itemModels()
                .withExistingParent(ModelProvider.ITEM_FOLDER + "/" + name, mcLoc(ModelProvider.ITEM_FOLDER + "/generated"))
                .texture("layer0", ModelProvider.BLOCK_FOLDER + "/" + name);
    }

    private void simpleRandomBlockWithItem(Block block, int numVariants) {
        String name = ForgeRegistries.BLOCKS.getKey(block).getPath();
        ConfiguredModel[] models = new ConfiguredModel[numVariants];
        for (int i = 0; i < numVariants; i++) {
            models[i] = new ConfiguredModel(models().getExistingFile(modLoc("block/" + name + "_" + i)));
        }
        getVariantBuilder(block).partialState().setModels(models);
        itemModels().getBuilder(name)
                .parent(models().getExistingFile(modLoc("block/" + name + "_0")));
    }

    private void stairsBlockWithItem(StairBlock block, Block parent) {
        stairsBlock(block, blockTexture(parent));
        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void stairsBlockWithItem(StairBlock block) {
        ResourceLocation side = blockTexture(block);
        ResourceLocation ends = extend(blockTexture(block), "_top");

        ModelFile stairs = models().stairs(name(block), side, ends, ends);
        ModelFile stairsInner = models().stairsInner(name(block) + "_inner", side, ends, ends);
        ModelFile stairsOuter = models().stairsOuter(name(block) + "_outer", side, ends, ends);
        stairsBlock(block, stairs, stairsInner, stairsOuter);
        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void slabBlockWithItems(SlabBlock block, Block parent) {
        ResourceLocation path = blockTexture(parent);
        slabBlock(block, path, blockTexture(parent));
        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void slabBlockWithItems(SlabBlock block) {
        ResourceLocation side = blockTexture(block);
        ResourceLocation bottom = extend(blockTexture(block), "_top");
        ResourceLocation top = extend(blockTexture(block), "_top");

        ModelFile slabBottom = models().slab(name(block), side, bottom, top);
        ModelFile slabTop = models().slabTop(name(block) + "_top", side, bottom, top);
        ModelFile slabDouble = models().cubeColumn(name(block) + "_double", side, top);

        slabBlock(block, slabBottom, slabTop, slabDouble);
        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void pressurePlateblockWithItems(PressurePlateBlock block) {
        ResourceLocation deactive = blockTexture(block);
        ResourceLocation active = extend(blockTexture(block), "_on");

        ModelFile off = models().pressurePlate(name(block), deactive).renderType("cutout");
        ModelFile on = models().pressurePlateDown(name(block) + "_on", active).renderType("cutout");

        pressurePlateBlock(block, off, on);
        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void rotatedPillarBlockWithItem(RotatedPillarBlock block) {
        logBlock(block);
        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void rotatedPillarCapBlockWithItem(PillarCapBlock block) {
        ResourceLocation side = blockTexture(block);
        ResourceLocation sideReversed = extend(blockTexture(block), "_reversed");
        ResourceLocation top = extend(blockTexture(block), "_top");
        ResourceLocation bottom = extend(blockTexture(block), "_bottom");

        ModelFile normal = models().cubeBottomTop(name(block), side, bottom, top);
        ModelFile reversed = models().cubeBottomTop(name(block) + "_reversed", sideReversed, bottom, top);

        getVariantBuilder(block)
                .partialState().with(PillarCapBlock.FACING, Direction.UP).modelForState().modelFile(normal).addModel()
                .partialState().with(PillarCapBlock.FACING, Direction.DOWN).modelForState().modelFile(reversed).rotationX(180).rotationY(180).addModel()
                .partialState().with(PillarCapBlock.FACING, Direction.NORTH).modelForState().modelFile(normal).rotationX(90).addModel()
                .partialState().with(PillarCapBlock.FACING, Direction.EAST).modelForState().modelFile(normal).rotationX(90).rotationY(90).addModel()
                .partialState().with(PillarCapBlock.FACING, Direction.SOUTH).modelForState().modelFile(reversed).rotationX(270).addModel()
                .partialState().with(PillarCapBlock.FACING, Direction.WEST).modelForState().modelFile(reversed).rotationX(90).rotationY(270).addModel();

        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void horizontalRotatedVarientBlock(Block block) {
        ModelFile model = models().cubeAll(name(block), blockTexture(block));

        getVariantBuilder(block)
                .partialState().addModels(ConfiguredModel.builder().modelFile(model).rotationY(0).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model).rotationY(90).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model).rotationY(180).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model).rotationY(270).build());

        simpleBlockItem(block, model);
    }

    private void fullyRotatedVarientBlock(Block block) {
        ResourceLocation degree_0 = extend(blockTexture(block), "_0");
        ResourceLocation degree_90 = extend(blockTexture(block), "_90");
        ResourceLocation degree_180 = extend(blockTexture(block), "_180");
        ResourceLocation degree_270 = extend(blockTexture(block), "_270");

        ModelFile model_0 = models().cubeAll(name(block) + "_0", degree_0).renderType("cutout");;
        ModelFile model_90 = models().cubeAll(name(block) + "_90", degree_90).renderType("cutout");;
        ModelFile model_180 = models().cubeAll(name(block) + "_180", degree_180).renderType("cutout");;
        ModelFile model_270 = models().cubeAll(name(block) + "_270", degree_270).renderType("cutout");

        getVariantBuilder(block)
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_0).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_90).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_180).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_270).build());

        simpleBlockItem(block, model_0);
    }

    private void fullyRotatedVarientStairsLikeBlockWithItem(Block block, Block parent) {
        ResourceLocation degree_0 = extend(blockTexture(parent), "_0");
        ResourceLocation degree_90 = extend(blockTexture(parent), "_90");
        ResourceLocation degree_180 = extend(blockTexture(parent), "_180");
        ResourceLocation degree_270 = extend(blockTexture(parent), "_270");

        ModelFile stairs_model_0 = models().stairs(name(block) + "_0", degree_0, degree_0, degree_0).renderType("cutout");;
        ModelFile stairs_model_90 = models().stairs(name(block) + "_90", degree_90, degree_90, degree_90).renderType("cutout");;
        ModelFile stairs_model_180 = models().stairs(name(block) + "_180", degree_180, degree_180, degree_180).renderType("cutout");;
        ModelFile stairs_model_270 = models().stairs(name(block) + "_270", degree_270, degree_270, degree_270).renderType("cutout");;

        ModelFile stairsInner_model_0 = models().stairsInner(name(block) + "_inner_0", degree_0, degree_0, degree_0).renderType("cutout");;
        ModelFile stairsInner_model_90 = models().stairsInner(name(block) + "_inner_90", degree_90, degree_90, degree_90).renderType("cutout");;
        ModelFile stairsInner_model_180 = models().stairsInner(name(block) + "_inner_180", degree_180, degree_180, degree_180).renderType("cutout");;
        ModelFile stairsInner_model_270 = models().stairsInner(name(block) + "_inner_270", degree_270, degree_270, degree_270).renderType("cutout");;

        ModelFile stairsOuter_model_0 = models().stairsOuter(name(block) + "_outer_0", degree_0, degree_0, degree_0).renderType("cutout");;
        ModelFile stairsOuter_model_90 = models().stairsOuter(name(block) + "_outer_90", degree_90, degree_90, degree_90).renderType("cutout");;
        ModelFile stairsOuter_model_180 = models().stairsOuter(name(block) + "_outer_180", degree_180, degree_180, degree_180).renderType("cutout");;
        ModelFile stairsOuter_model_270 = models().stairsOuter(name(block) + "_outer_270", degree_270, degree_270, degree_270).renderType("cutout");;

        this.getVariantBuilder(block).forAllStatesExcept((state) -> {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING); // Use your block’s FACING property
            Half half = state.getValue(BlockStateProperties.HALF);
            StairsShape shape = state.getValue(BlockStateProperties.STAIRS_SHAPE);

            int yRot = (int) facing.getClockWise().toYRot();

            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) {
                yRot += 270;
            }
            if (shape != StairsShape.STRAIGHT && half == Half.TOP) {
                yRot += 90;
            }
            yRot %= 360;

            boolean uvlock = yRot != 0 || half == Half.TOP;

            // Choose the correct model based on the shape and yRot
            ModelFile selectedModel;
            if (shape == StairsShape.STRAIGHT) {
                selectedModel = switch (yRot) {
                    case 90 -> stairs_model_90;
                    case 180 -> stairs_model_180;
                    case 270 -> stairs_model_270;
                    default -> stairs_model_0;
                };
            } else if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
                selectedModel = switch (yRot) {
                    case 90 -> stairsInner_model_90;
                    case 180 -> stairsInner_model_180;
                    case 270 -> stairsInner_model_270;
                    default -> stairsInner_model_0;
                };
            } else { // OUTER_LEFT or OUTER_RIGHT
                selectedModel = switch (yRot) {
                    case 90 -> stairsOuter_model_90;
                    case 180 -> stairsOuter_model_180;
                    case 270 -> stairsOuter_model_270;
                    default -> stairsOuter_model_0;
                };
            }

            return ConfiguredModel.builder()
                    .modelFile(selectedModel)
                    .rotationX(half == Half.BOTTOM ? 0 : 180)
                    .rotationY(yRot)
                    .uvLock(uvlock)
                    .build();
        }, BlockStateProperties.WATERLOGGED);

        simpleBlockItem(block, stairs_model_0);
        /*ModelFile stairs = models().stairs(name(block), side, ends, ends);
        ModelFile stairsInner = models().stairsInner(name(block) + "_inner", side, ends, ends);
        ModelFile stairsOuter = models().stairsOuter(name(block) + "_outer", side, ends, ends);
        stairsBlock(block, stairs, stairsInner, stairsOuter);
        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));*/
    }

    private void fullyRotatedVarientSlabLikeBlockWithItem(Block block, Block parent) {
        ResourceLocation degree_0 = extend(blockTexture(parent), "_0");
        ResourceLocation degree_90 = extend(blockTexture(parent), "_90");
        ResourceLocation degree_180 = extend(blockTexture(parent), "_180");
        ResourceLocation degree_270 = extend(blockTexture(parent), "_270");

        ModelFile slabBottom_0 = models().slab(name(block) + "_0", degree_0, degree_0, degree_0).renderType("cutout");;
        ModelFile slabBottom_90 = models().slab(name(block) + "_90", degree_90, degree_90, degree_90).renderType("cutout");;
        ModelFile slabBottom_180 = models().slab(name(block) + "_180", degree_180, degree_180, degree_180).renderType("cutout");;
        ModelFile slabBottom_270 = models().slab(name(block) + "_270", degree_270, degree_270, degree_270).renderType("cutout");;

        ModelFile slabTop_0 = models().slabTop(name(block) + "_top_0", degree_0, degree_0, degree_0).renderType("cutout");;
        ModelFile slabTop_90 = models().slabTop(name(block) + "_top_90", degree_90, degree_90, degree_90).renderType("cutout");;
        ModelFile slabTop_180 = models().slabTop(name(block) + "_top_180", degree_180, degree_180, degree_180).renderType("cutout");;
        ModelFile slabTop_270 = models().slabTop(name(block) + "_top_270", degree_270, degree_270, degree_270).renderType("cutout");;

        ModelFile slabDouble_0 = models().cubeAll(name(block) + "_double_0", blockTexture(parent)).renderType("cutout");;
        ModelFile slabDouble_90 = models().cubeAll(name(block) + "_double_90", blockTexture(parent)).renderType("cutout");;
        ModelFile slabDouble_180 = models().cubeAll(name(block) + "_double_180", blockTexture(parent)).renderType("cutout");;
        ModelFile slabDouble_270 = models().cubeAll(name(block) + "_double_270", blockTexture(parent)).renderType("cutout");;

        this.getVariantBuilder(block).forAllStatesExcept((state) -> {
            SlabType slabType = state.getValue(SlabBlock.TYPE);

            boolean uvlock = false; // No rotation needed

            // Create random variations
            ConfiguredModel[] randomVariants;
            if (slabType == SlabType.BOTTOM) {
                randomVariants = new ConfiguredModel[]{
                        new ConfiguredModel(slabBottom_0),
                        new ConfiguredModel(slabBottom_90),
                        new ConfiguredModel(slabBottom_180),
                        new ConfiguredModel(slabBottom_270)
                };
            } else if (slabType == SlabType.TOP) {
                randomVariants = new ConfiguredModel[]{
                        new ConfiguredModel(slabTop_0),
                        new ConfiguredModel(slabTop_90),
                        new ConfiguredModel(slabTop_180),
                        new ConfiguredModel(slabTop_270)
                };
            } else { // DOUBLE SLAB
                randomVariants = new ConfiguredModel[]{
                        new ConfiguredModel(slabDouble_0),
                        new ConfiguredModel(slabDouble_90),
                        new ConfiguredModel(slabDouble_180),
                        new ConfiguredModel(slabDouble_270)
                };
            }

            return randomVariants;
        }, BlockStateProperties.WATERLOGGED);

        // Ensure correct item model (should match one of the variations)
        simpleBlockItem(block, slabBottom_0);
    }

    private void fullyRotatedVarientFenceLikeBlockWithItem(Block block, Block parent) {
        ResourceLocation degree_0 = extend(blockTexture(parent), "_0");
        ResourceLocation degree_90 = extend(blockTexture(parent), "_90");
        ResourceLocation degree_180 = extend(blockTexture(parent), "_180");
        ResourceLocation degree_270 = extend(blockTexture(parent), "_270");

        ModelFile post_0 = models().fencePost(name(block) + "_post_0", degree_0).renderType("cutout");;
        ModelFile post_90 = models().fencePost(name(block) + "_post_90", degree_90).renderType("cutout");;
        ModelFile post_180 = models().fencePost(name(block) + "_post_180", degree_180).renderType("cutout");;
        ModelFile post_270 = models().fencePost(name(block) + "_post_270", degree_270).renderType("cutout");;

        ModelFile side_0 = models().fenceSide(name(block) + "_side_0", degree_0).renderType("cutout");;
        ModelFile side_90 = models().fenceSide(name(block) + "_side_90", degree_90).renderType("cutout");;
        ModelFile side_180 = models().fenceSide(name(block) + "_side_180", degree_180).renderType("cutout");;
        ModelFile side_270 = models().fenceSide(name(block) + "_side_270", degree_270).renderType("cutout");;

        MultiPartBlockStateBuilder builder = ((MultiPartBlockStateBuilder.PartBuilder)this.getMultipartBuilder(block).part().modelFile(post_0).addModel()).end();
        PipeBlock.PROPERTY_BY_DIRECTION.entrySet().forEach((e) -> {
            Direction dir = (Direction)e.getKey();
            if (dir.getAxis().isHorizontal()) {
                ((MultiPartBlockStateBuilder.PartBuilder)builder.part().modelFile(side_0).rotationY(((int)dir.toYRot() + 180) % 360).uvLock(true).addModel()).condition((Property)e.getValue(), new Boolean[]{true});
            }

        });

        itemModels().withExistingParent(ForgeRegistries.BLOCKS.getKey(block).getPath(), mcLoc("block/fence_inventory"))
                .texture("texture", degree_0);
    }

    private void fullyRotatedVarientWallLikeBlockWithItem(Block block, Block parent) {
        ResourceLocation degree_0 = extend(blockTexture(parent), "_0");
        ResourceLocation degree_90 = extend(blockTexture(parent), "_90");
        ResourceLocation degree_180 = extend(blockTexture(parent), "_180");
        ResourceLocation degree_270 = extend(blockTexture(parent), "_270");

        ModelFile post_0 = models().wallPost(name(block) + "_post_0", degree_0).renderType("cutout");;
        ModelFile post_90 = models().wallPost(name(block) + "_post_90", degree_90).renderType("cutout");;
        ModelFile post_180 = models().wallPost(name(block) + "_post_180", degree_180).renderType("cutout");;
        ModelFile post_270 = models().wallPost(name(block) + "_post_270", degree_270).renderType("cutout");;

        ModelFile side_0 = models().wallSide(name(block) + "_side_0", degree_0).renderType("cutout");;
        ModelFile side_90 = models().wallSide(name(block) + "_side_90", degree_90).renderType("cutout");;
        ModelFile side_180 = models().wallSide(name(block) + "_side_180", degree_180).renderType("cutout");;
        ModelFile side_270 = models().wallSide(name(block) + "_side_270", degree_270).renderType("cutout");;

        ModelFile side_tall_0 = models().wallSideTall(name(block) + "_side_tall_0", degree_0).renderType("cutout");;
        ModelFile side_tall_90 = models().wallSideTall(name(block) + "_side_tall_90", degree_90).renderType("cutout");;
        ModelFile side_tall_180 = models().wallSideTall(name(block) + "_side_tall_180", degree_180).renderType("cutout");;
        ModelFile side_tall_270 = models().wallSideTall(name(block) + "_side_tall_270", degree_270).renderType("cutout");;

        MultiPartBlockStateBuilder builder = ((MultiPartBlockStateBuilder.PartBuilder)this.getMultipartBuilder(block).part().modelFile(post_0).addModel()).condition(WallBlock.UP, new Boolean[]{true}).end();
        WALL_PROPS.entrySet().stream().filter((e) -> {
            return ((Direction)e.getKey()).getAxis().isHorizontal();
        }).forEach((e) -> {
            this.wallSidePart(builder, side_0, e, WallSide.LOW);
            this.wallSidePart(builder, side_tall_0, e, WallSide.TALL);
        });

        itemModels().withExistingParent(ForgeRegistries.BLOCKS.getKey(block).getPath(), mcLoc("block/wall_inventory"))
                .texture("wall", degree_0);
    }

    private void wallSidePart(MultiPartBlockStateBuilder builder, ModelFile model, Map.Entry<Direction, Property<WallSide>> entry, WallSide height) {
        ((MultiPartBlockStateBuilder.PartBuilder)builder.part().modelFile(model).rotationY(((int)((Direction)entry.getKey()).toYRot() + 180) % 360).uvLock(true).addModel()).condition((Property)entry.getValue(), new WallSide[]{height});
    }

    private void fullyRotatedVarientPathLikeBlockWithItem(Block block, Block parent) {
        ResourceLocation degree_0 = extend(blockTexture(parent), "_0");
        ResourceLocation degree_90 = extend(blockTexture(parent), "_90");
        ResourceLocation degree_180 = extend(blockTexture(parent), "_180");
        ResourceLocation degree_270 = extend(blockTexture(parent), "_270");

        ModelFile model_0 = models().withExistingParent(name(block) + "_crop_0", mcLoc("block/template_farmland"))
                .texture("dirt", degree_0)
                .texture("top", degree_0).renderType("cutout");;
        ModelFile model_90 = models().withExistingParent(name(block) + "_crop_90", mcLoc("block/template_farmland"))
                .texture("dirt", degree_90)
                .texture("top", degree_90).renderType("cutout");;
        ModelFile model_180 = models().withExistingParent(name(block) + "_crop_180", mcLoc("block/template_farmland"))
                .texture("dirt", degree_180)
                .texture("top", degree_180).renderType("cutout");;
        ModelFile model_270 = models().withExistingParent(name(block) + "_crop_270", mcLoc("block/template_farmland"))
                .texture("dirt", degree_270)
                .texture("top", degree_270).renderType("cutout");;

        getVariantBuilder(block)
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_0).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_90).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_180).build())
                .partialState().addModels(ConfiguredModel.builder().modelFile(model_270).build());

        simpleBlockItem(block, model_0);

    }

    private void fullyRotatedPaneLikeBlockWithItem(Block block, Block parent) {
        ResourceLocation degree_0 = extend(blockTexture(parent), "_0");
        ResourceLocation degree_90 = extend(blockTexture(parent), "_90");
        ResourceLocation degree_180 = extend(blockTexture(parent), "_180");
        ResourceLocation degree_270 = extend(blockTexture(parent), "_270");

        ModelFile post_0 = models().panePost(name(block) + "_post_0", degree_0, degree_0).renderType("cutout");;
        ModelFile post_90 = models().panePost(name(block) + "_post_90", degree_90, degree_90).renderType("cutout");;
        ModelFile post_180 = models().panePost(name(block) + "_post_180", degree_180, degree_180).renderType("cutout");;
        ModelFile post_270 = models().panePost(name(block) + "_post_270", degree_270, degree_270).renderType("cutout");;

        ModelFile side_0 = models().paneSide(name(block) + "_side_0", degree_0, degree_0).renderType("cutout");;
        ModelFile side_90 = models().paneSide(name(block) + "_side_90", degree_90, degree_90).renderType("cutout");;
        ModelFile side_180 = models().paneSide(name(block) + "_side_180", degree_180, degree_180).renderType("cutout");;
        ModelFile side_270 = models().paneSide(name(block) + "_side_270", degree_270, degree_270).renderType("cutout");;

        ModelFile no_side_0 = models().paneNoSide(name(block) + "_no_side_0", degree_0).renderType("cutout");;
        ModelFile no_side_90 = models().paneNoSide(name(block) + "_no_side_90", degree_90).renderType("cutout");;
        ModelFile no_side_180 = models().paneNoSide(name(block) + "_no_side_180", degree_180).renderType("cutout");;
        ModelFile no_side_270 = models().paneNoSide(name(block) + "_no_side_270", degree_270).renderType("cutout");;

        ModelFile side_alt_0 = models().paneSideAlt(name(block) + "_side_alt_0", degree_0, degree_0).renderType("cutout");;
        ModelFile side_alt_90 = models().paneSideAlt(name(block) + "_side_alt_90", degree_90, degree_90).renderType("cutout");;
        ModelFile side_alt_180 = models().paneSideAlt(name(block) + "_side_alt_180", degree_180, degree_180).renderType("cutout");;
        ModelFile side_alt_270 = models().paneSideAlt(name(block) + "_side_alt_270", degree_270, degree_270).renderType("cutout");;

        ModelFile no_side_alt_0 = models().paneNoSideAlt(name(block) + "_no_side_alt_0", degree_0).renderType("cutout");;
        ModelFile no_side_alt_90 = models().paneNoSideAlt(name(block) + "_no_side_alt_90", degree_90).renderType("cutout");;
        ModelFile no_side_alt_180 = models().paneNoSideAlt(name(block) + "_no_side_alt_180", degree_180).renderType("cutout");;
        ModelFile no_side_alt_270 = models().paneNoSideAlt(name(block) + "_no_side_alt_270", degree_270).renderType("cutout");;

        MultiPartBlockStateBuilder builder = ((MultiPartBlockStateBuilder.PartBuilder)this.getMultipartBuilder(block).part().modelFile(post_0).addModel()).end();
        PipeBlock.PROPERTY_BY_DIRECTION.entrySet().forEach((e) -> {
            Direction dir = (Direction)e.getKey();
            if (dir.getAxis().isHorizontal()) {
                boolean alt = dir == Direction.SOUTH;
                ((MultiPartBlockStateBuilder.PartBuilder)((MultiPartBlockStateBuilder.PartBuilder)builder.part().modelFile(!alt && dir != Direction.WEST ? side_0 : side_alt_0).rotationY(dir.getAxis() == Direction.Axis.X ? 90 : 0).addModel()).condition((Property)e.getValue(), new Boolean[]{true}).end().part().modelFile(!alt && dir != Direction.EAST ? no_side_0 : no_side_alt_0).rotationY(dir == Direction.WEST ? 270 : (dir == Direction.SOUTH ? 90 : 0)).addModel()).condition((Property)e.getValue(), new Boolean[]{false});
            }

        });
        /*itemModels().withExistingParent(ForgeRegistries.BLOCKS.getKey(block).getPath(), mcLoc("item/generated"))
                .texture("layer_0", degree_0);*/
        itemModels().withExistingParent(this.key(block).getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("block/" + this.key(parent).getPath() + "_0"));
    }

    private void chestLikeRandomTexturedBlock(
            Block block,
            Block textureParent,                          // where mending_aura_* textures come from
            DirectionProperty facingProp,                 // e.g., MendingAuraChestBlock.FACING
            EnumProperty<ChestType> typeProp,             // e.g., MendingAuraChestBlock.CHEST_TYPE
            @Nullable BooleanProperty waterloggedProp) {

        // 4 texture variants taken from the "parent" block's base name
        ResourceLocation tex0   = extend(blockTexture(textureParent), "_0");
        ResourceLocation tex90  = extend(blockTexture(textureParent), "_90");
        ResourceLocation tex180 = extend(blockTexture(textureParent), "_180");
        ResourceLocation tex270 = extend(blockTexture(textureParent), "_270");

        // We’ll just use simple cubes (your block’s voxel shape is handled in code)
        ModelFile single_0   = chestBoxModel(name(block) + "_single_0",  tex0);
        ModelFile single_90  = chestBoxModel(name(block) + "_single_90", tex90);
        ModelFile single_180 = chestBoxModel(name(block) + "_single_180",tex180);
        ModelFile single_270 = chestBoxModel(name(block) + "_single_270",tex270);

        ModelFile left_0     = chestBoxModel(name(block) + "_left_0",    tex0);
        ModelFile left_90    = chestBoxModel(name(block) + "_left_90",   tex90);
        ModelFile left_180   = chestBoxModel(name(block) + "_left_180",  tex180);
        ModelFile left_270   = chestBoxModel(name(block) + "_left_270",  tex270);

        ModelFile right_0    = chestBoxModel(name(block) + "_right_0",   tex0);
        ModelFile right_90   = chestBoxModel(name(block) + "_right_90",  tex90);
        ModelFile right_180  = chestBoxModel(name(block) + "_right_180", tex180);
        ModelFile right_270  = chestBoxModel(name(block) + "_right_270", tex270);

        var builder = getVariantBuilder(block);

        java.util.function.Function<BlockState, ConfiguredModel[]> states = state -> {
            Direction facing = state.getValue(facingProp);
            ChestType type   = state.getValue(typeProp);

            // Rotation only cares about horizontal facings
            int y = switch (facing) {
                case NORTH -> 0;
                case EAST  -> 90;
                case SOUTH -> 180;
                case WEST  -> 270;
                default    -> 0;
            };

            ModelFile[] pool = switch (type) {
                case SINGLE -> new ModelFile[] { single_0, single_90, single_180, single_270 };
                case LEFT   -> new ModelFile[] { left_0, left_90, left_180, left_270 };
                case RIGHT  -> new ModelFile[] { right_0, right_90, right_180, right_270 };
            };

            // Return all 4 as random-weighted variants
            return new ConfiguredModel[] {
                    new ConfiguredModel(pool[0], 0, y, false, 1),
                    new ConfiguredModel(pool[1], 0, y, false, 1),
                    new ConfiguredModel(pool[2], 0, y, false, 1),
                    new ConfiguredModel(pool[3], 0, y, false, 1)
            };
        };

        if (waterloggedProp != null && block.defaultBlockState().hasProperty(waterloggedProp)) {
            builder.forAllStatesExcept(states, waterloggedProp);
        } else {
            builder.forAllStates(states);
        }

        // Item model: pick one of the variants (or make your own layered item if you prefer)
        simpleBlockItem(block, single_0);
    }

    private ModelFile chestBoxModel(String modelName, ResourceLocation tex) {
        BlockModelBuilder b = models().getBuilder(modelName)
                .renderType("cutout")
                .texture("all", tex)
                .texture("particle", tex);

        // geometry matches your MendingAuraChestBlock SHAPE
        b.element().from(1, 0, 1).to(15, 14, 15)
                .face(Direction.NORTH).texture("#all").end()
                .face(Direction.SOUTH).texture("#all").end()
                .face(Direction.WEST ).texture("#all").end()
                .face(Direction.EAST ).texture("#all").end()
                .face(Direction.UP   ).texture("#all").end()
                .face(Direction.DOWN ).texture("#all").end()
                .end();

        // hinge: from(7,7,0) to(9,11,1)
        b.element().from(7, 7, 0).to(9, 11, 1)
                .face(Direction.NORTH).texture("#all").end()
                .face(Direction.SOUTH).texture("#all").end()
                .face(Direction.WEST ).texture("#all").end()
                .face(Direction.EAST ).texture("#all").end()
                .face(Direction.UP   ).texture("#all").end()
                .face(Direction.DOWN ).texture("#all").end()
                .end();

        return b;
    }

    public static boolean always(BlockState blockState) {
        return true;
    }

    private void orientableWithBottomBlockWithItem(HorizontalDirectionalBlock block) {
        ResourceLocation side = extend(blockTexture(block), "_side");
        ResourceLocation front = extend(blockTexture(block), "_front");
        ResourceLocation bottom = extend(blockTexture(block), "_bottom");
        ResourceLocation top = extend(blockTexture(block), "_top");

        ModelFile normal = models().orientableWithBottom(name(block), side, front, bottom, top);

        getVariantBuilder(block)
                .partialState().with(HorizontalDirectionalBlock.FACING, Direction.NORTH).modelForState().modelFile(normal).addModel()
                .partialState().with(HorizontalDirectionalBlock.FACING, Direction.EAST).modelForState().modelFile(normal).rotationY(90).addModel()
                .partialState().with(HorizontalDirectionalBlock.FACING, Direction.SOUTH).modelForState().modelFile(normal).addModel()
                .partialState().with(HorizontalDirectionalBlock.FACING, Direction.WEST).modelForState().modelFile(normal).rotationX(90).addModel();

        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void preserverWithItem(PreserverBlock block) {
        ResourceLocation m_on = extend(blockTexture(block), "_on_0");
        ResourceLocation e_on = extend(blockTexture(block), "_on_1");
        ResourceLocation n_on = extend(blockTexture(block), "_on_2");
        ResourceLocation d_on = extend(blockTexture(block), "_on_3");
        ResourceLocation i_on = extend(blockTexture(block), "_on_4");
        ResourceLocation g_on = extend(blockTexture(block), "_on_5");
        ResourceLocation m_off = extend(blockTexture(block), "_off_0");
        ResourceLocation e_off = extend(blockTexture(block), "_off_1");
        ResourceLocation n_off = extend(blockTexture(block), "_off_2");
        ResourceLocation d_off = extend(blockTexture(block), "_off_3");
        ResourceLocation i_off = extend(blockTexture(block), "_off_4");
        ResourceLocation g_off = extend(blockTexture(block), "_off_5");

        ModelFile m_on_model = models().cubeAll(name(block) + "_on_0", m_on);
        ModelFile e_on_model = models().cubeAll(name(block) + "_on_1", e_on);
        ModelFile n_on_model = models().cubeAll(name(block) + "_on_2", n_on);
        ModelFile d_on_model = models().cubeAll(name(block) + "_on_3", d_on);
        ModelFile i_on_model = models().cubeAll(name(block) + "_on_4", i_on);
        ModelFile g_on_model = models().cubeAll(name(block) + "_on_5", g_on);
        ModelFile m_off_model = models().cubeAll(name(block) + "_off_0", m_off);
        ModelFile e_off_model = models().cubeAll(name(block) + "_off_1", e_off);
        ModelFile n_off_model = models().cubeAll(name(block) + "_off_2", n_off);
        ModelFile d_off_model = models().cubeAll(name(block) + "_off_3", d_off);
        ModelFile i_off_model = models().cubeAll(name(block) + "_off_4", i_off);
        ModelFile g_off_model = models().cubeAll(name(block) + "_off_5", g_off);

        getVariantBuilder(block)
                .partialState().with(PreserverBlock.LIT, true).addModels(ConfiguredModel.builder().modelFile(m_on_model).build())
                .partialState().with(PreserverBlock.LIT, true).addModels(ConfiguredModel.builder().modelFile(e_on_model).build())
                .partialState().with(PreserverBlock.LIT, true).addModels(ConfiguredModel.builder().modelFile(n_on_model).build())
                .partialState().with(PreserverBlock.LIT, true).addModels(ConfiguredModel.builder().modelFile(d_on_model).build())
                .partialState().with(PreserverBlock.LIT, true).addModels(ConfiguredModel.builder().modelFile(i_on_model).build())
                .partialState().with(PreserverBlock.LIT, true).addModels(ConfiguredModel.builder().modelFile(g_on_model).build())
                .partialState().with(PreserverBlock.LIT, false).addModels(ConfiguredModel.builder().modelFile(m_off_model).build())
                .partialState().with(PreserverBlock.LIT, false).addModels(ConfiguredModel.builder().modelFile(e_off_model).build())
                .partialState().with(PreserverBlock.LIT, false).addModels(ConfiguredModel.builder().modelFile(n_off_model).build())
                .partialState().with(PreserverBlock.LIT, false).addModels(ConfiguredModel.builder().modelFile(d_off_model).build())
                .partialState().with(PreserverBlock.LIT, false).addModels(ConfiguredModel.builder().modelFile(i_off_model).build())
                .partialState().with(PreserverBlock.LIT, false).addModels(ConfiguredModel.builder().modelFile(g_off_model).build());

        simpleBlockItem(block, m_off_model);
    }

    private void signalGateWithItem(SignalGateBlock block) {
        ResourceLocation front = extend(blockTexture(block), "_front");
        ResourceLocation back_on = extend(blockTexture(block), "_back_on");
        ResourceLocation back_off = extend(blockTexture(block), "_back_off");
        ResourceLocation bottom = extend(blockTexture(block), "_bottom");
        ResourceLocation side_on = extend(blockTexture(block), "_side_on");
        ResourceLocation side_on_reverse = extend(blockTexture(block), "_side_on_reverse");
        ResourceLocation side_off = extend(blockTexture(block), "_side_off");
        ResourceLocation side_off_reverse = extend(blockTexture(block), "_side_off_reverse");
        ResourceLocation top_00 = extend(blockTexture(block), "_top_00");
        ResourceLocation top_01 = extend(blockTexture(block), "_top_01");
        ResourceLocation top_02 = extend(blockTexture(block), "_top_02");
        ResourceLocation top_03 = extend(blockTexture(block), "_top_03");
        ResourceLocation top_04 = extend(blockTexture(block), "_top_04");
        ResourceLocation top_05 = extend(blockTexture(block), "_top_05");
        ResourceLocation top_06 = extend(blockTexture(block), "_top_06");
        ResourceLocation top_07 = extend(blockTexture(block), "_top_07");
        ResourceLocation top_08 = extend(blockTexture(block), "_top_08");
        ResourceLocation top_09 = extend(blockTexture(block), "_top_09");
        ResourceLocation top_10 = extend(blockTexture(block), "_top_10");
        ResourceLocation top_11 = extend(blockTexture(block), "_top_11");
        ResourceLocation top_12 = extend(blockTexture(block), "_top_12");
        ResourceLocation top_13 = extend(blockTexture(block), "_top_13");
        ResourceLocation top_14 = extend(blockTexture(block), "_top_14");
        ResourceLocation top_15 = extend(blockTexture(block), "_top_15");

        ModelFile off_00 = models().cube(name(block), bottom, top_00, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_01 = models().cube(name(block) + "_off_01", bottom, top_01, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_02 = models().cube(name(block) + "_off_02", bottom, top_02, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_03 = models().cube(name(block) + "_off_03", bottom, top_03, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_04 = models().cube(name(block) + "_off_04", bottom, top_04, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_05 = models().cube(name(block) + "_off_05", bottom, top_05, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_06 = models().cube(name(block) + "_off_06", bottom, top_06, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_07 = models().cube(name(block) + "_off_07", bottom, top_07, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_08 = models().cube(name(block) + "_off_08", bottom, top_08, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_09 = models().cube(name(block) + "_off_09", bottom, top_09, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_10 = models().cube(name(block) + "_off_10", bottom, top_10, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_11 = models().cube(name(block) + "_off_11", bottom, top_11, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_12 = models().cube(name(block) + "_off_12", bottom, top_12, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_13 = models().cube(name(block) + "_off_13", bottom, top_13, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_14 = models().cube(name(block) + "_off_14", bottom, top_14, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile off_15 = models().cube(name(block) + "_off_15", bottom, top_15, front, back_off, side_off_reverse, side_off).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_00 = models().cube(name(block) + "_on_00", bottom, top_00, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_01 = models().cube(name(block) + "_on_01", bottom, top_01, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_02 = models().cube(name(block) + "_on_02", bottom, top_02, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_03 = models().cube(name(block) + "_on_03", bottom, top_03, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_04 = models().cube(name(block) + "_on_04", bottom, top_04, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_05 = models().cube(name(block) + "_on_05", bottom, top_05, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_06 = models().cube(name(block) + "_on_06", bottom, top_06, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_07 = models().cube(name(block) + "_on_07", bottom, top_07, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_08 = models().cube(name(block) + "_on_08", bottom, top_08, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_09 = models().cube(name(block) + "_on_09", bottom, top_09, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_10 = models().cube(name(block) + "_on_10", bottom, top_10, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_11 = models().cube(name(block) + "_on_11", bottom, top_11, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_12 = models().cube(name(block) + "_on_12", bottom, top_12, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_13 = models().cube(name(block) + "_on_13", bottom, top_13, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_14 = models().cube(name(block) + "_on_14", bottom, top_14, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));
        ModelFile on_15 = models().cube(name(block) + "_on_15", bottom, top_15, front, back_on, side_on_reverse, side_on).texture("particle", extend(blockTexture(block), "_front"));

        List<ModelFile> modelFileListOff = Arrays.asList(off_00, off_01, off_02, off_03, off_04, off_05, off_06, off_07, off_08, off_09, off_10, off_11, off_12, off_13, off_14, off_15);
        int j = 0;
        for (ModelFile model : modelFileListOff) {
            getVariantBuilder(block)
                    .partialState().with(SignalGateBlock.FACING, Direction.UP).with(SignalGateBlock.POWER, j).with(SignalGateBlock.POWERED, false).modelForState().modelFile(model).rotationX(270).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.DOWN).with(SignalGateBlock.POWER, j).with(SignalGateBlock.POWERED, false).modelForState().modelFile(model).rotationX(90).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.NORTH).with(SignalGateBlock.POWER, j).with(SignalGateBlock.POWERED, false).modelForState().modelFile(model).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.EAST).with(SignalGateBlock.POWER, j).with(SignalGateBlock.POWERED, false).modelForState().modelFile(model).rotationY(90).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.SOUTH).with(SignalGateBlock.POWER, j).with(SignalGateBlock.POWERED, false).modelForState().modelFile(model).rotationY(180).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.WEST).with(SignalGateBlock.POWER, j).with(SignalGateBlock.POWERED, false).modelForState().modelFile(model).rotationY(270).addModel();
            j++;
        }

        List<ModelFile> modelFileList = Arrays.asList(on_00, on_01, on_02, on_03, on_04, on_05, on_06, on_07, on_08, on_09, on_10, on_11, on_12, on_13, on_14, on_15);
        int i = 0;
        for (ModelFile model : modelFileList) {
            getVariantBuilder(block)
                    .partialState().with(SignalGateBlock.FACING, Direction.UP).with(SignalGateBlock.POWER, i).with(SignalGateBlock.POWERED, true).modelForState().modelFile(model).rotationX(270).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.DOWN).with(SignalGateBlock.POWER, i).with(SignalGateBlock.POWERED, true).modelForState().modelFile(model).rotationX(90).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.NORTH).with(SignalGateBlock.POWER, i).with(SignalGateBlock.POWERED, true).modelForState().modelFile(model).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.EAST).with(SignalGateBlock.POWER, i).with(SignalGateBlock.POWERED, true).modelForState().modelFile(model).rotationY(90).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.SOUTH).with(SignalGateBlock.POWER, i).with(SignalGateBlock.POWERED, true).modelForState().modelFile(model).rotationY(180).addModel()
                    .partialState().with(SignalGateBlock.FACING, Direction.WEST).with(SignalGateBlock.POWER, i).with(SignalGateBlock.POWERED, true).modelForState().modelFile(model).rotationY(270).addModel();
            i++;
        }

        simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
    }

    private void redstoneLaneWithItem(RedstoneLaneBlock block) {
        ResourceLocation base_lane = new ResourceLocation(DungeonNowLoading.MOD_ID + ":block/redstone_lane");
        ResourceLocation side = extend(base_lane, "_side");
        ResourceLocation front = extend(base_lane, "_front");
        ResourceLocation front_powered = extend(base_lane, "_front_powered");
        ResourceLocation front_overpowered = extend(base_lane, "_front_overpowered");
        ResourceLocation bottom = extend(base_lane, "_bottom");
        ResourceLocation top = extend(blockTexture(block), "_top_unpowered");
        ResourceLocation top_powered = extend(blockTexture(block), "_top_powered");
        ResourceLocation top_overpowered = extend(blockTexture(block), "_top_overpowered");

        ModelFile unpowered = models().cube(name(block) + "_unpowered", bottom, top, front, front, side, side).texture("particle", top);
        ModelFile powered = models().cube(name(block) + "_powered", bottom, top_powered, front_powered, front_powered, side, side).texture("particle", top_powered);
        ModelFile overpowered= models().cube(name(block) + "_overpowered", bottom, top_overpowered, front_overpowered, front_overpowered, side, side).texture("particle", top_overpowered);
        if (name(block).equals("redstone_lane_l")) {
            unpowered = models().cube(name(block) + "_unpowered", bottom, top, side, front, front, side).texture("particle", top);
            powered = models().cube(name(block) + "_powered", bottom, top_powered, side, front_powered, front_powered, side).texture("particle", top_powered);
            overpowered = models().cube(name(block) + "_overpowered", bottom, top_overpowered, side, front_overpowered, front_overpowered, side).texture("particle", top_overpowered);
        } else if (name(block).equals("redstone_lane_t")) {
            unpowered = models().cube(name(block) + "_unpowered", bottom, top, side, front, front, front).texture("particle", top);
            powered = models().cube(name(block) + "_powered", bottom, top_powered, side, front_powered, front_powered, front_powered).texture("particle", top_powered);
            overpowered = models().cube(name(block) + "_overpowered", bottom, top_overpowered, side, front_overpowered, front_overpowered, front_overpowered).texture("particle", top_overpowered);
        }

        if (name(block).equals("redstone_lane_i")) {
            getVariantBuilder(block)
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.NORTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.EAST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).rotationY(90).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.SOUTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.WEST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).rotationY(90).addModel()

                    .partialState().with(RedstoneLaneBlock.FACING, Direction.NORTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.EAST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).rotationY(90).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.SOUTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.WEST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).rotationY(90).addModel()

                    .partialState().with(RedstoneLaneBlock.FACING, Direction.NORTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.EAST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).rotationY(90).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.SOUTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.WEST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).rotationY(90).addModel();
        } else {
            getVariantBuilder(block)
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.NORTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).rotationY(180).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.EAST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).rotationY(270).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.SOUTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.WEST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.UNPOWERED).modelForState().modelFile(unpowered).rotationY(90).addModel()

                    .partialState().with(RedstoneLaneBlock.FACING, Direction.NORTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).rotationY(180).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.EAST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).rotationY(270).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.SOUTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.WEST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.POWERED).modelForState().modelFile(powered).rotationY(90).addModel()

                    .partialState().with(RedstoneLaneBlock.FACING, Direction.NORTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).rotationY(180).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.EAST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).rotationY(270).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.SOUTH).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).addModel()
                    .partialState().with(RedstoneLaneBlock.FACING, Direction.WEST).with(RedstoneLaneBlock.REDSTONE_LANE_MODE, RedstoneLaneMode.OVERPOWERED).modelForState().modelFile(overpowered).rotationY(90).addModel();

        }
        simpleBlockItem(block, models().getExistingFile(extend(blockTexture(block), "_unpowered")));
    }

    private void wallBlockWithItem(WallBlock block , Block parent) {
        wallBlock(block, blockTexture(parent));
        itemModels().withExistingParent(ForgeRegistries.BLOCKS.getKey(block).getPath(), mcLoc("block/wall_inventory"))
                .texture("wall",  new ResourceLocation(DungeonNowLoading.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(parent).getPath()));
    }

    private void wallBlockWithItem(WallBlock block) {
        wallBlock(block, blockTexture(block));
        //itemModels().getBuilder(name(block)).texture(name(block), blockTexture(block));
        //simpleBlockItem(block, models().getExistingFile(blockTexture(block)));
        itemModels().withExistingParent(ForgeRegistries.BLOCKS.getKey(block).getPath(), mcLoc("block/wall_inventory"))
                .texture("wall",  new ResourceLocation(DungeonNowLoading.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(block).getPath()));
    }

    // --- Helpers: rotations for 6-way FACING
    private static int xRotFor(Direction f) {
        return switch (f) {
            case SOUTH, WEST, NORTH, EAST -> 0;
            case UP -> 270;
            case DOWN -> 90;
        };
    }
    private static int yRotFor(Direction f) {
        return switch (f) {
            case NORTH -> 0;
            case EAST  -> 90;
            case SOUTH -> 180;
            case WEST  -> 270;
            case UP, DOWN -> 0;
        };
    }

    // --- Texture resolver for base (randomized 0..5)
    private ResourceLocation baseTex(String baseName, int variant, boolean lit) {
        // e.g. block/mendstone_chalk_mark_base_3[_lit].png
        return modLoc("block/" + baseName + "_base_" + variant + (lit ? "_lit" : ""));
    }

    // --- Texture resolver for outline stage (0..3 only)
    private ResourceLocation outlineTex(String baseName, int outlineStage, boolean lit) {
        // e.g. block/mendstone_chalk_mark_outline_2[_lit].png
        return modLoc("block/" + baseName + "_outline_" + outlineStage + (lit ? "_lit" : ""));
    }

    /**
     * Build a composite thin-slab model:
     *  - base = randomized variant texture
     *  - if outlineStage in [0..3], render exactly one overlay "outline_<stage>"
     *  - if outlineStage == 4, render no overlay
     */
    private BlockModelBuilder mendingChalkMarkCompositeExact(String modelName,
                                                             ResourceLocation base,
                                                             @Nullable ResourceLocation outline /* null => none */) {

        final float eps   = 0.001f;
        final float thick = 0.002f;

        // Base slab hugging SOUTH face
        float fx = 0, fy = 0, fz = 16 - eps - thick, tx = 16, ty = 16, tz = 16 - eps;

        BlockModelBuilder b = models().getBuilder(modelName)
                .ao(false)
                .renderType("cutout")
                .texture("particle", base)
                .texture("base", base);

        // 1) Base element (front=SOUTH, back=NORTH mirrored)
        {
            var elem = b.element().from(fx, fy, fz).to(tx, ty, tz);
            // Flip horizontally on the outward SOUTH face: (U1=16 → U2=0)
            elem.face(Direction.SOUTH).uvs(16, 0, 0, 16).texture("#base").end();
            // Back face now uses normal mapping so it aligns after the front flip
            elem.face(Direction.NORTH).uvs(0, 0, 16, 16).texture("#base").end();
            elem.end();
        }

        // 2) Single outline overlay (if provided)
        if (outline != null) {
            b.texture("outline", outline);
            float z = (16 - eps - thick) + 0.00025f;
            var elem = b.element().from(0, 0, z).to(16, 16, z);
            // Flip horizontally on SOUTH
            elem.face(Direction.SOUTH).uvs(16, 0, 0, 16).texture("#outline").end();
            // Back face normal to match
            elem.face(Direction.NORTH).uvs(0, 0, 16, 16).texture("#outline").end();
            elem.end();
        }

        return b;
    }

    /**
     * Datagen for: MendstoneChalkMarkBlock
     * - Base randomized across 6 variants (0..5), lit-aware
     * - OUTLINE: 0..3 => use that outline stage; 4 => no overlay
     * Block has: FACING (6), LIT (bool), OUTLINE (0..4)
     */
    private void generateMendstoneChalkMarkModels(MendstoneChalkMarkBlock block, Item item, IntegerProperty OUTLINE) {
        final String baseName = name(block); // e.g., "mendstone_chalk_mark"
        final String itemName = ForgeRegistries.ITEMS.getKey(item).getPath();

        var vb = getVariantBuilder(block);

        for (Direction f : Direction.values()) {
            int xr = xRotFor(f), yr = yRotFor(f);

            for (boolean lit : new boolean[]{false, true}) {
                for (int outline = 0; outline <= 4; outline++) { // 0..3 => overlay, 4 => none

                    ConfiguredModel[] variants = new ConfiguredModel[6];

                    for (int v = 0; v < 6; v++) {
                        ResourceLocation base = baseTex(baseName, v, lit);
                        ResourceLocation outlineTex =
                                (outline <= 3) ? outlineTex(baseName, outline, lit) : null;

                        String modelId = baseName
                                + "_lit" + (lit ? "1" : "0")
                                + "_outline" + outline
                                + "_base_" + v;

                        ModelFile mf = mendingChalkMarkCompositeExact(modelId, base, outlineTex);
                        variants[v] = new ConfiguredModel(mf, xr, yr, false, 1); // equal weight
                    }

                    vb.partialState()
                            .with(BlockStateProperties.FACING, f)
                            .with(BlockStateProperties.LIT, lit)
                            .with(OUTLINE, outline)
                            .addModels(variants);
                }
            }
        }

        // Item: base_0 + a fixed outline_2 overlay
        itemModels().withExistingParent(itemName, mcLoc("item/generated"))
                .texture("layer0", baseTex(baseName, 0, false))                 // base
                .texture("layer1", outlineTex(baseName, /*stage*/ 0, false));   // overlay
    }

    private void fairkeeperSpawnerWithItem(FairkeeperSpawnerBlock block) {
        ResourceLocation side_on = extend(blockTexture(block), "_on");
        ResourceLocation side_off = extend(blockTexture(block), "_off");
        ResourceLocation top = extend(blockTexture(block), "_top");

        ModelFile on = models().cubeBottomTop(name(block) + "_on", side_on, top, top).renderType("cutout");
        ModelFile off = models().cubeBottomTop(name(block) + "_off", side_off, top, top).renderType("cutout");

        getVariantBuilder(block)
                .partialState().with(DNLProperties.FAIRKEEPER_ALERT, Boolean.TRUE).modelForState().modelFile(on).addModel()
                .partialState().with(DNLProperties.FAIRKEEPER_ALERT, Boolean.FALSE).modelForState().modelFile(off).addModel();

        simpleBlockItem(block, models().getExistingFile(extend(blockTexture(block), "_off")));
    }
// inside your DNLBlockStateProvider

    private void multifaceBlockWithItem(Block block, String textureName) {
        String name = key(block).getPath();
        ResourceLocation texture = modLoc("block/" + textureName); // block/web_carpet

        ModelFile model = models()
                .withExistingParent(name, mcLoc("block/glow_lichen"))
                .texture("glow_lichen", texture) // <- IMPORTANT
                .texture("particle", texture);   // item particles too

        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);

        for (Direction dir : Direction.values()) {
            BooleanProperty prop = MultifaceBlock.getFaceProperty(dir);

            int xRot = 0;
            int yRot = 0;
            switch (dir) {
                case NORTH -> { xRot = 0;   yRot = 0;   }
                case EAST  -> { xRot = 0;   yRot = 90;  }
                case SOUTH -> { xRot = 0;   yRot = 180; }
                case WEST  -> { xRot = 0;   yRot = 270; }
                case UP    -> { xRot = 270; yRot = 0;   }
                case DOWN  -> { xRot = 90;  yRot = 0;   }
            }

            builder.part()
                    .modelFile(model)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .uvLock(true)
                    .addModel()
                    .condition(prop, true);
        }

        simpleBlockItem(block, model);
    }

    private void multifaceWebCarpet(Block block) {
        String name = key(block).getPath();

        // === Model files for normal & burning ===
        ModelFile normal = models()
                .withExistingParent(name, mcLoc("block/glow_lichen"))
                .texture("glow_lichen", modLoc("block/web_carpet"))
                .texture("particle", modLoc("block/web_carpet"));

        ModelFile burning = models()
                .withExistingParent(name + "_burning", mcLoc("block/glow_lichen"))
                .texture("glow_lichen", modLoc("block/web_carpet_burning"))
                .texture("particle", modLoc("block/web_carpet_burning"));

        // === Multipart blockstate builder ===
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);

        for (Direction dir : Direction.values()) {
            BooleanProperty faceProp = MultifaceBlock.getFaceProperty(dir);

            if (faceProp == null) continue; // Safety

            int xRot = switch (dir) {
                case UP -> 270;
                case DOWN -> 90;
                default -> 0;
            };
            int yRot = switch (dir) {
                case NORTH -> 0;
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };

            // Normal face
            builder.part()
                    .modelFile(normal)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .uvLock(true)
                    .addModel()
                    .condition(faceProp, true)
                    .condition(WebCarpetBlock.BURNING, false);

            // Burning face
            builder.part()
                    .modelFile(burning)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .uvLock(true)
                    .addModel()
                    .condition(faceProp, true)
                    .condition(WebCarpetBlock.BURNING, true);
        }

        // === Item model generation ===
        simpleItem(block);
    }

    private void burnacleSixWayWithStages(Block block) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction f = state.getValue(BlockStateProperties.FACING);
            BurnacleBlock.Stage stage = state.getValue(BurnacleBlock.STAGE);

            String modelName = switch (stage) {
                case BUD      -> "burnacle_bud";
                case JUVENILE -> "burnacle_juvenile";
                case MATURE   -> "burnacle_mature";
            };

            ModelFile model = models().getExistingFile(modLoc("block/" + modelName));

            int x = 0, y = 0;
            switch (f) {
                case UP    -> { x = 0;   y = 0;   }
                case DOWN  -> { x = 180; y = 0;   }
                case NORTH -> { x = 90;  y = 0;   }
                case SOUTH -> { x = 90;  y = 180; }
                case WEST  -> { x = 90;  y = 270; }
                case EAST  -> { x = 90;  y = 90;  }
            }

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(x)
                    .rotationY(y)
                    .build();
        });

        // Item model: use burnacle_bud block model as parent (has handheld transforms)
        simpleBlockItem(block, models().getExistingFile(modLoc("block/burnacle_bud")));
    }






    private void facingSixWayWithExistingModel(Block block, String modelName) {
        ModelFile model = models().getExistingFile(modLoc("block/" + modelName));

        getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction f = state.getValue(BlockStateProperties.FACING);
            int x = 0, y = 0;

            switch (f) {
                case UP    -> { x = 0;   y = 0;   }
                case DOWN  -> { x = 180; y = 0;   }
                case NORTH -> { x = 90;  y = 0;   }
                case SOUTH -> { x = 90;  y = 180; }
                case WEST  -> { x = 90;  y = 270; }
                case EAST  -> { x = 90;  y = 90;  }
            }

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(x)
                    .rotationY(y)
                    .build();
        }, BlockStateProperties.WATERLOGGED);

        simpleItem(block);
    }

    private void modelFromParent(Block block, String parentModelName, String textureKey, ResourceLocation texture) {
        String n = name(block);

        // models/block/<blockname>.json with parent and texture override
        ModelFile mf = models()
                .withExistingParent(n, modLoc("block/" + parentModelName))
                .texture(textureKey, texture)
                .texture("particle", texture);

        // blockstate uses that model
        simpleBlock(block, mf);

        // item model just points to the block model
        itemModels().withExistingParent(n, modLoc("block/" + n));
    }

    private void modelFromParent(Block block, String parentModelName, Map<String, ResourceLocation> textures) {
        String n = name(block);

        BlockModelBuilder b = models()
                .withExistingParent(n, modLoc("block/" + parentModelName));

        // Apply all provided key → texture overrides
        textures.forEach(b::texture);

        // Choose a decent particle if present; else fall back to any entry
        ResourceLocation particle = textures.getOrDefault("all",
                textures.values().stream().findFirst().orElseGet(() -> blockTexture(block)));
        b.texture("particle", particle);

        simpleBlock(block, b);
        itemModels().withExistingParent(n, modLoc("block/" + n));
    }

    private void horizontalModelFromParent(Block block, String parentModelName, String textureKey, ResourceLocation blockTexture) {
        horizontalModelFromParent(block, parentModelName, textureKey, blockTexture, (ResourceLocation[]) null);
    }

    private void horizontalModelFromParent(Block block,
                                           String parentModelName,
                                           String textureKey,
                                           ResourceLocation blockTexture,
                                           @Nullable ResourceLocation... itemTextures) {
        String n = name(block);

        // Block model
        ModelFile mf = models()
                .withExistingParent(n, modLoc("block/" + parentModelName))
                .texture(textureKey, blockTexture)
                .texture("particle", blockTexture);

        // Blockstate: rotate by Horizontal FACING
        horizontalBlock(block, mf);

        // Item model:
        if (itemTextures != null && itemTextures.length > 0) {
            // Build a fresh item model with custom layers
            ItemModelBuilder ib = itemModels()
                    .withExistingParent(n, mcLoc("item/generated"));
            for (int i = 0; i < itemTextures.length; i++) {
                ib = ib.texture("layer" + i, itemTextures[i]);
            }
        } else {
            // Fallback to the block model
            simpleBlockItem(block, mf);
        }
    }
    private void dungeonDirectorBlock(Block block) {
        ModelFile normal = models()
                .cubeAll("dungeon_director", modLoc("block/dungeon_director")).renderType("cutout");
        ModelFile remove = models()
                .cubeAll("dungeon_director_remove", modLoc("block/dungeon_director_remove")).renderType("cutout");

        getVariantBuilder(block)
                .partialState().with(DungeonDirectorBlock.REMOVE_AFTER_SUMMON, false)
                .modelForState().modelFile(normal).addModel()
                .partialState().with(DungeonDirectorBlock.REMOVE_AFTER_SUMMON, true)
                .modelForState().modelFile(remove).addModel();

        // Item model uses normal
        simpleBlockItem(block, normal);
    }

    private void railPlatformStates(Block block) {
        ModelFile normal = models().getExistingFile(modLoc("block/rail_platform"));
        ModelFile raised = models().getExistingFile(modLoc("block/rail_platform_raised"));

        getVariantBuilder(block).forAllStates(state -> {
            Direction dir = state.getValue(RailPlatformBlock.FACING);
            boolean isRaised = state.getValue(RailPlatformBlock.RAISED);

            ModelFile model = isRaised ? raised : normal;

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY((int) dir.toYRot())
                    .build();
        });
    }


    private void horizontalExistingModel(Block block, String existingBlockModelName) {
        String n = name(block); // usually the registry path, ex: "rail_platform"

        ModelFile existingModel = models().getExistingFile(
                modLoc("block/" + existingBlockModelName)
        );

        horizontalBlock(block, existingModel);
    }

    private void particleOnlyModel(Block block) {
        String name = ForgeRegistries.BLOCKS.getKey(block).getPath();
        String ns   = ForgeRegistries.BLOCKS.getKey(block).getNamespace();

        // models/block/<name>_particle.json → parent builtin/entity + particle tex
        ModelFile model = models().getBuilder(name + "_particle")
                .parent(new ModelFile.UncheckedModelFile("minecraft:builtin/entity"))
                .texture("particle", new ResourceLocation(ns, "block/" + name + "_particle"));

        // blockstates/<name>.json → map every state (rotation_16, waterlogged, …) to that model
        getVariantBuilder(block).forAllStates(s -> new ConfiguredModel[]{new ConfiguredModel(model)});
    }

    private ResourceLocation key(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    public String name(Block block) {
        return key(block).getPath();
    }

    private ResourceLocation extend(ResourceLocation rl, String suffix) {
        return new ResourceLocation(rl.getNamespace(), rl.getPath() + suffix);
    }
}
