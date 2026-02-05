package dev.hexnowloading.dungeonnowloading.spawn_node;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SpawnNodes {

    private static Map<ResourceLocation, SpawnNode> NODES = Collections.emptyMap();

    private SpawnNodes() {}

    public static SpawnNode get(ResourceLocation id) {
        return NODES.get(id);
    }

    public static Map<ResourceLocation, SpawnNode> all() {
        return NODES;
    }

    public static void replaceAll(Map<ResourceLocation, SpawnNode> nodes) {
        NODES = Collections.unmodifiableMap(new HashMap<>(nodes));
    }
}
