package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class PlayerBlockDestroyCancelMixin {

    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;
    private static boolean shouldCancelDestruction;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockBreak(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        BlockDestructionManager.reset();
        level.gameEvent(DNLGameEvents.PLAYER_BLOCK_DESTROY_EARLY.get(), blockPos, GameEvent.Context.of(player, level.getBlockState(blockPos)));

        var be = level.getBlockEntity(blockPos);
        if (be instanceof dev.hexnowloading.dungeonnowloading.block.entity.DuriteQuellerBlockEntity quellerBe) {
            quellerBe.tryReplaceSelfWithMendingAura(level);
        }

        if (BlockDestructionManager.shouldCancel()) {
            cir.setReturnValue(false);
        }
    }
}
