package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.platform.services.ClientHelper;
import dev.hexnowloading.dungeonnowloading.registry.DNLMenuTypes;
import dev.hexnowloading.dungeonnowloading.screen.MendingTableScreen;

public final class DNLClient {
    private static final ClientHelper CLIENT = Services.load(ClientHelper.class);

    private DNLClient() {}

    public static void registerMenuScreens() {
        CLIENT.registerMenuScreen(DNLMenuTypes.MENDING_TABLE.get(), MendingTableScreen::new);
    }
}
