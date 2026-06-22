package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public interface DNLAnimatedItem<T extends Enum<T> & DNLAnimationState> {

    String ITEM_UUID = "ItemUUID";

    Class<T> getAnimationEnum();

    default T getDefaultAnimationState() {
        return null;
    }

    void playDroppedAnimation(Player player, ItemStack itemStack);

    default void ensureItemUUID(ItemStack stack) {
        CompoundTag tag = StackNbt.getOrCreateTag(stack);
        if (!tag.hasUUID(ITEM_UUID)) {
            tag.putUUID(ITEM_UUID, UUID.randomUUID());
        }
    }

    default UUID getItemUUID(ItemStack stack) {
        if (!StackNbt.hasTag(stack) || !StackNbt.getTag(stack).hasUUID(ITEM_UUID)) {
            return null;
        }
        return StackNbt.getTag(stack).getUUID(ITEM_UUID);
    }

    default void resetItemUUID(ItemStack stack) {
        if (!StackNbt.hasTag(stack)) return;
        StackNbt.update(stack, t -> t.remove(ITEM_UUID));
    }
}