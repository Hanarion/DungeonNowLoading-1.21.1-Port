package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MimiclingFeedHintHandler {
    private static final int OPEN_FRAME_COUNT = 4;
    private static final int HOVER_TICKS_PER_FRAME = 3;
    private static final int CARRIED_TICKS_PER_FRAME = 2;
    private static boolean hoveringFeedableItem;
    private static boolean carryingFeedableItem;

    private MimiclingFeedHintHandler() {}

    public static void update(ItemStack hoveredStack, ItemStack carriedStack, Player player) {
        boolean hasBaseMimicling = hasBaseMimicling(player);
        hoveringFeedableItem = MimiclingItem.isFeedableTool(hoveredStack) && hasBaseMimicling;
        carryingFeedableItem = MimiclingItem.isFeedableTool(carriedStack) && hasBaseMimicling;
    }

    public static void clear() {
        hoveringFeedableItem = false;
        carryingFeedableItem = false;
    }

    public static boolean isOpenFrame(ItemStack stack, long gameTime, int frame) {
        if ((!hoveringFeedableItem && !carryingFeedableItem) || !MimiclingItem.isBaseStorageForm(stack)) {
            return false;
        }

        int ticksPerFrame = carryingFeedableItem ? CARRIED_TICKS_PER_FRAME : HOVER_TICKS_PER_FRAME;
        return ((int) (gameTime / ticksPerFrame) % OPEN_FRAME_COUNT) == frame;
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
