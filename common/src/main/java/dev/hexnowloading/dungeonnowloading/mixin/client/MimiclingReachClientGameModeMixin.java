package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MimiclingReachClientGameModeMixin {
    @Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
    private void dnl$extendMimiclingPickRange(CallbackInfoReturnable<Float> callbackInfo) {
        Player player = Minecraft.getInstance().player;
        if (player != null && MimiclingFoodEffects.hasExtendedReach(player)) {
            callbackInfo.setReturnValue((float)MimiclingFoodEffects.getMimiclingReachDistance(player));
        }
    }

    @Inject(method = "hasFarPickRange", at = @At("RETURN"), cancellable = true)
    private void dnl$extendMimiclingFarPickRange(CallbackInfoReturnable<Boolean> callbackInfo) {
        Player player = Minecraft.getInstance().player;
        if (player != null && MimiclingFoodEffects.hasExtendedReach(player)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
