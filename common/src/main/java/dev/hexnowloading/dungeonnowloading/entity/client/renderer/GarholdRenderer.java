package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.GarholdModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GarholdRenderer<T extends GarholdEntity> extends MobRenderer<T, GarholdModel<T>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/garhold/garhold.png");

    public GarholdRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GarholdModel<>(renderManager.bakeLayer(GarholdModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isNoAnimation()) return;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GarholdEntity entity) { return TEXTURE; }
}
