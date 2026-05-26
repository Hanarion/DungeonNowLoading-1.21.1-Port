package dev.hexnowloading.dungeonnowloading.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MendingAuraOverlayClientState {
    private static final Map<BlockPos, Overlay> OVERLAYS = new ConcurrentHashMap<>();

    private MendingAuraOverlayClientState() {
    }

    public static void add(BlockPos pos, int durationTicks) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        OVERLAYS.put(pos.immutable(), new Overlay(level.getGameTime(), Math.max(1, durationTicks)));
    }

    public static List<ActiveOverlay> getActive(Level level, float partialTick) {
        if (OVERLAYS.isEmpty()) {
            return List.of();
        }

        long gameTime = level.getGameTime();
        List<ActiveOverlay> activeOverlays = new ArrayList<>();
        Iterator<Map.Entry<BlockPos, Overlay>> iterator = OVERLAYS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Overlay> entry = iterator.next();
            Overlay overlay = entry.getValue();
            float age = (gameTime - overlay.startTick()) + partialTick;
            if (age >= overlay.durationTicks()) {
                iterator.remove();
                continue;
            }

            float alpha = Mth.clamp(1.0F - age / overlay.durationTicks(), 0.0F, 1.0F);
            activeOverlays.add(new ActiveOverlay(entry.getKey(), alpha));
        }

        return activeOverlays;
    }

    public record ActiveOverlay(BlockPos pos, float alpha) {
    }

    private record Overlay(long startTick, int durationTicks) {
    }
}
