package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class MendingAuraBlockEntityRenderer implements BlockEntityRenderer<MendingAuraBlockEntity> {

    private static final int U_OFFSET = 4;
    private static final int V_OFFSET = 5;
    private static final int VERTEX_STRIDE = 8;
    private static final int VERTEX_COUNT = 4;
    private static final int MAX_MASKED_PIXELS_PER_QUAD = 4096;
    private static final float SHAPE_OVERLAY_EPSILON = 0.001F;
    public static final Map<TextureAtlasSprite, Map<BakedQuad, List<BakedQuad>>> GLOBAL_REMAPPED_QUAD_CACHE = new IdentityHashMap<>();
    private final Map<TextureAtlasSprite, Map<BakedQuad, List<BakedQuad>>> remappedQuadCache = new IdentityHashMap<>();

    public MendingAuraBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MendingAuraBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState storedState = blockEntity.getStoredBlockState();
        if (storedState == null || storedState.getBlock() instanceof MendingAuraBlock) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        BakedModel storedModel = dispatcher.getBlockModel(storedState);
        TextureAtlasSprite auraSprite = dispatcher.getBlockModel(DNLBlocks.MENDING_AURA.get().defaultBlockState()).getParticleIcon();

        if (blockEntity.getLevel() == null) {
            return;
        }

        BakedModel auraModel = new AuraTextureModel(storedModel, auraSprite, blockEntity.getLevel(), blockEntity.getBlockPos(), this.remappedQuadCache);
        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.translucent()),
                storedState,
                auraModel,
                1.0F,
                1.0F,
                1.0F,
                LightTexture.FULL_BRIGHT,
                packedOverlay
        );

        if (shouldRenderInteractionShapeOverlay(storedState, storedModel, blockEntity)) {
            renderInteractionShapeOverlay(storedState, blockEntity, poseStack, buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS, false)), auraSprite, packedOverlay);
        }
    }

    private static boolean shouldRenderInteractionShapeOverlay(BlockState state, BakedModel model, MendingAuraBlockEntity blockEntity) {
        return model.isCustomRenderer() || !hasBakedQuads(state, model, blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static boolean hasBakedQuads(BlockState state, BakedModel model, @Nullable BlockAndTintGetter level, BlockPos pos) {
        RandomSource random = RandomSource.create(state.getSeed(pos));
        for (Direction direction : Direction.values()) {
            if (!model.getQuads(state, direction, random).isEmpty()) {
                return true;
            }
        }
        return !model.getQuads(state, null, random).isEmpty();
    }

    private static void renderInteractionShapeOverlay(BlockState storedState, MendingAuraBlockEntity blockEntity, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite auraSprite, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        VoxelShape shape = storedState.getInteractionShape(blockEntity.getLevel(), blockEntity.getBlockPos());
        if (shape.isEmpty()) {
            shape = storedState.getShape(blockEntity.getLevel(), blockEntity.getBlockPos(), CollisionContext.empty());
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
                packedOverlay,
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

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, TextureAtlasSprite auraSprite, int packedOverlay, AABB box) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        renderFace(consumer, matrix, normal, auraSprite, packedOverlay, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, Direction.WEST, minZ, maxZ, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, packedOverlay, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, Direction.EAST, minZ, maxZ, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, packedOverlay, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, Direction.NORTH, minX, maxX, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, packedOverlay, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, Direction.SOUTH, minX, maxX, minY, maxY);
        renderFace(consumer, matrix, normal, auraSprite, packedOverlay, minX, maxY, maxZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, Direction.UP, minX, maxX, minZ, maxZ);
        renderFace(consumer, matrix, normal, auraSprite, packedOverlay, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, Direction.DOWN, minX, maxX, minZ, maxZ);
    }

    private static void renderFace(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, TextureAtlasSprite auraSprite, int packedOverlay, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Direction direction, float minUBlock, float maxUBlock, float minVBlock, float maxVBlock) {
        float u0 = auraSprite.getU(minUBlock * 16.0F);
        float u1 = auraSprite.getU(maxUBlock * 16.0F);
        float v0 = auraSprite.getV(minVBlock * 16.0F);
        float v1 = auraSprite.getV(maxVBlock * 16.0F);
        vertex(consumer, matrix, normal, packedOverlay, direction, x1, y1, z1, u0, v1);
        vertex(consumer, matrix, normal, packedOverlay, direction, x2, y2, z2, u0, v0);
        vertex(consumer, matrix, normal, packedOverlay, direction, x3, y3, z3, u1, v0);
        vertex(consumer, matrix, normal, packedOverlay, direction, x4, y4, z4, u1, v1);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, int packedOverlay, Direction direction, float x, float y, float z, float u, float v) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .endVertex();
    }

    private static List<BakedQuad> remapQuad(BakedQuad quad, TextureAtlasSprite auraSprite) {
        TextureAtlasSprite originalSprite = quad.getSprite();
        SpriteContents originalContents = originalSprite.contents();
        UvBounds uvBounds = UvBounds.from(quad, originalSprite);

        if (uvBounds.isDegenerate() || !hasTransparentPixels(originalContents, uvBounds)) {
            return Collections.singletonList(remapFullQuad(quad, auraSprite));
        }

        int xStart = Math.max(0, (int) Math.floor(uvBounds.minU / 16.0F * originalContents.width()));
        int xEnd = Math.min(originalContents.width(), (int) Math.ceil(uvBounds.maxU / 16.0F * originalContents.width()));
        int yStart = Math.max(0, (int) Math.floor(uvBounds.minV / 16.0F * originalContents.height()));
        int yEnd = Math.min(originalContents.height(), (int) Math.ceil(uvBounds.maxV / 16.0F * originalContents.height()));

        if ((xEnd - xStart) * (yEnd - yStart) > MAX_MASKED_PIXELS_PER_QUAD) {
            return Collections.singletonList(remapFullQuad(quad, auraSprite));
        }

        boolean[][] opaquePixels = new boolean[yEnd - yStart][xEnd - xStart];
        for (int y = yStart; y < yEnd; y++) {
            for (int x = xStart; x < xEnd; x++) {
                opaquePixels[y - yStart][x - xStart] = !originalContents.isTransparent(0, x, y);
            }
        }

        return remapOpaqueRectangles(quad, originalSprite, auraSprite, uvBounds, originalContents, opaquePixels, xStart, yStart);
    }

    private static BakedQuad remapFullQuad(BakedQuad quad, TextureAtlasSprite auraSprite) {
        int[] vertices = quad.getVertices().clone();
        TextureAtlasSprite originalSprite = quad.getSprite();

        for (int vertex = 0; vertex < VERTEX_COUNT; vertex++) {
            int vertexOffset = vertex * VERTEX_STRIDE;
            int uIndex = vertexOffset + U_OFFSET;
            int vIndex = vertexOffset + V_OFFSET;
            float originalU = Float.intBitsToFloat(vertices[uIndex]);
            float originalV = Float.intBitsToFloat(vertices[vIndex]);
            float localU = originalSprite.getUOffset(originalU);
            float localV = originalSprite.getVOffset(originalV);

            vertices[uIndex] = Float.floatToRawIntBits(auraSprite.getU(localU));
            vertices[vIndex] = Float.floatToRawIntBits(auraSprite.getV(localV));
        }

        return new BakedQuad(vertices, -1, quad.getDirection(), auraSprite, quad.isShade());
    }

    private static BakedQuad remapPixelQuad(BakedQuad quad, TextureAtlasSprite originalSprite, TextureAtlasSprite auraSprite, UvBounds uvBounds, float minU, float maxU, float minV, float maxV) {
        int[] sourceVertices = quad.getVertices();
        int[] vertices = sourceVertices.clone();

        for (int vertex = 0; vertex < VERTEX_COUNT; vertex++) {
            int vertexOffset = vertex * VERTEX_STRIDE;
            float sourceU = originalSprite.getUOffset(Float.intBitsToFloat(sourceVertices[vertexOffset + U_OFFSET]));
            float sourceV = originalSprite.getVOffset(Float.intBitsToFloat(sourceVertices[vertexOffset + V_OFFSET]));
            float targetU = closerToMax(sourceU, uvBounds.minU, uvBounds.maxU) ? maxU : minU;
            float targetV = closerToMax(sourceV, uvBounds.minV, uvBounds.maxV) ? maxV : minV;
            float s = (targetU - uvBounds.minU) / (uvBounds.maxU - uvBounds.minU);
            float t = (targetV - uvBounds.minV) / (uvBounds.maxV - uvBounds.minV);

            Vec3f position = interpolatePosition(quad, originalSprite, uvBounds, s, t);
            vertices[vertexOffset] = Float.floatToRawIntBits(position.x);
            vertices[vertexOffset + 1] = Float.floatToRawIntBits(position.y);
            vertices[vertexOffset + 2] = Float.floatToRawIntBits(position.z);
            vertices[vertexOffset + U_OFFSET] = Float.floatToRawIntBits(auraSprite.getU(targetU));
            vertices[vertexOffset + V_OFFSET] = Float.floatToRawIntBits(auraSprite.getV(targetV));
        }

        return new BakedQuad(vertices, -1, quad.getDirection(), auraSprite, quad.isShade());
    }

    private static List<BakedQuad> remapOpaqueRectangles(BakedQuad quad, TextureAtlasSprite originalSprite, TextureAtlasSprite auraSprite, UvBounds uvBounds, SpriteContents originalContents, boolean[][] opaquePixels, int xStart, int yStart) {
        List<BakedQuad> maskedQuads = new ArrayList<>();
        boolean[][] usedPixels = new boolean[opaquePixels.length][opaquePixels[0].length];

        for (int localY = 0; localY < opaquePixels.length; localY++) {
            for (int localX = 0; localX < opaquePixels[localY].length; localX++) {
                if (!opaquePixels[localY][localX] || usedPixels[localY][localX]) {
                    continue;
                }

                int width = findRectangleWidth(opaquePixels, usedPixels, localX, localY);
                int height = findRectangleHeight(opaquePixels, usedPixels, localX, localY, width);
                markRectangleUsed(usedPixels, localX, localY, width, height);

                int x = xStart + localX;
                int y = yStart + localY;
                float minU = x / (float) originalContents.width() * 16.0F;
                float maxU = (x + width) / (float) originalContents.width() * 16.0F;
                float minV = y / (float) originalContents.height() * 16.0F;
                float maxV = (y + height) / (float) originalContents.height() * 16.0F;
                maskedQuads.add(remapPixelQuad(quad, originalSprite, auraSprite, uvBounds, minU, maxU, minV, maxV));
            }
        }

        return maskedQuads;
    }

    private static int findRectangleWidth(boolean[][] opaquePixels, boolean[][] usedPixels, int startX, int startY) {
        int width = 0;
        while (startX + width < opaquePixels[startY].length && opaquePixels[startY][startX + width] && !usedPixels[startY][startX + width]) {
            width++;
        }
        return width;
    }

    private static int findRectangleHeight(boolean[][] opaquePixels, boolean[][] usedPixels, int startX, int startY, int width) {
        int height = 1;
        while (startY + height < opaquePixels.length) {
            for (int x = startX; x < startX + width; x++) {
                if (!opaquePixels[startY + height][x] || usedPixels[startY + height][x]) {
                    return height;
                }
            }
            height++;
        }
        return height;
    }

    private static void markRectangleUsed(boolean[][] usedPixels, int startX, int startY, int width, int height) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                usedPixels[y][x] = true;
            }
        }
    }

    private static boolean hasTransparentPixels(SpriteContents contents, UvBounds uvBounds) {
        int xStart = Math.max(0, (int) Math.floor(uvBounds.minU / 16.0F * contents.width()));
        int xEnd = Math.min(contents.width(), (int) Math.ceil(uvBounds.maxU / 16.0F * contents.width()));
        int yStart = Math.max(0, (int) Math.floor(uvBounds.minV / 16.0F * contents.height()));
        int yEnd = Math.min(contents.height(), (int) Math.ceil(uvBounds.maxV / 16.0F * contents.height()));

        for (int y = yStart; y < yEnd; y++) {
            for (int x = xStart; x < xEnd; x++) {
                if (contents.isTransparent(0, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean closerToMax(float value, float min, float max) {
        return Math.abs(value - max) < Math.abs(value - min);
    }

    private static Vec3f interpolatePosition(BakedQuad quad, TextureAtlasSprite sprite, UvBounds uvBounds, float s, float t) {
        Vec3f minMin = findNearestPosition(quad, sprite, uvBounds.minU, uvBounds.minV);
        Vec3f maxMin = findNearestPosition(quad, sprite, uvBounds.maxU, uvBounds.minV);
        Vec3f maxMax = findNearestPosition(quad, sprite, uvBounds.maxU, uvBounds.maxV);
        Vec3f minMax = findNearestPosition(quad, sprite, uvBounds.minU, uvBounds.maxV);

        Vec3f top = lerp(minMin, maxMin, s);
        Vec3f bottom = lerp(minMax, maxMax, s);
        return lerp(top, bottom, t);
    }

    private static Vec3f findNearestPosition(BakedQuad quad, TextureAtlasSprite sprite, float targetU, float targetV) {
        int[] vertices = quad.getVertices();
        int closestVertexOffset = 0;
        float closestDistance = Float.MAX_VALUE;

        for (int vertex = 0; vertex < VERTEX_COUNT; vertex++) {
            int vertexOffset = vertex * VERTEX_STRIDE;
            float localU = sprite.getUOffset(Float.intBitsToFloat(vertices[vertexOffset + U_OFFSET]));
            float localV = sprite.getVOffset(Float.intBitsToFloat(vertices[vertexOffset + V_OFFSET]));
            float distance = Math.abs(localU - targetU) + Math.abs(localV - targetV);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestVertexOffset = vertexOffset;
            }
        }

        return new Vec3f(
                Float.intBitsToFloat(vertices[closestVertexOffset]),
                Float.intBitsToFloat(vertices[closestVertexOffset + 1]),
                Float.intBitsToFloat(vertices[closestVertexOffset + 2])
        );
    }

    private static Vec3f lerp(Vec3f from, Vec3f to, float amount) {
        return new Vec3f(
                from.x + (to.x - from.x) * amount,
                from.y + (to.y - from.y) * amount,
                from.z + (to.z - from.z) * amount
        );
    }

    public static class AuraTextureModel implements BakedModel {
        private final BakedModel wrapped;
        private final TextureAtlasSprite auraSprite;
        @Nullable
        private final BlockAndTintGetter level;
        @Nullable
        private final BlockPos pos;
        private final Map<TextureAtlasSprite, Map<BakedQuad, List<BakedQuad>>> remappedQuadCache;

        public AuraTextureModel(BakedModel wrapped, TextureAtlasSprite auraSprite, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, Map<TextureAtlasSprite, Map<BakedQuad, List<BakedQuad>>> remappedQuadCache) {
            this.wrapped = wrapped;
            this.auraSprite = auraSprite;
            this.level = level;
            this.pos = pos;
            this.remappedQuadCache = remappedQuadCache;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            if (state != null && direction != null && this.level != null && this.pos != null && !shouldRenderAgainstNeighborAura(state, this.level, this.pos, direction)) {
                return Collections.emptyList();
            }

            List<BakedQuad> remappedQuads = new ArrayList<>();
            Map<BakedQuad, List<BakedQuad>> cachedQuadsBySource = this.remappedQuadCache.computeIfAbsent(this.auraSprite, sprite -> new IdentityHashMap<>());
            for (BakedQuad quad : this.wrapped.getQuads(state, direction, random)) {
                remappedQuads.addAll(cachedQuadsBySource.computeIfAbsent(quad, sourceQuad -> remapQuad(sourceQuad, this.auraSprite)));
            }
            return remappedQuads;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.wrapped.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return this.wrapped.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return false;
        }

        @Override
        public boolean isCustomRenderer() {
            return this.wrapped.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return this.auraSprite;
        }

        @Override
        public ItemTransforms getTransforms() {
            return this.wrapped.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.wrapped.getOverrides();
        }

        public static boolean shouldRenderAgainstNeighborAura(BlockState storedState, BlockAndTintGetter level, BlockPos pos, Direction direction) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(neighborPos);
            if (!(blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity)) {
                return true;
            }

            BlockState neighborStoredState = mendingAuraBlockEntity.getStoredBlockState();
            if (neighborStoredState == null || neighborStoredState.getBlock() instanceof MendingAuraBlock) {
                return true;
            }

            if (storedState.skipRendering(neighborStoredState, direction)) {
                return false;
            }
            if (!neighborStoredState.canOcclude()) {
                return true;
            }

            VoxelShape shape = storedState.getFaceOcclusionShape(level, pos, direction);
            if (shape.isEmpty()) {
                return true;
            }

            VoxelShape neighborShape = neighborStoredState.getFaceOcclusionShape(level, neighborPos, direction.getOpposite());
            return Shapes.joinIsNotEmpty(shape, neighborShape, BooleanOp.ONLY_FIRST);
        }
    }

    private record UvBounds(float minU, float maxU, float minV, float maxV) {
        private static UvBounds from(BakedQuad quad, TextureAtlasSprite sprite) {
            int[] vertices = quad.getVertices();
            float minU = Float.MAX_VALUE;
            float maxU = -Float.MAX_VALUE;
            float minV = Float.MAX_VALUE;
            float maxV = -Float.MAX_VALUE;

            for (int vertex = 0; vertex < VERTEX_COUNT; vertex++) {
                int vertexOffset = vertex * VERTEX_STRIDE;
                float localU = sprite.getUOffset(Float.intBitsToFloat(vertices[vertexOffset + U_OFFSET]));
                float localV = sprite.getVOffset(Float.intBitsToFloat(vertices[vertexOffset + V_OFFSET]));
                minU = Math.min(minU, localU);
                maxU = Math.max(maxU, localU);
                minV = Math.min(minV, localV);
                maxV = Math.max(maxV, localV);
            }

            return new UvBounds(minU, maxU, minV, maxV);
        }

        private boolean isDegenerate() {
            return this.maxU <= this.minU || this.maxV <= this.minV;
        }
    }

    private record Vec3f(float x, float y, float z) {
    }
}
