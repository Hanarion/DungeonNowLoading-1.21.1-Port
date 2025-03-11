package dev.hexnowloading.dungeonnowloading.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperOurosModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.FairkeeperOurosRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperOurosLayer<T extends FairkeeperOurosEntity, M extends FairkeeperOurosModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE_EMISSIVE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_ouros/fairkeeper_ouros_head_emissive.png");

    public FairkeeperOurosLayer(FairkeeperOurosRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, FairkeeperOurosEntity fairkeeperOuros, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_EMISSIVE, true));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(fairkeeperOuros, 0), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
