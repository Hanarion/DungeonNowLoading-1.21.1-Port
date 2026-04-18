package dev.hexnowloading.dungeonnowloading.menu;

import dev.hexnowloading.dungeonnowloading.block.entity.MendingTableBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLMenuTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import dev.hexnowloading.dungeonnowloading.item.ScrapItem;

public class MendingTableMenu extends AbstractContainerMenu {
    public static final int PICKAXE_SLOT = 0;
    public static final int DURITE_SLOT_1 = 1;
    public static final int DURITE_SLOT_2 = 2;
    public static final int OUTPUT_SLOT = 3;
    private final Container container;
    private final ContainerLevelAccess access;
    private int clientBasePercent = 0;
    private int clientBonusPercent = 0;

    public MendingTableMenu(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(4));
    }

    public MendingTableMenu(int id, Inventory playerInv, Container container) {
        super(DNLMenuTypes.MENDING_TABLE.get(), id);
        this.container = container;
        if (container instanceof MendingTableBlockEntity be && be.getLevel() != null) {
            this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
        } else {
            this.access = ContainerLevelAccess.NULL;
        }
        container.startOpen(playerInv.player);

        this.addSlot(new Slot(container, PICKAXE_SLOT, 30, 45) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return stack.isDamageableItem() || stack.is(DNLItems.ITEM_SCRAPS.get()) || stack.is(DNLItems.MUSIC_DISC_BROKEN_AOTSUGI.get()); }
            @Override public void setChanged() { super.setChanged(); broadcastChanges(); }
        });
        this.addSlot(new Slot(container, DURITE_SLOT_1, 56, 33) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) {
                return true;
            }
            @Override public void setChanged() { super.setChanged(); broadcastChanges(); }
        });
        this.addSlot(new Slot(container, DURITE_SLOT_2, 56, 57) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) {
                return true;
            }
            @Override public void setChanged() { super.setChanged(); broadcastChanges(); }
        });

        this.addSlot(new Slot(container, OUTPUT_SLOT, 124, 45) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public void onTake (Player player, ItemStack stack) {
                super.onTake(player, stack);
                consumeInputs();
                access.execute((level, pos) -> {
                    level.playSound(
                            null,
                            pos,
                            DNLSounds.MENDING_TABLE_MEND.get(),
                            SoundSource.BLOCKS,
                            1.0F,
                            1.0F);
                });
            }
        });

        int invX = 8;
        int invY = 105;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, invX + col * 18, invY + row * 18));
            }
        }
        int hotbarY = invY + 58;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, invX + col * 18, hotbarY));
        }

        this.addDataSlot(new DataSlot() {
            @Override public int get() { return (container instanceof MendingTableBlockEntity be) ? be.getPotentialBasePercent() : 0; }
            @Override public void set(int value) { clientBasePercent = value; }
        });
        this.addDataSlot(new DataSlot() {
            @Override public int get() { return (container instanceof MendingTableBlockEntity be) ? be.getPotentialBonusPercent() : 0; }
            @Override public void set(int value) { clientBonusPercent = value; }
        });
    }

    public int getBasePercent() {
        if (container instanceof MendingTableBlockEntity be) return be.getPotentialBasePercent();
        return clientBasePercent;
    }

    public int getBonusPercent() {
        if (container instanceof MendingTableBlockEntity be) return be.getPotentialBonusPercent();
        return clientBonusPercent;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.access == ContainerLevelAccess.NULL || stillValid(access, player, DNLBlocks.MENDING_TABLE.get());
    }

    private boolean isAllowedMaterial(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ItemStack inputItem = this.getSlot(PICKAXE_SLOT).getItem();
        boolean isScrap = inputItem.is(DNLItems.ITEM_SCRAPS.get());
        if (isScrap) {
            ItemStack original = ScrapItem.getOriginal(inputItem);
            if (!original.isEmpty()) {
                if (stack.is(DNLItems.DURITE.get()) || stack.is(DNLItems.MENDSTONE.get())) return true;
                return original.getItem().isValidRepairItem(original, stack);
            }
            return stack.is(DNLItems.DURITE.get()) || stack.is(DNLItems.MENDSTONE.get());
        }
        if (stack.is(DNLItems.DURITE.get()) || stack.is(DNLItems.MENDSTONE.get())) return true;
        if (!inputItem.isEmpty()) {
            return inputItem.getItem().isValidRepairItem(inputItem, stack);
        }
        return false;
    }

    private void consumeInputs() {
        if (container instanceof MendingTableBlockEntity be) {
            be.applyRepair();
        } else {
            container.setItem(PICKAXE_SLOT, ItemStack.EMPTY);
            container.setItem(DURITE_SLOT_1, ItemStack.EMPTY);
            container.setItem(DURITE_SLOT_2, ItemStack.EMPTY);
            container.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
        }
        broadcastChanges();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            int containerSlots = 4;
            if (index == OUTPUT_SLOT) {
                // Capture result before moving
                result = stack.copy();
                // Move the actual stack (not a copy) to player inventory
                if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY; // no space -> do nothing
                }
                // Now consume inputs (clears input + output slots)
                slot.onTake(player, stack); // triggers consumeInputs()
                return result;
            }
            result = stack.copy();
            if (index < containerSlots) { // from container (non-output)
                if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else { // from player inventory
                if (stack.isDamageableItem() || stack.is(DNLItems.ITEM_SCRAPS.get()) || stack.is(DNLItems.MUSIC_DISC_BROKEN_AOTSUGI.get())) {
                    if (!this.moveItemStackTo(stack, PICKAXE_SLOT, PICKAXE_SLOT + 1, false)) return ItemStack.EMPTY;
                } else if (isAllowedMaterial(stack)) {
                    if (!this.moveItemStackTo(stack, DURITE_SLOT_1, DURITE_SLOT_2 + 1, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return result;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

}
