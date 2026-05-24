package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MendingAuraBlockEntityRenderer implements BlockEntityRenderer<MendingAuraBlockEntity> {

    private static final int U_OFFSET = 4;
    private static final int V_OFFSET = 5;
    private static final int VERTEX_STRIDE = 8;
    private static final int VERTEX_COUNT = 4;

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

        BakedModel auraModel = new AuraTextureModel(storedModel, auraSprite);
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
    }

    private static BakedQuad remapQuad(BakedQuad quad, TextureAtlasSprite auraSprite) {
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

    private static class AuraTextureModel implements BakedModel {
        private final BakedModel wrapped;
        private final TextureAtlasSprite auraSprite;

        private AuraTextureModel(BakedModel wrapped, TextureAtlasSprite auraSprite) {
            this.wrapped = wrapped;
            this.auraSprite = auraSprite;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            return this.wrapped.getQuads(state, direction, random)
                    .stream()
                    .map(quad -> remapQuad(quad, this.auraSprite))
                    .toList();
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
    }
}
