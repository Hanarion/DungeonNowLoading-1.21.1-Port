package dev.hexnowloading.dungeonnowloading.spawn_node;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnDefinitionReloadListener extends SimpleJsonResourceReloadListener {

    private final Logger logger;

    // Folder: data/<namespace>/dnl_spawn_definitions/*.json
    public SpawnDefinitionReloadListener(Gson gson, Logger logger) {
        super(gson, "spawn_nodes");
        this.logger = logger;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SpawnDefinition> defs = new HashMap<>();

        for (var entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();

            try {
                JsonObject obj = entry.getValue().getAsJsonObject();

                // TABLE MODE
                if (obj.has("entries") && obj.get("entries").isJsonArray()) {
                    var arr = obj.getAsJsonArray("entries");
                    List<SpawnEntry> entriesList = new java.util.ArrayList<>();

                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject eObj = el.getAsJsonObject();

                        int weight = eObj.has("weight") ? eObj.get("weight").getAsInt() : 1;

                        SpawnEntry spawnEntry = parseEntryObject(eObj, weight);
                        if (spawnEntry != null) entriesList.add(spawnEntry);
                    }

                    if (!entriesList.isEmpty()) {
                        defs.put(id, new SpawnDefinition(id, entriesList));
                    } else {
                        logger.warn("Spawn definition {} has entries[], but none were valid.", id);
                    }

                    continue;
                }

                // SINGLE MODE (your current behavior)
                SpawnEntry single = parseEntryObject(obj, 1);
                if (single == null) {
                    logger.warn("Spawn definition {} is invalid.", id);
                    continue;
                }

                defs.put(id, new SpawnDefinition(
                        id,
                        single.entityType,
                        single.count,
                        single.chance,
                        single.spawnEffect,
                        single.nbtPatch,
                        single.snbtPatch
                ));

            } catch (Exception e) {
                logger.error("Failed to load spawn definition {}: {}", id, e.toString());
            }
        }

        SpawnDefinitions.replaceAll(defs);
        logger.info("Loaded {} spawn definitions.", defs.size());
    }

    private SpawnEntry parseEntryObject(JsonObject obj, int weight) {
        try {
            if (!obj.has("entity")) return null;

            ResourceLocation entityId = new ResourceLocation(obj.get("entity").getAsString());
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);

            // BuiltInRegistries never returns null; you can validate by key match
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) return null;

            int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
            double chance = obj.has("chance") ? obj.get("chance").getAsDouble() : 1.0;
            String spawnEffect = obj.has("spawn_effect") ? obj.get("spawn_effect").getAsString() : "none";

            CompoundTag nbtPatch = new CompoundTag();
            if (obj.has("nbt") && obj.get("nbt").isJsonObject()) {
                nbtPatch = jsonObjectToNbt(obj.getAsJsonObject("nbt"));
            }

            CompoundTag snbtPatch = new CompoundTag();
            if (obj.has("snbt")) {
                String snbt = obj.get("snbt").getAsString();
                snbtPatch = TagParser.parseTag(snbt);
            }

            return new SpawnEntry(weight, type, count, chance, spawnEffect, nbtPatch, snbtPatch);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Minimal JSON -> NBT converter for "nbt_patch".
     * Supports primitives, nested objects, and arrays (as ListTag of strings/numbers/compounds).
     * If you want full fidelity (bytes/shorts/etc.), use snbt_patch.
     */
    private static CompoundTag jsonObjectToNbt(JsonObject obj) {
        CompoundTag tag = new CompoundTag();
        for (String key : obj.keySet()) {
            JsonElement el = obj.get(key);

            // Keep this simple: use SNBT for complex typing.
            if (el.isJsonObject()) {
                tag.put(key, jsonObjectToNbt(el.getAsJsonObject()));
            } else if (el.isJsonPrimitive()) {
                var prim = el.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    tag.putBoolean(key, prim.getAsBoolean());
                } else if (prim.isNumber()) {
                    // default number as double; use snbt_patch if you need byte/float/etc
                    tag.putDouble(key, prim.getAsDouble());
                } else if (prim.isString()) {
                    tag.putString(key, prim.getAsString());
                }
            } else {
                // For arrays / other types: prefer snbt_patch (simpler + exact)
                // You can extend this later if you want.
            }
        }
        return tag;
    }
}
