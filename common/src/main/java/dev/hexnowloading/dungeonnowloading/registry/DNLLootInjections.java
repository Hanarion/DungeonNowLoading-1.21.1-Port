package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class DNLLootInjections {

    public static final byte CLOSE = 0;  // ~128x128 blocks
    public static final byte MEDIUM = 1; // ~256x256
    public static final byte FAR = 2;    // ~512x512
    public static final byte VERY_FAR = 3; // ~1024x1024
    public static final byte MAX = 4;    // ~2048x2048

    public static void setup() {
        // Inject for Temple of Duality
        injectMapLoot(new ResourceLocation("minecraft", "chests/jungle_temple"), DNLTags.TEMPLE_OF_DUALITY, "item.dungeonnowloading.temple_of_duality_map", 0.5f, FAR);
        injectMapLoot(new ResourceLocation("minecraft", "chests/simple_dungeon"), DNLTags.LABYRINTH, "item.dungeonnowloading.labyrinth_map", 1.0f, FAR);
    }

    public static void injectMapLoot(ResourceLocation lootTable, TagKey<Structure> structureTag, String translationKey, float chance, byte zoom) {
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(
                        LootItem.lootTableItem(Items.MAP)
                                .when(LootItemRandomChanceCondition.randomChance(chance))
                                .apply(ExplorationMapFunction.makeExplorationMap()
                                        .setDestination(structureTag)
                                        .setMapDecoration(MapDecoration.Type.RED_X)
                                        .setZoom(zoom)
                                        .setSkipKnownStructures(false))
                                .apply(SetNameFunction.setName(Component.translatable(translationKey)))
                )
                .build();

        Services.LOOT.injectLoot(lootTable, pool);
    }


}
