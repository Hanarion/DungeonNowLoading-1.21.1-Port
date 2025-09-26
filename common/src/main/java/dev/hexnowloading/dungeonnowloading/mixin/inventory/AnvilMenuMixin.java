package dev.hexnowloading.dungeonnowloading.mixin.inventory;

import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void dnl$blockBreakProtectionOnMendstone(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu)(Object)this;
        ItemStack left = self.getSlot(0).getItem();
        ItemStack right = self.getSlot(1).getItem();
        if (!right.isEmpty() && (left.is(DNLItems.MENDSTONE_PICKAXE.get()) || right.is(DNLItems.MENDSTONE_PICKAXE.get()))) {
            self.getSlot(2).set(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
