package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class MimiclingHoeItem extends HoeItem implements MimiclingFormItem {
    public MimiclingHoeItem(Properties properties) {
        this(Tiers.DIAMOND, properties);
    }

    public MimiclingHoeItem(Tier tier, Properties properties) {
        super(tier, getAttackDamageBonus(tier), getAttackSpeed(tier), properties);
    }

    private static int getAttackDamageBonus(Tier tier) {
        if (tier == Tiers.STONE) {
            return -1;
        }
        if (tier == Tiers.IRON) {
            return -2;
        }
        if (tier == Tiers.DIAMOND) {
            return -3;
        }
        if (tier == Tiers.NETHERITE) {
            return -4;
        }
        return 0;
    }

    private static float getAttackSpeed(Tier tier) {
        if (tier == Tiers.STONE) {
            return -2.0F;
        }
        if (tier == Tiers.IRON) {
            return -1.0F;
        }
        if (tier == Tiers.DIAMOND || tier == Tiers.NETHERITE) {
            return 0.0F;
        }
        return -3.0F;
    }

    @Override
    public String getMimiclingForm() {
        return MimiclingItem.getHoeForm();
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
