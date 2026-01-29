package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SealedChaosModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SealedChaosRenderer<T extends SealedChaosEntity> extends MobRenderer<T, SealedChaosModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/sealed_chaos/sealed_chaos.png");
    private static final ResourceLocation TEXTURE_BASIC = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/sealed_chaos/sealed_chaos_basic.png");

    public SealedChaosRenderer(EntityRendererProvider.Context context) {
        super(context, new SealedChaosModel<>(context.bakeLayer(SealedChaosModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    protected void scale(T t, PoseStack poseStack, float f) {
        poseStack.scale(0.98F, 0.98F, 0.98F);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return t.isBasicVariant() ? TEXTURE_BASIC : TEXTURE;
    }
}
