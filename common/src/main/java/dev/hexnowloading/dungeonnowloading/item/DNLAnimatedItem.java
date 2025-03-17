package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public interface DNLAnimatedItem<T extends Enum<T> & DNLAnimationState> {

    String ITEM_UUID = "ItemUUID";

    Class<T> getAnimationEnum();

    default T getDefaultAnimationState() {
        return null;
    }

    default void ensureItemUUID(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(ITEM_UUID)) {
            tag.putUUID(ITEM_UUID, UUID.randomUUID());
        }
    }

    default UUID getItemUUID(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().hasUUID(ITEM_UUID)) {
            return null;
        }
        return stack.getTag().getUUID(ITEM_UUID);
    }

    default void resetItemUUID(ItemStack stack) {
        if (!stack.hasTag()) return;
        stack.getTag().remove(ITEM_UUID);
    }
}