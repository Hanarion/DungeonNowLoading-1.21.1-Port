package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WoodenBoardBlock extends Block implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape WEST_AABB = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
    private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);

    public WoodenBoardBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VARIANT, Variant.SINGLE)
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

        return updateVariant(state, ctx.getLevel(), pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT, WATERLOGGED);
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

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        Direction facing = state.getValue(FACING);
        if (dir == facing.getClockWise() || dir == facing.getCounterClockWise()) {
            state = updateVariant(state, level, pos);
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    private BlockState updateVariant(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        boolean right = connectsTo(level.getBlockState(pos.relative(facing.getClockWise())), facing);
        boolean left = connectsTo(level.getBlockState(pos.relative(facing.getCounterClockWise())), facing);

        Variant variant = left && right ? Variant.MIDDLE
                : right ? Variant.LEFT
                : left ? Variant.RIGHT
                : Variant.SINGLE;
        return state.setValue(VARIANT, variant);
    }

    private boolean connectsTo(BlockState neighbor, Direction facing) {
        return neighbor.is(this) && neighbor.getValue(FACING) == facing;
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
        Variant variant = state.getValue(VARIANT);
        if (mirror != Mirror.NONE) {
            variant = variant == Variant.LEFT ? Variant.RIGHT
                    : variant == Variant.RIGHT ? Variant.LEFT
                    : variant;
        }
        return state.rotate(mirror.getRotation(state.getValue(FACING))).setValue(VARIANT, variant);
    }

    // ----- Misc -----

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }

    public enum Variant implements StringRepresentable {
        SINGLE("single"),
        LEFT("left"),
        MIDDLE("middle"),
        RIGHT("right");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
