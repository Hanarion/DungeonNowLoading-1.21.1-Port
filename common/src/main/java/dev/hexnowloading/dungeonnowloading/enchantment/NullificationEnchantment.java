package dev.hexnowloading.dungeonnowloading.enchantment;

import dev.hexnowloading.dungeonnowloading.item.BossSummoningItem;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class NullificationEnchantment extends Enchantment {
    public NullificationEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.VANISHABLE, slots);
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return itemStack.getItem() instanceof BossSummoningItem;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return other != DNLEnchantments.AMPLIFICATION.get() && super.checkCompatibility(other);
    }

    @Override
    public int getMinCost(int level) {
        return 1; // allow at very low table power
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean isTradeable() { return false; }

    @Override
    public boolean isDiscoverable() {
        return false;
    }
}