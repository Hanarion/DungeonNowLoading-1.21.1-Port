package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperBorosModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperOurosModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperOurosRenderer<T extends FairkeeperOurosEntity> extends MobRenderer<T, FairkeeperOurosModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper/fairkeeper_ouros.png");

    public FairkeeperOurosRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FairkeeperOurosModel<>(renderManager.bakeLayer(FairkeeperOurosModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float v) {
        poseStack.scale(1.0F, 1.0F, 1.0F);
        super.scale(entity, poseStack, v);
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperOurosEntity fairkeeperEntity) {
        return TEXTURE;
    }
}

