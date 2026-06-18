package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
public class MimiclingReachGameRendererMixin {
    @ModifyConstant(method = "pick", constant = @Constant(doubleValue = 9.0D))
    private double dnl$extendMimiclingEntityPickDistance(double original) {
        Player player = Minecraft.getInstance().player;
        return player != null && MimiclingFoodEffects.hasExtendedReach(player) ? MimiclingFoodEffects.getMimiclingReachDistanceSqr(player) : original;
    }
}
