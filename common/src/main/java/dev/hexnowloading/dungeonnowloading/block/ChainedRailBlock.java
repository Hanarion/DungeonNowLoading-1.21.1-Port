package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChainedRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final int POWER_RANGE = 8;

    public ChainedRailBlock(Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, RailShape.NORTH_SOUTH)
                .setValue(FACING, Direction.SOUTH)
                .setValue(WATERLOGGED, false)
                .setValue(POWER, 0));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, FACING, WATERLOGGED, POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        Direction facing = context.getHorizontalDirection();
        RailShape shape = facing.getAxis() == Direction.Axis.X ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        return state.setValue(FACING, facing).setValue(SHAPE, shape);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        schedulePowerUpdate(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
        if (level.getBlockState(pos).is(this)) {
            schedulePowerUpdate(level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateConnectedPower(level, pos);
    }

    protected void schedulePowerUpdate(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    private void updateConnectedPower(ServerLevel level, BlockPos source) {
        Set<BlockPos> connected = findConnectedRails(level, source);
        Map<BlockPos, Integer> propagatedPower = new HashMap<>();
        for (BlockPos pos : connected) {
            int signal = level.getBestNeighborSignal(pos);
            if (signal == 0) {
                continue;
            }
            for (BlockPos poweredPos : findConnectedRails(level, pos, POWER_RANGE)) {
                propagatedPower.merge(poweredPos, signal, Math::max);
            }
        }

        for (BlockPos pos : connected) {
            BlockState state = level.getBlockState(pos);
            int power = propagatedPower.getOrDefault(pos, 0);
            if (state.getBlock() instanceof ChainedRailBlock && state.getValue(POWER) != power) {
                level.setBlock(pos, state.setValue(POWER, power), UPDATE_CLIENTS);
            }
        }
    }

    private Set<BlockPos> findConnectedRails(Level level, BlockPos source) {
        return findConnectedRails(level, source, Integer.MAX_VALUE);
    }

    private Set<BlockPos> findConnectedRails(Level level, BlockPos source, int maxDistance) {
        Set<BlockPos> connected = new HashSet<>();
        Map<BlockPos, Integer> distances = new HashMap<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        connected.add(source.immutable());
        distances.put(source.immutable(), 0);
        pending.add(source);

        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            int distance = distances.get(current);
            if (distance >= maxDistance) {
                continue;
            }
            BlockState state = level.getBlockState(current);
            if (!(state.getBlock() instanceof ChainedRailBlock)) {
                continue;
            }

            RailShape shape = state.getValue(SHAPE);
            for (Direction direction : railDirections(shape)) {
                int endpointHeight = endpointHeight(shape, direction);
                for (int yOffset = -1; yOffset <= 1; yOffset++) {
                    BlockPos candidatePos = current.relative(direction).offset(0, yOffset, 0);
                    if (!level.hasChunkAt(candidatePos) || connected.contains(candidatePos)) {
                        continue;
                    }

                    BlockState candidateState = level.getBlockState(candidatePos);
                    if (!(candidateState.getBlock() instanceof ChainedRailBlock)
                            || candidateState.getValue(FACING) != state.getValue(FACING)) {
                        continue;
                    }

                    RailShape candidateShape = candidateState.getValue(SHAPE);
                    Direction opposite = direction.getOpposite();
                    if (hasEndpoint(candidateShape, opposite)
                            && yOffset + endpointHeight(candidateShape, opposite) == endpointHeight) {
                        BlockPos immutable = candidatePos.immutable();
                        connected.add(immutable);
                        distances.put(immutable, distance + 1);
                        pending.add(immutable);
                    }
                }
            }
        }
        return connected;
    }

    public static void applyMinecartSpeed(AbstractMinecart minecart, BlockState state, double poweredRailSpeed) {
        if (!(state.getBlock() instanceof ChainedRailBlock)) {
            return;
        }

        int power = state.getValue(POWER);
        if (power == 0) {
            minecart.setDeltaMovement(Vec3.ZERO);
            return;
        }

        double targetSpeed = poweredRailSpeed * power / 15.0D;
        Vec3 movement = minecart.getDeltaMovement();
        RailShape shape = state.getValue(SHAPE);
        Direction facing = state.getValue(FACING);
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        if (northSouth) {
            double sign = facing.getStepZ();
            minecart.setDeltaMovement(0.0D, movement.y, targetSpeed * (sign == 0.0D ? 1.0D : sign));
        } else {
            double sign = facing.getStepX();
            minecart.setDeltaMovement(targetSpeed * (sign == 0.0D ? 1.0D : sign), movement.y, 0.0D);
        }
    }

    public static Direction facingForShape(RailShape shape, Direction preferred) {
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        if ((northSouth && preferred.getAxis() == Direction.Axis.Z)
                || (!northSouth && preferred.getAxis() == Direction.Axis.X)) {
            return preferred;
        }
        return switch (shape) {
            case ASCENDING_NORTH -> Direction.NORTH;
            case ASCENDING_SOUTH -> Direction.SOUTH;
            case ASCENDING_WEST -> Direction.WEST;
            default -> northSouth ? Direction.SOUTH : Direction.EAST;
        };
    }

    private static Direction[] railDirections(RailShape shape) {
        return shape == RailShape.NORTH_SOUTH || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH
                ? new Direction[]{Direction.NORTH, Direction.SOUTH}
                : new Direction[]{Direction.WEST, Direction.EAST};
    }

    private static boolean hasEndpoint(RailShape shape, Direction direction) {
        for (Direction endpoint : railDirections(shape)) {
            if (endpoint == direction) {
                return true;
            }
        }
        return false;
    }

    private static int endpointHeight(RailShape shape, Direction direction) {
        return switch (shape) {
            case ASCENDING_NORTH -> direction == Direction.NORTH ? 1 : 0;
            case ASCENDING_SOUTH -> direction == Direction.SOUTH ? 1 : 0;
            case ASCENDING_EAST -> direction == Direction.EAST ? 1 : 0;
            case ASCENDING_WEST -> direction == Direction.WEST ? 1 : 0;
            default -> 0;
        };
    }
}
