package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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

    // 1.21: isSameRailWithPower detects neighbours via `instanceof PoweredRailBlock` (no
    // BlockState.is(Block) call), and DeepsteelMountedPoweredRailBlock extends PoweredRailBlock,
    // so deepsteel powered rails already chain power natively. The old @Redirect was removed.

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
