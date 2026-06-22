package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.item.Item;
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
            int recklessLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(attacker.level(), DNLEnchantments.RECKLESS), attacker.getMainHandItem());
            float selfDamage = 1.0F + recklessLevel;

            // Don't self-damage in creative/spectator.
            if (attacker instanceof Player player && (player.getAbilities().instabuild || player.isSpectator())) {
                return true;
            }

            // If self damage would kill the attacker, do nothing (no self damage and no bonus damage).
            // This matches: "shouldn't kill the user, but also shouldn't apply the bonus damage".
            if (recklessLevel > 0 && attacker.getHealth() <= selfDamage) {
                return result;
            }

            // Always apply: 1 + reckless level.
            attacker.hurt(attacker.damageSources().magic(), selfDamage);
        }
        return result;
    }

    public static float soulDispersionEffect(LivingEntity attacker, LivingEntity target, float damage) {
        return attacker.getHealth() > 1 ? damage + 3.0F : damage;
    }

    public static float onLivingDamage(LivingEntity attacker, LivingEntity target, float damage) {
        int recklessLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(attacker.level(), DNLEnchantments.RECKLESS), attacker.getMainHandItem());
        if (recklessLevel > 0 && damage > 0.0F) {
            float selfDamage = 1.0F + recklessLevel;
            // If applying reckless self-damage would kill the attacker, don't grant the bonus damage.
            if (attacker.getHealth() <= selfDamage) {
                return damage;
            }
            // Increase outgoing damage: +2 * level
            damage += 2.0F * recklessLevel;
        }
        return damage;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairItemStack) {
        return repairItemStack.is(DNLItems.SPAWNER_BLADE.get()) || super.isValidRepairItem(itemStack, repairItemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.spawner_sword.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.spawner_sword.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}