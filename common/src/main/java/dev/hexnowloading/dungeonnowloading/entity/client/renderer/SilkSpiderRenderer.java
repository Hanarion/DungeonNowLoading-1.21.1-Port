package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.SilkSpiderEyesLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SilkSpiderModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.SilkSpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SilkSpiderRenderer<T extends SilkSpiderEntity> extends MobRenderer<T, SilkSpiderModel<T>> {
    private static final ResourceLocation SPIDER_LOCATION = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/silk_spider/silk_spider.png");

    public SilkSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SilkSpiderModel<>(context.bakeLayer(SilkSpiderModel.LAYER_LOCATION)), 0.8F);
        this.addLayer(new SilkSpiderEyesLayer<>(this));
    }

    protected float getFlipDegrees(T $$0) {
        return 180.0F;
    }

    public ResourceLocation getTextureLocation(T $$0) {
        return SPIDER_LOCATION;
    }
}
