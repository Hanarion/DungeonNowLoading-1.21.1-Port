package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class DNLLootInjections {
    public static void setup() {
        //chests
        injectLootTableRef(ResourceLocation.fromNamespaceAndPath("minecraft", "chests/jungle_temple"), ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "vanilla/chests/jungle_temple"));
        injectLootTableRef(ResourceLocation.fromNamespaceAndPath("minecraft", "chests/simple_dungeon"), ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "vanilla/chests/simple_dungeon"));

        //blocks
        injectLootTableRef(ResourceLocation.fromNamespaceAndPath("minecraft", "blocks/spawner"), ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "vanilla/blocks/spawner"));
    }

    private static void injectLootTableRef(ResourceLocation targetLootTable, ResourceLocation injectTable) {
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, injectTable)))
                .build();

        Services.LOOT.injectLoot(targetLootTable, pool);
    }
}
