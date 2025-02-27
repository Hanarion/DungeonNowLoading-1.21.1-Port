package dev.hexnowloading.dungeonnowloading.block;

import com.google.common.collect.UnmodifiableIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class MendingAuraFenceBlock extends MendingAuraBlock {
    public static final BooleanProperty NORTH;
    public static final BooleanProperty EAST;
    public static final BooleanProperty SOUTH;
    public static final BooleanProperty WEST;
    public static final BooleanProperty WATERLOGGED;
    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION;
    protected final VoxelShape[] collisionShapeByIndex;
    protected final VoxelShape[] shapeByIndex;
    private final Object2IntMap<BlockState> stateToIndex = new Object2IntOpenHashMap();
    private final VoxelShape[] occlusionByIndex;

    private boolean hasConnectionFixed;

    public MendingAuraFenceBlock(Properties properties) {
        super(properties);
        this.collisionShapeByIndex = this.makeShapes(2.0F, 2.0F, 24.0F, 0.0F, 24.0F);
        this.shapeByIndex = this.makeShapes(2.0F, 2.0F, 16.0F, 0.0F, 16.0F);
        UnmodifiableIterator var7 = this.stateDefinition.getPossibleStates().iterator();
        while(var7.hasNext()) {
            BlockState blockstate = (BlockState)var7.next();
            this.getAABBIndex(blockstate);
        }
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
        this.occlusionByIndex = this.makeShapes(2.0F, 1.0F, 16.0F, 6.0F, 15.0F);
        this.hasConnectionFixed = false;
    }

    protected VoxelShape[] makeShapes(float p_52327_, float p_52328_, float p_52329_, float p_52330_, float p_52331_) {
        float f = 8.0F - p_52327_;
        float f1 = 8.0F + p_52327_;
        float f2 = 8.0F - p_52328_;
        float f3 = 8.0F + p_52328_;
        VoxelShape voxelshape = Block.box((double)f, 0.0, (double)f, (double)f1, (double)p_52329_, (double)f1);
        VoxelShape voxelshape1 = Block.box((double)f2, (double)p_52330_, 0.0, (double)f3, (double)p_52331_, (double)f3);
        VoxelShape voxelshape2 = Block.box((double)f2, (double)p_52330_, (double)f2, (double)f3, (double)p_52331_, 16.0);
        VoxelShape voxelshape3 = Block.box(0.0, (double)p_52330_, (double)f2, (double)f3, (double)p_52331_, (double)f3);
        VoxelShape voxelshape4 = Block.box((double)f2, (double)p_52330_, (double)f2, 16.0, (double)p_52331_, (double)f3);
        VoxelShape voxelshape5 = Shapes.or(voxelshape1, voxelshape4);
        VoxelShape voxelshape6 = Shapes.or(voxelshape2, voxelshape3);
        VoxelShape[] avoxelshape = new VoxelShape[]{Shapes.empty(), voxelshape2, voxelshape3, voxelshape6, voxelshape1, Shapes.or(voxelshape2, voxelshape1), Shapes.or(voxelshape3, voxelshape1), Shapes.or(voxelshape6, voxelshape1), voxelshape4, Shapes.or(voxelshape2, voxelshape4), Shapes.or(voxelshape3, voxelshape4), Shapes.or(voxelshape6, voxelshape4), voxelshape5, Shapes.or(voxelshape2, voxelshape5), Shapes.or(voxelshape3, voxelshape5), Shapes.or(voxelshape6, voxelshape5)};

        for(int i = 0; i < 16; ++i) {
            avoxelshape[i] = Shapes.or(voxelshape, avoxelshape[i]);
        }

        return avoxelshape;
    }
    public boolean propagatesSkylightDown(BlockState p_52348_, BlockGetter p_52349_, BlockPos p_52350_) {
        return !(Boolean)p_52348_.getValue(WATERLOGGED);
    }

    public VoxelShape getOcclusionShape(BlockState $$0, BlockGetter $$1, BlockPos $$2) {
        return this.occlusionByIndex[this.getAABBIndex($$0)];
    }

    public VoxelShape getVisualShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return this.getShape($$0, $$1, $$2, $$3);
    }

    public VoxelShape getShape(BlockState p_52352_, BlockGetter p_52353_, BlockPos p_52354_, CollisionContext p_52355_) {
        return this.shapeByIndex[this.getAABBIndex(p_52352_)];
    }

    public VoxelShape getCollisionShape(BlockState p_52357_, BlockGetter p_52358_, BlockPos p_52359_, CollisionContext p_52360_) {
        return this.collisionShapeByIndex[this.getAABBIndex(p_52357_)];
    }

    public boolean connectsTo(BlockState $$0, boolean $$1, Direction $$2) {
        Block $$3 = $$0.getBlock();
        boolean $$4 = this.isSameFence($$0);
        boolean $$5 = $$3 instanceof FenceGateBlock && FenceGateBlock.connectsToDirection($$0, $$2);
        return !isExceptionForConnection($$0) && $$1 || $$4 || $$5;
    }

    private boolean isSameFence(BlockState blockState) {
        return blockState.is(BlockTags.FENCES) || blockState.getBlock() instanceof MendingAuraFenceBlock;
    }

    private static int indexFor(Direction p_52344_) {
        return 1 << p_52344_.get2DDataValue();
    }

    protected int getAABBIndex(BlockState p_52364_) {
        return this.stateToIndex.computeIntIfAbsent(p_52364_, (p_52366_) -> {
            int i = 0;
            if ((Boolean)p_52366_.getValue(NORTH)) {
                i |= indexFor(Direction.NORTH);
            }

            if ((Boolean)p_52366_.getValue(EAST)) {
                i |= indexFor(Direction.EAST);
            }

            if ((Boolean)p_52366_.getValue(SOUTH)) {
                i |= indexFor(Direction.SOUTH);
            }

            if ((Boolean)p_52366_.getValue(WEST)) {
                i |= indexFor(Direction.WEST);
            }

            return i;
        });
    }

    public BlockState getStateForPlacement(BlockPlaceContext $$0) {
        BlockGetter $$1 = $$0.getLevel();
        BlockPos $$2 = $$0.getClickedPos();
        FluidState $$3 = $$0.getLevel().getFluidState($$0.getClickedPos());
        BlockPos $$4 = $$2.north();
        BlockPos $$5 = $$2.east();
        BlockPos $$6 = $$2.south();
        BlockPos $$7 = $$2.west();
        BlockState $$8 = $$1.getBlockState($$4);
        BlockState $$9 = $$1.getBlockState($$5);
        BlockState $$10 = $$1.getBlockState($$6);
        BlockState $$11 = $$1.getBlockState($$7);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)super.getStateForPlacement($$0).setValue(NORTH, this.connectsTo($$8, $$8.isFaceSturdy($$1, $$4, Direction.SOUTH), Direction.SOUTH))).setValue(EAST, this.connectsTo($$9, $$9.isFaceSturdy($$1, $$5, Direction.WEST), Direction.WEST))).setValue(SOUTH, this.connectsTo($$10, $$10.isFaceSturdy($$1, $$6, Direction.NORTH), Direction.NORTH))).setValue(WEST, this.connectsTo($$11, $$11.isFaceSturdy($$1, $$7, Direction.EAST), Direction.EAST))).setValue(WATERLOGGED, $$3.getType() == Fluids.WATER);
    }

    public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
        if ((Boolean)$$0.getValue(WATERLOGGED)) {
            $$3.scheduleTick($$4, Fluids.WATER, Fluids.WATER.getTickDelay($$3));
        }

        return $$1.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? (BlockState)$$0.setValue((Property)PROPERTY_BY_DIRECTION.get($$1), this.connectsTo($$2, $$2.isFaceSturdy($$3, $$5, $$1.getOpposite()), $$1.getOpposite())) : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5);
    }

    public FluidState getFluidState(BlockState p_52362_) {
        return (Boolean)p_52362_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_52362_);
    }

    public boolean isPathfindable(BlockState p_52333_, BlockGetter p_52334_, BlockPos p_52335_, PathComputationType p_52336_) {
        return false;
    }

    public BlockState rotate(BlockState p_52341_, Rotation p_52342_) {
        switch (p_52342_) {
            case CLOCKWISE_180 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)p_52341_.setValue(NORTH, (Boolean)p_52341_.getValue(SOUTH))).setValue(EAST, (Boolean)p_52341_.getValue(WEST))).setValue(SOUTH, (Boolean)p_52341_.getValue(NORTH))).setValue(WEST, (Boolean)p_52341_.getValue(EAST));
            }
            case COUNTERCLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)p_52341_.setValue(NORTH, (Boolean)p_52341_.getValue(EAST))).setValue(EAST, (Boolean)p_52341_.getValue(SOUTH))).setValue(SOUTH, (Boolean)p_52341_.getValue(WEST))).setValue(WEST, (Boolean)p_52341_.getValue(NORTH));
            }
            case CLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)p_52341_.setValue(NORTH, (Boolean)p_52341_.getValue(WEST))).setValue(EAST, (Boolean)p_52341_.getValue(NORTH))).setValue(SOUTH, (Boolean)p_52341_.getValue(EAST))).setValue(WEST, (Boolean)p_52341_.getValue(SOUTH));
            }
            default -> {
                return p_52341_;
            }
        }
    }

    public BlockState mirror(BlockState p_52338_, Mirror p_52339_) {
        switch (p_52339_) {
            case LEFT_RIGHT -> {
                return (BlockState)((BlockState)p_52338_.setValue(NORTH, (Boolean)p_52338_.getValue(SOUTH))).setValue(SOUTH, (Boolean)p_52338_.getValue(NORTH));
            }
            case FRONT_BACK -> {
                return (BlockState)((BlockState)p_52338_.setValue(EAST, (Boolean)p_52338_.getValue(WEST))).setValue(WEST, (Boolean)p_52338_.getValue(EAST));
            }
            default -> {
                return super.mirror(p_52338_, p_52339_);
            }
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        $$0.add(new Property[]{NORTH, EAST, WEST, SOUTH, WATERLOGGED});
    }

    static {
        NORTH = PipeBlock.NORTH;
        EAST = PipeBlock.EAST;
        SOUTH = PipeBlock.SOUTH;
        WEST = PipeBlock.WEST;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        PROPERTY_BY_DIRECTION = (Map)PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_52346_) -> {
            return ((Direction)p_52346_.getKey()).getAxis().isHorizontal();
        }).collect(Util.toMap());
    }
}
