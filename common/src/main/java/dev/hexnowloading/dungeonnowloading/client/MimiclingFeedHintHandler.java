package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MimiclingFeedHintHandler {
    private static final int OPEN_FRAME_COUNT = 4;
    private static final int HOVER_OPEN_FRAME_COUNT = 3;
    private static final int HELD_OPEN_FRAME = 2;
    private static final int HOVER_TICKS_PER_FRAME = 2;
    private static boolean hoveringFeedableItem;
    private static boolean carryingFeedableItem;
    private static boolean hoveringMimiclingWithFeedableItem;
    private static boolean wasHoveringFeedableItem;
    private static boolean wasOpenHintActive;
    private static boolean closingOpenHint;
    private static long hoverStartGameTime;
    private static long closeStartGameTime;

    private MimiclingFeedHintHandler() {}

    public static void update(ItemStack hoveredStack, ItemStack carriedStack, Player player) {
        boolean hasBaseMimicling = hasBaseMimicling(player);
        hoveringFeedableItem = MimiclingItem.isFeedableTool(hoveredStack) && hasBaseMimicling;
        carryingFeedableItem = MimiclingItem.isFeedableTool(carriedStack) && hasBaseMimicling;
        hoveringMimiclingWithFeedableItem = MimiclingItem.isBaseStorageForm(hoveredStack) && MimiclingItem.isFeedableTool(carriedStack);
        boolean hoverOpenHintActive = hoveringFeedableItem && !hoveringMimiclingWithFeedableItem;
        if (hoverOpenHintActive && !wasHoveringFeedableItem) {
            hoverStartGameTime = player != null ? player.level().getGameTime() : 0L;
        }
        wasHoveringFeedableItem = hoverOpenHintActive;

        boolean openHintActive = hoveringFeedableItem || carryingFeedableItem;
        if (openHintActive) {
            closingOpenHint = false;
        } else if (wasOpenHintActive) {
            closingOpenHint = true;
            closeStartGameTime = player != null ? player.level().getGameTime() : 0L;
        }
        wasOpenHintActive = openHintActive;
    }

    public static void clear() {
        hoveringFeedableItem = false;
        carryingFeedableItem = false;
        hoveringMimiclingWithFeedableItem = false;
        wasHoveringFeedableItem = false;
        wasOpenHintActive = false;
        closingOpenHint = false;
        hoverStartGameTime = 0L;
        closeStartGameTime = 0L;
    }

    public static boolean isOpenFrame(ItemStack stack, long gameTime, int frame) {
        if ((!hoveringFeedableItem && !carryingFeedableItem && !closingOpenHint) || !MimiclingItem.isBaseStorageForm(stack) || isChewing(stack, gameTime)) {
            return false;
        }

        if (closingOpenHint) {
            long elapsed = gameTime - closeStartGameTime;
            if (elapsed < 0 || elapsed >= HOVER_OPEN_FRAME_COUNT * HOVER_TICKS_PER_FRAME) {
                closingOpenHint = false;
                return false;
            }

            int currentFrame = HELD_OPEN_FRAME - (int) (elapsed / HOVER_TICKS_PER_FRAME);
            return frame == currentFrame;
        }

        if (hoveringFeedableItem && !hoveringMimiclingWithFeedableItem) {
            int currentFrame = Math.min((int) ((gameTime - hoverStartGameTime) / HOVER_TICKS_PER_FRAME), HOVER_OPEN_FRAME_COUNT - 1);
            return frame == currentFrame;
        }

        return carryingFeedableItem && frame == HELD_OPEN_FRAME;
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
