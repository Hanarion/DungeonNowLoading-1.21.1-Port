package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.block.entity.BallistaGolemStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.property.BallistaGolemStatueStates;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
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
        if (!world.isClientSide && state.getBlock() != newState.getBlock()) {
            BallistaGolemStatueStates partState = state.getValue(STATES);
            Direction partFacing = state.getValue(FACING);

            BlockPos corePos = findCorePosition(pos, partState, partFacing);

            BlockState coreState = world.getBlockState(corePos);
            if (!(coreState.getBlock() instanceof BallistaGolemStatueBlock ballistaGolemStatueBlock)) {
                return;
            }

            ballistaGolemStatueBlock.setPlayerDestroyed(this.playerDestroyedPart);

            Direction coreFacing = coreState.getValue(FACING);

            for (Map.Entry<BallistaGolemStatueStates, BlockPos> entry : BallistaGolemStatueBlock.statePositions.entrySet()) {
                BlockPos relativePos = entry.getValue();
                BlockPos adjustedPos = applyReverseRotation(relativePos, coreFacing);
                BlockPos partPos = corePos.offset(adjustedPos);
                world.destroyBlock(partPos, false);
            }
            world.destroyBlock(corePos, false);
        }

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide) {
            this.playerDestroyedPart = !player.getAbilities().instabuild;
            if (this.playerDestroyedPart) {
                ItemStack heldItem = player.getMainHandItem();
                this.playerDestroyedPart = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(world, Enchantments.SILK_TOUCH), heldItem) < 1;
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    public static BlockPos findCorePosition(BlockPos partPos, BallistaGolemStatueStates partState, Direction partFacing) {
        BlockPos relativePos = BallistaGolemStatueBlock.statePositions.get(partState);
        BlockPos adjustedPos = applyReverseRotation(relativePos, partFacing);
        return partPos.subtract(adjustedPos);
    }

    private static BlockPos applyReverseRotation(BlockPos pos, Direction facing) {
        switch (facing) {
            case NORTH:
                return pos; // No rotation needed
            case SOUTH:
                return new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case WEST:
                return new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
            case EAST:
                return new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
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
            this.playerDestroyedPart = false;
        }

        BallistaGolemStatueBlock.destroyAllBlocks(level, corePos);
        BallistaGolemStatueBlock.destroyBlocksAbove(level, corePos);
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

    @Override
    public ItemStack getCloneItemStack(BlockGetter $$0, BlockPos $$1, BlockState $$2) {
        return new ItemStack(DNLItems.BALLISTA_GOLEM_STATUE.get());
    }
}
