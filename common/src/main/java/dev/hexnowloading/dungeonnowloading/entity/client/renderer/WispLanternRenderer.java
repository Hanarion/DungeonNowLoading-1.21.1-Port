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

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/wisp_lantern/wisp_lantern.png");

    public WispLanternRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WispLanternModel<>(renderManager.bakeLayer(WispLanternModel.LAYER_LOCATION)), 1.0F);
        this.addLayer(new WispLanternLayer<>(this));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -1.0D, 0.0D);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }


    @Override
    public ResourceLocation getTextureLocation(WispLanternEntity t) {
        return TEXTURE;
    }
}
