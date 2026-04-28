package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFormItem;
import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import dev.hexnowloading.dungeonnowloading.network.packets.C2SMimiclingSelectSlotPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    private CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$scrollMimiclingSlot(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (hoveredSlot != null) {
            ItemStack stack = hoveredSlot.getItem();
            int delta = amount < 0.0D ? 1 : -1;
            ItemStack carriedStack = menu.getCarried();
            boolean changed = carriedStack.isEmpty()
                    ? MimiclingItem.tryScrollSelectedSlot(stack, delta)
                    : MimiclingItem.tryScrollSelectedFoodSlot(stack, carriedStack, delta);
            if (stack.getItem() instanceof MimiclingFormItem && changed) {
                Services.NETWORK.sendToServer(new C2SMimiclingSelectSlotPacket(menu.containerId, hoveredSlot.index, delta));
                cir.setReturnValue(true);
            }
        }
    }
}
