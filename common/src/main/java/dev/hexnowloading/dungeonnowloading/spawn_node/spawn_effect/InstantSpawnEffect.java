package dev.hexnowloading.dungeonnowloading.spawn_node.spawn_effect;

import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import dev.hexnowloading.dungeonnowloading.spawn_node.SpawnRequest;
import dev.hexnowloading.dungeonnowloading.spawn_node.SpawnTask;
import net.minecraft.server.level.ServerLevel;

public class InstantSpawnEffect implements SpawnTask {

    private final SpawnRequest req;
    private boolean done = false;

    public InstantSpawnEffect(SpawnRequest req) {
        this.req = req;
    }

    @Override
    public boolean tick(ServerLevel level, DungeonDirectorBlockEntity director) {
        if (done) return true;

        int count = Math.max(1, req.def().count);
        for (int i = 0; i < count; i++) {
            director.spawnOne(level, req.def(), req.patch(), req.basePos());
        }

        done = true;
        return true;
    }
}
