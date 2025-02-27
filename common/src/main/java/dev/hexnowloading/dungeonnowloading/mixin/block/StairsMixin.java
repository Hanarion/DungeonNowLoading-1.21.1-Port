package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.MendingAuraStairBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StairBlock.class) // Replace with the actual class name containing isStairs
public class StairsMixin {

    @Inject(method = "isStairs", at = @At("HEAD"), cancellable = true)
    private static void modifyIsStairs(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof MendingAuraStairBlock) {
            cir.setReturnValue(true);
        }
    }
}

