package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MimiclingPlayerMixin {
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void dnl$removeMimiclingUnderwaterMiningPenalty(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player player = (Player)(Object)this;
        float multiplier = MimiclingFoodEffects.getUnderwaterMiningSpeedMultiplier(player);
        if (player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER) && player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SUBMERGED_MINING_SPEED) < 1.0D && multiplier > 1.0F) {
            cir.setReturnValue(cir.getReturnValue() * multiplier);
        }
    }
}
