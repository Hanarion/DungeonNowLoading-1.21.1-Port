package dev.hexnowloading.dungeonnowloading.components.spawn_node.spawn_effect;

import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.SpawnRequest;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.SpawnTask;
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

        int slots = Math.max(1, req.node().count);
        double chance = req.node().chance;

        for (int i = 0; i < slots; i++) {
            if (!roll(level, chance)) continue;
            director.spawnOne(level, req.node(), req.patch(), req.basePos());
        }

        done = true;
        return true;
    }

    private static boolean roll(ServerLevel level, double chance) {
        if (chance >= 1.0) return true;
        if (chance <= 0.0) return false;
        return level.random.nextDouble() < chance;
    }
}
