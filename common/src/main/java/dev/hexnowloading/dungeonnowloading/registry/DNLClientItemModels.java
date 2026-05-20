package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.platform.services.ClientHelper;

public final class DNLClientItemModels {
    private static final ClientHelper CLIENT = Services.load(ClientHelper.class);

    private DNLClientItemModels() {}

    public static void register() {
        CLIENT.registerItemModel(DungeonNowLoading.id("item/wisplight_rod_gui"));
        CLIENT.registerItemModel(DungeonNowLoading.id("item/wisplight_rod_handheld"));
    }
}
