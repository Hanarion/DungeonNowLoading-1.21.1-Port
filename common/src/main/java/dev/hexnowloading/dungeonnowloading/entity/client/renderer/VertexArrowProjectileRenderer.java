package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.VertexArrowProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class VertexArrowProjectileRenderer extends ArrowRenderer<VertexArrowProjectileEntity> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_2.png");
    private static final ResourceLocation TEXTURE_3 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_3.png");

    private VertexArrowProjectileModel model;
    private RenderType renderType = RenderType.entityTranslucent(TEXTURE_0);

    public VertexArrowProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new VertexArrowProjectileModel(context.bakeLayer(VertexArrowProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(VertexArrowProjectileEntity entity, float v, float v1, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
//        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.renderType);



        super.render(entity, v, v1, poseStack, multiBufferSource, i);

//        entity.foo
//        poseStack.pushPose();
//        poseStack.scale(1f, 1f, 1f);
//        poseStack.translate(0.0f, -entity.getBbHeight() + 0.5f, 0.0f);
//        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
//        poseStack.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(VertexArrowProjectileEntity entity) {
        return switch (entity.getPowerLevel()) {
            case 0 -> TEXTURE_0;
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case 3 -> TEXTURE_3;
            default -> null;
        };
    }
}
