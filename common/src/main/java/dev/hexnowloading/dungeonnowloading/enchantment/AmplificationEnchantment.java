package dev.hexnowloading.dungeonnowloading.enchantment;

import dev.hexnowloading.dungeonnowloading.item.BossSummoningItem;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class AmplificationEnchantment extends Enchantment {
    public AmplificationEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.VANISHABLE, slots);
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return itemStack.getItem() instanceof BossSummoningItem;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return other != DNLEnchantments.NULLIFICATION.get() && super.checkCompatibility(other);
    }

    public int getMinCost(int $$0) {
        return 1 + ($$0 - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean isTradeable() { return false; }

    @Override
    public boolean isDiscoverable() {
        return false;
    }
}
