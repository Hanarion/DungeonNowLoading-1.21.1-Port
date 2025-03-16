package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ScorcherHeatMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!player.level().isClientSide) {
            boolean isShooting = isPlayerShooting(player);

            if (!isShooting) {
                int currentHeat = Services.DATA.getScorcherHeat(player);
                if (currentHeat > 0) {
                    Services.DATA.setScorcherHeat(player, currentHeat - 1);
                }
            }
        }
    }

    // ✅ Helper method to check if the player is using a Scorcher in "Shooting" animation
    @Unique
    private boolean isPlayerShooting(Player player) {
        // ✅ Check main hand first
        ItemStack mainHandItem = player.getMainHandItem();
        System.out.println(mainHandItem);
        if (mainHandItem.getItem() instanceof ScorcherItem &&
                ItemAnimationState.isAnimating(mainHandItem, ScorcherItem.ScorcherAnimationState.SCORCHER_SHOOT.getName(), player.level().getGameTime())) {
            return true;
        }

        // ✅ Check offhand as well
        ItemStack offhandItem = player.getOffhandItem();
        return (offhandItem.getItem() instanceof ScorcherItem &&
                ItemAnimationState.isAnimating(offhandItem, ScorcherItem.ScorcherAnimationState.SCORCHER_SHOOT.getName(), player.level().getGameTime()));
    }
}
