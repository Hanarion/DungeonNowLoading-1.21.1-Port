package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WebSpitModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.WebSpitProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class WebSpitProjectileRenderer extends EntityRenderer<WebSpitProjectileEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/web_spit.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final double MODEL_Y_OFFSET = 1.1D;
    private static final double MODEL_BACK_OFFSET = 0.3D;
    private static final float MODEL_YAW_OFFSET = 180.0F;

    private final WebSpitModel<WebSpitProjectileEntity> model;

    public WebSpitProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.model = new WebSpitModel<>(renderManager.bakeLayer(WebSpitModel.LAYER_LOCATION));
    }

    @Override
    public void render(WebSpitProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.tickCount <= 1) {
            return;
        }

        poseStack.pushPose();
        float yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.translate(0.0D, MODEL_Y_OFFSET, 0.0D);
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-7D) {
            Vec3 offset = motion.normalize().scale(-MODEL_BACK_OFFSET);
            poseStack.translate(offset.x, offset.y, offset.z);
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        this.model.headAnim(yRot + MODEL_YAW_OFFSET, -xRot);

        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WebSpitProjectileEntity entity) {
        return TEXTURE;
    }
}
