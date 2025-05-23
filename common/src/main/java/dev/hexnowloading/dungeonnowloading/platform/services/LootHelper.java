package dev.hexnowloading.dungeonnowloading.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;

public interface LootHelper {
    void injectLoot(ResourceLocation id, LootPool pool);
}
