package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void ignoreAnimationNBT(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if (stack1.getItem() instanceof DNLAnimatedItem<?> && stack2.getItem() instanceof DNLAnimatedItem<?>) {
            cir.setReturnValue(true);
            /*if (stacksAreEqualIgnoringAnimations(stack1, stack2)) {
                cir.setReturnValue(true); // Forces items to match if only "Animations" changed
            }*/
        }
    }

    private static boolean stacksAreEqualIgnoringAnimations(ItemStack stack1, ItemStack stack2) {
        // Basic checks: Same item, same count
        if (stack1.getItem() != stack2.getItem() || stack1.getCount() != stack2.getCount()) {
            return false;
        }

        // Check tags, ignoring "Animations" tag
        CompoundTag tag1 = stack1.getTag();
        CompoundTag tag2 = stack2.getTag();

        if (tag1 == null || tag2 == null) {
            return tag1 == tag2; // If both are null, they match
        }

        // Create copies to avoid modifying original tags
        CompoundTag copy1 = tag1.copy();
        CompoundTag copy2 = tag2.copy();

        copy1.remove("Animations"); // Remove animation data
        copy2.remove("Animations"); // Remove animation data

        return Objects.equals(copy1, copy2);
    }
}
