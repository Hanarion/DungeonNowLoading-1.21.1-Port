package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

/**
 * Durable enchantment implementation (deterministic).
 *
 * Each armor piece only loses 1 durability after N durability-damage attempts.
 * N = 2 + 2*level => L1=4, L2=6, L3=8.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackDurableEnchantmentMixin {

    @Unique
    private static final String dnl$DURABLE_HIT_COUNTER_TAG = "dnl_durable_hits";

    @Unique
    private static int dnl$getDurableLevel(ItemStack self) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.DURABLE.get(), self);
        if (level > 0) return level;

        // Fallback: check raw enchantment tags
        var enchList = self.getEnchantmentTags();
        for (int i = 0; i < enchList.size(); i++) {
            var tag = enchList.getCompound(i);
            if ("dungeonnowloading:durable".equals(tag.getString("id"))) {
                return tag.getInt("lvl");
            }
        }
        return 0;
    }

    @Unique
    private static int dnl$gateAmount(ItemStack self, int amount) {
        if (amount <= 0) return amount;
        if (!(self.getItem() instanceof ArmorItem)) return amount;

        int level = dnl$getDurableLevel(self);
        if (level <= 0) return amount;

        int threshold = 2 + 2 * level; // L1=4

        var tag = self.getOrCreateTag();
        int hits = tag.getInt(dnl$DURABLE_HIT_COUNTER_TAG);
        hits += amount;

        int apply = hits / threshold;
        hits = hits % threshold;
        tag.putInt(dnl$DURABLE_HIT_COUNTER_TAG, hits);

        // Apply only the computed durability loss (can be 0).
        return apply;
    }

    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private int dnl$durable$counting_hurtAndBreak(int amount, int originalAmount, LivingEntity entity, Consumer<LivingEntity> onBroken) {
        ItemStack self = (ItemStack) (Object) this;
        return dnl$gateAmount(self, amount);
    }

    @ModifyVariable(
            method = "hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private int dnl$durable$counting_hurt(int amount, int originalAmount, RandomSource random, ServerPlayer player) {
        ItemStack self = (ItemStack) (Object) this;
        return dnl$gateAmount(self, amount);
    }
}