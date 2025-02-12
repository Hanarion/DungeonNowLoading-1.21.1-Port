package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.VertexOrbProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class VertexOrbProjectileRenderer<T extends VertexOrbProjectileEntity> extends EntityRenderer<VertexOrbProjectileEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_orb_projectile.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private VertexOrbProjectileModel model;

    public VertexOrbProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new VertexOrbProjectileModel(context.bakeLayer(VertexOrbProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(VertexOrbProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0f, -entity.getBbHeight() * 1.5 + 0.5F, 0.0f);
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        //int p = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
        boolean bl = entity.getHurtTime() > 0;
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.pack(0.0f, bl), 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(VertexOrbProjectileEntity vertexOrbProjectileEntity) {
        return TEXTURE;
    }
}
