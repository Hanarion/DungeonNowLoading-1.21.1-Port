package dev.hexnowloading.dungeonnowloading.mixin;

import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockBurnManager;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockDestructionManager;
import dev.hexnowloading.dungeonnowloading.util.event_managers.ContainerDropManager;
import dev.hexnowloading.dungeonnowloading.util.event_managers.ExplosionDestructionManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "tick",
            at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        // Reset all event flags at the start of each tick
        BlockDestructionManager.reset();
        BlockBurnManager.reset();
        ContainerDropManager.reset();
        ExplosionDestructionManager.reset();
    }
}
