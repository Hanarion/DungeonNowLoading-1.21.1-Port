package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class MimiclingTooltip implements TooltipComponent {
    private final NonNullList<ItemStack> items;
    private final int selectedSlot;
    private final int capacity;

    public MimiclingTooltip(NonNullList<ItemStack> items, int selectedSlot, int capacity) {
        this.items = items;
        this.selectedSlot = selectedSlot;
        this.capacity = capacity;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getCapacity() {
        return capacity;
    }
}
