package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/**
 * 1.21 made enchantments data-driven. These are {@link ResourceKey}s into the
 * ENCHANTMENT registry; the actual definitions live in JSON under
 * {@code data/dungeonnowloading/enchantment/}. The mod's custom effect logic
 * is applied externally via {@code EnchantmentHelper.getEnchantmentLevel(holder, ...)},
 * so call sites resolve a {@link Holder} from a registry via {@link #holder}.
 */
public class DNLEnchantments {
    public static final ResourceKey<Enchantment> BREAK_PROTECTION = key("break_protection");
    public static final ResourceKey<Enchantment> AMPLIFICATION = key("amplification");
    public static final ResourceKey<Enchantment> NULLIFICATION = key("nullification");
    public static final ResourceKey<Enchantment> DURABLE = key("durable");
    public static final ResourceKey<Enchantment> GIGANTISM = key("gigantism");
    public static final ResourceKey<Enchantment> OVERWORKED = key("overworked");
    public static final ResourceKey<Enchantment> PACK_BLESSING = key("pack_blessing");
    public static final ResourceKey<Enchantment> ARC_SHOT = key("arc_shot");
    public static final ResourceKey<Enchantment> PULSE_SHOT = key("pulse_shot");
    public static final ResourceKey<Enchantment> SACRIFICE = key("sacrifice");
    public static final ResourceKey<Enchantment> RECKLESS = key("reckless");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, DungeonNowLoading.id(name));
    }

    /** Resolve an enchantment holder from a level's registry access. */
    public static Holder<Enchantment> holder(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }

    /** Resolve an enchantment holder from a registry lookup provider. */
    public static Holder<Enchantment> holder(HolderLookup.Provider provider, ResourceKey<Enchantment> key) {
        return provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    public static void init() {}
}
