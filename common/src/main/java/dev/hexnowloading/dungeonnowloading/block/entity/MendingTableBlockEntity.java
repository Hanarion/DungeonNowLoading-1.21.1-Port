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
import org.jetbrains.annotations.NotNull;

public class MendingTableBlockEntity extends BlockEntity implements MenuProvider, Container {
    private static final int INVENTORY_SIZE = 4; // 0: pickaxe/scrap, 1-2: mats, 3: output
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public MendingTableBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.MENDING_TABLE.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.dungeonnowloading.mending_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new MendingTableMenu(id, inventory, this);
    }

    // Persistence
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
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
    public @NotNull ItemStack getItem(int index) { return items.get(index); }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        ItemStack result = ContainerHelper.removeItem(items, index, count);
        if (!result.isEmpty()) recalc();
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = items.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        items.set(index, ItemStack.EMPTY);
        recalc();
        return stack;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        recalc();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
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

        // Determine tool we are repairing
        boolean isScrap = input.is(DNLItems.ITEM_SCRAPS.get());
        ItemStack inputTool = isScrap ? ScrapItem.getOriginal(input) : input;

        if (isSpecialMendingConversion(input)) {
            boolean hasMendstone = mat1.is(DNLItems.MENDSTONE.get()) || mat2.is(DNLItems.MENDSTONE.get());
            if (!hasMendstone) {
                setChanged();
                return;
            }

            items.set(OUTPUT_SLOT, getSpecialMendingResult(input));
            cachedRepairPercent = 100;
            cachedBasePercent = 100;
            cachedBonusPercent = 0;
            cachedPotentialBasePercent = 100;
            cachedPotentialBonusPercent = 0;
            setChanged();
            return;
        }

        // If it's not a valid tool or has no damage, abort
        if (inputTool.isEmpty() || !isTool(inputTool)) { setChanged(); return; }
        // Special case: Mendstone Pickaxe cannot be repaired at the table
        if (inputTool.is(DNLItems.MENDSTONE_PICKAXE.get())) { setChanged(); return; }

        int damage = inputTool.getDamageValue();
        if (damage <= 0) { setChanged(); return; }
        int max = inputTool.getMaxDamage();

        int neededPercent = (damage * 100 + max - 1) / max;

        int p1 = percentPerItem(inputTool, mat1);
        int p2 = percentPerItem(inputTool, mat2);
        int potentialBase = p1 + p2;
        if (potentialBase <= 0) { setChanged(); return; }

        boolean hasToolMat = (!mat1.isEmpty() && isToolRepairIngredient(inputTool, mat1)) || (!mat2.isEmpty() && isToolRepairIngredient(inputTool, mat2));
        boolean hasDuriteOrMendstone = (!mat1.isEmpty() && (mat1.is(DNLItems.DURITE.get()) || mat1.is(DNLItems.MENDSTONE.get()))) || (!mat2.isEmpty() && (mat2.is(DNLItems.DURITE.get()) || mat2.is(DNLItems.MENDSTONE.get())));
        int potentialBonus = (hasToolMat && hasDuriteOrMendstone) ? BONUS_PERCENT : 0;

        cachedPotentialBasePercent = Math.min(potentialBase, 100);
        cachedPotentialBonusPercent = potentialBonus;

        int appliedBase = Math.min(potentialBase, neededPercent);
        int appliedBonus = Math.min(potentialBonus, Math.max(0, neededPercent - appliedBase));
        int appliedTotal = appliedBase + appliedBonus;
        int repairAmount = (appliedTotal * max + 99) / 100;
        int newDamage = Math.max(0, damage - repairAmount);

        ItemStack result = inputTool.copy();
        result.setDamageValue(newDamage);
        items.set(OUTPUT_SLOT, result);
        cachedBasePercent = appliedBase;
        cachedBonusPercent = appliedBonus;
        cachedRepairPercent = Math.min(appliedTotal, 100);
        setChanged();
    }

    public void applyRepair() {
        ItemStack input = items.get(PICKAXE_SLOT);
        if (input.isEmpty()) return;

        if (isSpecialMendingConversion(input)) {
            ItemStack mat1 = items.get(MAT_SLOT_1);
            ItemStack mat2 = items.get(MAT_SLOT_2);

            if (mat1.is(DNLItems.MENDSTONE.get())) {
                mat1.shrink(1);
            } else if (mat2.is(DNLItems.MENDSTONE.get())) {
                mat2.shrink(1);
            } else {
                return;
            }

            items.set(PICKAXE_SLOT, ItemStack.EMPTY);
            items.set(OUTPUT_SLOT, ItemStack.EMPTY);
            recalc();
            return;
        }

        if (input.isEmpty() || cachedRepairPercent <= 0) return;

        // Reconstruct tool from scraps if needed
        boolean isScrap = input.is(DNLItems.ITEM_SCRAPS.get());
        ItemStack inputTool = isScrap ? ScrapItem.getOriginal(input) : input;
        if (inputTool.isEmpty()) return;

        ItemStack mat1 = items.get(MAT_SLOT_1);
        ItemStack mat2 = items.get(MAT_SLOT_2);

        int p1 = percentPerItem(inputTool, mat1);
        int p2 = percentPerItem(inputTool, mat2);
        boolean s1ToolMat = !mat1.isEmpty() && isToolRepairIngredient(inputTool, mat1);
        boolean s1DuriteOrMendstone = !mat1.isEmpty() && (mat1.is(DNLItems.DURITE.get()) || mat1.is(DNLItems.MENDSTONE.get()));
        boolean s2ToolMat = !mat2.isEmpty() && isToolRepairIngredient(inputTool, mat2);
        boolean s2DuriteOrMendstone = !mat2.isEmpty() && (mat2.is(DNLItems.DURITE.get()) || mat2.is(DNLItems.MENDSTONE.get()));

        // Minimal base consumption in slot order
        int remaining = cachedBasePercent;
        boolean c1 = false, c2 = false;
        if (remaining > 0 && p1 > 0) { c1 = true; remaining = Math.max(0, remaining - p1); }
        if (remaining > 0 && p2 > 0) { c2 = true; }


        if (cachedBonusPercent > 0) {
            boolean haveToolMat = (c1 && s1ToolMat) || (c2 && s2ToolMat);
            if (!haveToolMat) {
                if (!c1 && p1 > 0 && s1ToolMat) c1 = true; else if (!c2 && p2 > 0 && s2ToolMat) c2 = true;
            }
            // Compute durite/mendstone coverage after possibly adding tool-mat
            boolean haveDurite = (c1 && s1DuriteOrMendstone) || (c2 && s2DuriteOrMendstone);
            if (!haveDurite) {
                if (!c1 && p1 > 0 && s1DuriteOrMendstone) c1 = true; else if (!c2 && p2 > 0 && s2DuriteOrMendstone) c2 = true;
            }
        }

        if (c1) mat1.shrink(1);
        if (c2) mat2.shrink(1);

        items.set(PICKAXE_SLOT, ItemStack.EMPTY);
        items.set(OUTPUT_SLOT, ItemStack.EMPTY);
        recalc();
    }

    private ItemStack getSpecialMendingResult(ItemStack stack) {
        if (stack.is(DNLItems.MUSIC_DISC_BROKEN_AOTSUGI.get())) {
            return new ItemStack(DNLItems.MUSIC_DISC_AOTSUGI.get());
        }
        return ItemStack.EMPTY;
    }

    private boolean isSpecialMendingConversion(ItemStack stack) {
        return stack.is(DNLItems.MUSIC_DISC_BROKEN_AOTSUGI.get());
    }
}
