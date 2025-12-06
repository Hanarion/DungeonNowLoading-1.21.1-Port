package dev.hexnowloading.dungeonnowloading.gauntlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class GauntletDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public GauntletDataLoader() { super(GSON, "gauntlets"); }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        GauntletRegistry.clear();
        map.forEach((id, json) -> {
            try {
                GauntletDefinition def = GSON.fromJson(json, GauntletDefinition.class);
                if (def != null) GauntletRegistry.put(id, def);
            } catch (Exception e) {
                DungeonNowLoading.LOGGER.error("Failed to parse gauntlet {}: {}", id, e.toString());
            }
        });
        DungeonNowLoading.LOGGER.info("Loaded {} gauntlet definitions from datapacks.", GauntletRegistry.all().size());
    }
}

