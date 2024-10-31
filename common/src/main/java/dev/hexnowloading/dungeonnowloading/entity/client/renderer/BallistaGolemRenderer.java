package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.BallistaGolemModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SpawnerCarrierModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BallistaGolemRenderer<T extends BallistaGolemEntity> extends MobRenderer<T, BallistaGolemModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/ballista_golem/ballista_golem.png");

    public BallistaGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BallistaGolemModel<>(renderManager.bakeLayer(BallistaGolemModel.LAYER_LOCATION)), 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BallistaGolemEntity ballistaGolemEntity) {
        return TEXTURE;
    }
}
