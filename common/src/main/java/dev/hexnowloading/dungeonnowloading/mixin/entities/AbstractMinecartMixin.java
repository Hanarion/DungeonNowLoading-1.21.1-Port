package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.block.ChainedRailBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {
    @Shadow
    protected abstract double getMaxSpeed();

    @Inject(method = "moveAlongTrack", at = {@At("HEAD"), @At("TAIL")})
    private void dungeonnowloading$applyChainedRailSpeed(net.minecraft.core.BlockPos pos, BlockState state, CallbackInfo ci) {
        ChainedRailBlock.applyMinecartSpeed((AbstractMinecart) (Object) this, state, this.getMaxSpeed());
    }

    // 1.21: moveAlongTrack detects powered rails via `instanceof PoweredRailBlock` rather than a
    // BlockState.is(Block) call, and DeepsteelMountedPoweredRailBlock extends PoweredRailBlock, so
    // the deepsteel mounted powered rail already gets the vanilla speed boost. The old @Redirect
    // (BlockState.is(Block)) no longer has a target and is unnecessary — removed.
}
