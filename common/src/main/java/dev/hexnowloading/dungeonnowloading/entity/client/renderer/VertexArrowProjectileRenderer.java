package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class VertexArrowProjectileRenderer<T extends VertexArrowProjectileEntity> extends EntityRenderer<VertexArrowProjectileEntity> {
    private static final ResourceLocation TEXTURE_0 = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_0.png");
    private static final ResourceLocation TEXTURE_1 = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_1.png");
    private static final ResourceLocation TEXTURE_2 = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_2.png");
    private static final ResourceLocation TEXTURE_3 = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_3.png");
    private static final ResourceLocation EMISSIVE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_emissive.png");

    public VertexArrowProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        //model = new VertexArrowProjectileModel(context.bakeLayer(VertexArrowProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(VertexArrowProjectileEntity vertexArrowProjectileEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, vertexArrowProjectileEntity.yRotO, vertexArrowProjectileEntity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, vertexArrowProjectileEntity.xRotO, vertexArrowProjectileEntity.getXRot())));

        float shakeTime = (float) vertexArrowProjectileEntity.shakeTime - partialTicks;
        if (shakeTime > 0.0F) {
            float $$17 = -Mth.sin(shakeTime * 3.0F) * shakeTime;
            poseStack.mulPose(Axis.ZP.rotationDegrees($$17));
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        float poseStackScale = 0.05625F;
        poseStack.scale(poseStackScale, poseStackScale, poseStackScale);
        poseStack.translate(-4.5F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(vertexArrowProjectileEntity)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float uStart = 1.0F;
        float uEnd = 0.0F;
        float vStart = 0.0F;
        float vEnd = 0.4375F;

        for (int arrowSide = 0; arrowSide < 4; arrowSide++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            this.vertex(matrix4f, normalMatrix, vertexConsumer, -8, -3, 0, uStart, vStart, 0, 1, 0, packedLight);
            this.vertex(matrix4f, normalMatrix, vertexConsumer, 8, -3, 0, uEnd, vStart, 0, 1, 0, packedLight);
            this.vertex(matrix4f, normalMatrix, vertexConsumer, 8, 3, 0, uEnd, vEnd, 0, 1, 0, packedLight);
            this.vertex(matrix4f, normalMatrix, vertexConsumer, -8, 3, 0, uStart, vEnd, 0, 1, 0, packedLight);
        }

        if (vertexArrowProjectileEntity.getPowerLevel() == 3) {
            //poseStack.translate(0.0F, 0.0F, 0.001F);
            VertexConsumer emissiveConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentCull(EMISSIVE));
            int emissiveLight = 0xF000F0;
            for (int arrowSide = 0; arrowSide < 4; arrowSide++) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                this.vertex(matrix4f, normalMatrix, emissiveConsumer, -8, -3, 0, uStart, vStart, 0, 1, 0, emissiveLight);
                this.vertex(matrix4f, normalMatrix, emissiveConsumer, 8, -3, 0, uEnd, vStart, 0, 1, 0, emissiveLight);
                this.vertex(matrix4f, normalMatrix, emissiveConsumer, 8, 3, 0, uEnd, vEnd, 0, 1, 0, emissiveLight);
                this.vertex(matrix4f, normalMatrix, emissiveConsumer, -8, 3, 0, uStart, vEnd, 0, 1, 0, emissiveLight);
            }
        }

        poseStack.popPose();
        super.render(vertexArrowProjectileEntity, entityYaw, partialTicks, poseStack, multiBufferSource, packedLight);
    }

    public void vertex(Matrix4f $$0, Matrix3f $$1, VertexConsumer $$2, int $$3, int $$4, int $$5, float $$6, float $$7, int $$8, int $$9, int $$10, int $$11) {
        $$2.addVertex($$0, (float)$$3, (float)$$4, (float)$$5)
                .setColor(255, 255, 255, 255)
                .setUv($$6, $$7)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight($$11)
                .setNormal((float)$$8, (float)$$10, (float)$$9);
    }

    @Override
    public ResourceLocation getTextureLocation(VertexArrowProjectileEntity entity) {
        return switch (entity.getPowerLevel()) {
            case 0 -> TEXTURE_0;
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case 3 -> TEXTURE_3;
            default -> null;
        };
    }
}
