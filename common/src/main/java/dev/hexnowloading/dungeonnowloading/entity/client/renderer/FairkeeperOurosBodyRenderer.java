package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperOurosBodyModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperOurosBodyRenderer<T extends FairkeeperOurosPartEntity> extends MobRenderer<T, FairkeeperOurosBodyModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper/fairkeeper_ouros_body.png");

    public FairkeeperOurosBodyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FairkeeperOurosBodyModel<>(renderManager.bakeLayer(FairkeeperOurosBodyModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float v) {
        poseStack.scale(1.0F, 1.0F, 1.0F);
        super.scale(entity, poseStack, v);
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperOurosPartEntity fairkeeperEntity) {
        return TEXTURE;
    }
}

