package dev.hexnowloading.dungeonnowloading.util.event_managers;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

public class ExplosionDestructionManager {
    private static boolean shouldCancelDestruction;
    private static BlockPos cancelPosition;
    private static final Set<BlockPos> pendingBlockUpdates = new HashSet<>();

    public static boolean shouldCancel() {
        return shouldCancelDestruction;
    }

    public static boolean shouldCancel(BlockPos blockPos) {
        return shouldCancelDestruction && (cancelPosition == null || blockPos.equals(cancelPosition));
    }

    public static void cancel() {
        shouldCancelDestruction = true;
    }

    public static void cancel(BlockPos blockPos) {
        cancelPosition = blockPos.immutable();
        shouldCancelDestruction = true;
        markBlockForUpdate(blockPos);
    }

    public static void markBlockForUpdate(BlockPos blockPos) {
        pendingBlockUpdates.add(blockPos.immutable());
    }

    public static Set<BlockPos> consumePendingBlockUpdates() {
        Set<BlockPos> positions = new HashSet<>(pendingBlockUpdates);
        pendingBlockUpdates.clear();
        return positions;
    }

    public static void reset() {
        shouldCancelDestruction = false;
        cancelPosition = null;
    }
}
