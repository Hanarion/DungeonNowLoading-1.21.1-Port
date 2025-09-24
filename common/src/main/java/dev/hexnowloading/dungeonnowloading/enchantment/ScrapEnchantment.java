package dev.hexnowloading.dungeonnowloading.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

public class ScrapEnchantment extends Enchantment {
    public ScrapEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.BREAKABLE, slots);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.isDamageableItem();
    }
}

