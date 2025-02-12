package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.VertexDomainProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class VertexDomainProjectileRenderer<T extends VertexDomainProjectileEntity> extends EntityRenderer<VertexDomainProjectileEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_domain_projectile.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private VertexDomainProjectileModel model;

    public VertexDomainProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new VertexDomainProjectileModel(context.bakeLayer(VertexDomainProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(VertexDomainProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0f, -entity.getBbHeight() * 1.5F, 0.0f);
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(VertexDomainProjectileEntity vertexDomainProjectileEntity) {
        return TEXTURE;
    }
}
