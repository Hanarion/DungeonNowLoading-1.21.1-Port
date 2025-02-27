package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.IntStream;

import static net.minecraft.world.level.block.StairBlock.isStairs;

public class MendingAuraStairBlock extends MendingAuraBlock {

    public static final DirectionProperty FACING;
    public static final EnumProperty<Half> HALF;
    public static final EnumProperty<StairsShape> SHAPE;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape TOP_AABB;
    protected static final VoxelShape BOTTOM_AABB;
    protected static final VoxelShape OCTET_NNN;
    protected static final VoxelShape OCTET_NNP;
    protected static final VoxelShape OCTET_NPN;
    protected static final VoxelShape OCTET_NPP;
    protected static final VoxelShape OCTET_PNN;
    protected static final VoxelShape OCTET_PNP;
    protected static final VoxelShape OCTET_PPN;
    protected static final VoxelShape OCTET_PPP;
    protected static final VoxelShape[] TOP_SHAPES;
    protected static final VoxelShape[] BOTTOM_SHAPES;
    private static final int[] SHAPE_BY_STATE;
    private final Block base;
    private final BlockState baseState;

    private static VoxelShape[] makeShapes(VoxelShape $$0, VoxelShape $$1, VoxelShape $$2, VoxelShape $$3, VoxelShape $$4) {
        return (VoxelShape[]) IntStream.range(0, 16).mapToObj(($$5) -> {
            return makeStairShape($$5, $$0, $$1, $$2, $$3, $$4);
        }).toArray(($$0x) -> {
            return new VoxelShape[$$0x];
        });
    }

    private static VoxelShape makeStairShape(int $$0, VoxelShape $$1, VoxelShape $$2, VoxelShape $$3, VoxelShape $$4, VoxelShape $$5) {
        VoxelShape $$6 = $$1;
        if (($$0 & 1) != 0) {
            $$6 = Shapes.or($$6, $$2);
        }

        if (($$0 & 2) != 0) {
            $$6 = Shapes.or($$6, $$3);
        }

        if (($$0 & 4) != 0) {
            $$6 = Shapes.or($$6, $$4);
        }

        if (($$0 & 8) != 0) {
            $$6 = Shapes.or($$6, $$5);
        }

        return $$6;
    }


    public MendingAuraStairBlock(BlockState blockState, Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATERLOGGED, false));
        this.base = blockState.getBlock();
        this.baseState = blockState;
    }

    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState oldBlockState, boolean isMoving) {
        if (!blockState.is(blockState.getBlock())) {
            level.neighborChanged(this.baseState, blockPos, Blocks.AIR, blockPos, false);
            this.base.onPlace(this.baseState, level, blockPos, oldBlockState, false);
        }
        super.onPlace(blockState, level, blockPos, oldBlockState, isMoving);
    }

    public void onRemove(BlockState $$0, Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
        if (!$$0.is($$3.getBlock())) {
            this.baseState.onRemove($$1, $$2, $$3, $$4);
        }
    }

    public VoxelShape getShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return ($$0.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[this.getShapeIndex($$0)]];
    }

    private int getShapeIndex(BlockState $$0) {
        return ((StairsShape)$$0.getValue(SHAPE)).ordinal() * 4 + ((Direction)$$0.getValue(FACING)).get2DDataValue();
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction $$1 = context.getClickedFace();
        BlockPos $$2 = context.getClickedPos();
        FluidState $$3 = context.getLevel().getFluidState($$2);
        BlockState $$4 = (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection())).setValue(HALF, $$1 != Direction.DOWN && ($$1 == Direction.UP || !(context.getClickLocation().y - (double)$$2.getY() > 0.5)) ? Half.BOTTOM : Half.TOP)).setValue(WATERLOGGED, $$3.getType() == Fluids.WATER);
        return (BlockState)$$4.setValue(SHAPE, getStairsShape($$4, context.getLevel(), $$2));
    }

    public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
        if ((Boolean)$$0.getValue(WATERLOGGED)) {
            $$3.scheduleTick($$4, Fluids.WATER, Fluids.WATER.getTickDelay($$3));
        }

        return $$1.getAxis().isHorizontal() ? (BlockState)$$0.setValue(SHAPE, getStairsShape($$0, $$3, $$4)) : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5);
    }

    private static StairsShape getStairsShape(BlockState $$0, BlockGetter $$1, BlockPos $$2) {
        Direction $$3 = (Direction)$$0.getValue(FACING);
        BlockState $$4 = $$1.getBlockState($$2.relative($$3));
        if (isStairs($$4) && $$0.getValue(HALF) == $$4.getValue(HALF)) {
            Direction $$5 = (Direction)$$4.getValue(FACING);
            if ($$5.getAxis() != ((Direction)$$0.getValue(FACING)).getAxis() && canTakeShape($$0, $$1, $$2, $$5.getOpposite())) {
                if ($$5 == $$3.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockState $$6 = $$1.getBlockState($$2.relative($$3.getOpposite()));
        if (isStairs($$6) && $$0.getValue(HALF) == $$6.getValue(HALF)) {
            Direction $$7 = (Direction)$$6.getValue(FACING);
            if ($$7.getAxis() != ((Direction)$$0.getValue(FACING)).getAxis() && canTakeShape($$0, $$1, $$2, $$7)) {
                if ($$7 == $$3.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }

                return StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, Direction $$3) {
        BlockState $$4 = $$1.getBlockState($$2.relative($$3));
        return !isStairs($$4) || $$4.getValue(FACING) != $$0.getValue(FACING) || $$4.getValue(HALF) != $$0.getValue(HALF);
    }

    public BlockState rotate(BlockState $$0, Rotation $$1) {
        return (BlockState)$$0.setValue(FACING, $$1.rotate((Direction)$$0.getValue(FACING)));
    }

    public BlockState mirror(BlockState $$0, Mirror $$1) {
        Direction $$2 = (Direction)$$0.getValue(FACING);
        StairsShape $$3 = (StairsShape)$$0.getValue(SHAPE);
        switch ($$1) {
            case LEFT_RIGHT:
                if ($$2.getAxis() == Direction.Axis.Z) {
                    switch ($$3) {
                        case INNER_LEFT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        }
                        case INNER_RIGHT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        }
                        case OUTER_LEFT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        }
                        case OUTER_RIGHT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        }
                        default -> {
                            return $$0.rotate(Rotation.CLOCKWISE_180);
                        }
                    }
                }
                break;
            case FRONT_BACK:
                if ($$2.getAxis() == Direction.Axis.X) {
                    switch ($$3) {
                        case INNER_LEFT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        }
                        case INNER_RIGHT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        }
                        case OUTER_LEFT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        }
                        case OUTER_RIGHT -> {
                            return (BlockState)$$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        }
                        case STRAIGHT -> {
                            return $$0.rotate(Rotation.CLOCKWISE_180);
                        }
                    }
                }
        }

        return super.mirror($$0, $$1);
    }


    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, HALF, SHAPE, WATERLOGGED});
    }

    public FluidState getFluidState(BlockState $$0) {
        return (Boolean)$$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
    }

    public boolean isPathfindable(BlockState $$0, BlockGetter $$1, BlockPos $$2, PathComputationType $$3) {
        return false;
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        HALF = BlockStateProperties.HALF;
        SHAPE = BlockStateProperties.STAIRS_SHAPE;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        TOP_AABB = MendingAuraSlabBlock.TOP_AABB;
        BOTTOM_AABB = MendingAuraSlabBlock.BOTTOM_AABB;
        OCTET_NNN = Block.box(0.0, 0.0, 0.0, 8.0, 8.0, 8.0);
        OCTET_NNP = Block.box(0.0, 0.0, 8.0, 8.0, 8.0, 16.0);
        OCTET_NPN = Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 8.0);
        OCTET_NPP = Block.box(0.0, 8.0, 8.0, 8.0, 16.0, 16.0);
        OCTET_PNN = Block.box(8.0, 0.0, 0.0, 16.0, 8.0, 8.0);
        OCTET_PNP = Block.box(8.0, 0.0, 8.0, 16.0, 8.0, 16.0);
        OCTET_PPN = Block.box(8.0, 8.0, 0.0, 16.0, 16.0, 8.0);
        OCTET_PPP = Block.box(8.0, 8.0, 8.0, 16.0, 16.0, 16.0);
        TOP_SHAPES = makeShapes(TOP_AABB, OCTET_NNN, OCTET_PNN, OCTET_NNP, OCTET_PNP);
        BOTTOM_SHAPES = makeShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
        SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
    }
}
