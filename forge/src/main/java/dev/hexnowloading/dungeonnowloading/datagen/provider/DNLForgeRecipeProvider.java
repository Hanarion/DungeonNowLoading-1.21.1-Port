package dev.hexnowloading.dungeonnowloading.datagen.provider;

import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class DNLForgeRecipeProvider extends RecipeProvider {

    public DNLForgeRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        buildShapedRecipes(consumer);
        buildStoneCutterRecipes(consumer);
        buildSmeltingRecipes(consumer);
    }

    private void buildShapedRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DNLItems.REDSTONE_CIRCUIT.get(), 1)
                .pattern("rpi")
                .pattern("pss")
                .pattern("gss")
                .define('s', DNLItems.REDSTONE_CHIP.get())
                .define('r', Items.REDSTONE_BLOCK)
                .define('p', Items.REPEATER)
                .define('i', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('g', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_redstone_chip", has(DNLItems.REDSTONE_CHIP.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DNLItems.REDSTONE_CORE.get(), 1)
                .pattern("srs")
                .pattern("rrr")
                .pattern("srs")
                .define('s', DNLItems.REDSTONE_SUPPRESSOR.get())
                .define('r', Items.REDSTONE_BLOCK)
                .unlockedBy("has_redstone_suppressor", has(DNLItems.REDSTONE_SUPPRESSOR.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR.get(), 2)
                .pattern("s")
                .pattern("s")
                .define('s', Items.STONE_BRICKS)
                .unlockedBy("has_stone_bricks", has(Items.STONE_BRICKS))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_SLAB.get(), 6)
                .pattern("ppp")
                .define('p', DNLItems.COILING_STONE_PILLAR.get())
                .unlockedBy("has_coiling_stone_pillar", has(DNLItems.COILING_STONE_PILLAR.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_STAIRS.get(), 4)
                .pattern("p  ")
                .pattern("pp ")
                .pattern("ppp")
                .define('p', DNLItems.COILING_STONE_PILLAR.get())
                .unlockedBy("has_coiling_stone_pillar", has(DNLItems.COILING_STONE_PILLAR.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.CHISELED_COILING_STONE_PILLAR.get(), 1)
                .pattern("s")
                .pattern("s")
                .define('s', DNLItems.COILING_STONE_PILLAR_SLAB.get())
                .unlockedBy("has_coiling_stone_pillar", has(DNLItems.COILING_STONE_PILLAR.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_CAPITAL.get(), 5)
                .pattern("ppp")
                .pattern("p p")
                .define('p', DNLItems.COILING_STONE_PILLAR.get())
                .unlockedBy("has_coiling_stone_pillar", has(DNLItems.COILING_STONE_PILLAR.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_WALL.get(), 6)
                .pattern("ppp")
                .pattern("ppp")
                .define('p', DNLItems.COILING_STONE_PILLAR.get())
                .unlockedBy("has_coiling_stone_pillar", has(DNLItems.COILING_STONE_PILLAR.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILES.get(), 4)
                .pattern("ss")
                .pattern("ss")
                .define('s', Items.STONE_BRICKS)
                .unlockedBy("has_stone_bricks", has(Items.STONE_BRICKS))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILE_SLAB.get(), 6)
                .pattern("ttt")
                .define('t', DNLItems.STONE_TILES.get())
                .unlockedBy("has_stone_tiles", has(DNLItems.STONE_TILES.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILE_STAIRS.get(), 4)
                .pattern("t  ")
                .pattern("tt ")
                .pattern("ttt")
                .define('t', DNLItems.STONE_TILES.get())
                .unlockedBy("has_stone_tiles", has(DNLItems.STONE_TILES.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILE_WALL.get(), 6)
                .pattern("ttt")
                .pattern("ttt")
                .define('t', DNLItems.STONE_TILES.get())
                .unlockedBy("has_stone_tiles", has(DNLItems.STONE_TILES.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.SIGNALING_STONE_EMBLEM.get(), 8)
                .pattern("ccc")
                .pattern("crc")
                .pattern("ccc")
                .define('c', Items.CHISELED_STONE_BRICKS)
                .define('r', Items.REDSTONE_BLOCK)
                .unlockedBy("has_stone_bricks", has(Items.STONE_BRICKS))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.DUELING_STONE_EMBLEM.get(), 8)
                .pattern("ccc")
                .pattern("crc")
                .pattern("ccc")
                .define('c', Items.CHISELED_STONE_BRICKS)
                .define('r', Items.KELP)
                .unlockedBy("has_stone_bricks", has(Items.STONE_BRICKS))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.PUZZLING_STONE_EMBLEM.get(), 8)
                .pattern("ccc")
                .pattern("crc")
                .pattern("ccc")
                .define('c', Items.CHISELED_STONE_BRICKS)
                .define('r', Items.DRIED_KELP)
                .unlockedBy("has_stone_bricks", has(Items.STONE_BRICKS))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.POLISHED_STONE.get(), 9)
                .pattern("sss")
                .pattern("sss")
                .pattern("sss")
                .define('s', Items.STONE)
                .unlockedBy("has_stone", has(Items.STONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.BORDERED_STONE.get(), 8)
                .pattern("sss")
                .pattern("s s")
                .pattern("sss")
                .define('s', Items.STONE)
                .unlockedBy("has_stone", has(Items.STONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_NOTCH.get(), 8)
                .pattern("sss")
                .pattern("s s")
                .pattern("sss")
                .define('s', Items.STONE_BRICKS)
                .unlockedBy("has_stone_bricks", has(Items.STONE_BRICKS))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.OVERCHARGED_REDSTONE_BLOCK.get(), 1)
                .pattern("rbr")
                .pattern("bcb")
                .pattern("rbr")
                .define('c', DNLItems.REDSTONE_CORE.get())
                .define('r', Items.REDSTONE_BLOCK)
                .define('b', Items.BLAZE_POWDER)
                .unlockedBy("has_stone_bricks", has(Items.STONE_BRICKS))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.REDSTONE_LANE_I.get(), 3)
                .pattern("srs")
                .pattern("srs")
                .pattern("srs")
                .define('s', Items.STONE_BRICKS)
                .define('r', Items.REDSTONE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.REDSTONE_LANE_L.get(), 3)
                .pattern("sss")
                .pattern("srr")
                .pattern("srs")
                .define('s', Items.STONE_BRICKS)
                .define('r', Items.REDSTONE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.REDSTONE_LANE_T.get(), 3)
                .pattern("sss")
                .pattern("rrr")
                .pattern("srs")
                .define('s', Items.STONE_BRICKS)
                .define('r', Items.REDSTONE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.ROTATOR_PRESSURE_PLATE.get(), 1)
                .pattern("   ")
                .pattern(" p ")
                .pattern("rcr")
                .define('p', Items.STONE_PRESSURE_PLATE)
                .define('r', Items.REDSTONE)
                .define('c', DNLItems.REDSTONE_CHIP.get())
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.SIGNAL_GATE.get(), 1)
                .pattern("srs")
                .pattern("cct")
                .pattern("sss")
                .define('s', Items.COBBLESTONE)
                .define('r', Items.REDSTONE)
                .define('c', Items.COMPARATOR)
                .define('t', Items.REDSTONE_TORCH)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, DNLItems.VERTEX_BOW.get(), 1)
                .pattern("cts")
                .pattern("a r")
                .pattern("cts")
                .define('a', DNLItems.CATALYZED_REDSTONE.get())
                .define('c', DNLItems.REDSTONE_CHIP.get())
                .define('s', Items.STRING)
                .define('t', Items.STICK)
                .define('r', Items.REDSTONE)
                .unlockedBy("has_redstone_circuit", has(DNLItems.CATALYZED_REDSTONE.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, DNLItems.SCORCHER.get(), 1)
                .pattern("lrf")
                .pattern("scr")
                .pattern("bs ")
                .define('r', DNLItems.REDSTONE_SUPPRESSOR.get())
                .define('s', Items.COBBLESTONE)
                .define('l', Items.LEVER)
                .define('c', DNLItems.COMBUSTION_CELL.get())
                .define('f', Items.FIRE_CHARGE)
                .define('b', Items.BLAST_FURNACE)
                .unlockedBy("has_redstone_circuit", has(DNLItems.COMBUSTION_CELL.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, DNLItems.SOUL_SCORCHER.get(), 1)
                .pattern("lrf")
                .pattern("scr")
                .pattern("bs ")
                .define('r', DNLItems.REDSTONE_SUPPRESSOR.get())
                .define('s', Items.COBBLESTONE)
                .define('l', Items.LEVER)
                .define('c', DNLItems.COMBUSTION_CELL.get())
                .define('f', Items.SOUL_SAND)
                .define('b', Items.BLAST_FURNACE)
                .unlockedBy("has_redstone_circuit", has(DNLItems.COMBUSTION_CELL.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, DNLItems.COPPER_DETONATOR.get(), 1)
                .pattern(" r ")
                .pattern("csc")
                .pattern("cic")
                .define('i', DNLItems.REDSTONE_CIRCUIT.get())
                .define('c', Items.COPPER_INGOT)
                .define('s', Items.STONE_BUTTON)
                .define('r', Items.LIGHTNING_ROD)
                .unlockedBy("has_redstone_circuit", has(DNLItems.REDSTONE_CIRCUIT.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, DNLItems.REPULSOR.get(), 1)
                .pattern("e e")
                .pattern("cic")
                .pattern("brb")
                .define('i', DNLItems.REDSTONE_CIRCUIT.get())
                .define('c', Items.COPPER_INGOT)
                .define('b', Items.COPPER_BLOCK)
                .define('e', Items.LIGHTNING_ROD)
                .define('r', DNLItems.REDSTONE_CORE.get())
                .unlockedBy("has_redstone_circuit_or_core", has(DNLTags.REDSTONE_CIRCUIT_OR_CORE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, DNLItems.REDSTONE_IDOL.get(), 1)
                .pattern("rar")
                .pattern("brb")
                .pattern("rar")
                .define('a', DNLItems.REDSTONE_CIRCUIT.get())
                .define('b', DNLItems.REDSTONE_CORE.get())
                .define('r', Items.REDSTONE_BLOCK)
                .unlockedBy("has_redstone_circuit_or_core", has(DNLTags.REDSTONE_CIRCUIT_OR_CORE))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, DNLItems.MUSIC_DISC_PYTHONIC_OVERDRIVE.get(), 1)
                .requires(DNLItems.MUSIC_DISC_BOROS.get())
                .requires(DNLItems.MUSIC_DISC_OUROS.get())
                .unlockedBy("has_ouros_boros_music_disc", has(DNLTags.OUROS_BOROS_MUSIC_DISC))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DNLItems.MENDING_TABLE.get(), 1)
                .pattern("mom")
                .pattern(" b ")
                .pattern("iii")
                .define('m', DNLItems.MENDSTONE.get())
                .define('o', Items.OBSIDIAN)
                .define('b', Items.IRON_BLOCK)
                .define('i', Items.IRON_INGOT)
                .unlockedBy("has_mendstone", has(DNLItems.MENDSTONE.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DNLItems.MENDSTONE.get(), 1)
                .pattern("dl")
                .pattern("ld")
                .define('d', DNLItems.DURITE.get())
                .define('l', Items.LAPIS_LAZULI)
                .unlockedBy("has_durite", has(DNLItems.DURITE.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_PLANKS.get(), 4)
                .requires(DNLItems.AZURO_OAK_LOG.get())
                .unlockedBy("has_azuro_log", has(DNLItems.AZURO_OAK_LOG.get()))
                .save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_PLANK_SLAB.get(), 6)
                .pattern("ppp")
                .define('p', DNLItems.AZURO_OAK_PLANKS.get())
                .unlockedBy("has_azuro_planks", has(DNLItems.AZURO_OAK_PLANKS.get()))
                .save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_PLANK_STAIRS.get(), 4)
                .pattern("p  ")
                .pattern("pp ")
                .pattern("ppp")
                .define('p', DNLItems.AZURO_OAK_PLANKS.get())
                .unlockedBy("has_azuro_planks", has(DNLItems.AZURO_OAK_PLANKS.get()))
                .save(consumer);

        // Azuro fence: vanilla-style shaped recipe (psp / psp -> 3 fences)
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_PLANK_FENCE.get(), 3)
                .pattern("psp")
                .pattern("psp")
                .define('p', DNLItems.AZURO_OAK_PLANKS.get())
                .define('s', Items.STICK)
                .unlockedBy("has_azuro_planks", has(DNLItems.AZURO_OAK_PLANKS.get()))
                .save(consumer);

        // Azuro fence gate: vanilla-style shaped recipe (psp / psp -> 1 gate)
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_PLANK_FENCE_GATE.get(), 1)
                .pattern("sps")
                .pattern("sps")
                .define('p', DNLItems.AZURO_OAK_PLANKS.get())
                .define('s', Items.STICK)
                .unlockedBy("has_azuro_planks", has(DNLItems.AZURO_OAK_PLANKS.get()))
                .save(consumer);

        // Azuro button (wooden button) - 1 plank -> 1 button (keep shapeless)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_BUTTON.get(), 1)
                .requires(DNLItems.AZURO_OAK_PLANKS.get())
                .unlockedBy("has_azuro_planks", has(DNLItems.AZURO_OAK_PLANKS.get()))
                .save(consumer);

        // Azuro pressure plate - shaped 2 planks -> 1 plate
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_PRESSURE_PLATE.get(), 1)
                .pattern("pp")
                .define('p', DNLItems.AZURO_OAK_PLANKS.get())
                .unlockedBy("has_azuro_planks", has(DNLItems.AZURO_OAK_PLANKS.get()))
                .save(consumer);

        // Azuro door (3 doors) - 6 planks -> 3 doors
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DNLItems.AZURO_OAK_DOOR.get(), 3)
                .pattern("pp")
                .pattern("pp")
                .pattern("pp")
                .define('p', DNLItems.AZURO_OAK_PLANKS.get())
                .unlockedBy("has_azuro_planks", has(DNLItems.AZURO_OAK_PLANKS.get()))
                .save(consumer);
    }

    private void buildStoneCutterRecipes(Consumer<FinishedRecipe> consumer) {
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR.get(), Items.STONE_BRICKS, 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.CHISELED_COILING_STONE_PILLAR.get(), DNLItems.COILING_STONE_PILLAR.get(), 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_CAPITAL.get(), DNLItems.COILING_STONE_PILLAR.get(), 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_SLAB.get(), DNLItems.COILING_STONE_PILLAR.get(), 2);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_STAIRS.get(), DNLItems.COILING_STONE_PILLAR.get(), 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.COILING_STONE_PILLAR_WALL.get(), DNLItems.COILING_STONE_PILLAR.get(), 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILES.get(), Items.STONE_BRICKS, 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILE_STAIRS.get(), DNLItems.STONE_TILES.get(), 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILE_SLAB.get(), DNLItems.STONE_TILES.get(), 2);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_TILE_WALL.get(), DNLItems.STONE_TILES.get(), 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.SIGNALING_STONE_EMBLEM.get(), Items.STONE_BRICKS, 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.DUELING_STONE_EMBLEM.get(), Items.STONE_BRICKS, 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.PUZZLING_STONE_EMBLEM.get(), Items.STONE_BRICKS, 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.STONE_NOTCH.get(), Items.STONE_BRICKS, 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.POLISHED_STONE.get(), Items.STONE, 1);
        stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, DNLItems.BORDERED_STONE.get(), Items.STONE, 1);

    }

    private void buildSmeltingRecipes(Consumer<FinishedRecipe> consumer) {
        smeltingResultFromBase(consumer, DNLItems.CRACKED_STONE_TILES.get(), DNLItems.STONE_TILES.get());
    }

}
