package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.BurnacleBlock;
import dev.hexnowloading.dungeonnowloading.block.client.model.BurnacleMatureModel;
import dev.hexnowloading.dungeonnowloading.block.entity.BurnacleBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class BurnacleBlockRenderer implements BlockEntityRenderer<BurnacleBlockEntity> {
    private static final ResourceLocation MATURE_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/burnacle_mature.png");
    private static final ResourceLocation[] DESTROY_STAGES = new ResourceLocation[] {
            new ResourceLocation("textures/block/destroy_stage_0.png"),
            new ResourceLocation("textures/block/destroy_stage_1.png"),
            new ResourceLocation("textures/block/destroy_stage_2.png"),
            new ResourceLocation("textures/block/destroy_stage_3.png"),
            new ResourceLocation("textures/block/destroy_stage_4.png"),
            new ResourceLocation("textures/block/destroy_stage_5.png"),
            new ResourceLocation("textures/block/destroy_stage_6.png"),
            new ResourceLocation("textures/block/destroy_stage_7.png"),
            new ResourceLocation("textures/block/destroy_stage_8.png"),
            new ResourceLocation("textures/block/destroy_stage_9.png")
    };
    private static final int MATURE_TEXTURE_WIDTH = 64;
    private static final int MATURE_TEXTURE_HEIGHT = 64;

    private final BurnacleMatureModel matureModel;

    public BurnacleBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.matureModel = new BurnacleMatureModel(context.bakeLayer(BurnacleMatureModel.LAYER_LOCATION));
    }

    @Override
    public void render(BurnacleBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof BurnacleBlock) || state.getValue(BurnacleBlock.STAGE) != BurnacleBlock.Stage.MATURE) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        rotateOutward(poseStack, state.getValue(BurnacleBlock.FACING));
        poseStack.translate(0.0F, 1.0F, 0.0F);
        poseStack.scale(1.0F, -1.0F, -1.0F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.matureModel.renderType(MATURE_TEXTURE));
        float ageInTicks = blockEntity.getLevel() == null ? 0.0F : blockEntity.getLevel().getGameTime() + partialTick;
        this.matureModel.setupAnim(blockEntity, partialTick, ageInTicks);
        this.matureModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        this.renderDestroyProgress(blockEntity, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    private void renderDestroyProgress(BurnacleBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int progress = AnimatedBlockDestroyProgress.getProgress(blockEntity.getBlockPos());
        if (progress < 0 || progress >= DESTROY_STAGES.length) {
            return;
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.crumbling(DESTROY_STAGES[progress]));
        VertexConsumer repeatingVertexConsumer = new CenteredRepeatingUvVertexConsumer(vertexConsumer, MATURE_TEXTURE_WIDTH, MATURE_TEXTURE_HEIGHT);
        this.matureModel.renderToBuffer(poseStack, repeatingVertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void rotateOutward(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case UP -> {
            }
        }
    }
}
