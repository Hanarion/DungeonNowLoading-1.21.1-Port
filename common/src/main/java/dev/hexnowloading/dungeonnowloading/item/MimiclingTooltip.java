package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MimiclingTooltip implements TooltipComponent {
    private final NonNullList<ItemStack> items;
    private final List<ActiveFood> activeFoods;
    private final ItemStack previewFood;
    private final List<String> previewFoodLines;
    private final int selectedSlot;
    private final int selectedFoodSlot;
    private final int capacity;

    public MimiclingTooltip(NonNullList<ItemStack> items, List<ActiveFood> activeFoods, int selectedSlot, int capacity) {
        this(items, activeFoods, ItemStack.EMPTY, List.of(), selectedSlot, -1, capacity);
    }

    public MimiclingTooltip(NonNullList<ItemStack> items, List<ActiveFood> activeFoods, int selectedSlot, int selectedFoodSlot, int capacity) {
        this(items, activeFoods, ItemStack.EMPTY, List.of(), selectedSlot, selectedFoodSlot, capacity);
    }

    public MimiclingTooltip(NonNullList<ItemStack> items, List<ActiveFood> activeFoods, ItemStack previewFood, List<String> previewFoodLines, int selectedSlot, int selectedFoodSlot, int capacity) {
        this.items = items;
        this.activeFoods = activeFoods;
        this.previewFood = previewFood;
        this.previewFoodLines = previewFoodLines;
        this.selectedSlot = selectedSlot;
        this.selectedFoodSlot = selectedFoodSlot;
        this.capacity = capacity;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public List<ActiveFood> getActiveFoods() {
        return activeFoods;
    }

    public ItemStack getPreviewFood() {
        return previewFood;
    }

    public List<String> getPreviewFoodLines() {
        return previewFoodLines;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getSelectedFoodSlot() {
        return selectedFoodSlot;
    }

    public int getCapacity() {
        return capacity;
    }

    public record ActiveFood(ItemStack stack, int uses, int maxUses, boolean infinite) {}
}
