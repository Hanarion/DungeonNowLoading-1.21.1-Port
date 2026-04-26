package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class MimiclingShovelItem extends ShovelItem implements MimiclingFormItem {
    public MimiclingShovelItem(Properties properties) {
        this(Tiers.DIAMOND, properties);
    }

    public MimiclingShovelItem(Tier tier, Properties properties) {
        super(tier, 1.5F, -3.0F, properties);
    }

    @Override
    public String getMimiclingForm() {
        return MimiclingItem.getShovelForm();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack carriedStack, Slot slot, ClickAction clickAction, Player player, net.minecraft.world.entity.SlotAccess carriedSlot) {
        return MimiclingItem.tryTemporarilyOpenForInventoryFeed(stack, carriedStack, slot, clickAction, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        MimiclingItem.tickMimiclingInventory(stack, level);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return MimiclingItem.isMimiclingFoil(stack);
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        MimiclingItem.onMimiclingDestroyed(itemEntity);
    }
}
