package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin {

    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void dnl$isCorrectToolForWebCarpet(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void dnl$destroySpeedForWebCarpet(net.minecraft.world.item.ItemStack stack,
                                              BlockState state,
                                              CallbackInfoReturnable<Float> cir) {
        if (state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(15.0F);
        }
    }
}