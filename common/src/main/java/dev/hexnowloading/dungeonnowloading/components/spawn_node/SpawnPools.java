package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SpawnPools {
    private static Map<ResourceLocation, SpawnPool> POOLS = Collections.emptyMap();

    private SpawnPools() {}

    public static SpawnPool get(ResourceLocation id) {
        return POOLS.get(id);
    }

    public static Map<ResourceLocation, SpawnPool> all() {
        return POOLS;
    }

    static void replaceAll(Map<ResourceLocation, SpawnPool> pools) {
        POOLS = Collections.unmodifiableMap(new HashMap<>(pools));
    }
}
