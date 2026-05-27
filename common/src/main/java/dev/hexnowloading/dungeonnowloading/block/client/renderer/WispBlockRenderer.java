package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.block.WispBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.WispBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.BlockState;

public class WispBlockRenderer implements BlockEntityRenderer<WispBlockEntity> {
    public WispBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WispBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        BlockState state = blockEntity.getBlockState();
        float age = blockEntity.getLevel().getGameTime() + partialTick + blockEntity.getBlockPos().asLong() % 37L;
        float wave = age * 0.08F;
        float bob = Mth.sin(wave) * 0.05F;
        float xTilt = Mth.sin(wave * 0.8F) * 4.0F;
        float zTilt = Mth.cos(wave * 0.7F) * 4.0F;
        float yRotation = -RotationSegment.convertToDegrees(state.getValue(WispBlock.ROTATION));

        poseStack.pushPose();
        poseStack.translate(0.0D, bob, 0.0D);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(xTilt));
        poseStack.mulPose(Axis.ZP.rotationDegrees(zTilt));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.translucent()),
                state,
                model,
                1.0F,
                1.0F,
                1.0F,
                LightTexture.FULL_BRIGHT,
                packedOverlay
        );
        poseStack.popPose();
    }
}
