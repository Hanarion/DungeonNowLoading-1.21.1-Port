package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnPoolReloadListener extends SimpleJsonResourceReloadListener {

    private final Logger logger;

    // Folder: data/<namespace>/spawn_pools/*.json
    public SpawnPoolReloadListener(Gson gson, Logger logger) {
        super(gson, "spawn_pools");
        this.logger = logger;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SpawnPool> pools = new HashMap<>();

        for (var entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();

            try {
                JsonObject obj = entry.getValue().getAsJsonObject();
                if (!obj.has("entries") || !obj.get("entries").isJsonArray()) {
                    logger.warn("Spawn pool {} missing entries[]", id);
                    continue;
                }

                var arr = obj.getAsJsonArray("entries");
                List<SpawnPool.Entry> entriesList = new ArrayList<>();

                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject eObj = el.getAsJsonObject();

                    int weight = eObj.has("weight") ? eObj.get("weight").getAsInt() : 1;

                    ResourceLocation nodeId = null;

                    if (eObj.has("node") && !eObj.get("node").isJsonNull()) {
                        String raw = eObj.get("node").getAsString();

                        // explicit "empty" support
                        if (!raw.equalsIgnoreCase("empty") && !raw.isBlank()) {
                            try {
                                nodeId = new ResourceLocation(raw);
                            } catch (Exception ex) {
                                // invalid RL -> treat as empty roll (or continue; up to you)
                                nodeId = null;
                            }
                        }
                    }

                    if (weight > 0) {
                        entriesList.add(new SpawnPool.Entry(weight, nodeId));
                    }

                }

                if (entriesList.isEmpty()) {
                    logger.warn("Spawn pool {} has entries[], but none were valid.", id);
                    continue;
                }

                pools.put(id, new SpawnPool(id, entriesList));

            } catch (Exception e) {
                logger.error("Failed to load spawn pool {}: {}", id, e.toString());
            }
        }

        SpawnPools.replaceAll(pools);
        logger.info("Loaded {} spawn pools.", pools.size());
    }
}

