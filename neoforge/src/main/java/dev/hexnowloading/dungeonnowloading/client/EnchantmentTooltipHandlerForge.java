package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = DungeonNowLoading.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EnchantmentTooltipHandlerForge {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment ench = entry.getKey();
            var id = BuiltInRegistries.ENCHANTMENT.getKey(ench);
            if (id != null && DungeonNowLoading.MOD_ID.equals(id.getNamespace())) {
                String key = "enchantment." + id.getNamespace() + "." + id.getPath() + ".desc";
                event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}

