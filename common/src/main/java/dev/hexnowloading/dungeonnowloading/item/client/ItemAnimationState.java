package dev.hexnowloading.dungeonnowloading.item.client;

import dev.hexnowloading.dungeonnowloading.network.packets.S2CItemAnimationPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemAnimationState {

    private static final String ANIMATIONS_TAG = "Animations";

    public static void start(ItemStack stack, String animationName, long gameTime, long duration, boolean loop, boolean resetAnimations) {
        if (resetAnimations) stopAll(stack);

        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag animationsTag = tag.getCompound(ANIMATIONS_TAG);

        CompoundTag animTag = new CompoundTag();
        animTag.putLong("StartTime", gameTime);
        animTag.putLong("Duration", duration);
        animTag.putBoolean("Looping", loop);

        animationsTag.put(animationName, animTag);
        tag.put(ANIMATIONS_TAG, animationsTag);

        /*if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.level().isClientSide) {
            Services.NETWORK.sendToServer(new C2SItemAnimationPacket(animationName, duration, loop));
        }*/
    }

    public static void startAndSendPacket(Level level, Player player, ItemStack itemStack, String animationName, long gameTime, long duration, boolean loop, boolean resetAnimations) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemAnimationState.start(itemStack, animationName, gameTime, duration, loop, resetAnimations);
            ItemAnimationState.sendStartAnimationPacket(serverPlayer, animationName, duration, loop, resetAnimations);
        }
    }

    public static void sendStartAnimationPacket(ServerPlayer serverPlayer, String animationName, long duration, boolean loop, boolean resetAnimations) {
        Services.NETWORK.sendToAllPlayers(new S2CItemAnimationPacket(serverPlayer.getUUID(), animationName, duration, loop, resetAnimations), serverPlayer.getServer());
    }

    public static void startIfStopped(ItemStack stack, String animationName, long gameTime, long duration, boolean loop, boolean resetAnimations) {
        if (!isAnimating(stack, animationName, gameTime)) {
            start(stack, animationName, gameTime, duration, loop, resetAnimations);
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

    public static String getCurrentAnimation(ItemStack stack, long gameTime) {
        if (!stack.hasTag()) return null;

        CompoundTag animationsTag = stack.getTag().getCompound(ANIMATIONS_TAG);

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
        if (!stack.hasTag()) return false;

        CompoundTag animationsTag = stack.getTag().getCompound(ANIMATIONS_TAG);
        if (!animationsTag.contains(animationName)) return false;

        CompoundTag animTag = animationsTag.getCompound(animationName);
        long startTime = animTag.getLong("StartTime");
        long duration = animTag.getLong("Duration");
        boolean looping = animTag.getBoolean("Looping");

        return looping || (gameTime - startTime) < duration;
    }

    public static void stopAll(ItemStack itemStack) {
        if (!itemStack.hasTag()) return;

        CompoundTag tag = itemStack.getTag();
        if (tag.contains(ANIMATIONS_TAG)) {
            tag.remove(ANIMATIONS_TAG);
        }
    }
}
