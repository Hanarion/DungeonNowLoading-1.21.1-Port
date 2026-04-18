package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public abstract class ShearsItemMixin {

    @Inject(method = "mineBlock", at = @At("HEAD"), cancellable = true)
    private void dnl$mineWebCarpet(ItemStack stack,
                                   Level level,
                                   BlockState state,
                                   BlockPos pos,
                                   LivingEntity entity,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (!state.is(DNLBlocks.WEB_CARPET.get())) {
            return;
        }

        if (!level.isClientSide && !state.is(BlockTags.FIRE)) {
            stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void dnl$isCorrectToolForWebCarpet(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(true);
        }
    }
    
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void dnl$destroySpeedForWebCarpet(ItemStack stack,
                                              BlockState state,
                                              CallbackInfoReturnable<Float> cir) {
        if (state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(15.0F);
        }
    }
}
