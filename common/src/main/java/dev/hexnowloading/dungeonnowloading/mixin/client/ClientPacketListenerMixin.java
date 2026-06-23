package dev.hexnowloading.dungeonnowloading.mixin.client;


import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.MimicartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    /**
     * Replace the overlay message "Press Left Shift to Dismount" with
     * "Derail the Mimicart to Dismount" when the player mounts a Mimicart.
     */
    // 1.21: ClientPacketListener no longer holds a `minecraft` field (it's on the parent), so use
    // Minecraft.getInstance() instead of a @Shadow.
    @ModifyArg(
            method = "handleSetEntityPassengersPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"
            ),
            index = 0
    )
    private Component dnl$replaceMountOverlay(Component original) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Entity vehicle = mc.player.getVehicle();
            if (vehicle instanceof GarholdEntity || vehicle instanceof BrokenGarholdEntity) {
                return Component.translatable("entity.dungeonnowloading.garhold.dismount_hint");
            } else if (vehicle instanceof MimicartEntity) {
                return Component.translatable("entity.dungeonnowloading.mimicart.dismount_hint");
            }
        }
        return original;
    }
}
