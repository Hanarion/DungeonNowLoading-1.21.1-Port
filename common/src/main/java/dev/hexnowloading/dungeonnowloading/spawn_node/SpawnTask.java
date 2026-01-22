package dev.hexnowloading.dungeonnowloading.spawn_node;

import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import net.minecraft.server.level.ServerLevel;

public interface SpawnTask {
    /** @return true when finished */
    boolean tick(ServerLevel level, DungeonDirectorBlockEntity director);
}
