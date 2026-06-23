package dev.hexnowloading.dungeonnowloading.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeepsteelPlatformBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<DeepsteelPlatformBlock> CODEC = simpleCodec(DeepsteelPlatformBlock::new);

    @Override
    public MapCodec<DeepsteelPlatformBlock> codec() {
        return CODEC;
    }
    public enum ShapeRotation {
        AXIS,
        FULL
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final VoxelShape northSouthShape;
    private final VoxelShape eastWestShape;
    private final VoxelShape northShape;
    private final VoxelShape eastShape;
    private final VoxelShape southShape;
    private final VoxelShape westShape;
    private final ShapeRotation shapeRotation;

    public DeepsteelPlatformBlock(Properties properties) {
        this(properties, Shapes.block(), ShapeRotation.FULL);
    }

    public DeepsteelPlatformBlock(Properties properties, VoxelShape shape, ShapeRotation shapeRotation) {
        super(properties);
        this.northSouthShape = shape;
        this.eastWestShape = rotateY(shape, 1);
        this.northShape = shape;
        this.eastShape = rotateY(shape, 1);
        this.southShape = rotateY(shape, 2);
        this.westShape = rotateY(shape, 3);
        this.shapeRotation = shapeRotation;
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult mountedRail = DeepsteelRailMounts.tryMountRail(state, level, pos, player, hand);
        return mountedRail.consumesAction() ? mountedRail : super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    protected VoxelShape shapeFor(Direction direction) {
        if (shapeRotation == ShapeRotation.AXIS) {
            return axisShapeFor(direction);
        }

        return switch (direction) {
            case EAST -> eastShape;
            case SOUTH -> southShape;
            case WEST -> westShape;
            default -> northShape;
        };
    }

    protected VoxelShape axisShapeFor(Direction direction) {
        return direction.getAxis() == Direction.Axis.X ? eastWestShape : northSouthShape;
    }

    private static VoxelShape rotateY(VoxelShape shape, int quarterTurns) {
        VoxelShape rotated = shape;
        for (int i = 0; i < quarterTurns; i++) {
            VoxelShape next = Shapes.empty();
            for (AABB box : rotated.toAabbs()) {
                next = Shapes.or(next, Shapes.create(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX));
            }
            rotated = next;
        }
        return rotated.optimize();
    }
}
