package dev.hexnowloading.dungeonnowloading.mobnode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class MobNodeDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public MobNodeDataLoader() { super(GSON, "mob_nodes"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        MobNodeRegistry.clear();
        map.forEach((id, json) -> {
            try {
                MobNodeDefinition def = GSON.fromJson(json, MobNodeDefinition.class);
                if (def != null) MobNodeRegistry.put(id, def);
            } catch (Exception e) {
                DungeonNowLoading.LOGGER.error("Failed to parse mob node {}: {}", id, e.toString());
            }
        });
        DungeonNowLoading.LOGGER.info("Loaded {} mob node definitions from datapacks.", MobNodeRegistry.all().size());
    }
}

