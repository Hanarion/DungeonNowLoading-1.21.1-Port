package dev.hexnowloading.dungeonnowloading.datagen.provider.blockitemstategenerators;

import dev.hexnowloading.dungeonnowloading.block.DungeonBannerBlock;
import dev.hexnowloading.dungeonnowloading.datagen.provider.DNLForgeBlockStateProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.Map;

public class BannerBlockItemGen {
    private final DNLForgeBlockStateProvider p;

    public static final Map<DungeonBannerBlock.DungeonBannerVariant, DeferredHolder<Block, Block>> DUNGEON_BANNERS = new EnumMap<>(DungeonBannerBlock.DungeonBannerVariant.class);


    public BannerBlockItemGen(DNLForgeBlockStateProvider p) {
        this.p = p;
    }

    public void dungeonBanner(Block bannerBlock) {
        dungeonBannerBlockstateAndModel(bannerBlock);
        dungeonBannerItemModelForBlock(bannerBlock);
    }

    private void dungeonBannerBlockstateAndModel(Block bannerBlock) {
        String n = p.name(bannerBlock); // make name() public/protected in provider or duplicate a small helper

        ModelFile blockModel = p.models().getBuilder(n)
                .parent(new ModelFile.UncheckedModelFile("minecraft:builtin/entity"))
                .texture("particle", p.modLoc("block/dungeon_banner_particle"));

        p.getVariantBuilder(bannerBlock).forAllStates(s -> new ConfiguredModel[]{
                new ConfiguredModel(blockModel)
        });
    }

    private void dungeonBannerItemModelForBlock(Block bannerBlock) {
        String n = p.name(bannerBlock); // "dungeon_banner_spawner_magenta" etc.

        var b = p.itemModels()
                .getBuilder(n)
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
