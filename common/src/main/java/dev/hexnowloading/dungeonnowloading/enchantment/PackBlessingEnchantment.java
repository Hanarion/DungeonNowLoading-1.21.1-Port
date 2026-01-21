package dev.hexnowloading.dungeonnowloading.enchantment;

import dev.hexnowloading.dungeonnowloading.item.MendstonePickaxeItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class PackBlessingEnchantment extends Enchantment {
    public PackBlessingEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.BREAKABLE, slots);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if (stack.getItem() instanceof MendstonePickaxeItem) {
            return false;
        }
        return stack.isDamageableItem();
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.MENDING) {
            return false;
        }
        return super.checkCompatibility(other);
    }

    @Override
    public int getMaxLevel() {
        // Pack Blessing max level 1
        return 1;
    }
}