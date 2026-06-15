package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.BurnacleBlock;
import dev.hexnowloading.dungeonnowloading.block.client.model.BurnacleBudModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.BurnacleElderModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.BurnacleJuvenileModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.BurnacleMatureModel;
import dev.hexnowloading.dungeonnowloading.block.entity.BurnacleBlockEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class BurnacleBlockRenderer implements BlockEntityRenderer<BurnacleBlockEntity> {
    private static final ResourceLocation BUD_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/burnacle_bud.png");
    private static final ResourceLocation JUVENILE_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/burnacle_juvenile.png");
    private static final ResourceLocation MATURE_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/burnacle_mature.png");
    private static final ResourceLocation ELDER_TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/burnacle_elder.png");
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

    private final BurnacleBudModel budModel;
    private final BurnacleJuvenileModel juvenileModel;
    private final BurnacleMatureModel matureModel;
    private final BurnacleElderModel elderModel;

    public BurnacleBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.budModel = new BurnacleBudModel(context.bakeLayer(BurnacleBudModel.LAYER_LOCATION));
        this.juvenileModel = new BurnacleJuvenileModel(context.bakeLayer(BurnacleJuvenileModel.LAYER_LOCATION));
        this.matureModel = new BurnacleMatureModel(context.bakeLayer(BurnacleMatureModel.LAYER_LOCATION));
        this.elderModel = new BurnacleElderModel(context.bakeLayer(BurnacleElderModel.LAYER_LOCATION));
    }

    @Override
    public void render(BurnacleBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof BurnacleBlock)) {
            return;
        }

        BurnacleRenderStage renderStage = BurnacleRenderStage.from(state.getValue(BurnacleBlock.STAGE), this);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        rotateOutward(poseStack, state.getValue(BurnacleBlock.FACING));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.getValue(BurnacleBlock.ROTATION) * 90.0F));
        poseStack.translate(0.0F, 1.0F, 0.0F);
        poseStack.scale(1.0F, -1.0F, -1.0F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderStage.model().renderType(renderStage.texture()));
        float ageInTicks = blockEntity.getLevel() == null ? 0.0F : blockEntity.getLevel().getGameTime() + partialTick;
        renderStage.setupAnim(blockEntity, partialTick, ageInTicks);
        renderStage.model().renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        this.renderDestroyProgress(blockEntity, renderStage, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    private void renderDestroyProgress(BurnacleBlockEntity blockEntity, BurnacleRenderStage renderStage, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int progress = AnimatedBlockDestroyProgress.getProgress(blockEntity.getBlockPos());
        if (progress < 0 || progress >= DESTROY_STAGES.length) {
            return;
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.crumbling(DESTROY_STAGES[progress]));
        VertexConsumer repeatingVertexConsumer = new CenteredRepeatingUvVertexConsumer(vertexConsumer, renderStage.textureWidth(), renderStage.textureHeight());
        renderStage.model().renderToBuffer(poseStack, repeatingVertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
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

    private record BurnacleRenderStage(HierarchicalModel<?> model, ResourceLocation texture, int textureWidth, int textureHeight, AnimationSetup animationSetup) {
        private static BurnacleRenderStage from(BurnacleBlock.Stage stage, BurnacleBlockRenderer renderer) {
            return switch (stage) {
                case BUD -> new BurnacleRenderStage(renderer.budModel, BUD_TEXTURE, 32, 32, renderer.budModel::setupAnim);
                case JUVENILE -> new BurnacleRenderStage(renderer.juvenileModel, JUVENILE_TEXTURE, 64, 64, renderer.juvenileModel::setupAnim);
                case MATURE -> new BurnacleRenderStage(renderer.matureModel, MATURE_TEXTURE, 64, 64, renderer.matureModel::setupAnim);
                case ELDER -> new BurnacleRenderStage(renderer.elderModel, ELDER_TEXTURE, 64, 64, renderer.elderModel::setupAnim);
            };
        }

        private void setupAnim(BurnacleBlockEntity blockEntity, float partialTick, float ageInTicks) {
            this.animationSetup.setup(blockEntity, partialTick, ageInTicks);
        }
    }

    @FunctionalInterface
    private interface AnimationSetup {
        void setup(BurnacleBlockEntity blockEntity, float partialTick, float ageInTicks);
    }
}
