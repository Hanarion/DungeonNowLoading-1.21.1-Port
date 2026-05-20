package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.platform.services.ClientHelper;
import dev.hexnowloading.dungeonnowloading.screen.MendingTableScreen;

public final class DNLMenus {
    private static final ClientHelper CLIENT = Services.load(ClientHelper.class);

    private DNLMenus() {}

    public static void registerScreens() {
        CLIENT.registerMenuScreen(DNLMenuTypes.MENDING_TABLE.get(), MendingTableScreen::new);
    }
}
