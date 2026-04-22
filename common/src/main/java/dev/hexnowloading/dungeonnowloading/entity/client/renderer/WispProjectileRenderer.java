package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WispProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.WispProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class WispProjectileRenderer extends EntityRenderer<WispProjectileEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/wisp/wisp.png");
    private static final ResourceLocation TEXTURE_EMISSIVE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/wisp/wisp_emissive.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final RenderType EMISSIVE_RENDER_TYPE = RenderType.entityTranslucentEmissive(TEXTURE_EMISSIVE, true);
    private static final float MODEL_SCALE = 1.5F;
    private static final float LIVING_MODEL_Y_OFFSET = -1.501F;

    private final WispProjectileModel model;

    public WispProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WispProjectileModel(context.bakeLayer(WispProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(WispProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        Vec3 motion = entity.getDeltaMovement();
        float yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (motion.lengthSqr() > 1.0E-7D) {
            double horizontalDistance = motion.horizontalDistance();
            yRot = (float)(Mth.atan2(motion.z, motion.x) * (double)(180F / (float)Math.PI)) - 90.0F;
            xRot = (float)(-(Mth.atan2(motion.y, horizontalDistance) * (double)(180F / (float)Math.PI)));
        }
        poseStack.translate(0.0D, -0.45D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-xRot));
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        // Mirror the extra transform that MobRenderer/LivingEntityRenderer applies before rendering the model.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, LIVING_MODEL_Y_OFFSET, 0.0D);

        int emissiveLight = 0xF000F0;
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, entityYaw, 0.0F);

        VertexConsumer base = buffer.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(poseStack, base, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        VertexConsumer emissive = buffer.getBuffer(EMISSIVE_RENDER_TYPE);
        this.model.renderToBuffer(poseStack, emissive, emissiveLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.5F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WispProjectileEntity entity) {
        return TEXTURE;
    }
}
