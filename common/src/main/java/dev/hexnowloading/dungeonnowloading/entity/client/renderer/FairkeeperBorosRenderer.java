package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.FairkeeperBorosLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperBorosModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperBorosRenderer<T extends FairkeeperBorosEntity> extends MobRenderer<T, FairkeeperBorosModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_boros/fairkeeper_boros_head.png");

    public FairkeeperBorosRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FairkeeperBorosModel<>(renderManager.bakeLayer(FairkeeperBorosModel.LAYER_LOCATION)), 1.0F);
        this.addLayer(new FairkeeperBorosLayer<>(this));
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float v) {
        poseStack.scale(1.75F, 1.75F, 1.75F);
        super.scale(entity, poseStack, v);
    }

    @Override
    protected float getFlipDegrees(T t) {
        return 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperBorosEntity fairkeeperEntity) {
        return TEXTURE;
    }
}
