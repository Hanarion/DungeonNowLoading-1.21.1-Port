package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void ignoreAnimationNBT(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if (stack1.getItem() instanceof DNLAnimatedItem<?> && stack2.getItem() instanceof DNLAnimatedItem<?>) {
            if (stack1.is(stack2.getItem())) {
                cir.setReturnValue(true); // Forces items to match if only "Animations" changed
            }
        }
    }
}
