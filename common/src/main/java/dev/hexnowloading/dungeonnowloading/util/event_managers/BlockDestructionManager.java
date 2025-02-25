package dev.hexnowloading.dungeonnowloading.util.event_managers;

public class BlockDestructionManager {
    private static boolean shouldCancelDestruction;

    public static boolean shouldCancel() {
        return shouldCancelDestruction;
    }

    public static void cancel() {
        shouldCancelDestruction = true;
    }

    public static void reset() {
        shouldCancelDestruction = false;
    }

}
