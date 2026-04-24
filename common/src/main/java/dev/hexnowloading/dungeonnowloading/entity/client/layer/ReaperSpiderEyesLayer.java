package dev.hexnowloading.dungeonnowloading.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.ReaperSpiderModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.ReaperSpiderRenderer;
import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class ReaperSpiderEyesLayer<T extends ReaperSpiderEntity, M extends ReaperSpiderModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE_EMISSIVE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/reaper_spider/reaper_spider_eyes.png");

    public ReaperSpiderEyesLayer(ReaperSpiderRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, ReaperSpiderEntity body, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean emissive = body.areEyesEmissive(partialTicks);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(emissive
                ? RenderType.entityTranslucentEmissive(TEXTURE_EMISSIVE, true)
                : RenderType.entityTranslucent(TEXTURE_EMISSIVE));
        float strength = body.getEyesAlpha(partialTicks);
        int light = emissive ? 0xF00000 : packedLightIn;
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(body, 0.0F), strength, strength, strength, strength);
    }
}
