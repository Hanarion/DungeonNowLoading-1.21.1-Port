package dev.hexnowloading.dungeonnowloading.spawn_node;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class SpawnNode {

    public final ResourceLocation id;

    // single mode
    public final EntityType<?> entityType;
    public final int count;
    public final double chance;
    public final String spawnEffect;
    public final net.minecraft.nbt.CompoundTag nbtPatch;
    public final net.minecraft.nbt.CompoundTag snbtPatch;

    // optional multi-entry (no weights now)
    public final List<SpawnEntry> entries;

    // single ctor
    public SpawnNode(ResourceLocation id, EntityType<?> entityType, int count, double chance,
                     String spawnEffect,
                     net.minecraft.nbt.CompoundTag nbtPatch,
                     net.minecraft.nbt.CompoundTag snbtPatch) {

        this.id = id;
        this.entityType = entityType;
        this.count = Math.max(1, count);
        this.chance = Math.max(0.0, Math.min(1.0, chance));
        this.spawnEffect = (spawnEffect == null || spawnEffect.isBlank()) ? "none" : spawnEffect;
        this.nbtPatch = nbtPatch == null ? new net.minecraft.nbt.CompoundTag() : nbtPatch;
        this.snbtPatch = snbtPatch == null ? new net.minecraft.nbt.CompoundTag() : snbtPatch;
        this.entries = List.of();
    }

    // multi-entry ctor (unweighted random)
    public SpawnNode(ResourceLocation id, List<SpawnEntry> entries) {
        this.id = id;
        this.entries = entries == null ? List.of() : List.copyOf(entries);

        this.entityType = null;
        this.count = 1;
        this.chance = 1.0;
        this.spawnEffect = "none";
        this.nbtPatch = new net.minecraft.nbt.CompoundTag();
        this.snbtPatch = new net.minecraft.nbt.CompoundTag();
    }

    public boolean isMulti() {
        return !entries.isEmpty();
    }

    public SpawnEntry pickEntry(RandomSource random) {
        if (!isMulti()) {
            return new SpawnEntry(
                    entityType,
                    count,
                    chance,
                    spawnEffect,
                    nbtPatch,
                    snbtPatch
            );
        }

        // simple random (no weights)
        return entries.get(random.nextInt(entries.size()));
    }
}
