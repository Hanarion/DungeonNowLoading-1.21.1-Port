package dev.hexnowloading.dungeonnowloading.item.client;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemAnimationState {

    private static final String ANIMATIONS_TAG = "Animations";

    public static void start(ItemStack stack, String animationName, long gameTime, long duration, boolean loop, boolean resetAnimations) {
        ensureUUID(stack);

        if (resetAnimations) stopAll(stack);

        CompoundTag tag = StackNbt.getOrCreateTag(stack);
        CompoundTag animationsTag = tag.getCompound(ANIMATIONS_TAG);

        CompoundTag animTag = new CompoundTag();
        animTag.putLong("StartTime", gameTime);
        animTag.putLong("Duration", duration);
        animTag.putBoolean("Looping", loop);

        animationsTag.put(animationName, animTag);
        tag.put(ANIMATIONS_TAG, animationsTag);
        StackNbt.setTag(stack, tag);
    }

    public static void startIfStopped(ItemStack stack, String animationName, long gameTime, long duration, boolean loop, boolean resetAnimations) {
        if (!isAnimating(stack, animationName, gameTime)) {
            start(stack, animationName, gameTime, duration, loop, resetAnimations);
        }
    }

    public static float getProgress(ItemStack stack, String animationName, long gameTime, float partialTicks) {
        if (!StackNbt.hasTag(stack)) return 0.0f;

        CompoundTag animationsTag = StackNbt.getTag(stack).getCompound("Animations");
        if (!animationsTag.contains(animationName)) return 0.0f;

        CompoundTag animTag = animationsTag.getCompound(animationName);
        long startTime = animTag.getLong("StartTime");
        long duration = animTag.getLong("Duration");

        if (duration <= 0) return 0.0f; // Prevent divide by zero

        float progress = (float) (gameTime + partialTicks - startTime) / duration;

        return Math.min(progress, 1.0f);
    }

    public static String getCurrentAnimation(ItemStack stack, long gameTime) {
        if (!StackNbt.hasTag(stack)) return null;

        CompoundTag animationsTag = StackNbt.getTag(stack).getCompound(ANIMATIONS_TAG);

        for (String key : animationsTag.getAllKeys()) {
            CompoundTag animTag = animationsTag.getCompound(key);
            long startTime = animTag.getLong("StartTime");
            long duration = animTag.getLong("Duration");
            boolean looping = animTag.getBoolean("Looping");

            if (looping || (gameTime - startTime) < duration) {
                return key; // Return the first valid animation found
            }
        }
        return null; // No active animation
    }

    public static boolean isAnimating(ItemStack stack, String animationName, long gameTime) {
        if (!StackNbt.hasTag(stack)) return false;

        CompoundTag animationsTag = StackNbt.getTag(stack).getCompound(ANIMATIONS_TAG);
        if (!animationsTag.contains(animationName)) return false;

        CompoundTag animTag = animationsTag.getCompound(animationName);
        long startTime = animTag.getLong("StartTime");
        long duration = animTag.getLong("Duration");
        boolean looping = animTag.getBoolean("Looping");

        return looping || (gameTime - startTime) < duration;
    }

    public static boolean isAnimatingOrHanging(ItemStack stack, String animationName, long gameTime) {
        if (!StackNbt.hasTag(stack)) return false;

        CompoundTag animationsTag = StackNbt.getTag(stack).getCompound(ANIMATIONS_TAG);
        if (!animationsTag.contains(animationName)) return false;

        CompoundTag animTag = animationsTag.getCompound(animationName);
        long startTime = animTag.getLong("StartTime");
        long duration = animTag.getLong("Duration");
        boolean looping = animTag.getBoolean("Looping");

        return looping || (gameTime - startTime) < duration || (!looping && (gameTime - startTime) >= duration);
    }

    public static boolean isAnimationHanging(ItemStack stack, String animationName, long gameTime) {
        if (!StackNbt.hasTag(stack)) return false;

        CompoundTag animationsTag = StackNbt.getTag(stack).getCompound(ANIMATIONS_TAG);
        if (!animationsTag.contains(animationName)) return false;

        CompoundTag animTag = animationsTag.getCompound(animationName);
        long startTime = animTag.getLong("StartTime");
        long duration = animTag.getLong("Duration");
        boolean looping = animTag.getBoolean("Looping");

        // Check if animation has ended but is still in NBT
        return !looping && (gameTime - startTime) >= duration;
    }

    public static void stopAll(ItemStack itemStack) {
        if (!StackNbt.hasTag(itemStack)) return;

        CompoundTag tag = StackNbt.getTag(itemStack);
        if (tag.contains(ANIMATIONS_TAG)) {
            tag.remove(ANIMATIONS_TAG);
        }
    }

    private static void ensureUUID(ItemStack itemStack) {
        if (itemStack.getItem() instanceof DNLAnimatedItem<?> dnlAnimatedItem) {
            dnlAnimatedItem.ensureItemUUID(itemStack);
        }
    }
}
