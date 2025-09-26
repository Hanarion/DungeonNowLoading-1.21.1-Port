package dev.hexnowloading.dungeonnowloading.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import dev.hexnowloading.dungeonnowloading.item.MendstonePickaxeItem;

public class ScrapEnchantment extends Enchantment {
    public ScrapEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.BREAKABLE, slots);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if (stack.getItem() instanceof MendstonePickaxeItem) {
            return false;
        }
        return stack.isDamageableItem();
    }
}
