package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MendingAuraBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty FENCE_LIKE = BooleanProperty.create("fence_like");
    public static final BooleanProperty WALL_LIKE = BooleanProperty.create("wall_like");
    public static final BooleanProperty STAIR_LIKE = BooleanProperty.create("stair_like");
    public static final BooleanProperty PANE_LIKE = BooleanProperty.create("pane_like");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    private static final SoundType SOUND_TYPE = new SoundType(1.0F, 1.0F, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, DNLSounds.MENDING_AURA_POP.get(), SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);

    public MendingAuraBlock(Properties $$0) {
        super($$0);
        this.registerDefaultState(configureDefaultState(this.stateDefinition.any()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FENCE_LIKE, WALL_LIKE, STAIR_LIKE, PANE_LIKE, FACING, HALF);
    }

    public static BlockState configureForStoredBlock(BlockState auraState, BlockState storedState) {
        if (auraState.hasProperty(FENCE_LIKE)) {
            auraState = auraState.setValue(FENCE_LIKE, storedState.is(BlockTags.FENCES) || storedState.getBlock() instanceof FenceBlock);
        }

        if (auraState.hasProperty(WALL_LIKE)) {
            auraState = auraState.setValue(WALL_LIKE, storedState.is(BlockTags.WALLS) || storedState.getBlock() instanceof WallBlock);
        }

        if (auraState.hasProperty(STAIR_LIKE)) {
            auraState = auraState.setValue(STAIR_LIKE, storedState.is(BlockTags.STAIRS) || storedState.getBlock() instanceof StairBlock);
        }

        if (auraState.hasProperty(PANE_LIKE)) {
            auraState = auraState.setValue(PANE_LIKE, storedState.getBlock() instanceof IronBarsBlock);
        }

        if (auraState.hasProperty(FACING) && storedState.hasProperty(FACING)) {
            auraState = auraState.setValue(FACING, storedState.getValue(FACING));
        }

        if (auraState.hasProperty(HALF) && storedState.hasProperty(HALF)) {
            auraState = auraState.setValue(HALF, storedState.getValue(HALF));
        }

        if (storedState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            auraState = auraState.setValue(WATERLOGGED, storedState.getValue(BlockStateProperties.WATERLOGGED));
        }

        return auraState;
    }

    public static BlockState refreshStoredConnections(BlockState storedState, LevelAccessor level, BlockPos pos) {
        if (!hasRefreshableConnections(storedState)) {
            return storedState;
        }

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            storedState = storedState.updateShape(direction, level.getBlockState(neighborPos), level, pos, neighborPos);
        }

        return storedState;
    }

    public static void refreshNeighboringStoredConnections(LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(neighborPos);
            if (blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
                BlockState storedState = mendingAuraBlockEntity.getStoredBlockState();
                BlockState refreshedState = storedState != null ? refreshStoredConnections(storedState, level, neighborPos) : null;
                if (refreshedState != null && refreshedState != storedState) {
                    mendingAuraBlockEntity.setStoredBlock(refreshedState, mendingAuraBlockEntity.getStoredBlockNbt());
                    if (level instanceof ServerLevel serverLevel) {
                        mendingAuraBlockEntity.syncToClients(serverLevel, serverLevel.getBlockState(neighborPos));
                    }
                }
            }
        }
    }

    private static boolean hasRefreshableConnections(BlockState state) {
        Block block = state.getBlock();
        return state.is(BlockTags.FENCES)
                || state.is(BlockTags.WALLS)
                || state.is(BlockTags.STAIRS)
                || block instanceof FenceBlock
                || block instanceof WallBlock
                || block instanceof StairBlock
                || block instanceof IronBarsBlock;
    }

    private static BlockState configureDefaultState(BlockState state) {
        if (state.hasProperty(WATERLOGGED)) {
            state = state.setValue(WATERLOGGED, Boolean.FALSE);
        }

        if (state.hasProperty(FENCE_LIKE)) {
            state = state.setValue(FENCE_LIKE, Boolean.FALSE);
        }

        if (state.hasProperty(WALL_LIKE)) {
            state = state.setValue(WALL_LIKE, Boolean.FALSE);
        }

        if (state.hasProperty(STAIR_LIKE)) {
            state = state.setValue(STAIR_LIKE, Boolean.FALSE);
        }

        if (state.hasProperty(PANE_LIKE)) {
            state = state.setValue(PANE_LIKE, Boolean.FALSE);
        }

        if (state.hasProperty(FACING)) {
            state = state.setValue(FACING, Direction.NORTH);
        }

        if (state.hasProperty(HALF)) {
            state = state.setValue(HALF, Half.BOTTOM);
        }

        return state;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean skipRendering(BlockState p_53972_, BlockState p_53973_, Direction p_53974_) {
        return super.skipRendering(p_53972_, p_53973_, p_53974_);
    }

    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

    public VoxelShape getVisualShape(BlockState p_48735_, BlockGetter p_48736_, BlockPos p_48737_, CollisionContext p_48738_) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        BlockState storedState = this.getStoredBlockState(blockGetter, pos);
        return storedState != null ? storedState.getShape(blockGetter, pos, context) : Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        BlockState storedState = this.getStoredBlockState(blockGetter, pos);
        return storedState != null ? storedState.getCollisionShape(blockGetter, pos, context) : Shapes.empty();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        BlockState storedState = this.getStoredBlockState(blockGetter, pos);
        return storedState != null ? storedState.getInteractionShape(blockGetter, pos) : Shapes.empty();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        BlockState storedState = this.getStoredBlockState(blockGetter, pos);
        return storedState != null ? storedState.getBlockSupportShape(blockGetter, pos) : Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        BlockState storedState = this.getStoredBlockState(blockGetter, pos);
        return storedState != null && storedState.canOcclude() ? storedState.getOcclusionShape(blockGetter, pos) : Shapes.empty();
    }

    @Nullable
    private BlockState getStoredBlockState(BlockGetter blockGetter, BlockPos pos) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(pos);
        if (blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            BlockState storedState = mendingAuraBlockEntity.getStoredBlockState();
            if (storedState != null && !(storedState.getBlock() instanceof MendingAuraBlock)) {
                return storedState;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MendingAuraBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public void startRestoration(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            level.scheduleTick(pos, this, mendingAuraBlockEntity.getRestoreTime());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            mendingAuraBlockEntity.restoreBlock(level, pos, state);
        }
    }

    @Override
    public SoundType getSoundType(BlockState blockState) {
        return SOUND_TYPE;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        super.animateTick(blockState, level, blockPos, randomSource);

        if (randomSource.nextInt(60) == 0) {
            List<Vec3> possibleDirections = new ArrayList<>();

            if (level.isEmptyBlock(blockPos.north())) possibleDirections.add(new Vec3(0.0, 0.0, -1.0)); // Move North (-Z)
            if (level.isEmptyBlock(blockPos.south())) possibleDirections.add(new Vec3(0.0, 0.0, 1.0));  // Move South (+Z)
            if (level.isEmptyBlock(blockPos.east())) possibleDirections.add(new Vec3(1.0, 0.0, 0.0));   // Move East (+X)
            if (level.isEmptyBlock(blockPos.west())) possibleDirections.add(new Vec3(-1.0, 0.0, 0.0));  // Move West (-X)
            if (level.isEmptyBlock(blockPos.above())) possibleDirections.add(new Vec3(0.0, 1.0, 0.0));  // Move Up (+Y)
            if (level.isEmptyBlock(blockPos.below())) possibleDirections.add(new Vec3(0.0, -1.0, 0.0)); // Move Down (-Y)

            Vec3 velocity = new Vec3(0.0, 0.0, 0.0);
            Vec3 spawnOffset = new Vec3(0.0, 0.0, 0.0);

            if (!possibleDirections.isEmpty()) {
                Vec3 chosenDirection = possibleDirections.get(randomSource.nextInt(possibleDirections.size()));
                velocity = chosenDirection.scale(0.06);
                spawnOffset = chosenDirection;
            }

            double x = blockPos.getX() + randomSource.nextDouble() + spawnOffset.x * (0.5F + randomSource.nextDouble() * 1.0F);
            double y = blockPos.getY() + randomSource.nextDouble() + spawnOffset.y * (0.5F + randomSource.nextDouble() * 1.0F);
            double z = blockPos.getZ() + randomSource.nextDouble() + spawnOffset.z * (0.5F + randomSource.nextDouble() * 1.0F);

            level.addParticle(DNLParticleTypes.MENDING_POP_AND_RUNE_PARTICLE.get(), true, x, y, z, -velocity.x, -velocity.y, -velocity.z);
        }
    }
}
