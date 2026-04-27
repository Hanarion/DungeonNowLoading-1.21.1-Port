package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class MimiclingBlockDropsMixin {
    @Inject(method = "getDrops", at = @At("RETURN"), cancellable = true)
    private void dnl$transformMimiclingDrops(LootParams.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        cir.setReturnValue(MimiclingFoodEffects.transformBlockDrops((BlockState)(Object)this, builder, cir.getReturnValue()));
    }

    @Inject(method = "spawnAfterBreak", at = @At("HEAD"), cancellable = true)
    private void dnl$suppressMimiclingOreExperience(ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (dropExperience && MimiclingFoodEffects.shouldSuppressVanillaBlockExperience((BlockState)(Object)this, stack)) {
            ci.cancel();
        }
    }
}
