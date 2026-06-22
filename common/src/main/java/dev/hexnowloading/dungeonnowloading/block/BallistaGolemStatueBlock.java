package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.block.entity.BallistaGolemStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.property.BallistaGolemStatueStates;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static dev.hexnowloading.dungeonnowloading.block.BallistaGolemStatuePartBlock.STATES;

public class BallistaGolemStatueBlock extends BaseEntityBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final Map<BallistaGolemStatueStates, BlockPos> statePositions = Map.ofEntries(
            Map.entry(BallistaGolemStatueStates.TOP_N, new BlockPos(0, 2, -1)),    // North center
            Map.entry(BallistaGolemStatueStates.TOP_NE, new BlockPos(1, 2, -1)),   // North-East
            Map.entry(BallistaGolemStatueStates.TOP_E, new BlockPos(1, 2, 0)),     // East center
            Map.entry(BallistaGolemStatueStates.TOP_SE, new BlockPos(1, 2, 1)),    // South-East
            Map.entry(BallistaGolemStatueStates.TOP_S, new BlockPos(0, 2, 1)),     // South center
            Map.entry(BallistaGolemStatueStates.TOP_SW, new BlockPos(-1, 2, 1)),   // South-West
            Map.entry(BallistaGolemStatueStates.TOP_W, new BlockPos(-1, 2, 0)),    // West center
            Map.entry(BallistaGolemStatueStates.TOP_NW, new BlockPos(-1, 2, -1)),  // North-West
            Map.entry(BallistaGolemStatueStates.TOP_C, new BlockPos(0, 2, 0)),     // Center

            // Middle layer (y = 1)
            Map.entry(BallistaGolemStatueStates.MIDDLE_N, new BlockPos(0, 1, -1)),    // North center
            Map.entry(BallistaGolemStatueStates.MIDDLE_NE, new BlockPos(1, 1, -1)),   // North-East
            Map.entry(BallistaGolemStatueStates.MIDDLE_E, new BlockPos(1, 1, 0)),     // East center
            Map.entry(BallistaGolemStatueStates.MIDDLE_SE, new BlockPos(1, 1, 1)),    // South-East
            Map.entry(BallistaGolemStatueStates.MIDDLE_S, new BlockPos(0, 1, 1)),     // South center
            Map.entry(BallistaGolemStatueStates.MIDDLE_SW, new BlockPos(-1, 1, 1)),   // South-West
            Map.entry(BallistaGolemStatueStates.MIDDLE_W, new BlockPos(-1, 1, 0)),    // West center
            Map.entry(BallistaGolemStatueStates.MIDDLE_NW, new BlockPos(-1, 1, -1)),  // North-West
            Map.entry(BallistaGolemStatueStates.MIDDLE_C, new BlockPos(0, 1, 0)),     // Center

            // Bottom layer (y = 0)
            Map.entry(BallistaGolemStatueStates.BOTTOM_N, new BlockPos(0, 0, -1)),    // North center
            Map.entry(BallistaGolemStatueStates.BOTTOM_NE, new BlockPos(1, 0, -1)),   // North-East
            Map.entry(BallistaGolemStatueStates.BOTTOM_E, new BlockPos(1, 0, 0)),     // East center
            Map.entry(BallistaGolemStatueStates.BOTTOM_SE, new BlockPos(1, 0, 1)),    // South-East
            Map.entry(BallistaGolemStatueStates.BOTTOM_S, new BlockPos(0, 0, 1)),     // South center
            Map.entry(BallistaGolemStatueStates.BOTTOM_SW, new BlockPos(-1, 0, 1)),   // South-West
            Map.entry(BallistaGolemStatueStates.BOTTOM_W, new BlockPos(-1, 0, 0)),    // West center
            Map.entry(BallistaGolemStatueStates.BOTTOM_NW, new BlockPos(-1, 0, -1))  // North-West
    );

    public boolean playerDestroyed = false;

    public BallistaGolemStatueBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Get the world and position for the core block
        Level world = context.getLevel();
        BlockPos corePos = context.getClickedPos();
        Direction playerFacing = context.getHorizontalDirection().getOpposite();

        // Check if the 3x3x3 area around the core is free
        if (isAreaClear(world, corePos, playerFacing)) {
            return this.defaultBlockState().setValue(FACING, playerFacing);
        } else {
            return null; // Cancel placement if the area is not clear
        }
    }

    private boolean isAreaClear(Level world, BlockPos corePos, Direction facing) {
        for (BlockPos relativePos : statePositions.values()) {
            BlockPos partPos = corePos.offset(getRotatedPos(relativePos, facing));
            BlockState stateAtPos = world.getBlockState(partPos);

            // Check if the position is not air or a replaceable block
            if (!stateAtPos.isAir() && !stateAtPos.canBeReplaced()) {
                return false; // Area is not clear, cancel placement
            }
        }
        return true; // Area is clear
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide && placer instanceof Player) {
            // Get the player's facing direction
            Direction playerFacing = placer.getDirection().getOpposite();

            boolean triggeredByRedstone = false;

            // Iterate over each state and position in this.statePositions
            for (Map.Entry<BallistaGolemStatueStates, BlockPos> entry : this.statePositions.entrySet()) {
                BallistaGolemStatueStates golemState = entry.getKey();
                BlockPos relativePos = entry.getValue();

                // Apply rotation to relativePos based on playerFacing
                BlockPos rotatedPos = getRotatedPos(relativePos, playerFacing);

                // Calculate the absolute position of the part based on the rotated position
                BlockPos partPos = pos.offset(rotatedPos);

                // Set the appropriate block state with the player's facing direction and any additional properties
                BlockState partBlockState = DNLBlocks.BALLISTA_GOLEM_STATUE_PART.get().defaultBlockState()
                        .setValue(BallistaGolemStatuePartBlock.FACING, playerFacing) // Set the facing direction
                        .setValue(STATES, golemState);  // Set custom state

                // Place the part block with the designated state
                world.setBlock(partPos, partBlockState, Block.UPDATE_ALL);
                if (world.hasNeighborSignal(partPos)) {
                    triggeredByRedstone = true;
                }
            }

            if (triggeredByRedstone) {
                // For clean up
                destroyAllBlocks(world, pos);
            } else if (world.hasNeighborSignal(pos)) {
                destroyAllBlocks(world, pos);
                destroyBlocksAbove(world, pos);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isClientSide && state.getBlock() != newState.getBlock()) {
            destroyAllBlocks(world, pos);
            destroyBlocksAbove(world, pos);

            if (playerDestroyed) {
                if (state.getBlock() instanceof BallistaGolemStatueBlock) {
                    BallistaGolemStatueBlockEntity blockEntity = (BallistaGolemStatueBlockEntity) world.getBlockEntity(pos);
                    if (blockEntity != null) {
                        blockEntity.summonBallistaGolemEntity(world, pos, state.getValue(FACING));
                    }
                }
            }
        }

        super.onRemove(state, world, pos, newState, isMoving);
    }

    public static void destroyAllBlocks(Level world, BlockPos pos) {
        for (Map.Entry<BallistaGolemStatueStates, BlockPos> entry : statePositions.entrySet()) {
            BlockPos relativePos = entry.getValue();
            BlockPos partPos = pos.offset(relativePos);

            world.destroyBlock(partPos, false);
        }
    }

    public static void destroyBlocksAbove(Level level, BlockPos blockPos) {
        BlockPos targetPlane = blockPos; // Y + 3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos breakPos = targetPlane.offset(x, 3, z); // (x, y+3, z)
                level.destroyBlock(breakPos, true);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide) {
            this.playerDestroyed = !player.getAbilities().instabuild;
            if (this.playerDestroyed) {
                ItemStack heldItem = player.getMainHandItem();
                this.playerDestroyed = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(world, Enchantments.SILK_TOUCH), heldItem) < 1;
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos1, boolean b) {
        if (level.isClientSide) {
            return;
        }
        if (!level.hasNeighborSignal(blockPos)) {
            return;
        }


        Direction facing = blockState.getValue(FACING);
        BallistaGolemStatueBlockEntity blockEntity = (BallistaGolemStatueBlockEntity) level.getBlockEntity(blockPos);
        if (blockEntity != null) {
            blockEntity.summonBallistaGolemEntity(level, blockPos, facing);
        }

        destroyAllBlocks(level, blockPos);
        destroyBlocksAbove(level, blockPos);
    }

    private BlockPos getRotatedPos(BlockPos pos, Direction facing) {
        switch (facing) {
            case NORTH:
                return pos; // No rotation needed (default orientation)
            case SOUTH:
                return new BlockPos(-pos.getX(), pos.getY(), -pos.getZ()); // 180-degree rotation
            case WEST:
                return new BlockPos(pos.getZ(), pos.getY(), -pos.getX()); // 90-degree counterclockwise
            case EAST:
                return new BlockPos(-pos.getZ(), pos.getY(), pos.getX()); // 90-degree clockwise
            default:
                return pos; // Default case
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BallistaGolemStatueBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState $blockState0) {
        return RenderShape.MODEL;
    }

    public void setPlayerDestroyed(boolean b) {
        this.playerDestroyed = b;
    }

    public static Direction getDirection(BlockState blockState) {
        return blockState.getValue(FACING);
    }
}
