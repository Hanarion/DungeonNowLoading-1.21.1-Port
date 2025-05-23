package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.LootHelper;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.HashSet;
import java.util.Set;

public class FabricLootHelper implements LootHelper {

    private final Set<ResourceLocation> injected = new HashSet<>();

    @Override
    public void injectLoot(ResourceLocation id, LootPool pool) {
        if (!injected.contains(id)) {
            LootTableEvents.MODIFY.register((rm, lm, tableId, builder, source) -> {
                if (tableId.equals(id)) {
                    builder.pool(pool);
                }
            });
            injected.add(id);
        }
    }
}
