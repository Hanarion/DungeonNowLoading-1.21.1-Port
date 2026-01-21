package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnerSword extends SwordItem {

    public SpawnerSword(Tier $$0, int $$1, float $$2, Properties $$3) {
        super($$0, $$1, $$2, $$3);
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(itemStack, target, attacker);
        if (result && !target.level().isClientSide) {
            // Base self-damage only when Reckless is NOT present; Reckless handles its own self-damage
            int recklessLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.RECKLESS.get(), attacker.getMainHandItem());
            if (recklessLevel == 0 && attacker.getHealth() > 1.0F) {
                attacker.hurt(attacker.damageSources().generic(), 1.0F);
            }
        }
        return result;
    }

    public static float soulDispersionEffect(LivingEntity attacker, LivingEntity target, float damage) {
        return attacker.getHealth() > 1 ? damage + 3.0F : damage;
    }

    public static float onLivingDamage(LivingEntity attacker, LivingEntity target, float damage) {
        int recklessLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.RECKLESS.get(), attacker.getMainHandItem());
        if (recklessLevel > 0 && damage > 0.0F) {
            // Increase outgoing damage: +2 * level
            damage += 2.0F * recklessLevel;

            // Apply self-damage: 1 * level, but not in creative/spectator and only if enough health to survive
            if (attacker instanceof Player player) {
                if (!player.getAbilities().instabuild && attacker.getHealth() > recklessLevel) {
                    attacker.hurt(attacker.damageSources().magic(), recklessLevel);
                }
            } else if (attacker.getHealth() > recklessLevel) {
                attacker.hurt(attacker.damageSources().magic(), recklessLevel);
            }
        }
        return damage;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairItemStack) {
        return repairItemStack.is(DNLItems.SPAWNER_BLADE.get()) || super.isValidRepairItem(itemStack, repairItemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.spawner_sword.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.spawner_sword.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}