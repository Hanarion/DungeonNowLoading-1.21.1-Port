package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.VertexOrbProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class VertexOrbProjectileRenderer<T extends VertexOrbProjectileEntity> extends EntityRenderer<VertexOrbProjectileEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_orb_projectile.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private VertexOrbProjectileModel model;

    public VertexOrbProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new VertexOrbProjectileModel(context.bakeLayer(VertexOrbProjectileModel.LAYER_LOCATION));
    }

    @Override
    public boolean shouldRender(VertexOrbProjectileEntity entity, Frustum frustum, double x, double y, double z) {
        if (!entity.shouldRender(x, y, z)) {
            return false;
        } else if (entity.noCulling) {
            return true;
        } else {
            AABB aabb = entity.getBoundingBoxForCulling().inflate(2.5);
            if (aabb.hasNaN() || aabb.getSize() == 0.0) {
                aabb = new AABB(entity.getX() - 2.0, entity.getY() - 2.0, entity.getZ() - 2.0, entity.getX() + 2.0, entity.getY() + 2.0, entity.getZ() + 2.0);
            }

            return frustum.isVisible(aabb);
        }
    }

    @Override
    public void render(VertexOrbProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0F, 1.0F);
        poseStack.translate(0.0f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        boolean bl = entity.getHurtTime() > 0;

        float alpha = 1.0F;
        int fadeStart = 20;
        if (entity.getLife() > 0 && entity.getLife() < fadeStart) {
            alpha = Math.max((float) entity.getLife() / fadeStart, 0.0F);
        } else if (entity.getDyingTick() > 0) {
            alpha = Math.max((float) entity.getDyingTick() / fadeStart, 0.0F);
        }

        int emissiveLight = 0xF000F0;

        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, entityYaw, 0);
        this.model.renderToBufferWithEntity(entity, poseStack, vertexConsumer, emissiveLight, OverlayTexture.pack(0.0f, bl), 1.0F, 1.0F, 1.0F, alpha);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(VertexOrbProjectileEntity vertexOrbProjectileEntity) {
        return TEXTURE;
    }
}
