package dev.hexnowloading.dungeonnowloading.spawn_node;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SpawnDefinitions {
    private static Map<ResourceLocation, SpawnDefinition> DEFINITIONS = Collections.emptyMap();

    private SpawnDefinitions() {}

    public static SpawnDefinition get(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, SpawnDefinition> all() {
        return DEFINITIONS;
    }

    static void replaceAll(Map<ResourceLocation, SpawnDefinition> defs) {
        DEFINITIONS = Collections.unmodifiableMap(new HashMap<>(defs));
    }
}
