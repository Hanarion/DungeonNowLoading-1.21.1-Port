package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class MimiclingSwordItem extends SwordItem implements MimiclingFormItem {
    public MimiclingSwordItem(Properties properties) {
        this(Tiers.DIAMOND, properties);
    }

    public MimiclingSwordItem(Tier tier, Properties properties) {
        super(tier, 3, -2.4F, properties);
    }

    @Override
    public String getMimiclingForm() {
        return MimiclingItem.getSwordForm();
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
