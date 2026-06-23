package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class MimiclingReachServerGameModeMixin {
    @Shadow
    protected ServerPlayer player;

    // 1.21: handleBlockBreakAction validates reach via ServerPlayer.canInteractWithBlock(pos, buffer)
    // instead of the old hardcoded 36.0D field. Redirect that call so the Mimicling extended
    // block-break reach is honoured (custom squared-distance check vs the validation range).
    @Redirect(
            method = "handleBlockBreakAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;canInteractWithBlock(Lnet/minecraft/core/BlockPos;D)Z"
            )
    )
    private boolean dnl$extendMimiclingBlockBreakDistance(ServerPlayer serverPlayer, BlockPos pos, double buffer) {
        if (MimiclingFoodEffects.hasExtendedReach(serverPlayer)) {
            Vec3 eye = serverPlayer.getEyePosition();
            double dx = eye.x - (pos.getX() + 0.5);
            double dy = eye.y - (pos.getY() + 0.5);
            double dz = eye.z - (pos.getZ() + 0.5);
            return dx * dx + dy * dy + dz * dz <= MimiclingFoodEffects.getMimiclingReachValidationDistanceSqr(serverPlayer);
        }
        return serverPlayer.canInteractWithBlock(pos, buffer);
    }
}
