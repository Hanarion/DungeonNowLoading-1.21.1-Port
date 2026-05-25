package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraPaneBlock;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraWallBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WallBlock.class)
public class WallMixin {

    @Inject(method = "connectsTo", at = @At("HEAD"), cancellable = true)
    private void modifyConnectsTo(BlockState state, boolean hasSolidSide, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();

        if (block instanceof MendingAuraWallBlock || block instanceof MendingAuraPaneBlock || state.hasProperty(MendingAuraBlock.WALL_LIKE) && state.getValue(MendingAuraBlock.WALL_LIKE) || state.hasProperty(MendingAuraBlock.PANE_LIKE) && state.getValue(MendingAuraBlock.PANE_LIKE)) {
            cir.setReturnValue(true);
        }
    }
}
