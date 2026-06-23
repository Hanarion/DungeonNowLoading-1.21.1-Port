package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.block.BurnacleBlock;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public class AnimatedBlockDestroyOverlayLevelRendererMixin {
    @Shadow
    private ClientLevel level;

    @Shadow
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    private final List<RemovedDestroyProgress> dungeonnowloading$removedDestroyProgress = new ArrayList<>();

    // 1.21: LevelRenderer.renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture,
    // Matrix4f frustumMatrix, Matrix4f projectionMatrix) — PoseStack/float/long replaced by
    // DeltaTracker, plus a second Matrix4f.
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void dungeonnowloading$removeAnimatedBlockDestroyOverlays(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (this.level == null || this.destructionProgress.isEmpty()) {
            return;
        }

        this.dungeonnowloading$removedDestroyProgress.clear();
        this.destructionProgress.long2ObjectEntrySet().removeIf(entry -> {
            SortedSet<BlockDestructionProgress> progressSet = entry.getValue();
            if (progressSet == null || progressSet.isEmpty()) {
                return false;
            }

            BlockPos pos = progressSet.first().getPos();
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() instanceof BurnacleBlock) {
                this.dungeonnowloading$removedDestroyProgress.add(new RemovedDestroyProgress(entry.getLongKey(), progressSet));
                return true;
            }
            return false;
        });
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void dungeonnowloading$restoreAnimatedBlockDestroyOverlays(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        for (RemovedDestroyProgress removedProgress : this.dungeonnowloading$removedDestroyProgress) {
            this.destructionProgress.putIfAbsent(removedProgress.posAsLong(), removedProgress.progressSet());
        }
        this.dungeonnowloading$removedDestroyProgress.clear();
    }

    private record RemovedDestroyProgress(long posAsLong, SortedSet<BlockDestructionProgress> progressSet) {
    }
}
