package dev.hexnowloading.dungeonnowloading.client.preview;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class PreviewOverlayFabric {
    private PreviewOverlayFabric() {}

    public static void register() {
        WorldRenderEvents.END.register(context -> {
            var mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) return;
            var pose = context.matrixStack();
            var camera = context.camera();
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            HologramPreviewRenderer.render(pose, buffers, camera, mc.level);
            buffers.endBatch(RenderType.lines());
            buffers.endBatch(RenderType.translucent());
        });
    }
}

