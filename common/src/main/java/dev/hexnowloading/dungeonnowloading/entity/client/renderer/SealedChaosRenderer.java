package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SealedChaosModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SealedChaosRenderer<T extends SealedChaosEntity> extends MobRenderer<T, SealedChaosModel<T>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/sealed_chaos/sealed_chaos.png");
    private static final ResourceLocation TEXTURE_BASIC = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/sealed_chaos/sealed_chaos_basic.png");

    public SealedChaosRenderer(EntityRendererProvider.Context context) {
        super(context, new SealedChaosModel<>(context.bakeLayer(SealedChaosModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        // Base scale so Sealed Chaos has presence even without Gigantism
        float baseScale = 0.98F;
        poseStack.scale(baseScale, baseScale, baseScale);

        // If Gigantism is active, double the size visually to match the 2x hitbox
        if (entity.isGigantic()) {
            poseStack.scale(2.0F, 2.0F, 2.0F);
        }

        super.scale(entity, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return t.isBasicVariant() ? TEXTURE_BASIC : TEXTURE;
    }
}
