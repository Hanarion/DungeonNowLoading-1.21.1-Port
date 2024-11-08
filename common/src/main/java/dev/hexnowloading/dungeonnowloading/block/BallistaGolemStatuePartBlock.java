package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.BallistaGolemStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.ScuttleStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.property.BallistaGolemStatueStates;
import dev.hexnowloading.dungeonnowloading.registry.DNLProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.*;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BallistaGolemStatuePartBlock extends Block implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<BallistaGolemStatueStates> STATES = DNLProperties.BALLISTA_GOLEM_STATUE_PARTS;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private boolean playerDestroyedPart = false;

    public BallistaGolemStatuePartBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(STATES, BallistaGolemStatueStates.TOP_C).setValue(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite()).setValue(STATES, BallistaGolemStatueStates.TOP_C).setValue(WATERLOGGED, false);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Ensure this block is actually being removed and not replaced
        if (!world.isClientSide && state.getBlock() != newState.getBlock()) {
            // Get the part's relative position from its state and facing direction before removal
            BallistaGolemStatueStates partState = state.getValue(STATES);
            Direction partFacing = state.getValue(FACING);  // Retrieve the facing before the block is removed

            // Calculate the core position based on the part's position and its relative position
            BlockPos corePos = findCorePosition(pos, partState, partFacing);

            // Check if core block exists and is the correct type
            BlockState coreState = world.getBlockState(corePos);
            if (!(coreState.getBlock() instanceof BallistaGolemStatueBlock ballistaGolemStatueBlock)) {
                return; // Exit if the core block isn't found or is incorrect
            }

            ballistaGolemStatueBlock.setPlayerDestroyed(this.playerDestroyedPart);

            Direction coreFacing = coreState.getValue(FACING);

            // Break all parts relative to the core position, adjusted for the core's facing direction
            for (Map.Entry<BallistaGolemStatueStates, BlockPos> entry : BallistaGolemStatueBlock.statePositions.entrySet()) {
                BlockPos relativePos = entry.getValue();
                BlockPos adjustedPos = applyReverseRotation(relativePos, coreFacing); // Reverse the rotation
                BlockPos partPos = corePos.offset(adjustedPos);

                // Destroy each part block without dropping items
                world.destroyBlock(partPos, false);
            }

            // Also destroy the core block
            world.destroyBlock(corePos, false);
        }

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide) {
            this.playerDestroyedPart = !player.getAbilities().instabuild;
            if (this.playerDestroyedPart) {
                ItemStack heldItem = player.getMainHandItem();
                this.playerDestroyedPart = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, heldItem) < 1;
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos findCorePosition(BlockPos partPos, BallistaGolemStatueStates partState, Direction partFacing) {
        // Retrieve the part's relative position from its state
        BlockPos relativePos = BallistaGolemStatueBlock.statePositions.get(partState);

        // Apply the reverse rotation based on the part's facing direction
        BlockPos adjustedPos = applyReverseRotation(relativePos, partFacing);

        // Calculate core position by subtracting the adjusted relative position from the part's position
        return partPos.subtract(adjustedPos);
    }

    private BlockPos applyReverseRotation(BlockPos pos, Direction facing) {
        switch (facing) {
            case NORTH:
                return pos; // No rotation needed
            case SOUTH:
                return new BlockPos(-pos.getX(), pos.getY(), -pos.getZ()); // Reverse 180-degree rotation
            case WEST:
                return new BlockPos(pos.getZ(), pos.getY(), -pos.getX()); // 90-degree clockwise rotation
            case EAST:
                return new BlockPos(-pos.getZ(), pos.getY(), pos.getX()); // 90-degree counterclockwise rotation
            default:
                return pos;
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos1, boolean b) {
        if (level.isClientSide) {
            return;
        }
        if (!level.hasNeighborSignal(blockPos)) {
            return;
        }

        BlockPos corePos = findCorePosition(blockPos, blockState.getValue(STATES), blockState.getValue(FACING));


        Direction facing = blockState.getValue(FACING);
        BallistaGolemStatueBlockEntity blockEntity = (BallistaGolemStatueBlockEntity) level.getBlockEntity(corePos);
        if (blockEntity != null) {
            blockEntity.summonBallistaGolemEntity(level, corePos, facing);
        }

        BallistaGolemStatueBlock.destroyAllBlocks(level, corePos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(FACING);
        stateBuilder.add(STATES);
        stateBuilder.add(WATERLOGGED);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }
}
