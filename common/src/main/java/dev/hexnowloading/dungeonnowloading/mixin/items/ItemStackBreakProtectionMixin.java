package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.ScrapItem;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackBreakProtectionMixin {

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), cancellable = true)
    private void dnl$convertToScrapOnBreak(int amount, LivingEntity entity, Consumer<LivingEntity> onBroken, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        if (!self.isDamageableItem()) return;
        // Only intercept if the item has our Break Protection enchantment
        if (EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.BREAK_PROTECTION.get(), self) <= 0) return;

        // Fire vanilla break callback (plays animation/sound via broadcastBreakEvent)
        if (entity != null && onBroken != null) {
            onBroken.accept(entity);
        }

        // Build the scrap with a snapshot of the original item
        ItemStack scrap = ScrapItem.ofOriginal(self.copy());

        boolean replaced = false;
        if (entity instanceof Player player) {
            // Try replace in main/off hand first
            if (player.getMainHandItem() == self) {
                player.setItemInHand(InteractionHand.MAIN_HAND, scrap);
                replaced = true;
            } else if (player.getOffhandItem() == self) {
                player.setItemInHand(InteractionHand.OFF_HAND, scrap);
                replaced = true;
            } else {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (player.getItemBySlot(slot) == self) {
                        player.setItemSlot(slot, scrap);
                        replaced = true;
                        break;
                    }
                }
            }
            if (!replaced) {
                // Fallback: try to add to inventory, otherwise drop
                if (!player.getInventory().add(scrap)) {
                    player.drop(scrap, false);
                }
            }
        } else if (entity != null) {
            entity.spawnAtLocation(scrap);
        }

        // Clear the original stack and cancel vanilla shrink()
        self.setCount(0);
        ci.cancel();
    }
}
