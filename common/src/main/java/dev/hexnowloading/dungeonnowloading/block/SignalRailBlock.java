package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.Map;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

public class SignalRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty SIGNAL = IntegerProperty.create("signal", 0, 2);
    public static final int OFF = 0;
    public static final int LEFT_SIDE = 1;
    public static final int RIGHT_SIDE = 2;
    private static final int SIGNAL_TIMEOUT = 4;
    private static final int MAX_SIGNAL_DISTANCE = 14;
    private static final Map<ServerLevel, Map<BlockPos, Long>> EXPIRY_TIMES = new WeakHashMap<>();

    public SignalRailBlock(Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, RailShape.NORTH_SOUTH)
                .setValue(WATERLOGGED, false)
                .setValue(SIGNAL, OFF));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED, SIGNAL);
    }

    public void activate(ServerLevel level, BlockPos pos, int signal) {
        BlockState sourceState = level.getBlockState(pos);
        if (!sourceState.is(this) || signal == OFF) {
            return;
        }

        Set<BlockPos> connectedRails = findConnectedRails(level, pos);
        for (BlockPos connectedPos : connectedRails) {
            activateSingle(level, connectedPos, signal);
        }
        switchConnectedJunctions(level, connectedRails, getSelectedWorldSide(sourceState, signal));
    }

    private void activateSingle(ServerLevel level, BlockPos pos, int signal) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SignalRailBlock signalRail)) {
            return;
        }
        long expiry = level.getGameTime() + SIGNAL_TIMEOUT;
        EXPIRY_TIMES.computeIfAbsent(level, ignored -> new java.util.HashMap<>()).put(pos.immutable(), expiry);
        if (state.getValue(SIGNAL) != signal) {
            level.setBlock(pos, state.setValue(SIGNAL, signal), UPDATE_CLIENTS);
            signalRail.updateSignalNeighbors(level, pos, state.getValue(SHAPE));
        }
        level.scheduleTick(pos, signalRail, SIGNAL_TIMEOUT);
    }

    private Set<BlockPos> findConnectedRails(ServerLevel level, BlockPos source) {
        Set<BlockPos> connected = new HashSet<>();
        Map<BlockPos, Integer> distances = new java.util.HashMap<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        connected.add(source.immutable());
        distances.put(source.immutable(), 0);
        pending.add(source);

        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            int distance = distances.get(current);
            if (distance >= MAX_SIGNAL_DISTANCE) {
                continue;
            }
            BlockState state = level.getBlockState(current);
            RailShape shape = state.getValue(SHAPE);
            for (Direction direction : getRailDirections(shape)) {
                int currentEndpointHeight = endpointHeight(shape, direction);
                for (int yOffset = -1; yOffset <= 1; yOffset++) {
                    BlockPos candidatePos = current.relative(direction).offset(0, yOffset, 0);
                    if (!level.hasChunkAt(candidatePos) || connected.contains(candidatePos)) {
                        continue;
                    }

                    BlockState candidateState = level.getBlockState(candidatePos);
                    if (!(candidateState.getBlock() instanceof SignalRailBlock)) {
                        continue;
                    }

                    RailShape candidateShape = candidateState.getValue(SHAPE);
                    Direction opposite = direction.getOpposite();
                    if (!hasEndpoint(candidateShape, opposite)) {
                        continue;
                    }

                    int candidateEndpointHeight = yOffset + endpointHeight(candidateShape, opposite);
                    if (candidateEndpointHeight == currentEndpointHeight) {
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

    private static Direction[] getRailDirections(RailShape shape) {
        return switch (shape) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> new Direction[]{Direction.NORTH, Direction.SOUTH};
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> new Direction[]{Direction.WEST, Direction.EAST};
            case NORTH_EAST -> new Direction[]{Direction.NORTH, Direction.EAST};
            case NORTH_WEST -> new Direction[]{Direction.NORTH, Direction.WEST};
            case SOUTH_EAST -> new Direction[]{Direction.SOUTH, Direction.EAST};
            case SOUTH_WEST -> new Direction[]{Direction.SOUTH, Direction.WEST};
        };
    }

    private static boolean hasEndpoint(RailShape shape, Direction direction) {
        for (Direction railDirection : getRailDirections(shape)) {
            if (railDirection == direction) {
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

    private void switchConnectedJunctions(ServerLevel level, Set<BlockPos> connectedRails, Direction selectedSide) {
        for (BlockPos railPos : connectedRails) {
            BlockState signalState = level.getBlockState(railPos);
            RailShape signalShape = signalState.getValue(SHAPE);
            for (Direction endpointDirection : getRailDirections(signalShape)) {
                BlockPos junctionPos = railPos.relative(endpointDirection)
                        .above(endpointHeight(signalShape, endpointDirection));
                BlockState junctionState = level.getBlockState(junctionPos);
                if (!junctionState.is(Blocks.RAIL)) {
                    continue;
                }

                RailShape currentShape = junctionState.getValue(RailBlock.SHAPE);
                if (!isCurve(currentShape)) {
                    continue;
                }

                Direction incomingSide = endpointDirection.getOpposite();
                RailShape selectedShape = curveFor(incomingSide, selectedSide);
                if (selectedShape != null && hasConnectedBranchRail(level, junctionPos, selectedSide)) {
                    level.setBlock(junctionPos, junctionState.setValue(RailBlock.SHAPE, selectedShape), UPDATE_ALL);
                }
            }
        }
    }

    private static boolean hasConnectedBranchRail(ServerLevel level, BlockPos junctionPos, Direction branchDirection) {
        return hasConnectedRail(level, junctionPos, branchDirection);
    }

    private static boolean hasConnectedRail(Level level, BlockPos junctionPos, Direction branchDirection) {
        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            BlockPos candidatePos = junctionPos.relative(branchDirection).offset(0, yOffset, 0);
            if (!level.hasChunkAt(candidatePos)) {
                continue;
            }

            BlockState candidateState = level.getBlockState(candidatePos);
            if (!candidateState.is(BlockTags.RAILS) || !(candidateState.getBlock() instanceof BaseRailBlock railBlock)) {
                continue;
            }

            RailShape candidateShape = candidateState.getValue(railBlock.getShapeProperty());
            Direction towardJunction = branchDirection.getOpposite();
            if (hasEndpoint(candidateShape, towardJunction)
                    && yOffset + endpointHeight(candidateShape, towardJunction) == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldKeepJunctionShape(Level level, BlockPos junctionPos, BlockState state) {
        if (!state.is(Blocks.RAIL)) {
            return false;
        }

        RailShape shape = state.getValue(RailBlock.SHAPE);
        if (!isCurve(shape)) {
            return false;
        }

        boolean connectedToSignalRail = false;
        for (Direction direction : getRailDirections(shape)) {
            if (!hasConnectedRail(level, junctionPos, direction)) {
                return false;
            }
            connectedToSignalRail |= hasConnectedSignalRail(level, junctionPos, direction);
        }
        return connectedToSignalRail;
    }

    private static boolean hasConnectedSignalRail(Level level, BlockPos junctionPos, Direction direction) {
        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            BlockPos candidatePos = junctionPos.relative(direction).offset(0, yOffset, 0);
            BlockState candidateState = level.getBlockState(candidatePos);
            if (!(candidateState.getBlock() instanceof SignalRailBlock)) {
                continue;
            }

            RailShape candidateShape = candidateState.getValue(SHAPE);
            Direction towardJunction = direction.getOpposite();
            if (hasEndpoint(candidateShape, towardJunction)
                    && yOffset + endpointHeight(candidateShape, towardJunction) == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCurve(RailShape shape) {
        return shape == RailShape.NORTH_EAST || shape == RailShape.NORTH_WEST
                || shape == RailShape.SOUTH_EAST || shape == RailShape.SOUTH_WEST;
    }

    private static RailShape curveFor(Direction first, Direction second) {
        if ((first == Direction.NORTH && second == Direction.EAST)
                || (first == Direction.EAST && second == Direction.NORTH)) {
            return RailShape.NORTH_EAST;
        }
        if ((first == Direction.NORTH && second == Direction.WEST)
                || (first == Direction.WEST && second == Direction.NORTH)) {
            return RailShape.NORTH_WEST;
        }
        if ((first == Direction.SOUTH && second == Direction.EAST)
                || (first == Direction.EAST && second == Direction.SOUTH)) {
            return RailShape.SOUTH_EAST;
        }
        if ((first == Direction.SOUTH && second == Direction.WEST)
                || (first == Direction.WEST && second == Direction.SOUTH)) {
            return RailShape.SOUTH_WEST;
        }
        return null;
    }

    private static Direction getSelectedWorldSide(BlockState state, int signal) {
        RailShape shape = state.getValue(SHAPE);
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        if (northSouth) {
            return signal == LEFT_SIDE ? Direction.WEST : Direction.EAST;
        }
        return signal == LEFT_SIDE ? Direction.NORTH : Direction.SOUTH;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Map<BlockPos, Long> expiryTimes = EXPIRY_TIMES.get(level);
        long expiry = expiryTimes == null ? 0L : expiryTimes.getOrDefault(pos, 0L);
        long remaining = expiry - level.getGameTime();
        if (remaining > 0L) {
            level.scheduleTick(pos, this, (int) remaining);
            return;
        }

        if (expiryTimes != null) {
            expiryTimes.remove(pos);
        }
        if (state.getValue(SIGNAL) != OFF) {
            level.setBlock(pos, state.setValue(SIGNAL, OFF), UPDATE_CLIENTS);
            updateSignalNeighbors(level, pos, state.getValue(SHAPE));
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Direction output = getOutputDirection(state);
        return output == direction ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    private static Direction getOutputDirection(BlockState state) {
        int signal = state.getValue(SIGNAL);
        if (signal == OFF) {
            return null;
        }

        RailShape shape = state.getValue(SHAPE);
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        if (northSouth) {
            return signal == LEFT_SIDE ? Direction.EAST : Direction.WEST;
        }
        return signal == LEFT_SIDE ? Direction.SOUTH : Direction.NORTH;
    }

    private void updateSignalNeighbors(Level level, BlockPos pos, RailShape shape) {
        if (shape == RailShape.NORTH_SOUTH || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH) {
            level.updateNeighborsAt(pos.west(), this);
            level.updateNeighborsAt(pos.east(), this);
        } else {
            level.updateNeighborsAt(pos.north(), this);
            level.updateNeighborsAt(pos.south(), this);
        }
    }
}
