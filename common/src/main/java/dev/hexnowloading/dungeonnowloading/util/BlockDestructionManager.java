package dev.hexnowloading.dungeonnowloading.util;

public class BlockDestructionManager {
    private static boolean shouldCancelDestruction;

    public static boolean shouldCancelDestruction() {
        return shouldCancelDestruction;
    }

    public static void cancelBlockDestruction() {
        shouldCancelDestruction = true;
    }

    public static void resetPlacementDestruction() {
        shouldCancelDestruction = false;
    }

}
