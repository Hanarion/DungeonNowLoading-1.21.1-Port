package dev.hexnowloading.dungeonnowloading.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.render.Caltrop;
import dev.hexnowloading.dungeonnowloading.entity.misc.PayloadEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;


public class PayloadEntityRenderer extends EntityRenderer<PayloadEntity> {
    private static final ResourceLocation BLANK = new ResourceLocation("minecraft", "textures/misc/white.png");
    private static final ResourceLocation CALTROP_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/caltrop.png");

    private final Caltrop<PayloadEntity> caltropModel;


    public PayloadEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.caltropModel = new Caltrop<>(ctx.bakeLayer(Caltrop.LAYER_LOCATION));
    }

    @Override
    public void render(PayloadEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if (entity.getKind() == PayloadEntity.Kind.CALTROP) {
            poseStack.pushPose();
            poseStack.translate(0.0D, -1.25D, 0.0D);
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));
            this.caltropModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            poseStack.popPose();
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            return;
        }

        else {
            poseStack.pushPose();
            float size = 0.25f;
            poseStack.translate(0.0, 0.1, 0.0);

            int cr, cg, cb, ca = 255; // opaque
            switch (entity.getKind()) {
                case CRYO -> {
                    cr = 255;
                    cg = 255;
                    cb = 255;
                }
                case OIL -> {
                    cr = 0;
                    cg = 0;
                    cb = 0;
                }
                case CALTROP -> {
                    cr = 128;
                    cg = 128;
                    cb = 128;
                } // gray
                default -> {
                    cr = 255;
                    cg = 255;
                    cb = 255;
                }
            }


            VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(BLANK));
            int light = LightTexture.FULL_BRIGHT;
            int overlay = OverlayTexture.NO_OVERLAY;

            float half = size / 2f;
            float x0 = -half, x1 = half;
            float y0 = -half, y1 = half;
            float z0 = -half, z1 = half;

            var pose = poseStack.last().pose();
            var normal = poseStack.last().normal();

            // Front (z1)
            vc.vertex(pose, x0, y0, z1).color(cr, cg, cb, ca).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
            vc.vertex(pose, x1, y0, z1).color(cr, cg, cb, ca).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
            vc.vertex(pose, x1, y1, z1).color(cr, cg, cb, ca).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
            vc.vertex(pose, x0, y1, z1).color(cr, cg, cb, ca).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();

            // Back (z0)
            vc.vertex(pose, x1, y0, z0).color(cr, cg, cb, ca).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
            vc.vertex(pose, x0, y0, z0).color(cr, cg, cb, ca).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
            vc.vertex(pose, x0, y1, z0).color(cr, cg, cb, ca).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
            vc.vertex(pose, x1, y1, z0).color(cr, cg, cb, ca).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();

            // Left (x0)
            vc.vertex(pose, x0, y0, z0).color(cr, cg, cb, ca).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
            vc.vertex(pose, x0, y0, z1).color(cr, cg, cb, ca).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
            vc.vertex(pose, x0, y1, z1).color(cr, cg, cb, ca).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
            vc.vertex(pose, x0, y1, z0).color(cr, cg, cb, ca).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();

            // Right (x1)
            vc.vertex(pose, x1, y0, z1).color(cr, cg, cb, ca).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
            vc.vertex(pose, x1, y0, z0).color(cr, cg, cb, ca).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
            vc.vertex(pose, x1, y1, z0).color(cr, cg, cb, ca).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
            vc.vertex(pose, x1, y1, z1).color(cr, cg, cb, ca).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();

            // Top (y1)
            vc.vertex(pose, x0, y1, z1).color(cr, cg, cb, ca).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
            vc.vertex(pose, x1, y1, z1).color(cr, cg, cb, ca).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
            vc.vertex(pose, x1, y1, z0).color(cr, cg, cb, ca).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
            vc.vertex(pose, x0, y1, z0).color(cr, cg, cb, ca).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();

            // Bottom (y0)
            vc.vertex(pose, x0, y0, z0).color(cr, cg, cb, ca).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
            vc.vertex(pose, x1, y0, z0).color(cr, cg, cb, ca).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
            vc.vertex(pose, x1, y0, z1).color(cr, cg, cb, ca).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
            vc.vertex(pose, x0, y0, z1).color(cr, cg, cb, ca).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();

            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PayloadEntity entity) {
        return entity.getKind() == PayloadEntity.Kind.CALTROP ? CALTROP_TEXTURE : BLANK;
    }
}
