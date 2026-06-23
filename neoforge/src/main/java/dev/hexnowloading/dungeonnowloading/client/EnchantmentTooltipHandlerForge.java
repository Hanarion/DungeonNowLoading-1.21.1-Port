package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;

@EventBusSubscriber(modid = DungeonNowLoading.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class EnchantmentTooltipHandlerForge {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        // 1.21: getEnchantments returns ItemEnchantments keyed by Holder<Enchantment>;
        // enchantments are a dynamic registry, so resolve the id from the holder key.
        for (var holder : stack.getEnchantments().keySet()) {
            var id = holder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
            if (id != null && DungeonNowLoading.MOD_ID.equals(id.getNamespace())) {
                String key = "enchantment." + id.getNamespace() + "." + id.getPath() + ".desc";
                event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}

