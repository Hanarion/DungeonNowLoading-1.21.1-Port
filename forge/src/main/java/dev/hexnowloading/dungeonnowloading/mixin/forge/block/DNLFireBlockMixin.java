package dev.hexnowloading.dungeonnowloading.mixin.forge.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockBurnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireBlock.class)
public abstract class DNLFireBlockMixin {

    @Redirect(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextInt(I)I",
                    ordinal = 1))
    private int injectBlockBurnEvent(RandomSource randomSource, int bound, Level level, BlockPos blockPos, int i, RandomSource originalRandom, int j) {
        BlockBurnManager.reset();

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.gameEvent(null, DNLGameEvents.BLOCK_BURNED.get(), Vec3.atCenterOf(blockPos));
        }

        if (BlockBurnManager.shouldCancel()) {
            return 69;
        }

        return randomSource.nextInt(bound); // 🔄 Continue with normal random function
    }


    @Redirect(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean preventFireBurning(Level level, BlockPos pos, boolean isMoving) {

        if (BlockBurnManager.shouldCancel()) {
            return false;
        }

        return level.removeBlock(pos, isMoving);
    }
}