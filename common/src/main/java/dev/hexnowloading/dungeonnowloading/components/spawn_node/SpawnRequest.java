package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record SpawnRequest(
        SpawnNode node,
        CompoundTag patch,
        BlockPos basePos
) {}
