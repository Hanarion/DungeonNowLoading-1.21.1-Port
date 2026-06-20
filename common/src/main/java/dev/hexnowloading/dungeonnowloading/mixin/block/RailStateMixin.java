package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.DeepsteelPlatformBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RailState.class)
public class RailStateMixin {
    @Shadow
    @Final
    private Level level;

    @Shadow
    @Final
    private BlockPos pos;

    @Shadow
    @Final
    private BaseRailBlock block;

    @Shadow
    private BlockState state;

    @Inject(method = "connectTo", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$keepDeepsteelPlatformRailsStraight(RailState railState, CallbackInfo ci) {
        if (isMountedDeepsteelRail(state)) {
            level.setBlock(pos, state, 3);
            ci.cancel();
            return;
        }

        BlockState supportState = level.getBlockState(pos.below());
        if (!isDirectionLockingDeepsteelPlatformRail(supportState)) {
            return;
        }

        state = state.setValue(block.getShapeProperty(), railShapeForPlatform(supportState));
        level.setBlock(pos, state, 3);
        ci.cancel();
    }

    private boolean isMountedDeepsteelRail(BlockState state) {
        return state.is(DNLBlocks.DEEPSTEEL_MOUNTED_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_MOUNTED_POWERED_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_MOUNTED_DETECTOR_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_MOUNTED_ACTIVATOR_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_MOUNTED_SIGNAL_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_MOUNTED_CHAINED_RAIL.get());
    }

    private boolean isDirectionLockingDeepsteelPlatformRail(BlockState state) {
        return state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED_RAIL.get());
    }

    private RailShape railShapeForPlatform(BlockState state) {
        return state.getValue(DeepsteelPlatformBlock.FACING).getAxis() == Direction.Axis.X
                ? RailShape.EAST_WEST
                : RailShape.NORTH_SOUTH;
    }
}
