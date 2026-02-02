package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.DungeonBannerBlock;
import dev.hexnowloading.dungeonnowloading.block.client.model.DungeonBannerBlockModel;
import dev.hexnowloading.dungeonnowloading.item.blockitem.DungeonBannerBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class DungeonBannerBlockItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final DungeonBannerBlockModel model;
    private static DungeonBannerBlockItemRenderer INSTANCE;

    private static final Map<DungeonBannerBlock.DungeonBannerVariant, ResourceLocation> TEX = new EnumMap<>(DungeonBannerBlock.DungeonBannerVariant.class);
    static {
        // expects: textures/block/dungeon_banner/dungeon_banner_<serialized>.png
        for (DungeonBannerBlock.DungeonBannerVariant v : DungeonBannerBlock.DungeonBannerVariant.values()) {
            TEX.put(v, new ResourceLocation(
                    DungeonNowLoading.MOD_ID,
                    "textures/block/dungeon_banner/dungeon_banner_" + v.getSerializedName() + ".png"
            ));
        }
    }

    private DungeonBannerBlockItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.model = new DungeonBannerBlockModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(DungeonBannerBlockModel.LAYER_LOCATION));
    }
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {

        if (!(stack.getItem() instanceof DungeonBannerBlockItem bannerItem)) return;

        DungeonBannerBlock.DungeonBannerVariant variant = bannerItem.getVariant();
        ResourceLocation tex = TEX.get(variant);
        if (tex == null) return;

        poseStack.pushPose();

        // These transforms are "good defaults". Tweak to taste.
        switch (ctx) {
            case GUI -> {
                poseStack.translate(0.5, 0.5, 0.0);
                poseStack.scale(1.0f, 1.0f, 1.0f);
            }
            case FIXED -> {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.9f, 0.9f, 0.9f);
            }
            case GROUND -> {
                poseStack.translate(0.5, 0.25, 0.5);
                poseStack.scale(0.7f, 0.7f, 0.7f);
            }
            default -> {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.85f, 0.85f, 0.85f);
            }
        }

        // If your model was authored assuming block-space (like your block renderer),
        // you may want to rotate it so it faces the camera nicely in GUI:
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));

        RenderType type = RenderType.entityCutoutNoCull(tex);
        VertexConsumer vc = buffer.getBuffer(type);

        model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);

        poseStack.popPose();
    }

    public static DungeonBannerBlockItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DungeonBannerBlockItemRenderer();
        }
        return INSTANCE;
    }
}
