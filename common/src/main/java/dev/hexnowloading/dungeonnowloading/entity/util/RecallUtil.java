package dev.hexnowloading.dungeonnowloading.entity.util;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class RecallUtil {
    private RecallUtil() {}

    public static final int RECALL_CAP = 100;

    /**
     * Returns the clamped recall count [0..100].
     */
    public static int clampCount(int defeatedCount) {
        return Mth.clamp(defeatedCount, 0, RECALL_CAP);
    }

    /**
     * Roman numeral for 1..100 (C). If 0, returns "0" (useful for debugging).
     */
    public static String toRomanUpTo100(int n) {
        n = clampCount(n);
        if (n == 0) return "0";

        int[] vals = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] syms = {"C","XC","L","XL","X","IX","V","IV","I"};
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < vals.length; i++) {
            while (n >= vals[i]) {
                n -= vals[i];
                sb.append(syms[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Builds "<base> Recalled <Roman>" as a Component.
     * If you want this translatable later, you can swap this to a translatable key.
     */
    public static Component recalledName(Component baseName, int defeatedCount) {
        int c = clampCount(defeatedCount);
        return baseName.copy()
                .append(" (")
                .append(Component.translatable("entity.dungeonnowloading.seeping_soul.recalled"))
                .append(" ")
                .append(Component.literal(toRomanUpTo100(c)))
                .append(")");
    }
}
