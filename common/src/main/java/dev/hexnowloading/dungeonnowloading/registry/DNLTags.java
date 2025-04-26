package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class DNLTags {
    public static final TagKey<DamageType> HOLLOW_HURTABLE = registerDamageTypeTag("hollow_hurtable");
    public static final TagKey<DamageType> SCUTTLE_HURTABLE = registerDamageTypeTag("scuttle_hurtable");
    public static final TagKey<DamageType> BALLISTA_GOLEM_HURTABLE = registerDamageTypeTag("ballista_golem_immunity");
    public static final TagKey<DamageType> FAIRKEEPER_HURTABLE = registerDamageTypeTag("fairkeeper_hurtable");
    public static final TagKey<DamageType> FAIRKEEPER_BOROS_ARMOR_HURTABLE = registerDamageTypeTag("fairkeeper_boros_armor_hurtable");
    public static final TagKey<DamageType> FAIRKEEPER_BOROS_BYPASS_ARMOR = registerDamageTypeTag("fairkeeper_boros_bypass_armor");
    public static final TagKey<EntityType<?>> PROJECTILES = registerEntityTypeTag("projectiles");
    public static final TagKey<EntityType<?>> REPULSOR_OMITTED_PROJECTILES = registerEntityTypeTag("repulsor_omitted_projectiles");
    public static final TagKey<EntityType<?>> REPULSOR_HIGH_DAMAGE_PROJECTILES = registerEntityTypeTag("repulsor_high_damage_projectiles");
    public static final TagKey<EntityType<?>> REPULSOR_LOW_DAMAGE_PROJECTILES = registerEntityTypeTag("repulsor_low_damage_projectiles");
    public static final TagKey<Item> STONE_NOTCH_MATERIAL = registerItemTag("stone_notch_material");
    public static final TagKey<Item> REDSTONE_CIRCUIT_OR_CORE = registerItemTag("redstone_circuit_or_core");
    public static final TagKey<Item> OUROS_BOROS_MUSIC_DISC = registerItemTag("ouros_boros_music_disc");
    public static final TagKey<Block> FAIRKEEPER_CHEST_IGNORE = registerBlockTag("fairkeeper_chest_ignore");
    public static final TagKey<Block> OVERCHARGED_REDSTONE_BLOCK_NEIGHBOUR_EXPLOSIVE = registerBlockTag("ovecharged_redstone_block_neighbour_explosive");
    public static final TagKey<Block> PRESERVER_IGNORE = registerBlockTag("preserver_ignore");
    public static final TagKey<Block> NEAR_FULL_HEIGHT_BLOCKS = registerBlockTag("near_full_height_blocks");
    public static final TagKey<Block> TORCH_BLOCKS = registerBlockTag("torch_blocks");
    public static final TagKey<Structure> NO_GEODES_TAG = registerStructureTag("no_geodes");
    public static final TagKey<Structure> TEMPLE_OF_DUALITY = registerStructureTag("temple_of_duality");

    private static TagKey<Block> registerBlockTag(String string) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }

    private static TagKey<Item> registerItemTag(String string) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }

    private static TagKey<DamageType> registerDamageTypeTag(String string) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }

    private static TagKey<EntityType<?>> registerEntityTypeTag(String string) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }

    private static TagKey<Structure> registerStructureTag(String string) {
        return TagKey.create(Registries.STRUCTURE, new ResourceLocation(DungeonNowLoading.MOD_ID, string));
    }
}
