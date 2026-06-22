package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.WispLanternLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WispLanternModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WispLanternRenderer<T extends WispLanternEntity> extends MobRenderer<T, WispLanternModel<T>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/wisp_lantern/wisp_lantern.png");
    private static final float MODEL_SCALE = 1.25F;

    public WispLanternRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WispLanternModel<>(renderManager.bakeLayer(WispLanternModel.LAYER_LOCATION)), 1.25F);
        this.addLayer(new WispLanternLayer<>(this));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -1.3D, 0.0D);
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }


    @Override
    public ResourceLocation getTextureLocation(WispLanternEntity t) {
        return TEXTURE;
    }
}
