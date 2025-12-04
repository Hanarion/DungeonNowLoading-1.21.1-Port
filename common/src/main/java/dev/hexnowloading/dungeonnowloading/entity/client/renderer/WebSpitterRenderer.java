package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.WebSpitterEyesLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WebSpitterModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.WebSpitterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WebSpitterRenderer<T extends WebSpitterEntity> extends MobRenderer<T, WebSpitterModel<T>> {
    private static final ResourceLocation SPIDER_LOCATION = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/web_spitter/web_spitter.png");

    public WebSpitterRenderer(EntityRendererProvider.Context context) {
        super(context, new WebSpitterModel<>(context.bakeLayer(WebSpitterModel.LAYER_LOCATION)), 0.8F);
        this.addLayer(new WebSpitterEyesLayer<>(this));
    }

    protected float getFlipDegrees(T $$0) {
        return 180.0F;
    }

    public ResourceLocation getTextureLocation(T $$0) {
        return SPIDER_LOCATION;
    }
}
