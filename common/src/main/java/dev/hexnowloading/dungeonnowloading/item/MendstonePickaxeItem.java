package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MendstonePickaxeItem extends PickaxeItem {

    public MendstonePickaxeItem(Properties properties) {
        super(Tiers.IRON, 1, -2.8F, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        components.add(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_name").withStyle(ChatFormatting.BLUE));
        components.add(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_description1").withStyle(ChatFormatting.DARK_GRAY));
        components.add(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.disclaimer").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairItem) {
        return false;
    }

    // Prevent enchanting entirely (enchanting table + "enchantability" weight)
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }
}