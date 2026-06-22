package dev.hexnowloading.dungeonnowloading.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.MendingAuraBlockEntityRenderer;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MendingAuraForgeBakedModel implements BakedModel, IForgeBakedModel {
    public static final ModelProperty<BlockState> STORED_STATE = new ModelProperty<>();
    public static final ModelProperty<Set<Direction>> CULLED_DIRECTIONS = new ModelProperty<>();

    private final BakedModel wrapped;

    public MendingAuraForgeBakedModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        BlockState storedState = getStoredBlockState(level, pos);
        if (storedState == null) {
            return modelData;
        }

        Set<Direction> culledDirections = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (!MendingAuraBlockEntityRenderer.AuraTextureModel.shouldRenderAgainstNeighborAura(storedState, level, pos, direction)) {
                culledDirections.add(direction);
            }
        }

        return modelData.derive()
                .with(STORED_STATE, storedState)
                .with(CULLED_DIRECTIONS, culledDirections)
                .build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random, ModelData modelData, @Nullable RenderType renderType) {
        BlockState storedState = modelData.get(STORED_STATE);
        if (storedState == null) {
            return List.of();
        }
        Set<Direction> culledDirections = modelData.get(CULLED_DIRECTIONS);
        if (direction != null && culledDirections != null && culledDirections.contains(direction)) {
            return List.of();
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel storedModel = dispatcher.getBlockModel(storedState);
        TextureAtlasSprite auraSprite = dispatcher.getBlockModel(DNLBlocks.MENDING_AURA.get().defaultBlockState()).getParticleIcon();
        BakedModel auraModel = new MendingAuraBlockEntityRenderer.AuraTextureModel(
                storedModel,
                auraSprite,
                null,
                null,
                MendingAuraBlockEntityRenderer.GLOBAL_REMAPPED_QUAD_CACHE
        );
        return auraModel.getQuads(storedState, direction, random);
    }

    @Nullable
    private static BlockState getStoredBlockState(BlockAndTintGetter blockView, BlockPos pos) {
        BlockEntity blockEntity = blockView.getBlockEntity(pos);
        if (blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            BlockState storedState = mendingAuraBlockEntity.getStoredBlockState();
            if (storedState != null && !(storedState.getBlock() instanceof MendingAuraBlock)) {
                return storedState;
            }
        }
        return null;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return this.wrapped.getQuads(state, direction, random);
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
        return this.wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return this.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.wrapped.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.wrapped.getOverrides();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return this.wrapped.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.translucent());
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return this.wrapped.getRenderTypes(itemStack, fabulous);
    }
}
