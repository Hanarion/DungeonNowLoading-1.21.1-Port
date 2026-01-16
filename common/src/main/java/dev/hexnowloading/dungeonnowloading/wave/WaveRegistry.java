package dev.hexnowloading.dungeonnowloading.wave;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WaveRegistry {
    private static final Map<ResourceLocation, WaveDefinition> DEFINITIONS = new HashMap<>();
    public static void clear() { DEFINITIONS.clear(); }
    public static void put(ResourceLocation id, WaveDefinition def) { if (id != null && def != null) DEFINITIONS.put(id, def); }
    public static WaveDefinition get(ResourceLocation id) { return DEFINITIONS.get(id); }
    public static Map<ResourceLocation, WaveDefinition> all() { return Collections.unmodifiableMap(DEFINITIONS); }
}

