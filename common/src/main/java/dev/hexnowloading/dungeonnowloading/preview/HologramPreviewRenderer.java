package dev.hexnowloading.dungeonnowloading.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.client.preview.ClientPreviewState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HologramPreviewRenderer {
    private HologramPreviewRenderer() {}

    public static void render(PoseStack poseStack, MultiBufferSource buffers, Camera camera, Level level) {
        if (level == null) return;
        boolean showRange = ClientPreviewState.showRange();
        boolean showArena = ClientPreviewState.showArena();
        boolean showNodes = ClientPreviewState.showNodes();
        if (!showRange && !showArena && !showNodes) return;

        Vec3 cam = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        if (showRange) drawRange(poseStack, buffers, ClientPreviewState.gauntletPos(), ClientPreviewState.activationRange());
        if (showArena) drawArena(poseStack, buffers, ClientPreviewState.arenaBox());
        if (showNodes) drawNodes(poseStack, buffers, ClientPreviewState.mobNodes());

        poseStack.popPose();
    }

    private static void drawRange(PoseStack pose, MultiBufferSource buffers, BlockPos center, int radius) {
        if (radius <= 0 || center == null) return;
        VertexConsumer vc = buffers.getBuffer(RenderType.lines());
        double cx = center.getX() + 0.5;
        double cy = center.getY();
        double cz = center.getZ() + 0.5;
        int steps = 144; // smoother circle
        float r = 0.20f, g = 0.90f, b = 1.0f, a = 1.0f;

        // Draw thicker ring by layering multiple concentric circles and slight Y offsets to avoid z-fighting
        double[] radialOffsets = {-0.60, -0.40, -0.20, 0.0, 0.20, 0.40, 0.60};
        double[] heightOffsets = {0.04, 0.06, 0.08, 0.10, 0.12, 0.14, 0.16};
        for (int ri = 0; ri < radialOffsets.length; ri++) {
            double rr = Math.max(0.0, radius + radialOffsets[ri]);
            float y = (float)(cy + heightOffsets[ri]);
            for (int i = 0; i < steps; i++) {
                double a0 = (Math.PI * 2.0) * i / steps;
                double a1 = (Math.PI * 2.0) * (i + 1) / steps;
                float x0 = (float)(cx + rr * Math.cos(a0));
                float z0 = (float)(cz + rr * Math.sin(a0));
                float x1 = (float)(cx + rr * Math.cos(a1));
                float z1 = (float)(cz + rr * Math.sin(a1));
                vc.vertex(pose.last().pose(), x0, y, z0).color(r, g, b, a).normal(pose.last().normal(), 0, 1, 0).endVertex();
                vc.vertex(pose.last().pose(), x1, y, z1).color(r, g, b, a).normal(pose.last().normal(), 0, 1, 0).endVertex();
            }
        }

        // Add clearer spokes so the outline reads like a cylinder boundary
        int spokeCount = 16; // every 22.5 degrees
        float inner = (float)Math.max(0.0, radius - 0.65);
        float outer = (float)(radius + 0.65);
        float sy = (float)(cy + 0.11f);
        for (int s = 0; s < spokeCount; s++) {
            double ang = (Math.PI * 2.0) * s / spokeCount;
            float dx = (float)Math.cos(ang);
            float dz = (float)Math.sin(ang);
            float x0 = (float)(cx + inner * dx);
            float z0 = (float)(cz + inner * dz);
            float x1 = (float)(cx + outer * dx);
            float z1 = (float)(cz + outer * dz);
            vc.vertex(pose.last().pose(), x0, sy, z0).color(r, g, b, a).normal(pose.last().normal(), 0, 1, 0).endVertex();
            vc.vertex(pose.last().pose(), x1, sy, z1).color(r, g, b, a).normal(pose.last().normal(), 0, 1, 0).endVertex();
        }
    }

    private static void drawArena(PoseStack pose, MultiBufferSource buffers, AABB boxWorld) {
        if (boxWorld == null) return;
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        float or = 0.0f, og = 0.8f, ob = 1.0f, oa = 0.95f;
        LevelRenderer.renderLineBox(pose, lines, boxWorld, or, og, ob, oa);
    }

    private static void drawNodes(PoseStack pose, MultiBufferSource buffers, java.util.List<BlockPos> nodes) {
        if (nodes == null || nodes.isEmpty()) return;
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        float r = 1.0f, g = 0.4f, b = 0.2f, a = 0.95f;
        for (BlockPos p : nodes) {
            if (p == null) continue;
            AABB bb = new AABB(p);
            LevelRenderer.renderLineBox(pose, lines, bb, r, g, b, a);
        }
    }
}
