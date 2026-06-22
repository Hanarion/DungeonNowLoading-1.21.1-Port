package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.VertexDomainProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class VertexDomainProjectileRenderer<T extends VertexDomainProjectileEntity> extends EntityRenderer<VertexDomainProjectileEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_domain_projectile.png");
    private static final ResourceLocation EMISSIVE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_domain_projectile_emissive.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final RenderType EMISSIVE_RENDER_TYPE = RenderType.entityTranslucent(EMISSIVE);
    private VertexDomainProjectileModel model;

    public VertexDomainProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new VertexDomainProjectileModel(context.bakeLayer(VertexDomainProjectileModel.LAYER_LOCATION));
    }

    @Override
    public boolean shouldRender(VertexDomainProjectileEntity entity, Frustum frustum, double x, double y, double z) {
            return true;
    }

    @Override
    public void render(VertexDomainProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-2.0F, -2.0F, 2.0F);
        poseStack.translate(0.0f, 0.0F, 0.0f);
        boolean bl = entity.getHurtTime() > 0;

        float alpha = 1.0F;
        int fadeStart = 20;
        if (entity.getLife() > 0 && entity.getLife() < fadeStart) {
            alpha = Math.max((float) entity.getLife() / fadeStart, 0.0F);
        } else if (entity.getDyingTick() > 0) {
            alpha = Math.max((float) entity.getDyingTick() / fadeStart, 0.0F);
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, entityYaw, 0);
        this.model.renderToBufferWithEntity(entity, poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.pack(0.0f, bl), 1.0F, 1.0F, 1.0F, alpha);

        float emissiveAlpha = getEmissiveAlpha(entity, partialTicks);
        if (emissiveAlpha > 0.01F) {
            VertexConsumer emissiveVertexConsumer = buffer.getBuffer(EMISSIVE_RENDER_TYPE);
            this.model.renderToBufferWithEntity(entity, poseStack, emissiveVertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, emissiveAlpha);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static float getEmissiveAlpha(VertexDomainProjectileEntity entity, float partialTicks) {
        if (entity.getLife() <= 0) {
            return 0.2F;
        }

        float lifeProgress = entity.getLife() / 1200.0F; // 1.0 at full life, 0.0 at death

        // Define total cycle length (Rise 5 + Fall 15 + Cooldown 20 = 40 ticks)
        float cycleTime = 40.0F;

        // Get the current tick in the heartbeat cycle
        float time = (1200 - entity.getLife() + partialTicks) % cycleTime;

        float pulse = 0.0F;

        // Phase 1: Quick Light-Up (0 - 5 ticks)
        if (time < 5) {
            pulse = time / 5.0F; // Linear rise from 0 to 1 in 5 ticks
        }
        // Phase 2: Slow Dim Down (5 - 20 ticks)
        else if (time < 20) {
            float fadeTime = (time - 5) / 15.0F; // Normalize 5→20 ticks into 0→1 range
            pulse = (1.0F - fadeTime * fadeTime); // Quadratic decay for smooth fade
        }
        // Phase 3: Cooldown (20 - 40 ticks) -> Pulse stays at 0

        // Ensure minimum alpha is 0.2 (scale pulse between 0.2 and 1.0)
        pulse = 0.2F + (0.8F * pulse);

        // Make emissive layer fade out as life reaches 0
        return lifeProgress * pulse; // Scales alpha from
    }


    @Override
    public ResourceLocation getTextureLocation(VertexDomainProjectileEntity vertexDomainProjectileEntity) {
        return TEXTURE;
    }
}
