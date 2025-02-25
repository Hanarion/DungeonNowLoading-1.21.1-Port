package dev.hexnowloading.dungeonnowloading.util.event_managers;

public class PistonPushManager {
    private static boolean shouldCancelPistonPush;

    public static boolean shouldCancel() {
        return shouldCancelPistonPush;
    }

    public static void cancel() {
        shouldCancelPistonPush = true;
    }

    public static void reset() {
        shouldCancelPistonPush = false;
    }
}
