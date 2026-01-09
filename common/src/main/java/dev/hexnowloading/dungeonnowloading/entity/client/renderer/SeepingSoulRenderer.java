package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulChaosSpawnerModel;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SeepingSoulRenderer extends EntityRenderer<SeepingSoulEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/seeping_soul/seeping_soul_chaos_spawner.png");
    private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/seeping_soul/seeping_soul_chaos_spawner_eyes.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final RenderType EYES_RENDER_TYPE = RenderType.eyes(EYES_TEXTURE);
    private SeepingSoulChaosSpawnerModel model;

    public SeepingSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new SeepingSoulChaosSpawnerModel(context.bakeLayer(SeepingSoulChaosSpawnerModel.LAYER_LOCATION));
    }

    @Override
    public void render(SeepingSoulEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if (entity.isNoAnimation()) return;

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-1.0f, -1.0F, 1.0F);
        poseStack.translate(0.0f, -1.5f, 0.0f);

        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);

        boolean bl = entity.getHurtTicks() > 0;

        // Base alpha
        float alpha = 0.5F * entity.getHp() / entity.getMaxHp() + 0.1F;

        int emissiveLight = 0xF000F0;

        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, entityYaw, 0);
        this.model.renderToBuffer(
                poseStack,
                vertexConsumer,
                emissiveLight,
                OverlayTexture.pack(0.0f, bl),
                1.0F, 1.0F, 1.0F,
                alpha
        );

        float alpha_eyes = 1.0F;

        VertexConsumer eyesVc = buffer.getBuffer(EYES_RENDER_TYPE);
        this.model.renderToBuffer(
                poseStack,
                eyesVc,
                emissiveLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F,
                alpha_eyes
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }


    @Override
    public ResourceLocation getTextureLocation(SeepingSoulEntity seepingSoulEntity) {
        return TEXTURE;
    }
}
