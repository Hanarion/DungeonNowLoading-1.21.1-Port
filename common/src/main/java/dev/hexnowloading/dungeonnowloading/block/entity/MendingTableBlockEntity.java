package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.menu.MendingTableMenu;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.item.ScrapItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MendingTableBlockEntity extends BlockEntity implements MenuProvider, Container {
    private static final int INVENTORY_SIZE = 4; // 0: pickaxe/scrap, 1-2: mats, 3: output
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public MendingTableBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.MENDING_TABLE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.dungeonnowloading.mending_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MendingTableMenu(id, inventory, this);
    }

    // Persistence
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public int getContainerSize() { return INVENTORY_SIZE; }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) if (!stack.isEmpty()) return false; return true;
    }

    @Override
    public ItemStack getItem(int index) { return items.get(index); }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack result = ContainerHelper.removeItem(items, index, count);
        if (!result.isEmpty()) recalc();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = items.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        items.set(index, ItemStack.EMPTY);
        recalc();
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        recalc();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() { items.clear(); recalc(); }

    private int cachedRepairPercent = 0; // applied total (base+bonus)
    int cachedBasePercent = 0;   // applied base
    int cachedBonusPercent = 0;  // applied bonus
    private int cachedPotentialBasePercent = 0; // potential base (uncapped by need)
    private int cachedPotentialBonusPercent = 0; // potential bonus (0 or 10)
    private int cachedNeededPercent = 0; // percent needed to fully repair
    private static final int PICKAXE_SLOT = 0;
    private static final int MAT_SLOT_1 = 1;
    private static final int MAT_SLOT_2 = 2;
    private static final int OUTPUT_SLOT = 3;
    private static final int DURITE_PERCENT = 20;
    private static final int MENDSTONE_PERCENT = 50;
    private static final int TOOL_MATERIAL_PERCENT = 40;
    private static final int BONUS_PERCENT = 10;

    public int getPotentialBasePercent() { return cachedPotentialBasePercent; }
    public int getPotentialBonusPercent() { return cachedPotentialBonusPercent; }

    private boolean isTool(ItemStack stack) { return stack.isDamageableItem(); }

    private boolean isToolRepairIngredient(ItemStack tool, ItemStack ingredientStack) {
        if (tool.isEmpty() || ingredientStack.isEmpty()) return false;
        if (!tool.isDamageableItem()) return false;
        return tool.getItem().isValidRepairItem(tool, ingredientStack);
    }

    private int percentPerItem(ItemStack tool, ItemStack stack) {
        if (stack.is(DNLItems.MENDSTONE.get())) return MENDSTONE_PERCENT;
        if (stack.is(DNLItems.DURITE.get())) return DURITE_PERCENT;
        if (!tool.isEmpty() && isToolRepairIngredient(tool, stack)) return TOOL_MATERIAL_PERCENT;
        return 0;
    }

    private void recalc() {
        ItemStack input = items.get(PICKAXE_SLOT);
        ItemStack mat1 = items.get(MAT_SLOT_1);
        ItemStack mat2 = items.get(MAT_SLOT_2);
        items.set(OUTPUT_SLOT, ItemStack.EMPTY);
        cachedRepairPercent = 0;
        cachedBasePercent = 0;
        cachedBonusPercent = 0;
        cachedPotentialBasePercent = 0;
        cachedPotentialBonusPercent = 0;
        cachedNeededPercent = 0;

        // Special handling: Item Scraps -> restore original using Mendstone(s) only
        if (input.is(DNLItems.ITEM_SCRAPS.get())) {
            ItemStack original = ScrapItem.getOriginal(input);
            if (original.isEmpty() || !original.isDamageableItem()) { setChanged(); return; }
            int damage = original.getDamageValue();
            int max = original.getMaxDamage();
            if (damage <= 0) { setChanged(); return; }

            int neededPercent = (damage * 100 + max - 1) / max;
            cachedNeededPercent = Math.min(neededPercent, 100);

            int potentialBase = 0;
            if (!mat1.isEmpty() && mat1.is(DNLItems.MENDSTONE.get())) potentialBase += MENDSTONE_PERCENT;
            if (!mat2.isEmpty() && mat2.is(DNLItems.MENDSTONE.get())) potentialBase += MENDSTONE_PERCENT;
            if (potentialBase <= 0) { setChanged(); return; }

            cachedPotentialBasePercent = Math.min(potentialBase, 100);
            cachedPotentialBonusPercent = 0; // no combo bonus for scraps

            int appliedBase = Math.min(potentialBase, neededPercent);
            int repairAmount = (appliedBase * max + 99) / 100; // ceil
            int newDamage = Math.max(0, damage - repairAmount);
            ItemStack result = original.copy();
            result.setDamageValue(newDamage);
            items.set(OUTPUT_SLOT, result);
            cachedBasePercent = appliedBase;
            cachedBonusPercent = 0;
            cachedRepairPercent = Math.min(appliedBase, 100);
            setChanged();
            return;
        }

        // Regular damaged tools flow
        if (input.isEmpty() || !isTool(input)) { setChanged(); return; }
        int damage = input.getDamageValue();
        if (damage <= 0) { setChanged(); return; }
        int max = input.getMaxDamage();

        int neededPercent = (damage * 100 + max - 1) / max;
        cachedNeededPercent = Math.min(neededPercent, 100);

        int potentialBase = percentPerItem(input, mat1) + percentPerItem(input, mat2);
        if (potentialBase <= 0) { setChanged(); return; }

        boolean hasToolMat = (!mat1.isEmpty() && isToolRepairIngredient(input, mat1)) || (!mat2.isEmpty() && isToolRepairIngredient(input, mat2));
        boolean hasDuriteOrMendstone = (!mat1.isEmpty() && (mat1.is(DNLItems.DURITE.get()) || mat1.is(DNLItems.MENDSTONE.get()))) || (!mat2.isEmpty() && (mat2.is(DNLItems.DURITE.get()) || mat2.is(DNLItems.MENDSTONE.get())));

        int potentialBonus = (hasToolMat && hasDuriteOrMendstone) ? BONUS_PERCENT : 0;

        cachedPotentialBasePercent = Math.min(potentialBase, 100);
        cachedPotentialBonusPercent = potentialBonus; // keep full potential bonus (0 or 10)
        int appliedBase = Math.min(potentialBase, neededPercent);
        int appliedBonus = Math.min(potentialBonus, Math.max(0, neededPercent - appliedBase));
        int appliedTotal = appliedBase + appliedBonus;
        int repairAmount = (appliedTotal * max + 99) / 100; // ceil
        int newDamage = Math.max(0, damage - repairAmount);
        ItemStack result = input.copy();
        result.setDamageValue(newDamage);
        items.set(OUTPUT_SLOT, result);
        cachedBasePercent = appliedBase;
        cachedBonusPercent = appliedBonus;
        cachedRepairPercent = Math.min(appliedTotal, 100);
        setChanged();
    }

    public void applyRepair() {
        ItemStack tool = items.get(PICKAXE_SLOT);
        if (tool.isEmpty() || cachedRepairPercent <= 0) return; // no need to check output slot anymore
        // Consume inputs and clear output
        for(int slot: new int[]{MAT_SLOT_1, MAT_SLOT_2}) {
            ItemStack mat = items.get(slot);
            mat.shrink(1);
            if (mat.getCount() <= 0) items.set(slot, ItemStack.EMPTY);
        }
        items.set(PICKAXE_SLOT, ItemStack.EMPTY);
        items.set(OUTPUT_SLOT, ItemStack.EMPTY);
        recalc();
    }
}
