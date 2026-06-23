package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.entity.DuriteQuellerBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Level.class)
public class BlockDestroyCancelMixin {
    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z", at = @At("HEAD"), cancellable = true)
    private void onDestroyBlock(BlockPos pos, boolean drop, @Nullable Entity entity, int flags, CallbackInfoReturnable<Boolean> cir) {
        BlockDestructionManager.reset();
        Level level = ((Level) (Object) this);
        level.gameEvent(DNLGameEvents.holder(DNLGameEvents.BLOCK_DESTROY_EARLY), pos, GameEvent.Context.of(entity, level.getBlockState(pos)));

        if (!level.isClientSide && level instanceof ServerLevel server) {
            BlockEntity be = server.getBlockEntity(pos);
            if (be instanceof DuriteQuellerBlockEntity quellerBe) {
                quellerBe.tryReplaceSelfWithMendingAura(server);
            }
        }

        if (BlockDestructionManager.shouldCancel()) {
            cir.setReturnValue(false);
        }
    }
}
