package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class MimiclingReachServerGameModeMixin {
    @Shadow protected ServerPlayer player;

    @Redirect(
            method = "handleBlockBreakAction",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/36.0D:D"
            )
    )
    private double dnl$extendMimiclingBlockBreakDistance() {
        return MimiclingFoodEffects.hasExtendedReach(this.player) ? MimiclingFoodEffects.getMimiclingReachValidationDistanceSqr(this.player) : 36.0D;
    }
}
