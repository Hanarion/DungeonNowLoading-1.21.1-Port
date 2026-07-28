package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.SignalRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockMixin {

    // Signal rails keep their junction shape instead of being re-derived by vanilla's
    // neighbour-based direction update.
    @Inject(method = "updateDir", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$keepSignalRailJunctionShape(Level level, BlockPos pos, BlockState state, boolean forceUpdate, CallbackInfoReturnable<BlockState> cir) {
        if (SignalRailBlock.shouldKeepJunctionShape(level, pos, state)) {
            cir.setReturnValue(state);
        }
    }
}
