package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.MimicartEntity;
import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
    private void beforeItemDrop(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {

        Player player = (Player) (Object) this;
        Level level = player.level();

        if (!level.isClientSide && itemStack.getItem() instanceof DNLAnimatedItem<?> animatedItem) {
            long gameTime = player.level().getGameTime();
            if (ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_ACTIVATED.getName(), gameTime) || ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                animatedItem.playDroppedAnimation(player, itemStack);
            }
        }
    }

    // 1.21 removed Entity.wantsToStopRiding(); dismount-on-sneak is no longer a gateable Entity
    // method (handled in player input/aiStep now). The "block dismount from Garhold/Mimicart"
    // behavior needs a different hook — disabled here to avoid the apply-time "target not found"
    // crash. TODO: re-implement via a LocalPlayer input mixin if the feature is needed.
    /*
    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void dnl$blockDismountOnMimicart(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player)(Object) this;
        Entity vehicle = self.getVehicle();

        if ((vehicle instanceof GarholdEntity || vehicle instanceof BrokenGarholdEntity || vehicle instanceof MimicartEntity) && !self.getAbilities().instabuild) {
            cir.setReturnValue(false);
        }
    }
    */
}
