package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.GauntletVaultBlock;
import dev.hexnowloading.dungeonnowloading.block.client.model.GauntletVaultModel;
import dev.hexnowloading.dungeonnowloading.block.entity.GauntletVaultBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class GauntletVaultRenderer implements BlockEntityRenderer<GauntletVaultBlockEntity> {

    private static final ResourceLocation TEX =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/gauntlet_vault.png");

    private final GauntletVaultModel model;

    public GauntletVaultRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(GauntletVaultModel.LAYER_LOCATION);
        this.model = new GauntletVaultModel(root);
    }

    @Override
    public void render(GauntletVaultBlockEntity be, float pt, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {

        Direction facing = be.getBlockState().getValue(GauntletVaultBlock.FACING);

        pose.pushPose();
        // center & face
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.XP.rotationDegrees(180f));
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        VertexConsumer vc = buffers.getBuffer(model.renderType(TEX));
        model.renderToBuffer(pose, vc, packedLight, packedOverlay, 1f, 1f, 1f, 1f);

        pose.popPose();
    }
}
