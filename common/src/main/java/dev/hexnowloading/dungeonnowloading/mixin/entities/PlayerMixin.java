package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
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
}
