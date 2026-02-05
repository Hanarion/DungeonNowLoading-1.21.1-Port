package dev.hexnowloading.dungeonnowloading.spawn_node;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class SpawnDefinition {
    public final ResourceLocation id;

    // single-mode fields (backwards compat)
    public final EntityType<?> entityType;
    public final int count;
    public final double chance;
    public final String spawnEffect;
    public final net.minecraft.nbt.CompoundTag nbtPatch;
    public final net.minecraft.nbt.CompoundTag snbtPatch;

    // table-mode
    public final List<SpawnEntry> entries;

    // single-mode ctor (your current one)
    public SpawnDefinition(ResourceLocation id, EntityType<?> entityType, int count, double chance, String spawnEffect,
                           net.minecraft.nbt.CompoundTag nbtPatch, net.minecraft.nbt.CompoundTag snbtPatch) {
        this.id = id;
        this.entityType = entityType;
        this.count = Math.max(1, count);
        this.chance = Math.max(0.0, Math.min(1.0, chance));
        this.spawnEffect = (spawnEffect == null || spawnEffect.isBlank()) ? "none" : spawnEffect;
        this.nbtPatch = nbtPatch == null ? new net.minecraft.nbt.CompoundTag() : nbtPatch;
        this.snbtPatch = snbtPatch == null ? new net.minecraft.nbt.CompoundTag() : snbtPatch;
        this.entries = List.of(); // empty => single mode
    }

    // table-mode ctor
    public SpawnDefinition(ResourceLocation id, List<SpawnEntry> entries) {
        this.id = id;
        this.entries = (entries == null) ? List.of() : List.copyOf(entries);

        // unused in table mode, but keep non-null defaults
        this.entityType = null;
        this.count = 1;
        this.chance = 1.0;
        this.spawnEffect = "none";
        this.nbtPatch = new net.minecraft.nbt.CompoundTag();
        this.snbtPatch = new net.minecraft.nbt.CompoundTag();
    }

    public boolean isTable() {
        return !entries.isEmpty();
    }

    public SpawnEntry pickEntry(RandomSource random) {
        if (!isTable()) {
            return new SpawnEntry(
                    1,
                    entityType,
                    count,
                    chance,
                    spawnEffect,
                    nbtPatch,
                    snbtPatch
            );
        }

        int total = 0;
        for (SpawnEntry e : entries) total += e.weight;
        if (total <= 0) {
            // all weights 0 -> nothing spawns; fallback: pick first if exists
            return entries.get(0);
        }

        int roll = random.nextInt(total);
        int acc = 0;
        for (SpawnEntry e : entries) {
            acc += e.weight;
            if (roll < acc) return e;
        }
        return entries.get(entries.size() - 1);
    }
}
