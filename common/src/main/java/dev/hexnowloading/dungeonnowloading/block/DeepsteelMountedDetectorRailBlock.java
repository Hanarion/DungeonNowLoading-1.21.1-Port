package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeepsteelMountedDetectorRailBlock extends DetectorRailBlock {
    private final Item railDrop;

    public DeepsteelMountedDetectorRailBlock(Properties properties, Item railDrop) {
        super(properties);
        this.railDrop = railDrop;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(super.getShape(state, level, pos, context), DeepsteelMountedRailBlock.platformShape(state)).optimize();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DeepsteelMountedRailBlock.platformShape(state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!level.isClientSide && newState.isAir()) {
            level.setBlock(pos, DeepsteelMountedRailBlock.platformStateFromRail(state), UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateDir(Level level, BlockPos pos, BlockState state, boolean forceUpdate) {
        return state;
    }

    @Override
    protected void updatePowerToConnected(Level level, BlockPos pos, BlockState state, boolean powered) {
        super.updatePowerToConnected(level, pos, state, powered);
        RailShape shape = state.getValue(SHAPE);

        switch (shape) {
            case ASCENDING_EAST -> {
                updateRailPowerNeighbor(level, pos.west(), pos);
                updateRailPowerNeighbor(level, pos.east(), pos);
                updateRailPowerNeighbor(level, pos.east().above(), pos);
            }
            case ASCENDING_WEST -> {
                updateRailPowerNeighbor(level, pos.west().above(), pos);
                updateRailPowerNeighbor(level, pos.west(), pos);
                updateRailPowerNeighbor(level, pos.east(), pos);
            }
            case ASCENDING_NORTH -> {
                updateRailPowerNeighbor(level, pos.north().above(), pos);
                updateRailPowerNeighbor(level, pos.north(), pos);
                updateRailPowerNeighbor(level, pos.south(), pos);
            }
            case ASCENDING_SOUTH -> {
                updateRailPowerNeighbor(level, pos.north(), pos);
                updateRailPowerNeighbor(level, pos.south(), pos);
                updateRailPowerNeighbor(level, pos.south().above(), pos);
            }
            case EAST_WEST -> {
                updateRailPowerNeighbor(level, pos.west(), pos);
                updateRailPowerNeighbor(level, pos.east(), pos);
            }
            default -> {
                updateRailPowerNeighbor(level, pos.north(), pos);
                updateRailPowerNeighbor(level, pos.south(), pos);
            }
        }
    }

    private void updateRailPowerNeighbor(Level level, BlockPos targetPos, BlockPos sourcePos) {
        BlockState targetState = level.getBlockState(targetPos);
        level.neighborChanged(targetState, targetPos, this, sourcePos, false);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(railDrop);
    }

    public Item getRailDrop() {
        return railDrop;
    }
}
