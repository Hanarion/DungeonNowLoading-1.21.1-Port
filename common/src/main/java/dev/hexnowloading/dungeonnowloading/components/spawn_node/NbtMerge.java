package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Utility for applying an NBT patch onto a target tag.
 *
 * Rules:
 * - If both values are CompoundTag -> merge recursively
 * - Otherwise -> patch overwrites target (including ListTag)
 *
 * This is ideal for "spawn patches": you only specify tags you want to change.
 */
public final class NbtMerge {
    private NbtMerge() {}

    public static void mergeCompound(CompoundTag target, CompoundTag patch) {
        if (target == null || patch == null || patch.isEmpty()) return;

        for (String key : patch.getAllKeys()) {
            Tag patchValue = patch.get(key);
            if (patchValue == null) continue;

            Tag targetValue = target.get(key);

            // Both compounds => recursive merge
            if (targetValue instanceof CompoundTag targetCompound
                    && patchValue instanceof CompoundTag patchCompound) {

                mergeCompound(targetCompound, patchCompound);
                target.put(key, targetCompound); // explicit keep

            } else {
                // Overwrite everything else
                target.put(key, patchValue.copy());
            }
        }
    }
}
