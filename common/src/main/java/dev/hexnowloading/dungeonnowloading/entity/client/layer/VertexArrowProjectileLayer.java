package dev.hexnowloading.dungeonnowloading.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.VertexArrowProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.VertexArrowProjectileRenderer;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class VertexArrowProjectileLayer<T extends VertexArrowProjectileEntity, M extends VertexArrowProjectileModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE_0 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_2.png");
    private static final ResourceLocation TEXTURE_3 = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/vertex_arrow_projectile/vertex_arrow_projectile_3.png");
//    private static final ResourceLocation TEXTURE_CHAINED = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/chaos_spawner/chaos_spawner_chained.png");
//    private static final ResourceLocation TEXTURE_SHOCKWAVE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/chaos_spawner/chaos_spawner_shockwave.png");
//    private static final ResourceLocation TEXTURE_CHAOS_HEXAHEDRON = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/chaos_spawner/chaos_spawner_chaos_hexahedron.png");

    public VertexArrowProjectileLayer(VertexArrowProjectileRenderer renderer) {
        super((RenderLayerParent<T, M>) renderer);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, VertexArrowProjectileEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//        if (entitylivingbaseIn.foo) {
//            VertexConsumer arrowConsumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_0, true));
//            this.getParentModel().renderToBuffer(matrixStackIn, arrowConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 1.0F, 1.0F, 1.0F, 1.0F);

        //        if (entitylivingbaseIn.smashAttackAnimationState.isStarted()) {
//            VertexConsumer shockwaveVertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_SHOCKWAVE, true));
//            this.getParentModel().renderToBuffer(matrixStackIn, shockwaveVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 1.0F, 1.0F, 1.0F, 1.0F);
//        } else if (entitylivingbaseIn.rangeAttackAnimationState.isStarted() || entitylivingbaseIn.rangeBurstAttackAnimationState.isStarted()) {
//            VertexConsumer chaosHexahedronVertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_CHAOS_HEXAHEDRON, true));
//            this.getParentModel().renderToBuffer(matrixStackIn, chaosHexahedronVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 1.0F, 1.0F, 1.0F, 0.8F);
//        } else if (entitylivingbaseIn.getState() == VertexArrowProjectileEntity.State.SLEEPING || entitylivingbaseIn.getAwakeningTick() > 100) {
//            VertexConsumer chainVertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_CHAINED, true));
//            this.getParentModel().renderToBuffer(matrixStackIn, chainVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 1.0F, 1.0F, 1.0F, 0.8F);
//        } else {
//            VertexConsumer eyesVertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_EYES, true));
//            this.getParentModel().renderToBuffer(matrixStackIn, eyesVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 1.0F, 1.0F, 1.0F, 1.0F);
//        }
    }
}
