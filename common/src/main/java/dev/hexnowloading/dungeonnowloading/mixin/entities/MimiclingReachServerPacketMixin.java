package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class MimiclingReachServerPacketMixin {
    @Shadow
    public ServerPlayer player;

    // 1.21: handleInteract validates entity-interaction reach via
    // ServerPlayer.canInteractWithEntity(AABB, double) instead of the old hardcoded 36.0D field.
    // Redirect it so the Mimicling extended reach is honoured (custom squared-distance check vs
    // the validation range); otherwise delegate to the original.
    @Redirect(
            method = "handleInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;canInteractWithEntity(Lnet/minecraft/world/phys/AABB;D)Z"
            )
    )
    private boolean dnl$extendMimiclingInteractionDistance(ServerPlayer serverPlayer, AABB aabb, double buffer) {
        if (MimiclingFoodEffects.hasExtendedReach(serverPlayer)) {
            Vec3 eye = serverPlayer.getEyePosition();
            double cx = (aabb.minX + aabb.maxX) * 0.5;
            double cy = (aabb.minY + aabb.maxY) * 0.5;
            double cz = (aabb.minZ + aabb.maxZ) * 0.5;
            double dx = eye.x - cx;
            double dy = eye.y - cy;
            double dz = eye.z - cz;
            return dx * dx + dy * dy + dz * dz <= MimiclingFoodEffects.getMimiclingReachValidationDistanceSqr(serverPlayer);
        }
        return serverPlayer.canInteractWithEntity(aabb, buffer);
    }
}
