package dev.hexnowloading.dungeonnowloading.item.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemAnimationState {
    private static final String ANIMATIONS_TAG = "Animations"; // NBT Key

    // ✅ Starts a specific animation for this item
    public static void start(ItemStack stack, String animationName, long gameTime, long duration, boolean loop) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag animationsTag = tag.getCompound(ANIMATIONS_TAG);

        CompoundTag animTag = new CompoundTag();
        animTag.putLong("StartTime", gameTime);
        animTag.putLong("Duration", duration);
        animTag.putBoolean("Looping", loop);

        animationsTag.put(animationName, animTag);
        tag.put(ANIMATIONS_TAG, animationsTag);
    }

    public static void startIfStopped(ItemStack stack, String animationName, long gameTime, long duration, boolean loop) {
        if (!isAnimating(stack, animationName, gameTime)) {
            start(stack, animationName, gameTime, duration, loop);
        }
    }

    public static float getProgress(ItemStack stack, String animationName, long gameTime, float partialTicks) {
        if (!stack.hasTag()) return 0.0f;

        CompoundTag animationsTag = stack.getTag().getCompound("Animations");
        if (!animationsTag.contains(animationName)) return 0.0f;

        CompoundTag animTag = animationsTag.getCompound(animationName);
        long startTime = animTag.getLong("StartTime");
        long duration = animTag.getLong("Duration");

        if (duration <= 0) return 0.0f; // Prevent divide by zero

        float progress = (float) (gameTime + partialTicks - startTime) / duration;

        return Math.min(progress, 1.0f);
    }

    public static boolean isAnimating(ItemStack stack, String animationName, long gameTime) {
        if (!stack.hasTag()) return false;

        CompoundTag animationsTag = stack.getTag().getCompound(ANIMATIONS_TAG);
        if (!animationsTag.contains(animationName)) return false;

        CompoundTag animTag = animationsTag.getCompound(animationName);
        long startTime = animTag.getLong("StartTime");
        long duration = animTag.getLong("Duration");
        boolean looping = animTag.getBoolean("Looping");

        return looping || (gameTime - startTime) < duration;
    }
}
