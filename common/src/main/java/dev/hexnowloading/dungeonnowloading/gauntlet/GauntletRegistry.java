package dev.hexnowloading.dungeonnowloading.gauntlet;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GauntletRegistry {
    private static final Map<ResourceLocation, GauntletDefinition> DEFINITIONS = new HashMap<>();

    public static void clear() { DEFINITIONS.clear(); }
    public static void put(ResourceLocation id, GauntletDefinition def) { if (id != null && def != null) DEFINITIONS.put(id, def); }
    public static GauntletDefinition get(ResourceLocation id) { return DEFINITIONS.get(id); }
    public static Map<ResourceLocation, GauntletDefinition> all() { return Collections.unmodifiableMap(DEFINITIONS); }
}

