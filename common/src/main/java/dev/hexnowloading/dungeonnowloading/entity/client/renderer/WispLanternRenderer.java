package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WispLanternModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WispLanternRenderer<T extends WispLanternEntity> extends MobRenderer<T, WispLanternModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/wisp_lantern/wisp_lantern.png");

    public WispLanternRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WispLanternModel<>(renderManager.bakeLayer(WispLanternModel.LAYER_LOCATION)), 1.0F);
    }


    @Override
    public ResourceLocation getTextureLocation(WispLanternEntity t) {
        return TEXTURE;
    }
}
