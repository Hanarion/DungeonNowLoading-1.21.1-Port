package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.DungeonBannerBlock;
import dev.hexnowloading.dungeonnowloading.block.client.model.DungeonBannerBlockModel;
import dev.hexnowloading.dungeonnowloading.block.entity.DungeonBannerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

public class DungeonBannerBlockRenderer implements BlockEntityRenderer<DungeonBannerBlockEntity> {

    private final DungeonBannerBlockModel model;

    private static final Map<DungeonBannerBlock.DungeonBannerVariant, ResourceLocation> TEX = new EnumMap<>(DungeonBannerBlock.DungeonBannerVariant.class);

    static {
        // If your textures are named by variant serialized name:
        // textures/block/dungeon_banner/dungeon_banner_spawner_magenta.png
        // textures/block/dungeon_banner/dungeon_banner_hollow.png
        // etc.
        for (DungeonBannerBlock.DungeonBannerVariant v : DungeonBannerBlock.DungeonBannerVariant.values()) {
            TEX.put(v, ResourceLocation.fromNamespaceAndPath(
                    DungeonNowLoading.MOD_ID,
                    "textures/block/dungeon_banner/dungeon_banner_" + v.getSerializedName() + ".png"
            ));
        }
    }

    public DungeonBannerBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new DungeonBannerBlockModel(ctx.bakeLayer(DungeonBannerBlockModel.LAYER_LOCATION));
    }

    @Override
    public void render(DungeonBannerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof DungeonBannerBlock)) return;

        Direction facing = state.getValue(WallBannerBlock.FACING);
        DungeonBannerBlock.DungeonBannerVariant variant = state.getValue(DungeonBannerBlock.VARIANT);

        poseStack.pushPose();

        // Center
        poseStack.translate(0.5, 0.5, 0.5);

        // Rotate to match wall-facing
        float yRot = switch (facing) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST  -> 90f;
            case EAST  -> -90f;
            default -> 0f;
        };

        poseStack.translate(0, (float) -10 /16, 0);

        poseStack.scale(1.0F, -1.0F, -1.0F);

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));

        ResourceLocation tex = TEX.get(variant);
        if (tex == null) return; // safety

        RenderType type = RenderType.entityCutoutNoCull(tex);
        VertexConsumer vc = bufferSource.getBuffer(type);

        waverBanner(be, partialTick);

        model.renderToBuffer(poseStack, vc, packedLight, packedOverlay, 0xFFFFFFFF);

        poseStack.popPose();
    }

    private void waverBanner(DungeonBannerBlockEntity be, float partialTick) {
        float time = 0.0F;
        if (be.getLevel() != null) {
            long gameTime = be.getLevel().getGameTime();
            var pos = be.getBlockPos();

            // vanilla-like per-block phase (stable between sessions)
            float phase = (float) Math.floorMod(
                    (long)(pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13) + gameTime,
                    100L
            );

            time = (phase + partialTick) / 100.0F; // 0..1 looping
        }

        float wave = (-0.0125F + 0.01F * net.minecraft.util.Mth.cos((float)(Math.PI * 2.0) * time))
                * (float)Math.PI;

        model.setWave(wave);
    }
}
