package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

public class PlayerStatueRenderer implements BlockEntityRenderer<PlayerStatueBlockEntity> {
    private final PlayerStatueModel statue;
    private final PlayerStatuePedestalModel pedestal;
    private final Font font; // 🔹 add

    private static final ResourceLocation PEDESTAL_TEX =
            new ResourceLocation("dungeonnowloading", "textures/block/player_statue_pedestal.png");
    private static final ResourceLocation STONE_OVERLAY_TEX =
            new ResourceLocation("dungeonnowloading", "textures/block/player_statue_stone.png");

    public PlayerStatueRenderer(BlockEntityRendererProvider.Context ctx) {
        this.statue   = new PlayerStatueModel(ctx.bakeLayer(PlayerStatueModel.LAYER_LOCATION));
        this.pedestal = new PlayerStatuePedestalModel(ctx.bakeLayer(PlayerStatuePedestalModel.LAYER_LOCATION));
        this.font = ctx.getFont(); // 🔹 grab the font renderer
    }

    @Override
    public void render(PlayerStatueBlockEntity be, float pt, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);

        // rotate whole thing to block facing (so statue & pedestal align)
        Direction facing = be.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - facing.toYRot()));

        // --- pedestal ---
        pose.pushPose();
        pose.translate(0f, 1.5f, 0f);
        pose.scale(-1f, -1f, 1f);
        var pedVx = buf.getBuffer(RenderType.entityCutoutNoCull(PEDESTAL_TEX));
        pedestal.renderPedestal(pose, pedVx, light, overlay);
        pose.popPose();

        // --- statue ---
        pose.pushPose();
        pose.translate(0f, 1.75f, 0f);
        pose.scale(-1f, -1f, 1f);
        var skin = StatueSkinCache.get(be.getOwner(), 0.60f, STONE_OVERLAY_TEX);
        statue.useSlimArms(skin.slim());
        var statVx = buf.getBuffer(RenderType.entityTranslucent(skin.texture()));
        statue.renderToBuffer(pose, statVx, light, overlay, 1f, 1f, 1f, 1f);
        pose.popPose();

        // --- text (do this in block space, not in the flipped model space) ---
        renderPedestalText(be, pose, buf, light);

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

    private void renderPedestalText(PlayerStatueBlockEntity be, PoseStack pose, MultiBufferSource buf, int worldLight) {
        // we’ll use ONLY the first line for the pedestal text
        var lineComp = be.getLine(0);
        if (lineComp == null) return;

        var seqs = this.font.split((FormattedText) lineComp, 4096);
        if (seqs.isEmpty()) return;
        var seq = seqs.get(0);

        // colors & lighting (same logic as before)
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
            // ⬅️ was: argbText = darkColor;  (alpha = 0 → invisible)
            argbText = 0xFF000000 | darkColor;
            packedLight = worldLight;
            outlineVisible = false;
        }

        // ---- position to the SOUTH face, centered between 4th & 5th pixels (like earlier answer) ----
        pose.pushPose();
        {
            // We already rotated the whole block so its “front” is +Z.
            // SOUTH face is -Z. Move there first…
            pose.translate(0.0f, TEXT_Y, -TEXT_Z);
            // …then rotate so glyphs face outward
            pose.mulPose(Axis.YP.rotationDegrees(180f));

            pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

            int w = this.font.width(seq);
            float fit = (w > PEDESTAL_TEXT_MAX_PX) ? (float) PEDESTAL_TEXT_MAX_PX / (float) w : 1.0f;
            pose.scale(fit, fit, fit);

            float x = -this.font.width(seq) / 2f;
            float y = -this.font.lineHeight / 2f;

            if (outlineVisible) {
                this.font.drawInBatch8xOutline(seq, x, y, argbText, darkColor, pose.last().pose(), buf, packedLight);
            } else {
                this.font.drawInBatch(seq, x, y, argbText, false, pose.last().pose(), buf,
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

    @Override
    public boolean shouldRenderOffScreen(PlayerStatueBlockEntity be) { return true; }
}
