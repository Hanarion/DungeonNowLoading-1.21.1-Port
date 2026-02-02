package dev.hexnowloading.dungeonnowloading.entity;

import dev.hexnowloading.dungeonnowloading.item.LifeStealerItem;
import dev.hexnowloading.dungeonnowloading.item.SpawnerSword;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class DNLEntityEvents {
    public static float onLivingDamageEvent(LivingEntity attacker, LivingEntity target, float damage) {
        ItemStack mainHandItem = attacker.getMainHandItem();
        if (mainHandItem.is(DNLItems.LIFE_STEALER.get())) {
            // Delegate Life Stealer-specific effects (Sacrifice etc.) to the item class
            damage = LifeStealerItem.onLivingDamage(attacker, target, damage);
        }

        if (mainHandItem.is(DNLItems.SPAWNER_SWORD.get())) {
            // Delegate Spawner Sword-specific effects (Reckless etc.) to the item class
            damage = SpawnerSword.onLivingDamage(attacker, target, damage);
        }

        return damage;
    }

    private static void retargetSummons(Player player, LivingEntity target) {
        // Keep it tight: only retarget nearby owned summons.
        double radius = 64.0D;

        for (WhimperEntity whimper : player.level().getEntitiesOfClass(WhimperEntity.class, player.getBoundingBox().inflate(radius))) {
            if (player.getUUID().equals(whimper.getOwnerUUID())) {
                whimper.setTarget(target);
                whimper.setLastHurtByMob(target);
            }
        }

        for (SealedChaosEntity chaos : player.level().getEntitiesOfClass(SealedChaosEntity.class, player.getBoundingBox().inflate(radius))) {
            if (chaos.getOwnerUUID() != null && player.getUUID().equals(chaos.getOwnerUUID())) {
                chaos.setTarget(target);
                chaos.setLastHurtByMob(target);
            }
        }

        for (CopperCreepEntity creep : player.level().getEntitiesOfClass(CopperCreepEntity.class, player.getBoundingBox().inflate(radius))) {
            // Copper Creep uses summoner UUID as owner
            if (creep.getSummonerUUID().filter(id -> id.equals(player.getUUID())).isPresent()) {
                creep.setTarget(target);
                creep.setLastHurtByMob(target);
            }
        }
    }

    private static int getCommanderLevel(Player player) {
        int best = 0;
        // Main/offhand first
        best = Math.max(best, EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.COMMANDER.get(), player.getMainHandItem()));
        best = Math.max(best, EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.COMMANDER.get(), player.getOffhandItem()));

        // Also scan hotbar so Commander on a summoning item still applies while attacking with something else.
        for (int i = 0; i < 9; i++) {
            best = Math.max(best, EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.COMMANDER.get(), player.getInventory().getItem(i)));
            if (best > 0) {
                // max level is 1, so we can early-exit
                return 1;
            }
        }
        return best;
    }

    public static float onLivingHurtEvent(LivingEntity attacker, LivingEntity target, float damage) {
        ItemStack mainHandItem = attacker.getMainHandItem();

        // Commander: allies immediately target the last mob you hit (only if damage actually lands).
        if (attacker instanceof Player player) {
            int commanderLevel = getCommanderLevel(player);
            if (commanderLevel > 0 && damage > 0.0F && target.isAlive()) {
                retargetSummons(player, target);
            }
        }

        if (mainHandItem.is(DNLItems.SPAWNER_SWORD.get())) {
            damage = SpawnerSword.soulDispersionEffect(attacker, target, damage);
        }
        return damage;
    }
}