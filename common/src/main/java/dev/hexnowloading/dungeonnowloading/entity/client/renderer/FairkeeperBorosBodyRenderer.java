package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.FairkeeperBorosBodyLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperBorosBodyModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperBorosBodyRenderer<T extends FairkeeperBorosPartEntity> extends MobRenderer<T, FairkeeperBorosBodyModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_boros/fairkeeper_boros_body.png");

    public FairkeeperBorosBodyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FairkeeperBorosBodyModel<>(renderManager.bakeLayer(FairkeeperBorosBodyModel.LAYER_LOCATION)), 1.0F);
        this.addLayer(new FairkeeperBorosBodyLayer<>(this));
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float v) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.scale(entity, poseStack, v);
    }

    @Override
    protected float getFlipDegrees(T t) {
        return 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperBorosPartEntity fairkeeperEntity) {
        return TEXTURE;
    }
}
