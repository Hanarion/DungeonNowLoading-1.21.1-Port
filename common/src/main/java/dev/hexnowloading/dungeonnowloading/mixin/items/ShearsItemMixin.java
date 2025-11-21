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

    /**
     * Make shears behave “naturally” when mining Web Carpet:
     * - Consume durability like vanilla
     * - Return true so the vanilla tool logic is happy
     */
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

    /**
     * Treat Web Carpet as a “correct tool” target for shears, like cobweb/tripwire.
     */
    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void dnl$isCorrectToolForWebCarpet(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Give shears a good destroy speed on Web Carpet.
     * Here I match vines/glow_lichen at 2.0F.
     * If you want “cobweb fast”, change to 15.0F.
     */
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void dnl$destroySpeedForWebCarpet(ItemStack stack,
                                              BlockState state,
                                              CallbackInfoReturnable<Float> cir) {
        if (state.is(DNLBlocks.WEB_CARPET.get())) {
            cir.setReturnValue(15.0F);
        }
    }
}
