package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.ReaperSpiderEyesLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.ReaperSpiderModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ReaperSpiderRenderer<T extends ReaperSpiderEntity> extends MobRenderer<T, ReaperSpiderModel<T>> {
    private static final ResourceLocation SPIDER_LOCATION = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/reaper_spider/reaper_spider.png");

    public ReaperSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new ReaperSpiderModel<>(context.bakeLayer(ReaperSpiderModel.LAYER_LOCATION)), 1.8F);
        this.addLayer(new ReaperSpiderEyesLayer<>(this));
    }

    protected float getFlipDegrees(T $$0) {
        return 180.0F;
    }

    protected void scale(ReaperSpiderEntity $$0, PoseStack $$1, float $$2) {
        $$1.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        float alpha = entity.getRevealAlpha(partialTicks);
        if (alpha >= 0.999F) {
            super.render(entity, entityYaw, partialTicks, poseStack, multiBufferSource, packedLight);
            return;
        }

        poseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(entity, partialTicks);
        this.model.riding = entity.isPassenger();
        this.model.young = entity.isBaby();

        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float ageInTicks = this.getBob(entity, partialTicks);
        float limbSwingAmount = entity.walkAnimation.speed(partialTicks);
        float limbSwing = entity.walkAnimation.position(partialTicks);

        this.setupRotations(entity, poseStack, ageInTicks, bodyYaw, partialTicks, 1.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        limbSwingAmount = Math.min(limbSwingAmount, 1.0F);
        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, xRot);

        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));

        for (RenderLayer<T, ReaperSpiderModel<T>> renderLayer : this.layers) {
            renderLayer.render(poseStack, multiBufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, xRot);
        }

        poseStack.popPose();
    }

    public ResourceLocation getTextureLocation(T $$0) {
        return SPIDER_LOCATION;
    }
}
