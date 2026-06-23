package dev.hexnowloading.dungeonnowloading.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.core.component.DataComponents;

/**
 * 1.21 moved armor trims from a "Trim" NBT tag to the {@code minecraft:trim} data
 * component ({@link ArmorTrim}). Materials and patterns are dynamic-registry holders,
 * so applying a trim now needs a {@link HolderLookup.Provider}.
 */
public final class ArmorTrimUtil {
    private ArmorTrimUtil() {}

    /** Applies a trim to {@code stack} from vanilla material/pattern ids (without namespace). */
    public static void applyTrim(HolderLookup.Provider registries, ItemStack stack, String material, String pattern) {
        if (registries == null || material == null || pattern == null) {
            return;
        }
        Holder<TrimMaterial> materialHolder = registries
                .lookupOrThrow(Registries.TRIM_MATERIAL)
                .getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL, id(material)));
        Holder<TrimPattern> patternHolder = registries
                .lookupOrThrow(Registries.TRIM_PATTERN)
                .getOrThrow(ResourceKey.create(Registries.TRIM_PATTERN, id(pattern)));
        stack.set(DataComponents.TRIM, new ArmorTrim(materialHolder, patternHolder));
    }

    private static ResourceLocation id(String value) {
        return value.indexOf(':') >= 0
                ? ResourceLocation.parse(value)
                : ResourceLocation.withDefaultNamespace(value);
    }
}
