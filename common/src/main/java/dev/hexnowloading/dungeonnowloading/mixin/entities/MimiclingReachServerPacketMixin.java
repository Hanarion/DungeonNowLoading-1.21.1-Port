package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class MimiclingReachServerPacketMixin {
    @Shadow public ServerPlayer player;

    @Redirect(
            method = {"handleInteract", "handleUseItemOn"},
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D"
            )
    )
    private double dnl$extendMimiclingInteractionDistance() {
        return MimiclingFoodEffects.hasExtendedReach(this.player) ? MimiclingFoodEffects.getMimiclingReachValidationDistanceSqr(this.player) : ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
    }

    @ModifyConstant(method = "handleUseItemOn", constant = @Constant(doubleValue = 64.0D))
    private double dnl$extendMimiclingUseOnCenterDistance(double original) {
        return MimiclingFoodEffects.hasExtendedReach(this.player) ? MimiclingFoodEffects.getMimiclingReachValidationDistanceSqr(this.player) : original;
    }
}
