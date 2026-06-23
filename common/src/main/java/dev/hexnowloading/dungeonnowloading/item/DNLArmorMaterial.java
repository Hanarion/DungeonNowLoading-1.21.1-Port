package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * 1.21 made ArmorMaterial a data-driven record registered in {@code Registries.ARMOR_MATERIAL}.
 * The durability multiplier moved onto the item ({@code Type.getDurability}); the material now
 * only carries defense/enchantability/sound/repair/toughness/knockback + render layers.
 */
public final class DNLArmorMaterial {
    private DNLArmorMaterial() {}

    /** Durability multiplier the old enum carried (BASE_DURABILITY * 26). */
    public static final int SPAWNER_DURABILITY_MULTIPLIER = 26;

    // 1.21 NeoForge: BuiltInRegistries.ARMOR_MATERIAL is frozen by the time the mod constructs, so
    // the old direct Registry.registerForHolder crashes ("Registry is already frozen"). Register
    // through the platform DeferredRegister instead (the returned supplier is a Holder at runtime).
    public static final Holder<ArmorMaterial> SPAWNER = register(
            "spawner",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.BODY, 8);
            }),
            10,                                   // enchantmentValue
            2.0F,                                 // toughness
            0.0F,                                 // knockbackResistance
            () -> Ingredient.of(DNLItems.SPAWNER_FRAME.get())
    );

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> defense,
                                                  int enchantmentValue, float toughness, float knockbackResistance,
                                                  Supplier<Ingredient> repairIngredient) {
        ResourceLocation id = DungeonNowLoading.id(name);
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(id, "", false));
        // Register the material; then resolve its Holder from the registry by id (works on both
        // NeoForge, where register() returns a DeferredHolder, and Fabric, where register() returns
        // a plain Supplier — so don't cast the supplier; look up the Holder directly).
        Services.REGISTRY.register(
                BuiltInRegistries.ARMOR_MATERIAL,
                name,
                () -> new ArmorMaterial(defense, enchantmentValue, SoundEvents.ARMOR_EQUIP_IRON, repairIngredient, layers, toughness, knockbackResistance)
        );
        return BuiltInRegistries.ARMOR_MATERIAL.getHolder(
                net.minecraft.resources.ResourceKey.create(BuiltInRegistries.ARMOR_MATERIAL.key(), id))
                .orElseThrow(() -> new IllegalStateException("DNL armor material not registered: " + id));
    }

    /** Forces class init so the static registration runs. */
    public static void init() {}
}
