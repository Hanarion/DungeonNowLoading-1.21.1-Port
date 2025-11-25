package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.ReaperSpiderModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;

public class ReaperSpiderRenderer<T extends ReaperSpiderEntity> extends MobRenderer<T, ReaperSpiderModel<T>> {
    private static final ResourceLocation SPIDER_LOCATION = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/reaper_spider/reaper_spider.png");

    public ReaperSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new ReaperSpiderModel<>(context.bakeLayer(ReaperSpiderModel.LAYER_LOCATION)), 0.8F);
        this.addLayer(new SpiderEyesLayer(this));
    }

    protected float getFlipDegrees(T $$0) {
        return 180.0F;
    }

    protected void scale(ReaperSpiderEntity $$0, PoseStack $$1, float $$2) {
        $$1.scale(1.5F, 2.0F, 1.5F);
    }

    public ResourceLocation getTextureLocation(T $$0) {
        return SPIDER_LOCATION;
    }
}
