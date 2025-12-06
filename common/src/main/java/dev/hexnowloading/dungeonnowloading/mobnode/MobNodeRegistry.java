package dev.hexnowloading.dungeonnowloading.mobnode;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MobNodeRegistry {
    private static final Map<ResourceLocation, MobNodeDefinition> DEFINITIONS = new HashMap<>();

    public static void clear() { DEFINITIONS.clear(); }
    public static void put(ResourceLocation id, MobNodeDefinition def) { if (id != null && def != null) DEFINITIONS.put(id, def); }
    public static MobNodeDefinition get(ResourceLocation id) { return DEFINITIONS.get(id); }
    public static Map<ResourceLocation, MobNodeDefinition> all() { return Collections.unmodifiableMap(DEFINITIONS); }
}

