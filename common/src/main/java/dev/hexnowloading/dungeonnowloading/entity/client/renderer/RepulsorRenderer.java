package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.RepulsorLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.RepulsorModel;
import dev.hexnowloading.dungeonnowloading.entity.misc.RepulsorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RepulsorRenderer<T extends RepulsorEntity> extends MobRenderer<T, RepulsorModel<T>> {
    private static final ResourceLocation BASE_DEFAULT = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/repulsor/repulsor.png");
    private static final ResourceLocation BASE_GOLDEN  =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/repulsor/repulsor_golden.png");

    public RepulsorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RepulsorModel<>(renderManager.bakeLayer(RepulsorModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new RepulsorLayer<>(this));
    }

    @Override
    public void render(RepulsorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.canRender()) {
            super.render((T) entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(RepulsorEntity entity) {
        return entity.getSkin() == RepulsorEntity.Skin.GOLDEN ? BASE_GOLDEN : BASE_DEFAULT;
    }
}
