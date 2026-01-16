package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.model.GauntletModel;
import dev.hexnowloading.dungeonnowloading.block.entity.GauntletBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GauntletRenderer implements BlockEntityRenderer<GauntletBlockEntity> {

    private final GauntletModel model;
    private static final ResourceLocation BASE_TEX = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/gauntlet.png");
    private static final ResourceLocation EMISSIVE_TEX = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/gauntlet_emissive.png");
    private static final ResourceLocation UNLIT_TEX = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/block/gauntlet_unlit.png");


    public GauntletRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new GauntletModel(ctx.bakeLayer(GauntletModel.LAYER_LOCATION));
    }

    enum Horn { CENTER, FL, FR, BL, BR }
    private static final Horn[] SEQUENCE = {Horn.CENTER, Horn.FL, Horn.FR, Horn.BL, Horn.BR};

    private Horn[] orderFor(int total) {
        int n = Math.max(1, Math.min(5, total));
        Horn[] arr = new Horn[n];
        System.arraycopy(SEQUENCE, 0, arr, 0, n);
        return arr;
    }

    private ModelPart partFor(Horn h) {
        return switch (h) {
            case CENTER -> model.finger_m;
            case FL     -> model.finger_fl;
            case FR     -> model.finger_fr;
            case BL     -> model.finger_bl;
            case BR     -> model.finger_br;
        };
    }

    private void setAllHornsVisible(boolean v) {
        model.finger_m.visible    = v;
        model.finger_fl.visible = v;
        model.finger_fr.visible = v;
        model.finger_bl.visible = v;
        model.finger_br.visible = v;
    }

    private void setHornVisible(Horn h, boolean v) { partFor(h).visible = v; }

    private void renderHorns(PoseStack ps, VertexConsumer vc, int packedLight, int packedOverlay, Horn[] order) {
        for (Horn h : order) partFor(h).render(ps, vc, packedLight, packedOverlay);
    }

    @Override
    public void render(GauntletBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buf,
                       int packedLight, int packedOverlay) {

        ps.pushPose();

        // 1) center + lift + flip to entity-style orientation
        ps.translate(0.5, 0.0, 0.5);
        ps.mulPose(Axis.XP.rotationDegrees(180f));

        // 2) (optional) rotate by block facing if you have a HORIZONTAL_FACING property
        Direction dir = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        ps.mulPose(Axis.YP.rotationDegrees(dir.toYRot()));

        // 3) use a NO-CULL render type so the inner cage faces are visible
        VertexConsumer baseVC = buf.getBuffer(RenderType.entityCutoutNoCull(BASE_TEX));
        model.brazier.render(ps, baseVC, packedLight, packedOverlay);

        // horns: set visibility by wave total
        var order = orderFor(be.getWavesTotal());
        setAllHornsVisible(false);
        for (Horn h : order) setHornVisible(h, true);

// A — base diffuse for all visible horns
        VertexConsumer hornsBase = buf.getBuffer(RenderType.entityCutoutNoCull(BASE_TEX));
        for (Horn h : order) partFor(h).render(ps, hornsBase, packedLight, packedOverlay);

// B/C — overlay: first N horns emissive, rest unlit
        int litCount = Math.min(be.getWavesCurrent(), order.length);  // <— no active

        for (int idx = 0; idx < order.length; idx++) {
            Horn h = order[idx];
            ps.pushPose();
            if (idx < litCount) {
                VertexConsumer hornsGlow  = buf.getBuffer(RenderType.entityCutoutNoCull(EMISSIVE_TEX));
                partFor(h).render(ps, hornsGlow, LightTexture.FULL_BRIGHT, packedOverlay);
            } else {
                VertexConsumer hornsUnlit = buf.getBuffer(RenderType.entityCutoutNoCull(UNLIT_TEX));
                partFor(h).render(ps, hornsUnlit, packedLight, packedOverlay);
            }
            ps.popPose();
        }

        if (be.isActive()) {
            VertexConsumer glow = buf.getBuffer(RenderType.entityCutoutNoCull(EMISSIVE_TEX));
            model.brazier.render(ps, glow, LightTexture.FULL_BRIGHT, packedOverlay);
        } else {
            VertexConsumer unlit = buf.getBuffer(RenderType.entityCutoutNoCull(UNLIT_TEX));
            model.brazier.render(ps, unlit, packedLight, packedOverlay);
        }

        ps.popPose();
    }

    private static class Bounds { final float cx, cy, cz; Bounds(float x, float y, float z){cx=x;cy=y;cz=z;} }
    private Bounds hornBounds(Horn h) {
        // centers in model "pixel" space (0..16). Adjust to your actual horn boxes.
        return switch (h) {
            case CENTER -> new Bounds(8f, 18f, 8f);
            case FL     -> new Bounds(4f, 18f, 4f);
            case FR     -> new Bounds(12f,18f, 4f);
            case BL     -> new Bounds(4f, 18f,12f);
            case BR     -> new Bounds(12f,18f,12f);
        };
    }
}
