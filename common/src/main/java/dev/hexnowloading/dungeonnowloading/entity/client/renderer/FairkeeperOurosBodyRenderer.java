package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.FairkeeperOurosBodyLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperOurosBodyModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperOurosBodyRenderer<T extends FairkeeperOurosPartEntity> extends MobRenderer<T, FairkeeperOurosBodyModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_ouros/fairkeeper_ouros_body.png");

    public FairkeeperOurosBodyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FairkeeperOurosBodyModel<>(renderManager.bakeLayer(FairkeeperOurosBodyModel.LAYER_LOCATION)), 1.0F);
        this.addLayer(new FairkeeperOurosBodyLayer<>(this));
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float v) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.scale(entity, poseStack, v);
    }

    @Override
    protected void setupRotations(T entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        poseStack.translate(0.0F, entity.getBbHeight(), 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperOurosPartEntity fairkeeperEntity) {
        return TEXTURE;
    }
}

