package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.model.WispwardChestModel;
import dev.hexnowloading.dungeonnowloading.block.entity.WispwardChestBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class WispwardChestBlockRenderer implements BlockEntityRenderer<WispwardChestBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/wispward_chest.png");

    private final WispwardChestModel model;

    public WispwardChestBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new WispwardChestModel(context.bakeLayer(WispwardChestModel.LAYER_LOCATION));
    }

    @Override
    public void render(WispwardChestBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) : Direction.NORTH;
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderBase(poseStack, vertexConsumer, light, overlay);
        this.model.renderFire(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, overlay, blockEntity.getLanternProgress());

        poseStack.popPose();
    }
}
