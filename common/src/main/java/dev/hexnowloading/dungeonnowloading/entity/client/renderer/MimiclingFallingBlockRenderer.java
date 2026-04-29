package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.entity.misc.MimiclingFallingBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class MimiclingFallingBlockRenderer extends EntityRenderer<MimiclingFallingBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public MimiclingFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(MimiclingFallingBlockEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        BlockState blockState = entity.getBlockState();
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        Level level = entity.level();
        if (blockState == level.getBlockState(entity.blockPosition()) || blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }

        poseStack.pushPose();
        BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        this.dispatcher.getModelRenderer().tesselateBlock(
                level,
                this.dispatcher.getBlockModel(blockState),
                blockState,
                blockPos,
                poseStack,
                bufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)),
                false,
                RandomSource.create(),
                blockState.getSeed(entity.getStartPos()),
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MimiclingFallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
