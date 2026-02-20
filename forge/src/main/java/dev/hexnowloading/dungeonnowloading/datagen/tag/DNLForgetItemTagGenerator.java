package dev.hexnowloading.dungeonnowloading.datagen.tag;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DNLForgetItemTagGenerator extends ItemTagsProvider {

    private static final Map<DyeColor, Item> SPAWNER_BANNERS = Map.of(
            DyeColor.MAGENTA, DNLItems.DUNGEON_BANNER_SPAWNER_MAGENTA.get(),
            DyeColor.BLACK,   DNLItems.DUNGEON_BANNER_SPAWNER_BLACK.get(),
            DyeColor.BLUE,    DNLItems.DUNGEON_BANNER_SPAWNER_BLUE.get(),
            DyeColor.PURPLE,  DNLItems.DUNGEON_BANNER_SPAWNER_PURPLE.get(),
            DyeColor.GREEN,   DNLItems.DUNGEON_BANNER_SPAWNER_GREEN.get()
    );

    public DNLForgetItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, DungeonNowLoading.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ItemTags.MUSIC_DISCS).add(
                DNLItems.MUSIC_DISC_BOROS.get(),
                DNLItems.MUSIC_DISC_OUROS.get(),
                DNLItems.MUSIC_DISC_PYTHONIC_OVERDRIVE.get()
        );

        for (var toEntry : SPAWNER_BANNERS.entrySet()) {

            DyeColor toColor = toEntry.getKey();

            var builder = this.tag(recolorFromTag(toColor));

            for (var fromEntry : SPAWNER_BANNERS.entrySet()) {

                DyeColor fromColor = fromEntry.getKey();

                if (fromColor != toColor) {
                    builder.add(fromEntry.getValue());
                }
            }
        }
    }

    private static TagKey<Item> recolorFromTag(DyeColor to) {
        return TagKey.create(
                Registries.ITEM,
                DungeonNowLoading.id("dungeon_banner_spawner_recolor_to_" + to.getName())
        );
    }
}
