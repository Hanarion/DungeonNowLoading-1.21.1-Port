package dev.hexnowloading.dungeonnowloading.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Iterator;
import java.util.Map;

public class MendingAuraWallBlock extends MendingAuraBlock {

    public static final BooleanProperty UP;
    public static final EnumProperty<WallSide> EAST_WALL;
    public static final EnumProperty<WallSide> NORTH_WALL;
    public static final EnumProperty<WallSide> SOUTH_WALL;
    public static final EnumProperty<WallSide> WEST_WALL;
    public static final BooleanProperty WATERLOGGED;
    private final Map<BlockState, VoxelShape> shapeByIndex;
    private final Map<BlockState, VoxelShape> collisionShapeByIndex;
    private static final int WALL_WIDTH = 3;
    private static final int WALL_HEIGHT = 14;
    private static final int POST_WIDTH = 4;
    private static final int POST_COVER_WIDTH = 1;
    private static final int WALL_COVER_START = 7;
    private static final int WALL_COVER_END = 9;
    private static final VoxelShape POST_TEST;
    private static final VoxelShape NORTH_TEST;
    private static final VoxelShape SOUTH_TEST;
    private static final VoxelShape WEST_TEST;
    private static final VoxelShape EAST_TEST;

    public MendingAuraWallBlock(Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(UP, true)).setValue(NORTH_WALL, WallSide.NONE)).setValue(EAST_WALL, WallSide.NONE)).setValue(SOUTH_WALL, WallSide.NONE)).setValue(WEST_WALL, WallSide.NONE)).setValue(WATERLOGGED, false));
        this.shapeByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
        this.collisionShapeByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
    }
    private static VoxelShape applyWallShape(VoxelShape $$0, WallSide $$1, VoxelShape $$2, VoxelShape $$3) {
        if ($$1 == WallSide.TALL) {
            return Shapes.or($$0, $$3);
        } else {
            return $$1 == WallSide.LOW ? Shapes.or($$0, $$2) : $$0;
        }
    }

    private Map<BlockState, VoxelShape> makeShapes(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
        float $$6 = 8.0F - $$0;
        float $$7 = 8.0F + $$0;
        float $$8 = 8.0F - $$1;
        float $$9 = 8.0F + $$1;
        VoxelShape $$10 = Block.box((double)$$6, 0.0, (double)$$6, (double)$$7, (double)$$2, (double)$$7);
        VoxelShape $$11 = Block.box((double)$$8, (double)$$3, 0.0, (double)$$9, (double)$$4, (double)$$9);
        VoxelShape $$12 = Block.box((double)$$8, (double)$$3, (double)$$8, (double)$$9, (double)$$4, 16.0);
        VoxelShape $$13 = Block.box(0.0, (double)$$3, (double)$$8, (double)$$9, (double)$$4, (double)$$9);
        VoxelShape $$14 = Block.box((double)$$8, (double)$$3, (double)$$8, 16.0, (double)$$4, (double)$$9);
        VoxelShape $$15 = Block.box((double)$$8, (double)$$3, 0.0, (double)$$9, (double)$$5, (double)$$9);
        VoxelShape $$16 = Block.box((double)$$8, (double)$$3, (double)$$8, (double)$$9, (double)$$5, 16.0);
        VoxelShape $$17 = Block.box(0.0, (double)$$3, (double)$$8, (double)$$9, (double)$$5, (double)$$9);
        VoxelShape $$18 = Block.box((double)$$8, (double)$$3, (double)$$8, 16.0, (double)$$5, (double)$$9);
        ImmutableMap.Builder<BlockState, VoxelShape> $$19 = ImmutableMap.builder();
        Iterator var21 = UP.getPossibleValues().iterator();

        while(var21.hasNext()) {
            Boolean $$20 = (Boolean)var21.next();
            Iterator var23 = EAST_WALL.getPossibleValues().iterator();

            while(var23.hasNext()) {
                WallSide $$21 = (WallSide)var23.next();
                Iterator var25 = NORTH_WALL.getPossibleValues().iterator();

                while(var25.hasNext()) {
                    WallSide $$22 = (WallSide)var25.next();
                    Iterator var27 = WEST_WALL.getPossibleValues().iterator();

                    while(var27.hasNext()) {
                        WallSide $$23 = (WallSide)var27.next();
                        Iterator var29 = SOUTH_WALL.getPossibleValues().iterator();

                        while(var29.hasNext()) {
                            WallSide $$24 = (WallSide)var29.next();
                            VoxelShape $$25 = Shapes.empty();
                            $$25 = applyWallShape($$25, $$21, $$14, $$18);
                            $$25 = applyWallShape($$25, $$23, $$13, $$17);
                            $$25 = applyWallShape($$25, $$22, $$11, $$15);
                            $$25 = applyWallShape($$25, $$24, $$12, $$16);
                            if ($$20) {
                                $$25 = Shapes.or($$25, $$10);
                            }

                            BlockState $$26 = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(UP, $$20)).setValue(EAST_WALL, $$21)).setValue(WEST_WALL, $$23)).setValue(NORTH_WALL, $$22)).setValue(SOUTH_WALL, $$24);
                            $$19.put((BlockState)$$26.setValue(WATERLOGGED, false), $$25);
                            $$19.put((BlockState)$$26.setValue(WATERLOGGED, true), $$25);
                        }
                    }
                }
            }
        }

        return $$19.build();
    }

    public VoxelShape getShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return (VoxelShape)this.shapeByIndex.get($$0);
    }

    public VoxelShape getCollisionShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return (VoxelShape)this.collisionShapeByIndex.get($$0);
    }

    public boolean isPathfindable(BlockState $$0, BlockGetter $$1, BlockPos $$2, PathComputationType $$3) {
        return false;
    }

    private boolean connectsTo(BlockState blockState, boolean b, Direction direction) {
        Block block = blockState.getBlock();
        boolean b1 = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(blockState, direction);
        return blockState.is(BlockTags.WALLS) || !isExceptionForConnection(blockState) && b || block instanceof IronBarsBlock || b1 || block instanceof MendingAuraWallBlock;
    }

    public BlockState getStateForPlacement(BlockPlaceContext $$0) {
        LevelReader $$1 = $$0.getLevel();
        BlockPos $$2 = $$0.getClickedPos();
        FluidState $$3 = $$0.getLevel().getFluidState($$0.getClickedPos());
        BlockPos $$4 = $$2.north();
        BlockPos $$5 = $$2.east();
        BlockPos $$6 = $$2.south();
        BlockPos $$7 = $$2.west();
        BlockPos $$8 = $$2.above();
        BlockState $$9 = $$1.getBlockState($$4);
        BlockState $$10 = $$1.getBlockState($$5);
        BlockState $$11 = $$1.getBlockState($$6);
        BlockState $$12 = $$1.getBlockState($$7);
        BlockState $$13 = $$1.getBlockState($$8);
        boolean $$14 = this.connectsTo($$9, $$9.isFaceSturdy($$1, $$4, Direction.SOUTH), Direction.SOUTH);
        boolean $$15 = this.connectsTo($$10, $$10.isFaceSturdy($$1, $$5, Direction.WEST), Direction.WEST);
        boolean $$16 = this.connectsTo($$11, $$11.isFaceSturdy($$1, $$6, Direction.NORTH), Direction.NORTH);
        boolean $$17 = this.connectsTo($$12, $$12.isFaceSturdy($$1, $$7, Direction.EAST), Direction.EAST);
        BlockState $$18 = (BlockState)this.defaultBlockState().setValue(WATERLOGGED, $$3.getType() == Fluids.WATER);
        return this.updateShape($$1, $$18, $$8, $$13, $$14, $$15, $$16, $$17);
    }

    public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
        if ((Boolean)$$0.getValue(WATERLOGGED)) {
            $$3.scheduleTick($$4, Fluids.WATER, Fluids.WATER.getTickDelay($$3));
        }

        if ($$1 == Direction.DOWN) {
            return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5);
        } else {
            return $$1 == Direction.UP ? this.topUpdate($$3, $$0, $$5, $$2) : this.sideUpdate($$3, $$4, $$0, $$5, $$2, $$1);
        }
    }

    private static boolean isConnected(BlockState $$0, Property<WallSide> $$1) {
        return $$0.getValue($$1) != WallSide.NONE;
    }

    private static boolean isCovered(VoxelShape $$0, VoxelShape $$1) {
        return !Shapes.joinIsNotEmpty($$1, $$0, BooleanOp.ONLY_FIRST);
    }

    private BlockState topUpdate(LevelReader $$0, BlockState $$1, BlockPos $$2, BlockState $$3) {
        boolean $$4 = isConnected($$1, NORTH_WALL);
        boolean $$5 = isConnected($$1, EAST_WALL);
        boolean $$6 = isConnected($$1, SOUTH_WALL);
        boolean $$7 = isConnected($$1, WEST_WALL);
        return this.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
    }

    private BlockState sideUpdate(LevelReader $$0, BlockPos $$1, BlockState $$2, BlockPos $$3, BlockState $$4, Direction $$5) {
        Direction $$6 = $$5.getOpposite();
        boolean $$7 = $$5 == Direction.NORTH ? this.connectsTo($$4, $$4.isFaceSturdy($$0, $$3, $$6), $$6) : isConnected($$2, NORTH_WALL);
        boolean $$8 = $$5 == Direction.EAST ? this.connectsTo($$4, $$4.isFaceSturdy($$0, $$3, $$6), $$6) : isConnected($$2, EAST_WALL);
        boolean $$9 = $$5 == Direction.SOUTH ? this.connectsTo($$4, $$4.isFaceSturdy($$0, $$3, $$6), $$6) : isConnected($$2, SOUTH_WALL);
        boolean $$10 = $$5 == Direction.WEST ? this.connectsTo($$4, $$4.isFaceSturdy($$0, $$3, $$6), $$6) : isConnected($$2, WEST_WALL);
        BlockPos $$11 = $$1.above();
        BlockState $$12 = $$0.getBlockState($$11);
        return this.updateShape($$0, $$2, $$11, $$12, $$7, $$8, $$9, $$10);
    }

    private BlockState updateShape(LevelReader $$0, BlockState $$1, BlockPos $$2, BlockState $$3, boolean $$4, boolean $$5, boolean $$6, boolean $$7) {
        VoxelShape $$8 = $$3.getCollisionShape($$0, $$2).getFaceShape(Direction.DOWN);
        BlockState $$9 = this.updateSides($$1, $$4, $$5, $$6, $$7, $$8);
        return (BlockState)$$9.setValue(UP, this.shouldRaisePost($$9, $$3, $$8));
    }

    private boolean shouldRaisePost(BlockState $$0, BlockState $$1, VoxelShape $$2) {
        boolean $$3 = $$1.getBlock() instanceof WallBlock && (Boolean)$$1.getValue(UP);
        if ($$3) {
            return true;
        } else {
            WallSide $$4 = (WallSide)$$0.getValue(NORTH_WALL);
            WallSide $$5 = (WallSide)$$0.getValue(SOUTH_WALL);
            WallSide $$6 = (WallSide)$$0.getValue(EAST_WALL);
            WallSide $$7 = (WallSide)$$0.getValue(WEST_WALL);
            boolean $$8 = $$5 == WallSide.NONE;
            boolean $$9 = $$7 == WallSide.NONE;
            boolean $$10 = $$6 == WallSide.NONE;
            boolean $$11 = $$4 == WallSide.NONE;
            boolean $$12 = $$11 && $$8 && $$9 && $$10 || $$11 != $$8 || $$9 != $$10;
            if ($$12) {
                return true;
            } else {
                boolean $$13 = $$4 == WallSide.TALL && $$5 == WallSide.TALL || $$6 == WallSide.TALL && $$7 == WallSide.TALL;
                if ($$13) {
                    return false;
                } else {
                    return $$1.is(BlockTags.WALL_POST_OVERRIDE) || isCovered($$2, POST_TEST);
                }
            }
        }
    }

    private BlockState updateSides(BlockState $$0, boolean $$1, boolean $$2, boolean $$3, boolean $$4, VoxelShape $$5) {
        return (BlockState)((BlockState)((BlockState)((BlockState)$$0.setValue(NORTH_WALL, this.makeWallState($$1, $$5, NORTH_TEST))).setValue(EAST_WALL, this.makeWallState($$2, $$5, EAST_TEST))).setValue(SOUTH_WALL, this.makeWallState($$3, $$5, SOUTH_TEST))).setValue(WEST_WALL, this.makeWallState($$4, $$5, WEST_TEST));
    }

    private WallSide makeWallState(boolean $$0, VoxelShape $$1, VoxelShape $$2) {
        if ($$0) {
            return isCovered($$1, $$2) ? WallSide.TALL : WallSide.LOW;
        } else {
            return WallSide.NONE;
        }
    }

    public FluidState getFluidState(BlockState $$0) {
        return (Boolean)$$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
    }

    public boolean propagatesSkylightDown(BlockState $$0, BlockGetter $$1, BlockPos $$2) {
        return !(Boolean)$$0.getValue(WATERLOGGED);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        $$0.add(new Property[]{UP, NORTH_WALL, EAST_WALL, WEST_WALL, SOUTH_WALL, WATERLOGGED});
    }

    public BlockState rotate(BlockState $$0, Rotation $$1) {
        switch ($$1) {
            case CLOCKWISE_180 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)$$0.setValue(NORTH_WALL, (WallSide)$$0.getValue(SOUTH_WALL))).setValue(EAST_WALL, (WallSide)$$0.getValue(WEST_WALL))).setValue(SOUTH_WALL, (WallSide)$$0.getValue(NORTH_WALL))).setValue(WEST_WALL, (WallSide)$$0.getValue(EAST_WALL));
            }
            case COUNTERCLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)$$0.setValue(NORTH_WALL, (WallSide)$$0.getValue(EAST_WALL))).setValue(EAST_WALL, (WallSide)$$0.getValue(SOUTH_WALL))).setValue(SOUTH_WALL, (WallSide)$$0.getValue(WEST_WALL))).setValue(WEST_WALL, (WallSide)$$0.getValue(NORTH_WALL));
            }
            case CLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)$$0.setValue(NORTH_WALL, (WallSide)$$0.getValue(WEST_WALL))).setValue(EAST_WALL, (WallSide)$$0.getValue(NORTH_WALL))).setValue(SOUTH_WALL, (WallSide)$$0.getValue(EAST_WALL))).setValue(WEST_WALL, (WallSide)$$0.getValue(SOUTH_WALL));
            }
            default -> {
                return $$0;
            }
        }
    }

    public BlockState mirror(BlockState $$0, Mirror $$1) {
        switch ($$1) {
            case LEFT_RIGHT -> {
                return (BlockState)((BlockState)$$0.setValue(NORTH_WALL, (WallSide)$$0.getValue(SOUTH_WALL))).setValue(SOUTH_WALL, (WallSide)$$0.getValue(NORTH_WALL));
            }
            case FRONT_BACK -> {
                return (BlockState)((BlockState)$$0.setValue(EAST_WALL, (WallSide)$$0.getValue(WEST_WALL))).setValue(WEST_WALL, (WallSide)$$0.getValue(EAST_WALL));
            }
            default -> {
                return super.mirror($$0, $$1);
            }
        }
    }

    static {
        UP = BlockStateProperties.UP;
        EAST_WALL = BlockStateProperties.EAST_WALL;
        NORTH_WALL = BlockStateProperties.NORTH_WALL;
        SOUTH_WALL = BlockStateProperties.SOUTH_WALL;
        WEST_WALL = BlockStateProperties.WEST_WALL;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        POST_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
        NORTH_TEST = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 9.0);
        SOUTH_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 16.0);
        WEST_TEST = Block.box(0.0, 0.0, 7.0, 9.0, 16.0, 9.0);
        EAST_TEST = Block.box(7.0, 0.0, 7.0, 16.0, 16.0, 9.0);
    }
}