package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.ExplosionDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    private static BlockPos targetBlockPos;

    @ModifyVariable(method = "finalizeExplosion",
            at = @At(value = "LOAD"),
            ordinal = 0)
    private BlockPos captureBlockPos(BlockPos blockPos) {
        targetBlockPos = blockPos; // Store the current block position
        return blockPos; // Return the same block position to avoid breaking the loop
    }

    @Shadow @Final private Level level;

    @Redirect(method = "finalizeExplosion",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean redirectIsAir(BlockState blockstate) {

        if (level instanceof ServerLevel serverLevel) {
            //Note: The ExplosionDestructionManager.reset() need to run only on the serverside and not on the clientside since the shouldCancelDestruction boolean is shared between client and server due to being a static variable.
            ExplosionDestructionManager.reset();
            serverLevel.gameEvent(null, DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION.get(), Vec3.atCenterOf(targetBlockPos));
        }

        return blockstate.isAir() || (level instanceof ServerLevel && ExplosionDestructionManager.shouldCancel());
    }
}