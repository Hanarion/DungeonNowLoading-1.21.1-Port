package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.ContainerDropManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Containers.class)
public abstract class ContainerMixin {

    @Inject(method = "dropContents(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/Container;)V",
            at = @At("HEAD"), cancellable = true)
    private static void onDropContents(Level level, BlockPos pos, Container container, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.gameEvent(null, DNLGameEvents.BLOCK_CONTENT_DROPPING.get(), Vec3.atCenterOf(pos));
        }
        if (ContainerDropManager.shouldCancel(pos)) {
            ContainerDropManager.reset();
            ci.cancel();
        }
    }
}