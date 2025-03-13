package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface DNLAnimatedItem<T extends Enum<T> & DNLAnimationState> {

    Class<T> getAnimationEnum(); // Get the correct animation enum

    // ✅ Retrieve animation state from NBT
    default T getAnimationState(ItemStack stack) {
        if (!stack.hasTag()) return null;

        CompoundTag tag = stack.getTag();
        String animationName = tag.getString("AnimationState");

        try {
            return DNLAnimationState.fromString(getAnimationEnum(), animationName);
        } catch (IllegalArgumentException e) {
            System.err.println("⚠ ERROR: Invalid animation state in NBT: " + animationName);
            return null;
        }
    }

    // ✅ Retrieve animation start time from NBT
    default long getAnimationStartTime(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getLong("AnimationStartTime");
    }

    // ✅ Set animation state and store start time in NBT
    default void setAnimationState(ItemStack stack, T newState, Level level) {
        if (newState == null) return;

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("AnimationState", newState.getName()); // Store the animation state
        tag.putLong("AnimationStartTime", level.getGameTime()); // Store when animation started
    }
}