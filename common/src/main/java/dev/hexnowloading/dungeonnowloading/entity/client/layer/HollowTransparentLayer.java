package dev.hexnowloading.dungeonnowloading.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.HollowModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.HollowRenderer;
import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;

public class HollowTransparentLayer<T extends HollowEntity, M extends HollowModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/hollow_body.png");
    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/hollow.png");

    public HollowTransparentLayer(HollowRenderer renderer) {
        super(renderer);
    }

    public float oscillatingAlphaValue(HollowEntity entity, float partialTicks, float oscillationRate, float maxAlpha) {
        float wave01 = ((float)Math.sin((entity.tickCount + partialTicks) * oscillationRate) + 1.0F) * 0.5F;
        return wave01 * maxAlpha;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       HollowEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        // 1) BODY (translucent + alpha logic)
        VertexConsumer bodyVc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        int light = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()); // 0..15

        float t = Mth.clamp(light / 15.0F, 0.0F, 1.0F);
        t = t * t * (3.0F - 2.0F * t); // smoothstep

        // Overshoot targets
        float baseAlpha = Mth.lerp(t, 0.08F, 1.10F);   // ends > 1
        float oscAmp    = Mth.lerp(t, 0.28F, 0.12F);   // still shimmers a bit

        float wave01 = ((float)Math.sin((entity.tickCount + partialTicks) * 0.05F) + 1.0F) * 0.5F;

        // raw alpha can exceed 1.0
        float rawAlpha = baseAlpha + wave01 * oscAmp;

        // clamp only at the end
        float alpha = Mth.clamp(rawAlpha, 0.0F, 1.0F);


        this.getParentModel().renderToBuffer(
                poseStack,
                bodyVc,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                1.0F, 1.0F, 1.0F,
                alpha
        );


        // 2) EYES (always emissive, ignore lighting)
        VertexConsumer eyeVc = buffer.getBuffer(RenderType.entityTranslucentEmissive(EYE_TEXTURE));
        this.getParentModel().renderToBuffer(
                poseStack,
                eyeVc,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                1.0F, 1.0F, 1.0F,
                1.0F
        );
    }
}
