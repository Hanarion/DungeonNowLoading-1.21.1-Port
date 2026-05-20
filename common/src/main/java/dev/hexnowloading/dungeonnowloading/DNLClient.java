package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.registry.DNLClientItemModels;
import dev.hexnowloading.dungeonnowloading.registry.DNLMenus;

public final class DNLClient {
    private DNLClient() {}

    public static void registerMenuScreens() {
        DNLMenus.registerScreens();
    }

    public static void registerItemModels() {
        DNLClientItemModels.register();
    }
}
