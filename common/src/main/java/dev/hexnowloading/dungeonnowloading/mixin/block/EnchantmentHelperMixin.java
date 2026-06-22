package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.item.BossSummoningItem;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(
            method = "getAvailableEnchantmentResults(ILnet/minecraft/world/item/ItemStack;Ljava/util/stream/Stream;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void dnl_addBossSummonEnchantments(
            int power,
            ItemStack stack,
            Stream<Holder<Enchantment>> possibleEnchantments,
            CallbackInfoReturnable<List<EnchantmentInstance>> cir
    ) {
        List<EnchantmentInstance> list = cir.getReturnValue();
        if (list == null) return;

        // Only inject for boss summoning items (NOT books)
        if (!(stack.getItem() instanceof BossSummoningItem)) return;
        if (stack.is(Items.BOOK)) return;

        // Resolve our enchantment holders from the candidate stream (shares the active registry).
        possibleEnchantments.forEach(holder -> {
            if (holder.is(DNLEnchantments.AMPLIFICATION) || holder.is(DNLEnchantments.NULLIFICATION)) {
                addIfInCostWindow(list, holder, power);
            }
        });

        cir.setReturnValue(list);
    }

    private static void addIfInCostWindow(List<EnchantmentInstance> out, Holder<Enchantment> ench, int power) {
        Enchantment value = ench.value();
        for (int lvl = value.getMaxLevel(); lvl >= value.getMinLevel(); --lvl) {
            if (power >= value.getMinCost(lvl) && power <= value.getMaxCost(lvl)) {
                out.add(new EnchantmentInstance(ench, lvl));
                return;
            }
        }
    }
}
