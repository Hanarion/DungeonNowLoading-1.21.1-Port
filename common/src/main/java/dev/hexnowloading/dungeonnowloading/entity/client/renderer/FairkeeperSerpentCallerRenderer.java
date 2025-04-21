package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperSerpentCallerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperSerpentCallerRenderer<T extends FairkeeperSerpentCallerEntity> extends EntityRenderer<FairkeeperSerpentCallerEntity> {

    private static final ResourceLocation TEXTURE_INACTIVE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_serpent_caller/fairkeeper_serpent_caller_inactive.png");
    private static final ResourceLocation TEXTURE_ACTIVE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_serpent_caller/fairkeeper_serpent_caller_active.png");
    private FairkeeperSerpentCallerModel model;
    private static final RenderType RENDER_TYPE_INACTIVE = RenderType.entityTranslucent(TEXTURE_INACTIVE);
    private static final RenderType RENDER_TYPE_ACTIVE = RenderType.entityTranslucent(TEXTURE_ACTIVE);

    public FairkeeperSerpentCallerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        model = new FairkeeperSerpentCallerModel(renderManager.bakeLayer(FairkeeperSerpentCallerModel.LAYER_LOCATION));
    }

    @Override
    public void render(FairkeeperSerpentCallerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0F, -1.0F);
        poseStack.translate(0.0f, -entity.getBbHeight() * 2.1F + 0.5F, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(- entityYaw));
        //float yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        //this.model.headAnim(yRot);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE_INACTIVE);
        if (entity.isActivated()) {
            vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE_ACTIVE);
        }
        this.model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, entityYaw, 0);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperSerpentCallerEntity fairkeeperEntity) {
        return TEXTURE_INACTIVE;
    }
}
