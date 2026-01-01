package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class DNLLootInjections {
    public static void setup() {
        injectLootTableRef(new ResourceLocation("minecraft", "chests/jungle_temple"), new ResourceLocation("dungeonnowloading", "chests/vanilla/jungle_temple"));
        injectLootTableRef(new ResourceLocation("minecraft", "chests/simple_dungeon"), new ResourceLocation("dungeonnowloading", "chests/vanilla/simple_dungeon"));
    }

    private static void injectLootTableRef(ResourceLocation targetLootTable, ResourceLocation injectTable) {
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(injectTable))
                .build();

        Services.LOOT.injectLoot(targetLootTable, pool);
    }
}
