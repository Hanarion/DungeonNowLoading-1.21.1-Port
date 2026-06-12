package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {
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
