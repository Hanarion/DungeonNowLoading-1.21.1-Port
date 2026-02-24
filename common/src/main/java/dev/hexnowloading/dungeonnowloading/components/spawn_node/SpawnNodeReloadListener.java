package dev.hexnowloading.dungeonnowloading.components.spawn_node;

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

public class SpawnNodeReloadListener extends SimpleJsonResourceReloadListener {

    private final Logger logger;

    public SpawnNodeReloadListener(Gson gson, Logger logger) {
        super(gson, "spawn_nodes");
        this.logger = logger;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {

        Map<ResourceLocation, SpawnNode> nodes = new HashMap<>();

        for (var entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();

            try {
                JsonObject obj = entry.getValue().getAsJsonObject();

                // MULTI ENTRY NODE
                if (obj.has("entries") && obj.get("entries").isJsonArray()) {

                    var arr = obj.getAsJsonArray("entries");
                    List<SpawnEntry> list = new java.util.ArrayList<>();

                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;

                        SpawnEntry se = parseEntryObject(el.getAsJsonObject());
                        if (se != null) list.add(se);
                    }

                    if (!list.isEmpty()) {
                        nodes.put(id, new SpawnNode(id, list));
                    }

                    continue;
                }

                // SINGLE NODE
                SpawnEntry single = parseEntryObject(obj);
                if (single == null) continue;

                nodes.put(id, new SpawnNode(
                        id,
                        single.entityType,
                        single.count,
                        single.chance,
                        single.spawnEffect,
                        single.nbtPatch,
                        single.snbtPatch
                ));

            } catch (Exception e) {
                logger.error("Failed loading spawn node {}: {}", id, e.toString());
            }
        }

        SpawnNodes.replaceAll(nodes);
        logger.info("Loaded {} spawn nodes.", nodes.size());
    }

    private SpawnEntry parseEntryObject(JsonObject obj) {
        try {
            if (!obj.has("entity")) return null;

            ResourceLocation entityId = new ResourceLocation(obj.get("entity").getAsString());
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) return null;

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);

            int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
            double chance = obj.has("chance") ? obj.get("chance").getAsDouble() : 1.0;
            String spawnEffect = obj.has("spawn_effect") ? obj.get("spawn_effect").getAsString() : "none";

            CompoundTag nbtPatch = new CompoundTag();
            if (obj.has("nbt")) {
                nbtPatch = jsonObjectToNbt(obj.getAsJsonObject("nbt"));
            }

            CompoundTag snbtPatch = new CompoundTag();
            if (obj.has("snbt")) {
                snbtPatch = TagParser.parseTag(obj.get("snbt").getAsString());
            }

            return new SpawnEntry(type, count, chance, spawnEffect, nbtPatch, snbtPatch);

        } catch (Exception ignored) {
            return null;
        }
    }

    private static CompoundTag jsonObjectToNbt(JsonObject obj) {
        CompoundTag tag = new CompoundTag();

        for (String key : obj.keySet()) {
            JsonElement el = obj.get(key);

            if (el == null || el.isJsonNull()) continue;

            if (el.isJsonObject()) {
                tag.put(key, jsonObjectToNbt(el.getAsJsonObject()));
                continue;
            }

            if (el.isJsonPrimitive()) {
                var prim = el.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    tag.putBoolean(key, prim.getAsBoolean());
                } else if (prim.isNumber()) {
                    // keep your current behavior; use SNBT for byte/int/etc exact types
                    tag.putDouble(key, prim.getAsDouble());
                } else if (prim.isString()) {
                    tag.putString(key, prim.getAsString());
                }
            }

            // Arrays intentionally ignored for now; use "snbt" for lists/Passengers/etc.
        }

        return tag;
    }
}
