package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.WispLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WispModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WispRenderer<T extends WispEntity> extends MobRenderer<T, WispModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/wisp/wisp.png");

    public WispRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WispModel<>(renderManager.bakeLayer(WispModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new WispLayer<>(this));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.25D, 0.0D);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(WispEntity wispEntity) {
        return TEXTURE;
    }
}
