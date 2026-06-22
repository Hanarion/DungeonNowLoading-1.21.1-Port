package dev.hexnowloading.dungeonnowloading.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.item.MimiclingFoods;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.SpawnNodeReloadListener;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.SpawnPoolReloadListener;
import net.minecraft.resources.ResourceLocation;

public final class DNLReloadListeners {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        Services.RELOAD_LISTENERS.registerDataReloadListener(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "spawn_pools"), new SpawnPoolReloadListener(GSON, DungeonNowLoading.LOGGER));
        Services.RELOAD_LISTENERS.registerDataReloadListener(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "spawn_nodes"), new SpawnNodeReloadListener(GSON, DungeonNowLoading.LOGGER));
        Services.RELOAD_LISTENERS.registerDataReloadListener(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "mimicling_foods"), new MimiclingFoods.ReloadListener(GSON, DungeonNowLoading.LOGGER));
    }
}
