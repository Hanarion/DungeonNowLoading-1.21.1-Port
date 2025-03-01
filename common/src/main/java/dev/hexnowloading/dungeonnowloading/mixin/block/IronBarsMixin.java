package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.MendingAuraPaneBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronBarsBlock.class)
public class IronBarsMixin {

    @Inject(method = "attachsTo", at = @At("HEAD"), cancellable = true)
    private void injectAttachsTo(BlockState state, boolean condition, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof MendingAuraPaneBlock) {
            cir.setReturnValue(true);
        }
    }
}

