package dev.hexnowloading.dungeonnowloading.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.MendingAuraBlockEntityRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class MendingAuraOverlayRenderer {
    private static final int OVERLAY_TICKS = 40;
    private static final float MODEL_OVERLAY_OFFSET = 0.002F;
    private static final float SHAPE_OVERLAY_EPSILON = 0.001F;
    private static final ResourceLocation MENDING_AURA_SPRITE = new ResourceLocation("dungeonnowloading", "block/mending_aura_0");
    private static final Map<TextureAtlasSprite, Map<BakedQuad, List<BakedQuad>>> OVERLAY_REMAPPED_QUAD_CACHE = new IdentityHashMap<>();

    private MendingAuraOverlayRenderer() {
    }

    public static void add(BlockPos pos) {
        MendingAuraOverlayClientState.add(pos, OVERLAY_TICKS);
    }

    public static void render(PoseStack poseStack, float partialTick, Camera camera) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            return;
        }

        var overlays = MendingAuraOverlayClientState.getActive(level, partialTick);
        if (overlays.isEmpty()) {
            return;
        }

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        TextureAtlasSprite auraSprite = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MENDING_AURA_SPRITE);
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer translucentConsumer = bufferSource.getBuffer(RenderType.translucent());

        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        for (MendingAuraOverlayClientState.ActiveOverlay overlay : overlays) {
            BlockPos pos = overlay.pos();
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getBlock() instanceof MendingAuraBlock) {
                continue;
            }

            BakedModel storedModel = dispatcher.getBlockModel(state);
            BakedModel auraModel = new MendingAuraBlockEntityRenderer.AuraTextureModel(
                    storedModel,
                    auraSprite,
                    level,
                    pos,
                    OVERLAY_REMAPPED_QUAD_CACHE,
                    MODEL_OVERLAY_OFFSET
            );

            poseStack.pushPose();
            poseStack.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
            VertexConsumer alphaConsumer = new AlphaVertexConsumer(translucentConsumer, overlay.alpha());
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (shouldUseOnlyBlockEntityOverlay(state, storedModel, pos, blockEntity)) {
                minecraft.getBlockEntityRenderDispatcher().render(
                        blockEntity,
                        partialTick,
                        poseStack,
                        new MendingAuraBlockEntityOverlayBuffer(bufferSource, overlay.alpha())
                );
            } else if (needsShapeOverlay(state, storedModel, pos)) {
                renderShapeOverlay(state, level, pos, poseStack, alphaConsumer, auraSprite);
            } else {
                dispatcher.getModelRenderer().renderModel(
                        poseStack.last(),
                        alphaConsumer,
                        state,
                        auraModel,
                        1.0F,
                        1.0F,
                        1.0F,
                        LightTexture.FULL_BRIGHT,
                        0
                );
                if (blockEntity != null) {
                    minecraft.getBlockEntityRenderDispatcher().render(
                            blockEntity,
                            partialTick,
                            poseStack,
                            new MendingAuraBlockEntityOverlayBuffer(bufferSource, overlay.alpha())
                    );
                }
            }
            poseStack.popPose();
        }

        bufferSource.endBatch(RenderType.translucent());
        bufferSource.endBatch(MendingAuraBlockEntityOverlayBuffer.RENDER_TYPE);
    }

    private static boolean shouldUseOnlyBlockEntityOverlay(BlockState state, BakedModel model, BlockPos pos, BlockEntity blockEntity) {
        return blockEntity != null && (state.getRenderShape() != RenderShape.MODEL || needsShapeOverlay(state, model, pos));
    }

    private static boolean needsShapeOverlay(BlockState state, BakedModel model, BlockPos pos) {
        return model.isCustomRenderer() || !hasAnyBakedQuads(state, model, pos);
    }

    private static boolean hasAnyBakedQuads(BlockState state, BakedModel model, BlockPos pos) {
        var random = net.minecraft.util.RandomSource.create(state.getSeed(pos));
        for (Direction direction : Direction.values()) {
            if (!model.getQuads(state, direction, random).isEmpty()) {
                return true;
            }
        }
        return !model.getQuads(state, null, random).isEmpty();
    }

    private static void renderShapeOverlay(BlockState state, Level level, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite auraSprite) {
        VoxelShape shape = state.getInteractionShape(level, pos);
        if (shape.isEmpty()) {
            shape = state.getShape(level, pos, CollisionContext.empty());
        }
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> renderBox(
                consumer,
                matrix,
                normal,
                auraSprite,
                inflateBox(minX, minY, minZ, maxX, maxY, maxZ)
        ));
    }

    private static AABB inflateBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AABB(
                Math.max(0.0D, minX - SHAPE_OVERLAY_EPSILON),
                Math.max(0.0D, minY - SHAPE_OVERLAY_EPSILON),
                Math.max(0.0D, minZ - SHAPE_OVERLAY_EPSILON),
                Math.min(1.0D, maxX + SHAPE_OVERLAY_EPSILON),
                Math.min(1.0D, maxY + SHAPE_OVERLAY_EPSILON),
                Math.min(1.0D, maxZ + SHAPE_OVERLAY_EPSILON)
        );
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, TextureAtlasSprite auraSprite, AABB box) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        renderFace(consumer, matrix, normal, auraSprite, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, Direction.WEST, minZ, maxZ, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, Direction.EAST, minZ, maxZ, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, Direction.NORTH, minX, maxX, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, Direction.SOUTH, minX, maxX, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, minX, maxY, maxZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, Direction.UP, minX, maxX, minZ, maxZ);
        renderFace(consumer, matrix, normal, auraSprite, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, Direction.DOWN, minX, maxX, minZ, maxZ);
    }

    private static void renderFace(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, TextureAtlasSprite auraSprite, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Direction direction, float minUBlock, float maxUBlock, float minVBlock, float maxVBlock) {
        float u0 = auraSprite.getU(minUBlock * 16.0F);
        float u1 = auraSprite.getU(maxUBlock * 16.0F);
        float v0 = auraSprite.getV(minVBlock * 16.0F);
        float v1 = auraSprite.getV(maxVBlock * 16.0F);
        vertex(consumer, matrix, normal, direction, x1, y1, z1, u0, v1);
        vertex(consumer, matrix, normal, direction, x2, y2, z2, u0, v0);
        vertex(consumer, matrix, normal, direction, x3, y3, z3, u1, v0);
        vertex(consumer, matrix, normal, direction, x4, y4, z4, u1, v1);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Direction direction, float x, float y, float z, float u, float v) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(0)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .endVertex();
    }

    private static class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alpha;

        private AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            return this.delegate.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this.delegate.color(red, green, blue, Math.round(alpha * this.alpha));
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this.delegate.uv(u, v);
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this.delegate.overlayCoords(u, v);
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this.delegate.uv2(u, v);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this.delegate.normal(x, y, z);
        }

        @Override
        public void endVertex() {
            this.delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            this.delegate.defaultColor(red, green, blue, Math.round(alpha * this.alpha));
        }

        @Override
        public void unsetDefaultColor() {
            this.delegate.unsetDefaultColor();
        }
    }
}
