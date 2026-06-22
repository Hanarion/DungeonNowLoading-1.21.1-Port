package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.BallistaGolemModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SpawnerCarrierModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BallistaArrowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BallistaGolemRenderer<T extends BallistaGolemEntity> extends MobRenderer<T, BallistaGolemModel<T>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/ballista_golem/ballista_golem.png");

    public BallistaGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BallistaGolemModel<>(renderManager.bakeLayer(BallistaGolemModel.LAYER_LOCATION)), 1.5F);
    }

    @Override
    public void render(BallistaGolemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Render only if the entity is past its first tick
        if (!entity.isSlumbering()) {
            super.render((T) entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(BallistaGolemEntity ballistaGolemEntity) {
        return TEXTURE;
    }
}
