package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.entity.projectile.GasCloudEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GasCloudRenderer extends EntityRenderer<GasCloudEntity> {

    public GasCloudRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        // make sure bounding box exists for view-frustum
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(GasCloudEntity entity) {
        // No texture needed, particles only
        return null;
    }

    @Override
    public void render(GasCloudEntity entity, float yaw, float partialTicks,
                       PoseStack stack, MultiBufferSource buffer, int packedLight) {
        // 🚫 Do not render any model
        // ✔ But this method fires every frame so particles aren't culled
        // ✔ Also preserves hitbox visibility in debug mode
        super.render(entity, yaw, partialTicks, stack, buffer, packedLight);
    }
}
