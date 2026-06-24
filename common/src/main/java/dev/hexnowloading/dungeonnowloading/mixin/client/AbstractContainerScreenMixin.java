package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.client.MimiclingFeedHintHandler;
import dev.hexnowloading.dungeonnowloading.item.MimiclingFormItem;
import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import dev.hexnowloading.dungeonnowloading.network.packets.C2SMimiclingSelectSlotPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> implements ContainerEventHandler {
    @Shadow
    protected T menu;

    @Shadow
    protected Slot hoveredSlot;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected abstract List<Component> getTooltipFromContainerItem(ItemStack stack);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount) {
        Slot hovered = getHoveredSlot(mouseX, mouseY);
        if (hovered != null) {
            ItemStack stack = hovered.getItem();
            int delta = amount < 0.0D ? 1 : -1;
            ItemStack carriedStack = menu.getCarried();
            boolean foodMode = Screen.hasShiftDown();
            boolean changed = foodMode
                    ? MimiclingItem.tryScrollSelectedActiveFoodSlot(stack, delta)
                    : carriedStack.isEmpty()
                    ? MimiclingItem.tryScrollSelectedSlot(stack, delta)
                    : MimiclingItem.tryScrollSelectedFoodSlot(stack, carriedStack, delta);
            if (stack.getItem() instanceof MimiclingFormItem && changed) {
                Services.NETWORK.sendToServer(new C2SMimiclingSelectSlotPacket(menu.containerId, hovered.index, delta, foodMode));
                return true;
            }
        }

        return this.getChildAt(mouseX, mouseY).filter(child -> child.mouseScrolled(mouseX, mouseY, scrollX, amount)).isPresent();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void dungeonnowloading$updateMimiclingFeedHint(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Slot hovered = getHoveredSlot(mouseX, mouseY);
        ItemStack hoveredStack = hovered != null && hovered.hasItem() ? hovered.getItem() : ItemStack.EMPTY;
        MimiclingFeedHintHandler.update(hoveredStack, menu.getCarried(), Minecraft.getInstance().player);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void dungeonnowloading$clearMimiclingFeedHint(CallbackInfo ci) {
        MimiclingFeedHintHandler.clear();
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$renderMimiclingTooltipWithCarriedStack(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            if (stack.getItem() instanceof MimiclingFormItem && Screen.hasShiftDown()) {
                List<Component> tooltip = getTooltipFromContainerItem(stack);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, MimiclingItem.getTooltipImageForSelectedFood(stack), mouseX, mouseY);
                ci.cancel();
                return;
            }
        }

        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            if (stack.getItem() instanceof MimiclingItem && MimiclingItem.trySelectNextOccupiedSlotIfSelectedEmpty(stack)) {
                Services.NETWORK.sendToServer(new C2SMimiclingSelectSlotPacket(menu.containerId, hoveredSlot.index, 0));
            }
        }

        if (!menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            if (stack.getItem() instanceof MimiclingFormItem) {
                if (MimiclingItem.trySelectDedicatedSlot(stack, menu.getCarried())) {
                    Services.NETWORK.sendToServer(new C2SMimiclingSelectSlotPacket(menu.containerId, hoveredSlot.index, 0));
                }
                guiGraphics.renderTooltip(Minecraft.getInstance().font, getTooltipFromContainerItem(stack), MimiclingItem.getTooltipImageForCarried(stack, menu.getCarried()), mouseX, mouseY);
                ci.cancel();
            }
        }
    }

    private Slot getHoveredSlot(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (slot.isActive() && dungeonnowloading$isHovering(slot, mouseX, mouseY)) {
                return slot;
            }
        }

        return null;
    }

    // Renamed (was isHovering) to avoid colliding with AbstractContainerScreen.isHovering, which is
    // protected in 1.21. A same-name private method in the mixin is treated as an overwrite and
    // rejected for reducing visibility ("cannot reduce visibility of PROTECTED target method").
    private boolean dungeonnowloading$isHovering(Slot slot, double mouseX, double mouseY) {
        return mouseX >= leftPos + slot.x
                && mouseX < leftPos + slot.x + 16
                && mouseY >= topPos + slot.y
                && mouseY < topPos + slot.y + 16;
    }
}
