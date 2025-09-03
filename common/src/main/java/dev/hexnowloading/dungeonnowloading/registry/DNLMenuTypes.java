package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.menu.MendingTableMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class DNLMenuTypes {
    // Will be set by platform-specific bootstrap (Forge/Fabric) using their own builders.
    public static Supplier<MenuType<MendingTableMenu>> MENDING_TABLE;

    public static void bootstrap(Supplier<MenuType<MendingTableMenu>> mendingTableSupplier) {
        MENDING_TABLE = mendingTableSupplier;
    }

    public static void init() { /* called to ensure class load */ }
}
