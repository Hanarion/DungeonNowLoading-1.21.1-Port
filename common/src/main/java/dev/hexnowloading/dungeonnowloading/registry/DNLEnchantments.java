package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.enchantment.AmplificationEnchantment;
import dev.hexnowloading.dungeonnowloading.enchantment.NullificationEnchantment;
import dev.hexnowloading.dungeonnowloading.enchantment.ScrapEnchantment;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.enchantment.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

import static dev.hexnowloading.dungeonnowloading.platform.Services.REGISTRY;

public class DNLEnchantments {
    public static final Supplier<Enchantment> BREAK_PROTECTION = register("break_protection", () -> new ScrapEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> AMPLIFICATION = register("amplification", () -> new AmplificationEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.HEAD));
    public static final Supplier<Enchantment> NULLIFICATION = register("nullification", () -> new NullificationEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.HEAD));
    public static final Supplier<Enchantment> DURABLE = register("durable", () -> new DurableEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> GIGANTISM = register("gigantism", () -> new GigantismEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> OVERWORKED = register("overworked", () -> new OverworkedEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> PACK_BLESSING = register("pack_blessing", () -> new PackBlessingEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> ARC_SHOT = register("arc_shot", () -> new ArcShotEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> PULSE_SHOT = register("pulse_shot", () -> new PulseShotEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> SACRIFICE = register("sacrifice", () -> new SacrificeEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));
    public static final Supplier<Enchantment> RECKLESS = register("reckless", () -> new RecklessEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.values()));



    private static <T extends Enchantment> Supplier<T> register(String name, Supplier<T> enchantmentSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.ENCHANTMENT, name, enchantmentSupplier);
    }

    public static void init() {}
}
