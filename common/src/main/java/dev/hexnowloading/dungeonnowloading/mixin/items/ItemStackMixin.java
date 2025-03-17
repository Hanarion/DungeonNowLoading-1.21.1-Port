package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void ignoreAnimationNBT(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if (stack1.getItem() instanceof DNLAnimatedItem<?> animatedItem1 && stack2.getItem() instanceof DNLAnimatedItem<?> animatedItem2) {
            // Ensure UUID exists before comparison
            UUID uuid1 = animatedItem1.getItemUUID(stack1);
            UUID uuid2 = animatedItem2.getItemUUID(stack2);

            // If either stack is missing a UUID, generate one
            if (uuid1 == null) {
                animatedItem1.ensureItemUUID(stack1);
                uuid1 = animatedItem1.getItemUUID(stack1);
            }
            if (uuid2 == null) {
                animatedItem2.ensureItemUUID(stack2);
                uuid2 = animatedItem2.getItemUUID(stack2);
            }

            // Check if both items have the same UUID
            if (uuid1 != null && uuid2 != null && uuid1.equals(uuid2)) {
                cir.setReturnValue(true); // Forces items to match if only animation NBT is different
            }
        }
    }
}
