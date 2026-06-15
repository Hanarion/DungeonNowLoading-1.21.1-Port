package dev.hexnowloading.dungeonnowloading.datagen.provider;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DNLForgeItemModelProvider extends ItemModelProvider {

    private static ModelFile ITEM_GENERATED;

    public DNLForgeItemModelProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen.getPackOutput(), DungeonNowLoading.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ITEM_GENERATED = getExistingFile(mcLoc("item/generated"));

        simpleItem(DNLItems.REDSTONE_SUPPRESSOR.get());
        simpleItem(DNLItems.REDSTONE_CORE.get());
        simpleItem(DNLItems.REDSTONE_CHIP.get());
        simpleItem(DNLItems.REDSTONE_CIRCUIT.get());
        simpleItem(DNLItems.COMBUSTION_CELL.get());
        simpleItem(DNLItems.CATALYZED_REDSTONE.get());
        simpleItem(DNLItems.REDSTONE_IDOL.get());
        simpleItem(DNLItems.FAIRKEEPER_SERPENT_CALLER.get());
        simpleItem(DNLItems.MUSIC_DISC_AOTSUGI.get());
        simpleItem(DNLItems.MUSIC_DISC_BROKEN_AOTSUGI.get());
        simpleItem(DNLItems.MUSIC_DISC_HELLSPAWN.get());
        simpleItem(DNLItems.MUSIC_DISC_OUROS.get());
        simpleItem(DNLItems.MUSIC_DISC_BOROS.get());
        simpleItem(DNLItems.MUSIC_DISC_PYTHONIC_OVERDRIVE.get());
        //simpleItem(DNLItems.COPPER_DETONATOR.get());
        simpleItem(DNLItems.MENDSTONE_CHALK.get());
        simpleItem(DNLItems.ZONE_WAND.get());
        simpleItem(DNLItems.ZONE_WAND.get());
        simpleItem(DNLItems.MIMICART.get());
        //spawnEggItem(DNLItems.FAIRKEEPER_SPAWNEGG.get());
        //spawnEggItem(DNLItems.FAIRKEEPER_OUROS_SPAWNEGG.get());
        spawnEggItem(DNLItems.SCUTTLE_SPAWNEGG.get());
        spawnEggItem(DNLItems.BALLISTA_GOLEM_SPAWNEGG.get());
        spawnEggItem(DNLItems.GARHOLD_SPAWNEGG.get());
        spawnEggItem(DNLItems.WISP_SPAWNEGG.get());
        spawnEggItem(DNLItems.WISP_LANTERN_SPAWNEGG.get());
        spawnEggItem(DNLItems.SILK_SPIDER_SPAWNEGG.get());
        spawnEggItem(DNLItems.REAPER_SPIDER_SPAWNEGG.get());
        fourStageBowItem(DNLItems.VERTEX_BOW.get(), 0.65f, 0.9f, 1.5f);
        fourStageBowItem(DNLItems.VERTEX_BOW.get(), 0.43f, 0.6f, 1.0f);
        booleanPropertyItem(DNLItems.COPPER_DETONATOR.get(), "mode_switch", "copper_detonator", "copper_detonator_switched");
        booleanPropertyItem(DNLItems.REPULSOR.get(), "golden_mode", "repulsor", "repulsor_golden");
        PlayerStatueItemWithDisplay(DNLItems.PLAYER_STATUE.get());
    }

    private void simpleItem(Item item) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();
        withExistingParent(ITEM_FOLDER + "/" + name, mcLoc(ITEM_FOLDER + "/generated")).texture("layer0", ITEM_FOLDER + "/" + name);
    }

    private void spawnEggItem(Item item) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();
        withExistingParent(ITEM_FOLDER + "/" + name, mcLoc(ITEM_FOLDER + "/template_spawn_egg"));
    }

    private void generatedItem(Item item) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();
        singleTexture(name, mcLoc("item/generated"), "layer0", modLoc("item/" + name));

    }

    private void booleanPropertyItem(Item item, String propertyName, String baseTexture, String overrideTexture) {
        String itemName = ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();

        getBuilder(overrideTexture)
                .parent(ITEM_GENERATED)
                .texture("layer0", modLoc("item/" + overrideTexture));

        getBuilder(itemName)
                .parent(ITEM_GENERATED)
                .texture("layer0", modLoc("item/" + baseTexture))
                .override()
                .predicate(new ResourceLocation(propertyName), 1.0F)
                .model(getExistingFile(modLoc("item/" + overrideTexture)));
    }


    private void fourStageBowItem(Item item, float pulling1, float pulling2, float pulling3) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();

        registerBowPullingModel(name, 0);
        registerBowPullingModel(name, 1);
        registerBowPullingModel(name, 2);
        registerBowPullingModel(name, 3);

        withExistingParent(ITEM_FOLDER + "/" + name, mcLoc(ITEM_FOLDER + "/bow"))
                .texture("layer0", modLoc(ITEM_FOLDER + "/" + name))
                .override()
                .predicate(mcLoc("pulling"), 1)
                .model(getExistingFile(modLoc(ITEM_FOLDER + "/" + name + "_pulling_0")))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1)
                .predicate(mcLoc("pull"), pulling1)
                .model(getExistingFile(modLoc(ITEM_FOLDER + "/" + name + "_pulling_1")))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1)
                .predicate(mcLoc("pull"), pulling2)
                .model(getExistingFile(modLoc(ITEM_FOLDER + "/" + name + "_pulling_2")))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1)
                .predicate(mcLoc("pull"), pulling3)
                .model(getExistingFile(modLoc(ITEM_FOLDER + "/" + name + "_pulling_3")))
                .end()
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(0, -90, 55)
                .translation(0, 4, 2)
                .scale(0.85f, 0.85f, 0.85f)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(-80, -280, 40)
                .translation(-1f, -2f, 2.5f)
                .scale(0.9f, 0.9f, 0.9f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -90, 25)
                .translation(1.13f, 3.2f, 1.13f)
                .scale(0.68f, 0.68f, 0.68f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 90, -25 )
                .translation(1.13f, 3.2f, 1.13f)
                .scale(0.68f, 0.68f, 0.68f)
                .end();
    }

    private void simpleBowItem(Item item) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();

        registerBowPullingModel(name, 0);
        registerBowPullingModel(name, 1);
        registerBowPullingModel(name, 2);

        withExistingParent(ITEM_FOLDER + "/" + name, mcLoc(ITEM_FOLDER + "/bow"))
                .texture("layer0", modLoc(ITEM_FOLDER + "/" + name))
                .override()
                .predicate(mcLoc("pulling"), 1)
                .model(getExistingFile(modLoc(ITEM_FOLDER + "/" + name + "_pulling_0")))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1)
                .predicate(mcLoc("pull"), 0.65f)
                .model(getExistingFile(modLoc(ITEM_FOLDER + "/" + name + "_pulling_1")))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1)
                .predicate(mcLoc("pull"), 0.9f)
                .model(getExistingFile(modLoc(ITEM_FOLDER + "/" + name + "_pulling_2")))
                .end()
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(0, -90, 55)
                .translation(0, 4, 2)
                .scale(0.85f, 0.85f, 0.85f)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(-80, -280, 40)
                .translation(-1f, -2f, 2.5f)
                .scale(0.9f, 0.9f, 0.9f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -90, 25)
                .translation(1.13f, 3.2f, 1.13f)
                .scale(0.68f, 0.68f, 0.68f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 90, -25 )
                .translation(1.13f, 3.2f, 1.13f)
                .scale(0.68f, 0.68f, 0.68f)
                .end();
    }

    private void registerBowPullingModel(String baseName, int pullingStage) {
        getBuilder(baseName + "_pulling_" + pullingStage)
                .parent(getExistingFile(mcLoc("item/bow")))
                .texture("layer0", modLoc("item/" + baseName + "_pulling_" + pullingStage));
    }
    /*private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(DungeonNowLoading.MOD_ID, "item/" + item.getId().getPath()));
    }*/

    public void evenSimplerBlockItem(RegistryObject<Block> block) {
        this.withExistingParent(DungeonNowLoading.MOD_ID + ":" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(DungeonNowLoading.MOD_ID,"item/" + item.getId().getPath()));
    }

    private void builtinEntityItem(Item item) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();

        ItemModelBuilder b = getBuilder(ITEM_FOLDER + "/" + name)
                // builtin/entity is virtual → UncheckedModelFile
                .parent(new ModelFile.UncheckedModelFile("minecraft:builtin/entity"));

        // Apply vanilla "item/block" default transforms
        b.transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(75, 45, 0)
                .translation(0f, 2.5f, 0f)
                .scale(0.375f, 0.375f, 0.375f)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(75, 45, 0)
                .translation(0f, 2.5f, 0f)
                .scale(0.375f, 0.375f, 0.375f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 45, 0)
                .scale(0.4f, 0.4f, 0.4f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 225, 0)
                .scale(0.4f, 0.4f, 0.4f)
                .end()
                .transform(ItemDisplayContext.GROUND)
                .translation(0f, 3f, 0f)
                .scale(0.25f, 0.25f, 0.25f)
                .end()
                .transform(ItemDisplayContext.GUI)
                .rotation(30, 225, 0)
                .scale(0.625f, 0.625f, 0.625f)
                .end()
                .transform(ItemDisplayContext.FIXED)
                .scale(0.5f, 0.5f, 0.5f)
                .end()
                .end();
    }

    private void PlayerStatueItemWithDisplay(Item item) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();

        ItemModelBuilder b = getBuilder(ITEM_FOLDER + "/" + name)
                // builtin/entity is virtual → UncheckedModelFile
                .parent(new ModelFile.UncheckedModelFile("minecraft:builtin/entity"));

        b.transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(75, 135, 0)
                .translation(0f, 2.0f, 0f)
                .scale(0.375f, 0.375f, 0.375f)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(75, 135, 0)
                .translation(0f, 2.0f, 0f)
                .scale(0.375f, 0.375f, 0.375f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 135, 0)
                .scale(0.38f, 0.38f, 0.38f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 135, 0)
                .scale(0.38f, 0.38f, 0.38f)
                .end()
                .transform(ItemDisplayContext.GROUND)
                .translation(0f, 2.0f, 0f)
                .scale(0.22f, 0.22f, 0.22f)
                .end()
                .transform(ItemDisplayContext.GUI)
                .rotation(30, 225, 0)   // classic block angle
                .translation(0.0f, -3.0f, 0f) // slight lift to center vertically
                .scale(0.35f, 0.35f, 0.35f) // fits cleanly in the slot
                .end()
                .transform(ItemDisplayContext.FIXED)
                .rotation(-90, 180, 0)
                .translation(0.0f, 0.0f, -6f)
                .scale(1f, 1f, 1f)
                .end()
                .end();
    }

    private void itemFromExistingModel(Item item, String existingModelPath) {
        String name = ForgeRegistries.ITEMS.getKey(item).getPath();

        getBuilder(ITEM_FOLDER + "/" + name)
                .parent(getExistingFile(modLoc(existingModelPath)));
    }

}
