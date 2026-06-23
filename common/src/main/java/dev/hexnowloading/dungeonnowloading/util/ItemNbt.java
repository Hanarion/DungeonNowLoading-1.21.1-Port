package dev.hexnowloading.dungeonnowloading.util;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 1.21 made ItemStack NBT (de)serialization require a HolderLookup.Provider
 * (ItemStack.of/save were removed/changed). These helpers use a provider when one
 * is supplied, otherwise fall back to the cached registry access in
 * {@link DungeonNowLoading#registryAccess()}.
 */
public final class ItemNbt {
    private ItemNbt() {}

    public static ItemStack load(HolderLookup.Provider provider, CompoundTag tag) {
        if (provider == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parseOptional(provider, tag);
    }

    /** Loads using the cached registry access (server start / client level). */
    public static ItemStack load(CompoundTag tag) {
        return load(DungeonNowLoading.registryAccess(), tag);
    }

    public static CompoundTag save(HolderLookup.Provider provider, ItemStack stack) {
        if (provider == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return (CompoundTag) stack.save(provider);
    }

    /** Saves using the cached registry access. */
    public static CompoundTag save(ItemStack stack) {
        return save(DungeonNowLoading.registryAccess(), stack);
    }
}
