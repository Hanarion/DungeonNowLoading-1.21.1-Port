package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WoodenBoardBlock extends Block implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape WEST_AABB = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
    private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);

    public WoodenBoardBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clickedFace = ctx.getClickedFace();
        Direction face = clickedFace.getAxis().isHorizontal()
                ? clickedFace // attach to the side you clicked
                : ctx.getHorizontalDirection().getOpposite(); // otherwise face toward player

        BlockPos pos = ctx.getClickedPos();
        FluidState fluid = ctx.getLevel().getFluidState(pos);

        BlockState state = this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);

        // Prevent placement if not supported
        return canSurvive(state, ctx.getLevel(), pos) ? state : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    // ----- Shape / rendering -----

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case EAST  -> EAST_AABB;
            case WEST  -> WEST_AABB;
            default    -> SOUTH_AABB;
        };
    }

    // ----- Survival / neighbor updates -----

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        // Needs a solid face behind it to “hang” on
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // break if support is gone
        if (dir == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        // keep water ticking
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    // ----- Fluids -----

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // ----- Rotation / mirroring -----

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ----- Misc -----

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }
}
