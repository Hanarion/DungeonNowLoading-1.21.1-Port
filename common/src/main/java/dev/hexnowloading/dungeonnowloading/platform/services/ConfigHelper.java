package dev.hexnowloading.dungeonnowloading.platform.services;

import net.neoforged.neoforge.common.ModConfigSpec;

public interface ConfigHelper {

    /** Loader-agnostic config sidedness, mapped to each loader's native ModConfig.Type. */
    enum ConfigType {
        CLIENT,
        COMMON,
        SERVER
    }

    void registerConfig(ConfigType type, ModConfigSpec spec);
}
