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

import com.google.common.base.Suppliers;

/**
 * 1.21 made ArmorMaterial a data-driven record registered in {@code Registries.ARMOR_MATERIAL}.
 * The durability multiplier moved onto the item ({@code Type.getDurability}); the material now
 * only carries defense/enchantability/sound/repair/toughness/knockback + render layers.
 */
public final class DNLArmorMaterial {
    private DNLArmorMaterial() {}

    /** Durability multiplier the old enum carried (BASE_DURABILITY * 26). */
    public static final int SPAWNER_DURABILITY_MULTIPLIER = 26;

    // 1.21: register the material via the platform helper (DeferredRegister on NeoForge — the
    // BuiltInRegistries.ARMOR_MATERIAL is frozen by mod-construction time; eager Registry.register
    // on Fabric). Expose a lazy Holder<ArmorMaterial> resolved from the registry on first access
    // (consumers run at item-registry time, post-init on both loaders), so neither loader's
    // registration-timing model (deferred vs eager) breaks it.
    public static final Supplier<Holder<ArmorMaterial>> SPAWNER = register(
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

    private static Supplier<Holder<ArmorMaterial>> register(String name, EnumMap<ArmorItem.Type, Integer> defense,
                                                            int enchantmentValue, float toughness, float knockbackResistance,
                                                            Supplier<Ingredient> repairIngredient) {
        ResourceLocation id = DungeonNowLoading.id(name);
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(id, "", false));
        net.minecraft.resources.ResourceKey<ArmorMaterial> key =
                net.minecraft.resources.ResourceKey.create(BuiltInRegistries.ARMOR_MATERIAL.key(), id);
        Services.REGISTRY.register(
                BuiltInRegistries.ARMOR_MATERIAL,
                name,
                () -> new ArmorMaterial(defense, enchantmentValue, SoundEvents.ARMOR_EQUIP_IRON, repairIngredient, layers, toughness, knockbackResistance)
        );
        // Resolve lazily: by the time anything reads SPAWNER, the material is registered on both loaders.
        return Suppliers.memoize(() -> BuiltInRegistries.ARMOR_MATERIAL.getHolder(key)
                .orElseThrow(() -> new IllegalStateException("DNL armor material not registered: " + id)));
    }

    /** Forces class init so the static registration runs. */
    public static void init() {}
}
