package dev.hexnowloading.dungeonnowloading.config;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.neoforged.neoforge.common.ModConfigSpec;
import dev.hexnowloading.dungeonnowloading.platform.services.ConfigHelper;

public class DNLClientConfig {
    public static void register() {
        ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

        Services.CONFIG.registerConfig(ConfigHelper.ConfigType.CLIENT, CLIENT_BUILDER.build());
    }
}
