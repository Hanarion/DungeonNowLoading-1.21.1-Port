package dev.hexnowloading.dungeonnowloading.entity;

import dev.hexnowloading.dungeonnowloading.item.LifeStealerItem;
import dev.hexnowloading.dungeonnowloading.item.SpawnerSword;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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

    public static float onLivingHurtEvent(LivingEntity attacker, LivingEntity target, float damage) {
        ItemStack mainHandItem = attacker.getMainHandItem();
        if (mainHandItem.is(DNLItems.SPAWNER_SWORD.get())) {
            damage = SpawnerSword.soulDispersionEffect(attacker, target, damage);
        }
        return damage;
    }
}