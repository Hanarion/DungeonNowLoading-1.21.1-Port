package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.FairkeeperOurosLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperOurosModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperOurosRenderer<T extends FairkeeperOurosEntity> extends MobRenderer<T, FairkeeperOurosModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_ouros/fairkeeper_ouros_head.png");

    public FairkeeperOurosRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FairkeeperOurosModel<>(renderManager.bakeLayer(FairkeeperOurosModel.LAYER_LOCATION)), 1.0F);
        this.addLayer(new FairkeeperOurosLayer<>(this));
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
    protected void setupRotations(T entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        poseStack.translate(0.0F, entity.getBbHeight(), 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperOurosEntity fairkeeperEntity) {
        return TEXTURE;
    }
}

