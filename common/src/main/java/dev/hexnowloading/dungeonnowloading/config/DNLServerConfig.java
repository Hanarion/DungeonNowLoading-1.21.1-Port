package dev.hexnowloading.dungeonnowloading.config;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.neoforged.neoforge.common.ModConfigSpec;
import dev.hexnowloading.dungeonnowloading.platform.services.ConfigHelper;

public class DNLServerConfig {
    public static void register() {
        ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

        GeneralConfig.registerServerConfig(SERVER_BUILDER);
        BossConfig.registerServerConfig(SERVER_BUILDER);
        MobConfig.registerServerConfig(SERVER_BUILDER);
        PvpConfig.registerServerConfig(SERVER_BUILDER);

        Services.CONFIG.registerConfig(ConfigHelper.ConfigType.SERVER, SERVER_BUILDER.build());
    }
}
