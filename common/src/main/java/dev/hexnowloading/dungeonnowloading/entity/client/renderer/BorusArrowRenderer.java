package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BorusArrowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BorusArrowRenderer<T extends BorusArrowEntity> extends EntityRenderer<BorusArrowEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/borus_arrow.png");

    public BorusArrowRenderer(EntityRendererProvider.Context renderManager) { super(renderManager); }

    @Override
    public void render(BorusArrowEntity borusArrowEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, borusArrowEntity.yRotO, borusArrowEntity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, borusArrowEntity.xRotO, borusArrowEntity.getXRot())));

        float shakeTime = (float)borusArrowEntity.shakeTime - partialTicks;
        if (shakeTime > 0.0F) {
            float $$17 = -Mth.sin(shakeTime * 3.0F) * shakeTime;
            poseStack.mulPose(Axis.ZP.rotationDegrees($$17));
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.16875F, 0.16875F, 0.16875F);
        poseStack.translate(-5.5F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(borusArrowEntity)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float uStart = 0.65625F;
        float uEnd = 0.0F;
        float vStart = 0.0F;
        float vEnd = 0.171875F;

        for (int arrowSide = 0; arrowSide < 4; arrowSide++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            this.vertex(matrix4f, normalMatrix, vertexConsumer, -8, -2, 0, uStart, vStart, 0, 1, 0, packedLight);
            this.vertex(matrix4f, normalMatrix, vertexConsumer, 8, -2, 0, uEnd, vStart, 0, 1, 0, packedLight);
            this.vertex(matrix4f, normalMatrix, vertexConsumer, 8, 2, 0, uEnd, vEnd, 0, 1, 0, packedLight);
            this.vertex(matrix4f, normalMatrix, vertexConsumer, -8, 2, 0, uStart, vEnd, 0, 1, 0, packedLight);
        }

        poseStack.popPose();
        super.render(borusArrowEntity, entityYaw, partialTicks, poseStack, multiBufferSource, packedLight);
    }

    public void vertex(Matrix4f $$0, Matrix3f $$1, VertexConsumer $$2, int $$3, int $$4, int $$5, float $$6, float $$7, int $$8, int $$9, int $$10, int $$11) {
        $$2.vertex($$0, (float)$$3, (float)$$4, (float)$$5)
                .color(255, 255, 255, 255)
                .uv($$6, $$7)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2($$11)
                .normal($$1, (float)$$8, (float)$$10, (float)$$9)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BorusArrowEntity borusArrowEntity) {
        return TEXTURE;
    }
}
