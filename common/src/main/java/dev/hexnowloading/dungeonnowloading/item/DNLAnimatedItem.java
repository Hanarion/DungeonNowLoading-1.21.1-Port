package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface DNLAnimatedItem<T extends Enum<T> & DNLAnimationState> {

    Class<T> getAnimationEnum(); // Get the correct animation enum

    // ✅ Retrieve animation state from NBT safely
    default T getAnimationState(ItemStack stack) {
        if (!stack.hasTag()) return getDefaultAnimationState();

        CompoundTag tag = stack.getTag();
        String animationName = tag.getString("AnimationState");

        return DNLAnimationState.fromString(getAnimationEnum(), animationName, getDefaultAnimationState());
    }

    // ✅ Provide a default animation state (Override per item)
    default T getDefaultAnimationState() {
        return null; // Items should override this with their own default
    }


    // ✅ Set animation state and store start time in NBT (Only if changed)
    default void setAnimationState(ItemStack stack, T newState, Level level) {
        if (newState == null) return;

        CompoundTag tag = stack.getOrCreateTag();

        // Only update if animation state has changed
        String currentState = tag.getString("AnimationState");
        if (!currentState.equals(newState.getName())) {
            tag.putString("AnimationState", newState.getName());
            tag.putLong("AnimationStartTime", level.getGameTime());
        }
    }
}