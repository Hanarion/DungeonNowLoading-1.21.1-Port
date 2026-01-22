package dev.hexnowloading.dungeonnowloading.spawn_node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SpawnDefinition {
    public final ResourceLocation id;
    public final EntityType<?> entityType;
    public final int count;
    public final double chance;          // 0..1, default 1
    public final String spawnEffect;

    // Optional patches (either/both can exist)
    public final CompoundTag nbtPatch;      // from JSON object
    public final CompoundTag snbtPatch;     // parsed from SNBT string

    public SpawnDefinition(ResourceLocation id, EntityType<?> entityType, int count, double chance, String spawnEffect,
                           CompoundTag nbtPatch, CompoundTag snbtPatch) {
        this.id = id;
        this.entityType = entityType;
        this.count = count;
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
