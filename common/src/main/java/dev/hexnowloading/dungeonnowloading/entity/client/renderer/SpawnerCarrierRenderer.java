package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.SpawnerCarrierPreviewLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SpawnerCarrierModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpawnerCarrierRenderer<T extends SpawnerCarrierEntity> extends MobRenderer<T, SpawnerCarrierModel<T>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/spawner_carrier.png");

    public SpawnerCarrierRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SpawnerCarrierModel<>(ctx.bakeLayer(SpawnerCarrierModel.LAYER_LOCATION)), 1.5F);

        // Add the “rotating mob preview” layer
        this.addLayer(new SpawnerCarrierPreviewLayer<>(this, ctx));
    }

    @Override
    public ResourceLocation getTextureLocation(SpawnerCarrierEntity entity) {
        return TEXTURE;
    }
}
