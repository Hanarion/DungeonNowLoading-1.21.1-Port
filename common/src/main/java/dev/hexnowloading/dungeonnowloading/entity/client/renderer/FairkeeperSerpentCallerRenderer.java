package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperBorosBodyModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperSerpentCallerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FairkeeperSerpentCallerRenderer<T extends FairkeeperSerpentCallerEntity> extends EntityRenderer<FairkeeperSerpentCallerEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper/fairkeeper_serpent_caller.png");
    private FairkeeperSerpentCallerModel model;
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public FairkeeperSerpentCallerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        model = new FairkeeperSerpentCallerModel(renderManager.bakeLayer(FairkeeperSerpentCallerModel.LAYER_LOCATION));
    }

    @Override
    public void render(FairkeeperSerpentCallerEntity entity, float v, float v1, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        float yRot = Mth.rotLerp(v1, entity.yRotO, entity.getYRot());
        this.model.headAnim(yRot);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        poseStack.translate(0.0f, -entity.getBbHeight() + 0.5F, 0.0f);
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, v, v1, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(FairkeeperSerpentCallerEntity fairkeeperEntity) {
        return TEXTURE;
    }
}
