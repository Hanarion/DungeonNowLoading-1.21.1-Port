package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoweredRailBlock.class)
public abstract class PoweredRailBlockMixin {
    @Inject(method = "isSameRailWithPower", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$readDeepsteelMountedDetectorRails(Level level, BlockPos pos, boolean forward, int distance, RailShape sourceShape, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (state.is(DNLBlocks.DEEPSTEEL_MOUNTED_DETECTOR_RAIL.get())) {
            RailShape detectorShape = state.getValue(DetectorRailBlock.SHAPE);
            if (canRailPowerContinue(sourceShape, detectorShape)) {
                cir.setReturnValue(state.getValue(DetectorRailBlock.POWERED));
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(
            method = "isSameRailWithPower",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"),
            require = 0
    )
    private boolean dungeonnowloading$connectDeepsteelMountedPoweredRails(BlockState state, Block block) {
        Block self = (Block) (Object) this;
        if (isPoweredRailPowerChainBlock(self) && isPoweredRailPowerChainBlock(state.getBlock())) {
            return true;
        }

        return state.is(block);
    }

    private boolean isPoweredRailPowerChainBlock(Block block) {
        return block == Blocks.POWERED_RAIL || block == DNLBlocks.DEEPSTEEL_MOUNTED_POWERED_RAIL.get();
    }

    private boolean canRailPowerContinue(RailShape sourceShape, RailShape targetShape) {
        if (sourceShape == RailShape.EAST_WEST) {
            return targetShape != RailShape.NORTH_SOUTH
                    && targetShape != RailShape.ASCENDING_NORTH
                    && targetShape != RailShape.ASCENDING_SOUTH;
        }

        if (sourceShape == RailShape.NORTH_SOUTH) {
            return targetShape != RailShape.EAST_WEST
                    && targetShape != RailShape.ASCENDING_EAST
                    && targetShape != RailShape.ASCENDING_WEST;
        }

        return true;
    }
}
