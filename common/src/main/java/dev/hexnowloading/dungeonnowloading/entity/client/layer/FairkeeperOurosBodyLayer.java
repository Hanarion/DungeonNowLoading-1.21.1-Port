package dev.hexnowloading.dungeonnowloading.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperOurosBodyModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.FairkeeperOurosBodyRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperOurosBodyLayer<T extends FairkeeperOurosPartEntity, M extends FairkeeperOurosBodyModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE_EMISSIVE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_ouros/fairkeeper_ouros_body_emissive.png");

    public FairkeeperOurosBodyLayer(FairkeeperOurosBodyRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, FairkeeperOurosPartEntity body, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_EMISSIVE, true));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(body, 0), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
