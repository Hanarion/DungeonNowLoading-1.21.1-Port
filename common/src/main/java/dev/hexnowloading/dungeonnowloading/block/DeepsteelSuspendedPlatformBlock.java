package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DeepsteelSuspendedPlatformBlock extends DeepsteelPlatformBlock {
    public DeepsteelSuspendedPlatformBlock(Properties properties) {
        super(properties);
    }

    public DeepsteelSuspendedPlatformBlock(Properties properties, VoxelShape shape) {
        super(properties, shape, ShapeRotation.FULL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (!clickedFace.getAxis().isHorizontal()) {
            return null;
        }

        BlockState state = this.defaultBlockState()
                .setValue(FACING, clickedFace)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, facing) || isDeepsteelPlatformFrameSupport(supportState);
    }

    private boolean isDeepsteelPlatformFrameSupport(BlockState state) {
        return state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL.get());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
