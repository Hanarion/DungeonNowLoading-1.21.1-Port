package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.block.ChainedRailBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {
    @Shadow
    protected abstract double getMaxSpeed();

    @Inject(method = "moveAlongTrack", at = {@At("HEAD"), @At("TAIL")})
    private void dungeonnowloading$applyChainedRailSpeed(net.minecraft.core.BlockPos pos, BlockState state, CallbackInfo ci) {
        ChainedRailBlock.applyMinecartSpeed((AbstractMinecart) (Object) this, state, this.getMaxSpeed());
    }

    @Redirect(
            method = "moveAlongTrack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    )
    private boolean dungeonnowloading$treatMountedPoweredRailAsPoweredRail(BlockState state, Block block) {
        if (block == Blocks.POWERED_RAIL && state.is(DNLBlocks.DEEPSTEEL_MOUNTED_POWERED_RAIL.get())) {
            return true;
        }

        return state.is(block);
    }
}
