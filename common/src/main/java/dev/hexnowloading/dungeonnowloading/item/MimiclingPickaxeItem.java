package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class MimiclingPickaxeItem extends PickaxeItem implements MimiclingFormItem {
    public MimiclingPickaxeItem(Properties properties) {
        this(Tiers.DIAMOND, properties);
    }

    public MimiclingPickaxeItem(Tier tier, Properties properties) {
        super(tier, 1, -2.8F, properties);
    }

    @Override
    public String getMimiclingForm() {
        return MimiclingItem.getPickaxeForm();
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
