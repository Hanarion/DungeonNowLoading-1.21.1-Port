package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
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
 * Requested formula: (4 hits + (durable level * 2)) hits = -1 durability.
 *
 * We treat the incoming durability damage amount as "hits" and reduce it to:
 * ceil(amount / (4 + 2*level)).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackDurableEnchantmentMixin {

    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private int dnl$durable$reduceDurabilityDamage(int amount, int originalAmount, LivingEntity entity, Consumer<LivingEntity> onBroken) {
        if (amount <= 0) return amount;

        ItemStack self = (ItemStack) (Object) this;
        if (!(self.getItem() instanceof ArmorItem)) return amount;

        int level = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.DURABLE.get(), self);
        if (level <= 0) return amount;

        int hitsPerDurability = 4 + (level * 2);
        int reduced = (amount + hitsPerDurability - 1) / hitsPerDurability;
        return Math.max(1, reduced);
    }
}
