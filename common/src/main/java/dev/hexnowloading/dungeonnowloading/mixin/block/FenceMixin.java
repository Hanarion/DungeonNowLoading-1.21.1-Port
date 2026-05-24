package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraFenceBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public class FenceMixin {

    @Inject(method = "connectsTo", at = @At("HEAD"), cancellable = true)
    private void modifyConnectsTo(BlockState blockState, boolean bl, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (blockState.getBlock() instanceof MendingAuraFenceBlock || blockState.hasProperty(MendingAuraBlock.FENCE_LIKE) && blockState.getValue(MendingAuraBlock.FENCE_LIKE)) {
            cir.setReturnValue(true);
        }
    }
}
