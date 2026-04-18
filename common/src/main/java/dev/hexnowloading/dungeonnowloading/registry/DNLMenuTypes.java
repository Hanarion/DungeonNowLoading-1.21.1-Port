package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.menu.MendingTableMenu;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class DNLMenuTypes {
    public static final Supplier<MenuType<MendingTableMenu>> MENDING_TABLE =
            Services.MENU.registerMenu("mending_table", MendingTableMenu::new);

    public static void init() { /* called to ensure class load */ }
}
