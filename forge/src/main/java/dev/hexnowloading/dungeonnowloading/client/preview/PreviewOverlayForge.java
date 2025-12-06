package dev.hexnowloading.dungeonnowloading.client.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public final class PreviewOverlayForge {
    private PreviewOverlayForge() {}

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // draw after entities so it appears on top of terrain
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        var mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;
        PoseStack pose = event.getPoseStack();
        var camera = event.getCamera();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        HologramPreviewRenderer.render(pose, buffers, camera, mc.level);
        buffers.endBatch(RenderType.lines());
        buffers.endBatch(RenderType.translucent());
    }
}
