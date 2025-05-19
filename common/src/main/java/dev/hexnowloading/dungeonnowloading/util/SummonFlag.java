package dev.hexnowloading.dungeonnowloading.util;

public class SummonFlag {
    private static final ThreadLocal<Boolean> IS_SUMMONING = ThreadLocal.withInitial(() -> false);

    public static void markSummoning() {
        IS_SUMMONING.set(true);
    }

    public static boolean isSummoning() {
        return IS_SUMMONING.get();
    }

    public static void clear() {
        IS_SUMMONING.remove(); // Clean up to avoid memory leak
    }
}
