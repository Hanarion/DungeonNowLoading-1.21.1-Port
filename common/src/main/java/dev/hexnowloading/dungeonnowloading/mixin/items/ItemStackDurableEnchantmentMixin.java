package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

/**
 * Durable enchantment implementation.
 *
 * Intended behaviour:
 * - Level 1: 4 hits per 1 durability
 * - Level 2: 6 hits per 1 durability
 * - Level 3: 8 hits per 1 durability
 */
@Mixin(ItemStack.class)
public abstract class ItemStackDurableEnchantmentMixin {

    @org.spongepowered.asm.mixin.Unique
    private static final String dnl$DURABLE_HIT_COUNTER_TAG = "dnl_durable_hits";

    @org.spongepowered.asm.mixin.Unique
    private static int dnl$getDurableLevel(ItemStack self) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.DURABLE.get(), self);
        if (level > 0) return level;

        // Fallback: in case registry indirection causes EnchantmentHelper to miss (shouldn't, but safe)
        // check raw enchantment tags for our durable id.
        var enchList = self.getEnchantmentTags();
        for (int i = 0; i < enchList.size(); i++) {
            var tag = enchList.getCompound(i);
            if ("dungeonnowloading:durable".equals(tag.getString("id"))) {
                return tag.getInt("lvl");
            }
        }
        return 0;
    }

    @org.spongepowered.asm.mixin.Unique
    private static int dnl$hitsPerDurability(ItemStack self) {
        int level = dnl$getDurableLevel(self);
        return 2 + (2 * level); // L1=4, L2=6, L3=8
    }

    @org.spongepowered.asm.mixin.Unique
    private static int dnl$durableGate(int amount, ItemStack self) {
        if (amount <= 0) return amount;
        if (!(self.getItem() instanceof ArmorItem)) return amount;

        int level = dnl$getDurableLevel(self);
        if (level <= 0) {
            // No Durable enchant -> do not interfere with vanilla durability loss.
            return amount;
        }

        int hitsPerDurability = 2 + (2 * level);
        var tag = self.getOrCreateTag();
        int hits = tag.getInt(dnl$DURABLE_HIT_COUNTER_TAG);

        hits += amount;

        int durabilityToTake = hits / hitsPerDurability;
        hits = hits % hitsPerDurability;

        tag.putInt(dnl$DURABLE_HIT_COUNTER_TAG, hits);

        if (durabilityToTake <= 0) {
            return 0;
        }

        return Math.min(amount, durabilityToTake);
    }

    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private int dnl$durable$reduceDurabilityDamage(int amount, int originalAmount, LivingEntity entity, Consumer<LivingEntity> onBroken) {
        ItemStack self = (ItemStack) (Object) this;
        return dnl$durableGate(amount, self);
    }

    @ModifyVariable(
            method = "hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private int dnl$durable$reduceDurabilityDamage_hurt(int amount, int originalAmount, RandomSource random, ServerPlayer player) {
        ItemStack self = (ItemStack) (Object) this;
        return dnl$durableGate(amount, self);
    }
}