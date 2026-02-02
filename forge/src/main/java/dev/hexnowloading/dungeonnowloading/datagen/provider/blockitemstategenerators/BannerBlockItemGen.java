package dev.hexnowloading.dungeonnowloading.datagen.provider.blockitemstategenerators;

import dev.hexnowloading.dungeonnowloading.datagen.provider.DNLForgeBlockStateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public class BannerBlockItemGen {
    private final DNLForgeBlockStateProvider p;

    public BannerBlockItemGen(DNLForgeBlockStateProvider p) {
        this.p = p;
    }

    public void dungeonBanners(Block bannerBlock) {
        dungeonBannerBlockstateAndModel(bannerBlock);
        dungeonBannerItemModels();
    }

    private void dungeonBannerBlockstateAndModel(Block bannerBlock) {
        String n = p.name(bannerBlock); // make name() public/protected in provider or duplicate a small helper

        ModelFile blockModel = p.models().getBuilder(n)
                .parent(new ModelFile.UncheckedModelFile("minecraft:builtin/entity"))
                .texture("particle", p.modLoc("block/" + n + "_particle"));

        p.getVariantBuilder(bannerBlock).forAllStates(s -> new ConfiguredModel[]{
                new ConfiguredModel(blockModel)
        });
    }

    private void dungeonBannerItemModels() {
        bannerItemModel("dungeon_banner_spawner_magenta");
        bannerItemModel("dungeon_banner_spawner_black");
        bannerItemModel("dungeon_banner_spawner_blue");
        bannerItemModel("dungeon_banner_spawner_purple");
        bannerItemModel("dungeon_banner_spawner_green");
        bannerItemModel("dungeon_banner_hollow");
        bannerItemModel("dungeon_banner_spawner_carrier");
        bannerItemModel("dungeon_banner_experience_bottle");
        bannerItemModel("dungeon_banner_chaos_spawner");
        bannerItemModel("dungeon_banner_whimper_lantern");
        bannerItemModel("dungeon_banner_garhold_upsidedown");
        bannerItemModel("dungeon_banner_skull_of_chaos");
    }

    private void bannerItemModel(String itemId) {
        var b = p.itemModels()
                .getBuilder(itemId)
                .parent(new ModelFile.UncheckedModelFile("minecraft:builtin/entity"));

        b.transforms()
                .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(75, 45, 0).translation(0f, 2.5f, 0f).scale(0.375f, 0.375f, 0.375f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(75, 45, 0).translation(0f, 2.5f, 0f).scale(0.375f, 0.375f, 0.375f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 45, 0).scale(0.4f, 0.4f, 0.4f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 225, 0).scale(0.4f, 0.4f, 0.4f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.GROUND)
                .translation(0f, 3f, 0f).scale(0.25f, 0.25f, 0.25f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.GUI)
                .rotation(30, 225, 0).scale(0.625f, 0.625f, 0.625f).end()
                .transform(net.minecraft.world.item.ItemDisplayContext.FIXED)
                .scale(0.5f, 0.5f, 0.5f).end()
                .end();
    }
}
