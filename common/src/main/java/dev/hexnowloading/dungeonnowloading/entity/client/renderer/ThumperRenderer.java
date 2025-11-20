package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.ThumperModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.ThumperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ThumperRenderer<T extends ThumperEntity> extends MobRenderer<T, ThumperModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/thumper/thumper.png");

    public ThumperRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ThumperModel<>(renderManager.bakeLayer(ThumperModel.LAYER_LOCATION)), 3.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ThumperEntity thumper) {
        return TEXTURE;
    }
}
