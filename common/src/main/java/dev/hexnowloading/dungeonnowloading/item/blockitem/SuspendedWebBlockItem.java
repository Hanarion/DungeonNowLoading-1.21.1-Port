package dev.hexnowloading.dungeonnowloading.item.blockitem;

import dev.hexnowloading.dungeonnowloading.block.SuspendedWebBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class SuspendedWebBlockItem extends BlockItem {
    private static final int MAX_SCAN_LENGTH = 16;
    private static final int STRUCTURE_PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Direction[] STRUCTURE_DIRECTIONS = new Direction[] { Direction.DOWN, Direction.SOUTH, Direction.WEST };

    public SuspendedWebBlockItem() {
        super(DNLBlocks.SUSPENDED_WEB.get(), new Properties());
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Placement placement = findPlacement(context);
        if (placement == null) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        if (!level.isClientSide) {
            Block block = this.getBlock();
            for (int offset = 0; offset < placement.length(); offset++) {
                BlockPos pos = placement.start().relative(placement.facing(), offset);
                BlockState state = block.defaultBlockState()
                        .setValue(SuspendedWebBlock.PART, SuspendedWebBlock.partFor(placement.length(), offset))
                        .setValue(SuspendedWebBlock.FACING, placement.facing());
                level.setBlock(pos, state, STRUCTURE_PLACE_FLAGS);
            }
            level.updateNeighborsAt(placement.start().relative(placement.facing().getOpposite()), block);
            level.updateNeighborsAt(placement.start().relative(placement.facing(), placement.length()), block);
            playPlacementSound(level, placement.start());
            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        } else {
            playPlacementSound(level, placement.start());
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void playPlacementSound(Level level, BlockPos pos) {
        SoundType soundType = Blocks.COBWEB.defaultBlockState().getSoundType();
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        if (level.isClientSide) {
            level.playLocalSound(x, y, z, soundType.getPlaceSound(), SoundSource.BLOCKS, volume, pitch, false);
        } else {
            level.playSound(null, x, y, z, soundType.getPlaceSound(), SoundSource.BLOCKS, volume, pitch);
        }
    }

    private Placement findPlacement(BlockPlaceContext context) {
        BlockPos clickedPos = context.getClickedPos();

        for (Direction direction : orderedDirections(context.getClickedFace())) {
            Placement placement = findInDirection(context, clickedPos, direction, context.getClickedFace());
            if (placement != null) {
                return placement;
            }
        }

        return null;
    }

    private Placement findInDirection(BlockPlaceContext context, BlockPos clickedPos, Direction direction, Direction clickedFace) {
        if (clickedFace == direction.getOpposite()) {
            Placement endPlacement = findFromEnd(context, clickedPos, direction);
            if (endPlacement != null) {
                return endPlacement;
            }
            return findFromStart(context, clickedPos, direction);
        }

        Placement startPlacement = findFromStart(context, clickedPos, direction);
        if (startPlacement != null) {
            return startPlacement;
        }
        return findFromEnd(context, clickedPos, direction);
    }

    private Direction[] orderedDirections(Direction clickedFace) {
        Direction preferred = preferredStructureDirection(clickedFace);
        Direction[] ordered = new Direction[STRUCTURE_DIRECTIONS.length];
        int index = 0;
        ordered[index++] = preferred;
        for (Direction direction : STRUCTURE_DIRECTIONS) {
            if (direction != preferred) {
                ordered[index++] = direction;
            }
        }
        return ordered;
    }

    private Direction preferredStructureDirection(Direction clickedFace) {
        return switch (clickedFace) {
            case NORTH, SOUTH -> Direction.SOUTH;
            case EAST, WEST -> Direction.WEST;
            default -> Direction.DOWN;
        };
    }

    private Placement findFromStart(BlockPlaceContext context, BlockPos start, Direction facing) {
        Level level = context.getLevel();
        if (!SuspendedWebBlock.isSupport(level, start.relative(facing.getOpposite()), facing)) {
            return null;
        }

        for (int length = 1; length <= MAX_SCAN_LENGTH; length++) {
            BlockPos pos = start.relative(facing, length - 1);
            if (!level.getBlockState(pos).canBeReplaced(context)) {
                return null;
            }
            if (SuspendedWebBlock.isSupport(level, pos.relative(facing), facing.getOpposite())) {
                return length > 1 && length < MAX_SCAN_LENGTH ? new Placement(start, facing, length) : null;
            }
        }

        return null;
    }

    private Placement findFromEnd(BlockPlaceContext context, BlockPos end, Direction facing) {
        Level level = context.getLevel();
        if (!SuspendedWebBlock.isSupport(level, end.relative(facing), facing.getOpposite())) {
            return null;
        }

        for (int length = 1; length <= MAX_SCAN_LENGTH; length++) {
            BlockPos pos = end.relative(facing.getOpposite(), length - 1);
            if (!level.getBlockState(pos).canBeReplaced(context)) {
                return null;
            }
            if (SuspendedWebBlock.isSupport(level, pos.relative(facing.getOpposite()), facing)) {
                return length > 1 && length < MAX_SCAN_LENGTH ? new Placement(pos, facing, length) : null;
            }
        }

        return null;
    }

    private record Placement(BlockPos start, Direction facing, int length) {
    }
}
