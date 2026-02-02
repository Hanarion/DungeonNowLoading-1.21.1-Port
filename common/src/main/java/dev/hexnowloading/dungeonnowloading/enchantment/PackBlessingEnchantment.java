package dev.hexnowloading.dungeonnowloading.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class PackBlessingEnchantment extends Enchantment {
    public PackBlessingEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.BREAKABLE, slots);
    }

    @Override
    public int getMaxLevel() {
        // Pack Blessing max level 1
        return 1;
    }
}