package dev.hexnowloading.dungeonnowloading.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/**
 * 1.21 replaced ItemStack's raw NBT (getTag/hasTag/getOrCreateTag/setTag) with the
 * CUSTOM_DATA data component. This helper mirrors the old API surface so the mod's
 * custom item NBT keeps working, backed by {@link CustomData}.
 *
 * <p>Note: {@link #getTag} / {@link #getOrCreateTag} return a COPY — mutating the
 * returned tag does not write back. Use {@link #update} to mutate persistently, or
 * {@link #setTag} to replace.
 */
public final class StackNbt {
    private StackNbt() {}

    public static boolean hasTag(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /** Returns a copy of the custom data tag, or null if absent (mirrors old getTag()). */
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    /** Returns a copy of the custom data tag, or an empty tag if absent. */
    public static CompoundTag getOrCreateTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** Replaces the custom data tag (mirrors old setTag()). */
    public static void setTag(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    /** Mutates the custom data tag in place and writes it back. */
    public static void update(ItemStack stack, Consumer<CompoundTag> mutator) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, mutator);
    }
}
