package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.entity.DuriteQuellerBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fires PLAYER_BLOCK_DESTROY_EARLY when a player mines a block.
 *
 * Level.destroyBlock (see BlockDestroyCancelMixin) does not cover this path: the player
 * mining route goes through ServerPlayerGameMode.destroyBlock instead, so without this
 * hook the Preserver never learns that a player broke anything inside its region.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class PlayerBlockDestroyCancelMixin {

    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockBreak(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        BlockDestructionManager.reset();
        level.gameEvent(DNLGameEvents.holder(DNLGameEvents.PLAYER_BLOCK_DESTROY_EARLY), blockPos, GameEvent.Context.of(player, level.getBlockState(blockPos)));

        BlockEntity be = level.getBlockEntity(blockPos);
        if (be instanceof DuriteQuellerBlockEntity quellerBe) {
            quellerBe.tryReplaceSelfWithMendingAura(level);
        }

        if (BlockDestructionManager.shouldCancel()) {
            cir.setReturnValue(false);
        }
    }
}
