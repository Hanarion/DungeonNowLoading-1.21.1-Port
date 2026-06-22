package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.BrokenGarholdModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BrokenGarholdRenderer<T extends BrokenGarholdEntity> extends MobRenderer<T, BrokenGarholdModel<T>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/garhold/broken_garhold.png");

    public BrokenGarholdRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BrokenGarholdModel<>(renderManager.bakeLayer(BrokenGarholdModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        //if (entity.isNoAnimation()) return;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BrokenGarholdEntity entity) { return TEXTURE; }
}
