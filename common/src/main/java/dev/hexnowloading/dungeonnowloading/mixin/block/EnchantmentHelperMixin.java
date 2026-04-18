package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.item.BossSummoningItem;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
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

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(
            method = "getAvailableEnchantmentResults(ILnet/minecraft/world/item/ItemStack;Z)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void dnl_addBossSummonEnchantments(
            int power,
            ItemStack stack,
            boolean allowTreasure,
            CallbackInfoReturnable<List<EnchantmentInstance>> cir
    ) {
        List<EnchantmentInstance> list = cir.getReturnValue();
        if (list == null) return;

        // Only inject for your boss summoning items (NOT books)
        if (!(stack.getItem() instanceof BossSummoningItem)) return;
        if (stack.is(Items.BOOK)) return;

        Enchantment amp = DNLEnchantments.AMPLIFICATION.get();
        Enchantment nul = DNLEnchantments.NULLIFICATION.get();

        // Optionally: if you want boss items to roll ONLY your enchants, uncomment:
        // list.clear();

        addIfInCostWindow(list, amp, power);
        addIfInCostWindow(list, nul, power);

        cir.setReturnValue(list);
    }

    private static void addIfInCostWindow(List<EnchantmentInstance> out, Enchantment ench, int power) {
        for (int lvl = ench.getMaxLevel(); lvl >= ench.getMinLevel(); --lvl) {
            if (power >= ench.getMinCost(lvl) && power <= ench.getMaxCost(lvl)) {
                out.add(new EnchantmentInstance(ench, lvl));
                return;
            }
        }
    }
}


