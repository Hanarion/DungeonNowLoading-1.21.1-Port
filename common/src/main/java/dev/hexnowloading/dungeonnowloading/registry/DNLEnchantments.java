package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.enchantment.AmplificationEnchantment;
import dev.hexnowloading.dungeonnowloading.enchantment.NullificationEnchantment;
import dev.hexnowloading.dungeonnowloading.enchantment.ScrapEnchantment;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

public class DNLEnchantments {
    public static final Supplier<Enchantment> BREAK_PROTECTION = register("break_protection", () -> new ScrapEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> AMPLIFICATION = register("amplification", () -> new AmplificationEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.HEAD));
    public static final Supplier<Enchantment> NULLIFICATION = register("nullification", () -> new NullificationEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.HEAD));

    private static <T extends Enchantment> Supplier<T> register(String name, Supplier<T> enchantmentSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.ENCHANTMENT, name, enchantmentSupplier);
    }

    public static void init() {}
}

