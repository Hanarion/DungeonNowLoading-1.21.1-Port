package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class DNLTags {
    public static final TagKey<DamageType> HOLLOW_HURTABLE = registerDamageTypeTag("hollow_hurtable");
    public static final TagKey<DamageType> SCUTTLE_HURTABLE = registerDamageTypeTag("scuttle_hurtable");
    public static final TagKey<DamageType> BALLISTA_GOLEM_HURTABLE = registerDamageTypeTag("ballista_golem_immunity");
    public static final TagKey<DamageType> FAIRKEEPER_HURTABLE = registerDamageTypeTag("fairkeeper_hurtable");
    public static final TagKey<Item> STONE_NOTCH_MATERIAL = registerItemTag("stone_notch_material");
    public static final TagKey<Block> FAIRKEEPER_CHEST_IGNORE = registerBlockTag("fairkeeper_chest_ignore");
    public static final TagKey<Block> OVERCHARGED_REDSTONE_BLOCK_NEIGHBOUR_EXPLOSIVE = registerBlockTag("ovecharged_redstone_block_neighbour_explosive");
    public static final TagKey<Structure> NO_GEODES_TAG = registerStructureTag("no_geodes");

    private static TagKey<Block> registerBlockTag(String string) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }

    private static TagKey<Item> registerItemTag(String string) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }

    private static TagKey<DamageType> registerDamageTypeTag(String string) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }

    private static TagKey<Structure> registerStructureTag(String string) {
        return TagKey.create(Registries.STRUCTURE, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }
}
