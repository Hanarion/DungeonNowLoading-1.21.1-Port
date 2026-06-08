package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import dev.hexnowloading.dungeonnowloading.mixin.client.LevelRendererAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;

public final class AnimatedBlockDestroyProgress {
    private AnimatedBlockDestroyProgress() {
    }

    public static int getProgress(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.levelRenderer == null) {
            return -1;
        }

        Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = ((LevelRendererAccessor) minecraft.levelRenderer).dungeonnowloading$getDestroyingBlocks();
        int progress = -1;
        for (BlockDestructionProgress blockProgress : destroyingBlocks.values()) {
            if (blockProgress.getPos().equals(pos)) {
                progress = Math.max(progress, blockProgress.getProgress());
            }
        }
        return progress;
    }
}
