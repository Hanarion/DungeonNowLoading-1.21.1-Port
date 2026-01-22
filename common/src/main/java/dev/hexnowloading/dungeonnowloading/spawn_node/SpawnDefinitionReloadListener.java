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
import java.util.Map;

public class SpawnDefinitionReloadListener extends SimpleJsonResourceReloadListener {

    private final Logger logger;

    // Folder: data/<namespace>/dnl_spawn_definitions/*.json
    public SpawnDefinitionReloadListener(Gson gson, Logger logger) {
        super(gson, "spawn_definitions");
        this.logger = logger;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SpawnDefinition> defs = new HashMap<>();

        for (var entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();

            try {
                JsonObject obj = entry.getValue().getAsJsonObject();

                ResourceLocation entityId = new ResourceLocation(obj.get("entity").getAsString());
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                if (type == EntityType.PIG && !entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG))) {
                    // ignore; just a silly example check
                }
                if (type == null || type == EntityType.PIG && entityId.toString().equals("minecraft:empty")) {
                    // BuiltInRegistries never returns null, but keep sanity checks if you want.
                }

                int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
                double chance = obj.has("chance") ? obj.get("chance").getAsDouble() : 1.0;
                String spawnEffect = obj.has("spawn_effect") ? obj.get("spawn_effect").getAsString() : "none";

                CompoundTag nbtPatch = new CompoundTag();
                if (obj.has("nbt_patch") && obj.get("nbt_patch").isJsonObject()) {
                    nbtPatch = jsonObjectToNbt(obj.getAsJsonObject("nbt_patch"));
                }

                CompoundTag snbtPatch = new CompoundTag();
                if (obj.has("snbt_patch")) {
                    String snbt = obj.get("snbt_patch").getAsString();
                    snbtPatch = TagParser.parseTag(snbt);
                }

                defs.put(id, new SpawnDefinition(id, type, count, chance, spawnEffect, nbtPatch, snbtPatch));
            } catch (Exception e) {
                logger.error("Failed to load spawn definition {}: {}", id, e.toString());
            }
        }

        SpawnDefinitions.replaceAll(defs);
        logger.info("Loaded {} spawn definitions.", defs.size());
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
