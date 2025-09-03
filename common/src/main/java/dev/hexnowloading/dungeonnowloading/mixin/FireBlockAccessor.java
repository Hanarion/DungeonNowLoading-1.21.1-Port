package dev.hexnowloading.dungeonnowloading.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor to invoke the private FireBlock#setFlammable method so we can register
 * mod block flammability after our blocks are registered (instead of editing vanilla bootstrap).
 */
@Mixin(FireBlock.class)
public interface FireBlockAccessor {
    @Invoker("setFlammable")
    void dungeonnowloading$setFlammable(Block block, int encouragement, int flammability);
}

