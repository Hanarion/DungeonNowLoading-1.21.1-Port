package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import dev.hexnowloading.dungeonnowloading.item.ScrapItem;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackBreakProtectionMixin {

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), cancellable = true)
    private void dnl$convertToScrapOnBreak(int amount, ServerLevel serverLevel, ServerPlayer player, Consumer<Item> onBroken, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        if (!self.isDamageableItem()) return;
        if (MimiclingItem.tryTransformBrokenToolFormToBase(self, player)) {
            ci.cancel();
            return;
        }

        // Only intercept if the item has our Break Protection enchantment
        if (EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(serverLevel, DNLEnchantments.BREAK_PROTECTION), self) <= 0) return;

        // Fire vanilla break callback (plays animation/sound)
        if (onBroken != null) {
            onBroken.accept(self.getItem());
        }

        // Build the scrap with a snapshot of the original item
        ItemStack scrap = ScrapItem.ofOriginal(self.copy());

        boolean replaced = false;
        if (player != null) {
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
        }

        // Clear the original stack and cancel vanilla shrink()
        self.setCount(0);
        ci.cancel();
    }
}
