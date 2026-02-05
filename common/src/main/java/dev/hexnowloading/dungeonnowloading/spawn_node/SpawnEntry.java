package dev.hexnowloading.dungeonnowloading.spawn_node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

public class SpawnEntry {

    public final EntityType<?> entityType;
    public final int count;
    public final double chance;
    public final String spawnEffect;
    public final CompoundTag nbtPatch;
    public final CompoundTag snbtPatch;

    public SpawnEntry(EntityType<?> entityType, int count, double chance, String spawnEffect,
                      CompoundTag nbtPatch, CompoundTag snbtPatch) {

        this.entityType = entityType;
        this.count = Math.max(1, count);
        this.chance = Math.max(0.0, Math.min(1.0, chance));
        this.spawnEffect = (spawnEffect == null || spawnEffect.isBlank()) ? "none" : spawnEffect;
        this.nbtPatch = nbtPatch == null ? new CompoundTag() : nbtPatch;
        this.snbtPatch = snbtPatch == null ? new CompoundTag() : snbtPatch;
    }

    public CompoundTag combinedPatchCopy() {
        CompoundTag out = new CompoundTag();
        out.merge(nbtPatch);
        out.merge(snbtPatch);
        return out;
    }
}
