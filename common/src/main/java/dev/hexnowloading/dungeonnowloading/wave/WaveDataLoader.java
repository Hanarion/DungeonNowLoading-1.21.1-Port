package dev.hexnowloading.dungeonnowloading.wave;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class WaveDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public WaveDataLoader() { super(GSON, "waves"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        WaveRegistry.clear();
        map.forEach((id, json) -> {
            try {
                WaveDefinition def = GSON.fromJson(json, WaveDefinition.class);
                if (def != null) WaveRegistry.put(id, def);
            } catch (Exception e) {
                DungeonNowLoading.LOGGER.error("Failed to parse wave {}: {}", id, e.toString());
            }
        });
        DungeonNowLoading.LOGGER.info("Loaded {} wave definitions from datapacks.", WaveRegistry.all().size());
    }
}

