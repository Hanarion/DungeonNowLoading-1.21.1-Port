package dev.hexnowloading.dungeonnowloading.spawn_node;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record SpawnRequest(
        SpawnDefinition def,
        CompoundTag patch,
        BlockPos basePos
) {}
