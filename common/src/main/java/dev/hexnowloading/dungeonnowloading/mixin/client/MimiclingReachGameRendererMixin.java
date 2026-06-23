package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// 1.21 removed the hardcoded 9.0 pick-distance constant; GameRenderer.pick(float) now reads the
// entity reach from LocalPlayer.entityInteractionRange(). Redirect that call to hand back the
// Mimicling extended (linear) reach when the effect is active.
@Mixin(GameRenderer.class)
public class MimiclingReachGameRendererMixin {
    @Redirect(method = "pick(F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;entityInteractionRange()D"))
    private double dnl$extendMimiclingEntityPickDistance(LocalPlayer localPlayer) {
        double original = localPlayer.entityInteractionRange();
        Player player = Minecraft.getInstance().player;
        return player != null && MimiclingFoodEffects.hasExtendedReach(player)
                ? Math.max(original, MimiclingFoodEffects.getMimiclingReachDistance(player))
                : original;
    }
}
