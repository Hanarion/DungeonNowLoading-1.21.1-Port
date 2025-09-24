package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.enchantment.ScrapEnchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.function.Supplier;

public class DNLEnchantments {
    public static final Supplier<Enchantment> BREAK_PROTECTION = register("break_protection", () -> new ScrapEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));

    private static <T extends Enchantment> Supplier<T> register(String name, Supplier<T> enchantmentSupplier) {
        return dev.hexnowloading.dungeonnowloading.platform.Services.REGISTRY.register(BuiltInRegistries.ENCHANTMENT, name, enchantmentSupplier);
    }

    public static void init() {}
}

