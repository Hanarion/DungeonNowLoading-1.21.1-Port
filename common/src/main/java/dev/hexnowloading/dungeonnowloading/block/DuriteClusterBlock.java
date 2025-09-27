package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class DuriteClusterBlock extends Block implements SimpleWaterloggedBlock {

    public enum Stage { SMALL, MEDIUM, LARGE, CLUSTER }

    public enum HitboxPreset {
        SMALL  (3, 10),
        MEDIUM (4, 10),
        LARGE  (6, 8),
        CLUSTER(7, 6);

        final Map<Direction, VoxelShape> oriented;

        HitboxPreset(int height, int cross) {
            int arm = cross / 2;
            this.oriented = buildAmethystLikeShapes(height, arm);
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final Map<Direction, VoxelShape> shapes;   // from preset

    public DuriteClusterBlock(HitboxPreset preset,
                              BlockBehaviour.Properties props) {
        super(props);
        this.shapes = preset.oriented;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    /** Same geometry mapping as AmethystClusterBlock. */
    private static Map<Direction, VoxelShape> buildAmethystLikeShapes(int height, int arm) {
        Map<Direction, VoxelShape> m = new EnumMap<>(Direction.class);

        int a = arm, invA = 16 - arm, invH = 16 - height;

        VoxelShape up    = box(a, 0,     a,    invA, height, invA);
        VoxelShape down  = box(a, invH,  a,    invA, 16,     invA);
        VoxelShape north = box(a, a,     invH, invA, invA,   16);
        VoxelShape south = box(a, a,     0,    invA, invA,   height);
        VoxelShape east  = box(0, a,     a,    height, invA, invA);
        VoxelShape west  = box(invH, a,  a,    16,     invA, invA);

        m.put(Direction.UP,    up);
        m.put(Direction.DOWN,  down);
        m.put(Direction.NORTH, north);
        m.put(Direction.SOUTH, south);
        m.put(Direction.EAST,  east);
        m.put(Direction.WEST,  west);
        return m;
    }

    // ----- state / fluids / survival (matches amethyst) -----

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face = ctx.getClickedFace();
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return defaultBlockState()
                .setValue(FACING, face)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction face = state.getValue(FACING);
        BlockPos behind = pos.relative(face.getOpposite());
        return level.getBlockState(behind).isFaceSturdy(level, behind, face);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ----- shapes -----

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
