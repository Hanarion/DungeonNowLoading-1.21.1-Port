package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 1.21: isCorrectToolForDrops / getDestroySpeed moved to Item with an added ItemStack param,
// and SwordItem no longer overrides them. Mix into Item and guard on SwordItem so swords can
// mine the web carpet.
@Mixin(Item.class)
public abstract class SwordItemMixin {

    @Inject(method = "isCorrectToolForDrops(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void dnl$isCorrectToolForWebCarpet(ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this instanceof SwordItem || (Object) this instanceof net.minecraft.world.item.ShearsItem) && state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getDestroySpeed(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)F", at = @At("HEAD"), cancellable = true)
    private void dnl$destroySpeedForWebCarpet(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        if (((Object) this instanceof SwordItem || (Object) this instanceof net.minecraft.world.item.ShearsItem) && state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(15.0F);
        }
    }
}
