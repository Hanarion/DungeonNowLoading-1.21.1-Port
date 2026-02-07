package dev.hexnowloading.dungeonnowloading.enchantment;

import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class GigantismEnchantment extends Enchantment {
    public GigantismEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.BREAKABLE, slots);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(DNLItems.COPPER_DETONATOR.get())
                || stack.is(DNLItems.SCEPTER_OF_SEALED_CHAOS.get())
                || stack.is(DNLItems.SPAWNER_HELMET.get());
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }
}