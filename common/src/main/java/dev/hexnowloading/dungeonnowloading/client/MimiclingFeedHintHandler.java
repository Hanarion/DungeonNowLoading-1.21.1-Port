package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MimiclingFeedHintHandler {
    private static final int OPEN_FRAME_COUNT = 3;
    private static final int HELD_OPEN_FRAME = 2;
    private static final int OPEN_TICKS_PER_FRAME = 2;
    private static boolean openHintActive;
    private static boolean wasOpenHintActive;
    private static boolean closingOpenHint;
    private static ItemStack targetedMimicling = ItemStack.EMPTY;
    private static ItemStack closingTargetedMimicling = ItemStack.EMPTY;
    private static long openStartGameTime;
    private static long closeStartGameTime;

    private MimiclingFeedHintHandler() {}

    public static void update(ItemStack hoveredStack, ItemStack carriedStack, Player player) {
        boolean hasBaseMimicling = hasBaseMimicling(player);
        boolean feedableCursor = MimiclingItem.isFeedableTool(carriedStack) && hasBaseMimicling;
        boolean mimiclingCursorOverFeedable = MimiclingItem.isBaseStorageForm(carriedStack) && MimiclingItem.isFeedableTool(hoveredStack);
        openHintActive = feedableCursor || mimiclingCursorOverFeedable;

        if (openHintActive) {
            if (!wasOpenHintActive) {
                openStartGameTime = player != null ? player.level().getGameTime() : 0L;
            }
            targetedMimicling = mimiclingCursorOverFeedable ? carriedStack : ItemStack.EMPTY;
            closingOpenHint = false;
            closingTargetedMimicling = ItemStack.EMPTY;
        } else if (wasOpenHintActive) {
            closingOpenHint = true;
            closingTargetedMimicling = targetedMimicling;
            targetedMimicling = ItemStack.EMPTY;
            closeStartGameTime = player != null ? player.level().getGameTime() : 0L;
        }
        wasOpenHintActive = openHintActive;
    }

    public static void clear() {
        openHintActive = false;
        wasOpenHintActive = false;
        closingOpenHint = false;
        targetedMimicling = ItemStack.EMPTY;
        closingTargetedMimicling = ItemStack.EMPTY;
        openStartGameTime = 0L;
        closeStartGameTime = 0L;
    }

    public static boolean isOpenFrame(ItemStack stack, long gameTime, int frame) {
        if ((!openHintActive && !closingOpenHint) || !MimiclingItem.isBaseStorageForm(stack) || isChewing(stack, gameTime)) {
            return false;
        }

        if (closingOpenHint) {
            if (!closingTargetedMimicling.isEmpty() && stack != closingTargetedMimicling) {
                return false;
            }

            long elapsed = gameTime - closeStartGameTime;
            if (elapsed < 0 || elapsed >= OPEN_FRAME_COUNT * OPEN_TICKS_PER_FRAME) {
                closingOpenHint = false;
                return false;
            }

            int currentFrame = HELD_OPEN_FRAME - (int) (elapsed / OPEN_TICKS_PER_FRAME);
            return frame == currentFrame;
        }

        if (!targetedMimicling.isEmpty() && stack != targetedMimicling) {
            return false;
        }

        int currentFrame = Math.min((int) ((gameTime - openStartGameTime) / OPEN_TICKS_PER_FRAME), HELD_OPEN_FRAME);
        return frame == currentFrame;
    }

    private static boolean isChewing(ItemStack stack, long gameTime) {
        for (int frame = 0; frame < 15; frame++) {
            if (MimiclingItem.isChewingFrame(stack, gameTime, frame)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasBaseMimicling(Player player) {
        if (player == null) {
            return false;
        }

        for (ItemStack item : player.getInventory().items) {
            if (MimiclingItem.isBaseStorageForm(item)) {
                return true;
            }
        }

        for (ItemStack item : player.getInventory().offhand) {
            if (MimiclingItem.isBaseStorageForm(item)) {
                return true;
            }
        }

        return false;
    }
}
