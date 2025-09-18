package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.StatueSkinCache;
import dev.hexnowloading.dungeonnowloading.block.client.model.PlayerStatueModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.PlayerStatuePedestalModel;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PlayerStatueRenderer implements BlockEntityRenderer<PlayerStatueBlockEntity> {
    private final PlayerStatueModel statue;
    private final PlayerStatuePedestalModel pedestal;
    private final Font font; // 🔹 add

    private static final ResourceLocation PEDESTAL_TEX =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/player_statue_pedestal.png");
    private static final ResourceLocation PEDESTAL_TEX_COPPER_NOTCH =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/player_statue_pedestal_copper_notch.png");
    private static final ResourceLocation PEDESTAL_TEX_IRON_NOTCH =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/player_statue_pedestal_iron_notch.png");
    private static final ResourceLocation PEDESTAL_TEX_GOLD_NOTCH =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/player_statue_pedestal_gold_notch.png");
    private static final ResourceLocation PEDESTAL_TEX_DIAMOND_NOTCH =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/player_statue_pedestal_diamond_notch.png");

    private static final ResourceLocation STONE_OVERLAY_TEX =
            new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/player_statue_stone.png");


    public PlayerStatueRenderer(BlockEntityRendererProvider.Context ctx) {
        this.statue   = new PlayerStatueModel(ctx.bakeLayer(PlayerStatueModel.LAYER_LOCATION));
        this.pedestal = new PlayerStatuePedestalModel(ctx.bakeLayer(PlayerStatuePedestalModel.LAYER_LOCATION));
        this.font = ctx.getFont(); // 🔹 grab the font renderer
    }

    @Override
    public void render(PlayerStatueBlockEntity be, float pt, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);

        var state = be.getBlockState();

// statue yaw: 16-step
        int rot16 = state.hasProperty(BlockStateProperties.ROTATION_16)
                ? state.getValue(BlockStateProperties.ROTATION_16)
                : 0;
        float statueYaw = 180.0f - (rot16 * 22.5f);

// pedestal yaw: use FACING if present; else snap rot16 to 0/4/8/12
        float pedestalYaw;
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction f = state.getValue(HorizontalDirectionalBlock.FACING);
            pedestalYaw = 180.0f - f.toYRot();
        } else {
            int cardinal = ((rot16 + 2) / 4) * 4; // nearest of {0,4,8,12}
            pedestalYaw = 180.0f - (cardinal * 22.5f);
        }

        // --- pedestal (rotates with 4-way facing) ---
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(pedestalYaw));
        pose.translate(0f, 1.5005f, 0f);
        pose.mulPose(Axis.YP.rotationDegrees(180f));
        pose.mulPose(Axis.XP.rotationDegrees(180f));

        var pedVx = buf.getBuffer(RenderType.entityCutoutNoCull(PEDESTAL_TEX));
        pedestal.renderPedestal(pose, pedVx, light, overlay);

        var notchTex = notchOverlayTex(be);
        if (notchTex != null) {
            pose.pushPose();
            pose.scale(1.001f, 1.001f, 1.001f); // avoid z-fighting
            var overlayVx = buf.getBuffer(RenderType.entityCutoutNoCull(notchTex));
            pedestal.renderPedestal(pose, overlayVx, light, overlay);
            pose.popPose();
        }
        pose.popPose();

        // --- statue (rotates with 16-step rotation) ---
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(statueYaw));
        pose.translate(0f, 1.75f, 0f);
        pose.mulPose(Axis.YP.rotationDegrees(180f));
        pose.mulPose(Axis.XP.rotationDegrees(180f));
        var skin = StatueSkinCache.get(be.getOwner(), 0.60f, STONE_OVERLAY_TEX);
        statue.useSlimArms(skin.slim());
        //var statVx = buf.getBuffer(RenderType.entityTranslucent(skin.texture()));
        var statVx = buf.getBuffer(RenderType.armorCutoutNoCull(skin.texture()));
        statue.renderToBuffer(pose, statVx, light, overlay, 1f, 1f, 1f, 1f);
        pose.popPose();

        // --- text (aligns with pedestal orientation) ---
        renderPedestalText(be, pose, buf, light, pedestalYaw);

        pose.popPose();
    }

    // Keep 1 px margin on both sides
    private static final float TEXT_MARGIN_BLOCKS = 1f / 16f; // 1 block-pixel
    private static final float FACE_WIDTH_BLOCKS = 1.0f - 2 * TEXT_MARGIN_BLOCKS;

    // Same scales as SignRenderer: 0.666... * (1/64)
    private static final float TEXT_SCALE = 0.6666667f * 0.015625f;

    // Where on the block to draw (front face is Z+ after the rotation we did above)
    // Your pedestal is 0.5 blocks tall; the center of the side is ~0.25.
    // Nudge Z slightly > 0.5 to be in front of the face and avoid z-fighting.
    private static final float TEXT_Y = 4f / 16f;   // tweak to taste
    private static final float TEXT_Z = 0.5005f;  // just in front of face

    private void renderPedestalText(PlayerStatueBlockEntity be, PoseStack pose, MultiBufferSource buf, int worldLight, float pedestalYaw) {
        var lineComp = be.getLine(0);
        if (lineComp == null) return;

        var seqs = this.font.split((FormattedText) lineComp, 4096);
        if (seqs.isEmpty()) return;
        var seq = seqs.get(0);

        boolean glowing = be.isGlowingText();
        int baseRGB = (be.getTextColor() != null ? be.getTextColor().getTextColor() : 0x000000);
        int darkColor = getDarkColorLikeSigns(baseRGB, glowing);

        int argbText, packedLight;
        boolean outlineVisible;
        if (glowing) {
            argbText = 0xFF000000 | baseRGB;
            packedLight = 0x00F000F0;
            outlineVisible = isOutlineVisibleLikeSigns(be.getBlockPos(), baseRGB);
        } else {
            argbText = 0xFF000000 | darkColor;
            packedLight = worldLight;
            outlineVisible = false;
        }

        pose.pushPose();
        {
            // rotate to the pedestal's "front"
            pose.mulPose(Axis.YP.rotationDegrees(pedestalYaw));

            // SOUTH face (front), slight z nudge forward
            pose.translate(0.0f, TEXT_Y, -TEXT_Z);
            pose.mulPose(Axis.YP.rotationDegrees(180f));

            pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

            int w = this.font.width(seqs.get(0));
            float fit = (w > PEDESTAL_TEXT_MAX_PX) ? (float) PEDESTAL_TEXT_MAX_PX / (float) w : 1.0f;
            pose.scale(fit, fit, fit);

            float x = -this.font.width(seqs.get(0)) / 2f;
            float y = -this.font.lineHeight / 2f;

            if (outlineVisible) {
                this.font.drawInBatch8xOutline(seqs.get(0), x, y, argbText, darkColor, pose.last().pose(), buf, packedLight);
            } else {
                this.font.drawInBatch(seqs.get(0), x, y, argbText, false, pose.last().pose(), buf,
                        Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
            }
        }
        pose.popPose();
    }

    // -------- helpers lifted from SignRenderer logic --------

    private static boolean isOutlineVisibleLikeSigns(BlockPos pos, int rgb) {
        // Always outline if black
        if (rgb == net.minecraft.world.item.DyeColor.BLACK.getTextColor()) return true;
        var mc = net.minecraft.client.Minecraft.getInstance();
        var player = mc.player;
        if (player != null && mc.options.getCameraType().isFirstPerson() && player.isScoping()) return true;
        var cam = mc.getCameraEntity();
        return cam != null && cam.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) < (double)(16 * 16);
    }

    private static int getDarkColorLikeSigns(int rgb, boolean glowing) {
        if (rgb == net.minecraft.world.item.DyeColor.BLACK.getTextColor() && glowing) {
            return -988212; // SignRenderer.BLACK_TEXT_OUTLINE_COLOR
        }
        // 40% darken like signs
        int r = (int)(FastColor.ARGB32.red(rgb)   * 0.4);
        int g = (int)(FastColor.ARGB32.green(rgb) * 0.4);
        int b = (int)(FastColor.ARGB32.blue(rgb)  * 0.4);
        return FastColor.ARGB32.color(0, r, g, b);
    }

    public static final int PEDESTAL_TEXT_MAX_PX = 100; // safe pixel budget for one line
    public static int pedestalMaxTextPixels(Font font) { return PEDESTAL_TEXT_MAX_PX; }

    private ResourceLocation notchOverlayTex(PlayerStatueBlockEntity be) {
        return switch (be.getNotchTier()) {
            case COPPER  -> PEDESTAL_TEX_COPPER_NOTCH;
            case IRON    -> PEDESTAL_TEX_IRON_NOTCH;
            case GOLD    -> PEDESTAL_TEX_GOLD_NOTCH;
            case DIAMOND -> PEDESTAL_TEX_DIAMOND_NOTCH;
            case NONE    -> null;
        };
    }
}
