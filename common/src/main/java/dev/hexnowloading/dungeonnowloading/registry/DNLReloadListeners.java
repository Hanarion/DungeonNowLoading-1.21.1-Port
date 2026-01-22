package dev.hexnowloading.dungeonnowloading.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.spawn_node.SpawnDefinitionReloadListener;
import net.minecraft.resources.ResourceLocation;

public final class DNLReloadListeners {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        Services.RELOAD_LISTENERS.registerDataReloadListener(new ResourceLocation(DungeonNowLoading.MOD_ID, "spawn_definitions"), new SpawnDefinitionReloadListener(GSON, DungeonNowLoading.LOGGER));
    }
}
