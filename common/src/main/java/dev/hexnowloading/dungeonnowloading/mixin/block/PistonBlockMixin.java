package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.PistonPushManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBlockMixin {

    @Inject(method = "isPushable",
            at = @At("RETURN"),
            cancellable = true) // Allows modifying the return value
    private static void modifyPushability(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean bl, Direction direction2, CallbackInfoReturnable<Boolean> cir) {
        PistonPushManager.reset();

        boolean isPushable = cir.getReturnValue(); // Get the return value from the original method

        // Fire a game event for debugging or tracking purposes
        if (level instanceof ServerLevel serverLevel) {
            BlockState stateWithDirection = blockState.setValue(PistonBaseBlock.FACING, direction);
            serverLevel.gameEvent(isPushable ? DNLGameEvents.BLOCK_PUSHED_EARLY.get() : DNLGameEvents.BLOCK_PUSHED_EARLY_FAILED.get(), Vec3.atCenterOf(blockPos), GameEvent.Context.of(stateWithDirection));
        }

        if (PistonPushManager.shouldCancel()) {
            cir.setReturnValue(false);
        }
    }
}
