package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;

public class SpawnPool {
    public final ResourceLocation id;
    public final List<Entry> entries;

    public SpawnPool(ResourceLocation id, List<Entry> entries) {
        this.id = id;
        this.entries = (entries == null) ? List.of() : List.copyOf(entries);
    }

    public ResourceLocation pickNodeId(RandomSource random) {
        if (entries.isEmpty()) return null;

        int total = 0;
        for (Entry e : entries) total += e.weight;

        if (total <= 0) return null; // all weights 0 => empty

        int roll = random.nextInt(total);
        int acc = 0;
        for (Entry e : entries) {
            acc += e.weight;
            if (roll < acc) return e.nodeId;
        }
        return entries.get(entries.size() - 1).nodeId;
    }

    public static class Entry {
        public final int weight;
        public final ResourceLocation nodeId;

        public Entry(int weight, ResourceLocation nodeId) {
            this.weight = Math.max(0, weight);
            this.nodeId = nodeId;
        }
    }
}
