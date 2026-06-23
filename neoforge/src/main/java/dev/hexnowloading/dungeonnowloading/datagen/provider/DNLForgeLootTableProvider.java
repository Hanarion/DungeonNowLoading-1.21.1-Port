package dev.hexnowloading.dungeonnowloading.datagen.provider;

import dev.hexnowloading.dungeonnowloading.datagen.loot.DNLForgeBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DNLForgeLootTableProvider {

    // 1.21: LootTableProvider + SubProviderEntry both take the HolderLookup.Provider now.
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(DNLForgeBlockLootTableProvider::new, LootContextParamSets.BLOCK)
        ), lookupProvider);
    }

}
