package dev.hexnowloading.dungeonnowloading.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.client.render.MendingAuraOverlayRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class InstantRepairOverlayLevelRendererMixin {
    // 1.21: renderLevel no longer receives a PoseStack or a float partialTick. Rebuild a world
    // PoseStack from the frustum matrix and read the partial tick from the DeltaTracker.
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void renderInstantRepairOverlay(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(frustumMatrix);
        MendingAuraOverlayRenderer.render(poseStack, deltaTracker.getGameTimeDeltaPartialTick(false), camera);
    }
}
