package dev.hexnowloading.dungeonnowloading.util.event_managers;

import net.minecraft.core.BlockPos;

public class ContainerDropManager {
    private static boolean shouldCancelDrop;
    private static BlockPos cancelPosition;

    public static boolean shouldCancel(BlockPos blockPos) {
        return shouldCancelDrop && cancelPosition.equals(blockPos);
    }

    public static void cancel(BlockPos blockPos) {
        cancelPosition = blockPos;
        shouldCancelDrop = true;
    }

    public static void reset() {
        shouldCancelDrop = false;
    }

}
