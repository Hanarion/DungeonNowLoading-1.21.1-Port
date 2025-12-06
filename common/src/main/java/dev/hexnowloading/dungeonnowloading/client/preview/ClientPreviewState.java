package dev.hexnowloading.dungeonnowloading.client.preview;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class ClientPreviewState {
    private static volatile BlockPos gauntletPos = BlockPos.ZERO;
    private static volatile int activationRange = 0;
    private static volatile boolean showRange = false;
    private static volatile boolean showArena = false;
    private static volatile boolean showNodes = false;
    private static volatile AABB arenaBox = null;
    private static volatile java.util.List<BlockPos> mobNodes = java.util.List.of();

    private ClientPreviewState() {}

    public static void updateGauntletPreview(BlockPos pos,
                                             int actRange,
                                             boolean showRange,
                                             boolean showArena,
                                             boolean showNodes,
                                             AABB box) {
        ClientPreviewState.gauntletPos = pos == null ? BlockPos.ZERO : pos.immutable();
        ClientPreviewState.activationRange = Math.max(0, actRange);
        ClientPreviewState.showRange = showRange;
        ClientPreviewState.showArena = showArena;
        ClientPreviewState.showNodes = showNodes;
        ClientPreviewState.arenaBox = box;
        if (!showNodes) ClientPreviewState.mobNodes = java.util.List.of();
    }

    public static void setMobNodes(java.util.List<BlockPos> nodes) {
        ClientPreviewState.mobNodes = nodes == null ? java.util.List.of() : java.util.List.copyOf(nodes);
    }

    public static BlockPos gauntletPos() { return gauntletPos; }
    public static int activationRange() { return activationRange; }
    public static boolean showRange() { return showRange; }
    public static boolean showArena() { return showArena; }
    public static boolean showNodes() { return showNodes; }
    public static AABB arenaBox() { return arenaBox; }
    public static java.util.List<BlockPos> mobNodes() { return mobNodes; }
}
