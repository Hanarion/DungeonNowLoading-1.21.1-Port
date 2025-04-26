package dev.hexnowloading.dungeonnowloading.datagen.tag;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class DNLForgetItemTagGenerator extends ItemTagsProvider {

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
    }
}
