package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public final class DeepsteelRailMounts {
    private DeepsteelRailMounts() {
    }

    public static InteractionResult tryMountRail(BlockState platformState, Level level, net.minecraft.core.BlockPos pos, Player player, InteractionHand hand) {
        if (!platformState.is(DNLBlocks.DEEPSTEEL_SLOPED_PLATFORM_FLOATING_RAIL.get())) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }

        Block mountedBlock = mountedBlockFor(blockItem.getBlock());
        if (mountedBlock == null) {
            return InteractionResult.PASS;
        }

        BlockState mountedState = mountedBlock.defaultBlockState()
                .setValue(((BaseRailBlock) mountedBlock).getShapeProperty(), railShapeFromFacing(platformState.getValue(DeepsteelPlatformBlock.FACING)))
                .setValue(BaseRailBlock.WATERLOGGED, platformState.getValue(DeepsteelPlatformBlock.WATERLOGGED));
        if (mountedState.hasProperty(PoweredRailBlock.POWERED)) {
            mountedState = mountedState.setValue(PoweredRailBlock.POWERED, level.hasNeighborSignal(pos));
        }
        if (mountedState.hasProperty(ChainedRailBlock.FACING)) {
            RailShape shape = mountedState.getValue(((BaseRailBlock) mountedBlock).getShapeProperty());
            mountedState = mountedState.setValue(ChainedRailBlock.FACING, ChainedRailBlock.facingForShape(shape, player.getDirection()));
        }

        if (!level.isClientSide) {
            level.setBlock(pos, mountedState, Block.UPDATE_ALL);
            BlockState railState = blockItem.getBlock().defaultBlockState();
            SoundType soundType = railState.getSoundType();
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static Block mountedBlockFor(Block railBlock) {
        if (railBlock == Blocks.RAIL) {
            return DNLBlocks.DEEPSTEEL_MOUNTED_RAIL.get();
        }
        if (railBlock == Blocks.POWERED_RAIL) {
            return DNLBlocks.DEEPSTEEL_MOUNTED_POWERED_RAIL.get();
        }
        if (railBlock == Blocks.DETECTOR_RAIL) {
            return DNLBlocks.DEEPSTEEL_MOUNTED_DETECTOR_RAIL.get();
        }
        if (railBlock == Blocks.ACTIVATOR_RAIL) {
            return DNLBlocks.DEEPSTEEL_MOUNTED_ACTIVATOR_RAIL.get();
        }
        if (railBlock == DNLBlocks.SIGNAL_RAIL.get()) {
            return DNLBlocks.DEEPSTEEL_MOUNTED_SIGNAL_RAIL.get();
        }
        if (railBlock == DNLBlocks.CHAINED_RAIL.get()) {
            return DNLBlocks.DEEPSTEEL_MOUNTED_CHAINED_RAIL.get();
        }
        return null;
    }

    public static RailShape railShapeFromFacing(Direction facing) {
        return switch (facing) {
            case EAST -> RailShape.ASCENDING_WEST;
            case SOUTH -> RailShape.ASCENDING_NORTH;
            case WEST -> RailShape.ASCENDING_EAST;
            default -> RailShape.ASCENDING_SOUTH;
        };
    }

    public static Direction facingFromRailShape(RailShape shape) {
        return switch (shape) {
            case ASCENDING_EAST -> Direction.WEST;
            case ASCENDING_WEST -> Direction.EAST;
            case ASCENDING_NORTH -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
    }
}
