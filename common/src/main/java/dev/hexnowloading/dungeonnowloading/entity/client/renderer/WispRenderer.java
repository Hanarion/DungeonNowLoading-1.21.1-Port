package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.WispLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WispModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WispRenderer<T extends WispEntity> extends MobRenderer<T, WispModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/wisp/wisp.png");
    private static final float MODEL_SCALE = 1.5F;

    public WispRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WispModel<>(renderManager.bakeLayer(WispModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new WispLayer<>(this));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.45D, 0.0D);
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    protected void setupRotations(T entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        float entityYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float entityPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F + entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entityPitch));
    }

    @Override
    public ResourceLocation getTextureLocation(WispEntity wispEntity) {
        return TEXTURE;
    }
}
