package dev.hexnowloading.dungeonnowloading.item.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ItemAnimationState {
    private static final String NBT_KEY = "AnimationStartTime";
    private static final String NBT_DURATION = "AnimationDuration";
    private static final String NBT_LOOPING = "AnimationLooping";

    public static void startAnimation(ItemStack stack, long currentGameTime, long duration, boolean looping) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(NBT_KEY, currentGameTime);
        tag.putLong(NBT_DURATION, duration);
        tag.putBoolean(NBT_LOOPING, looping);
    }

    public static void stopAnimation(ItemStack stack) {
        stack.getOrCreateTag().remove(NBT_KEY);
    }

    public static boolean isAnimating(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(NBT_KEY);
    }

    public static float getProgress(ItemStack stack, long currentGameTime) {
        if (!isAnimating(stack)) return 0.0F;

        CompoundTag tag = stack.getTag();
        long startTime = tag.getLong(NBT_KEY);
        long duration = tag.getLong(NBT_DURATION);
        boolean looping = tag.getBoolean(NBT_LOOPING);

        long elapsedTime = currentGameTime - startTime;
        if (!looping && elapsedTime >= duration) {
            stopAnimation(stack);
            return 1.0F; // Animation has fully completed
        }

        return Mth.clamp((float) elapsedTime / duration, 0.0F, 1.0F);
    }
}
