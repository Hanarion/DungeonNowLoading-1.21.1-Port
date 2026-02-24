package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import dev.hexnowloading.dungeonnowloading.components.spawn_node.spawn_effect.InstantSpawnEffect;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.spawn_effect.PoofDelaySpreadSpawnEffect;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.spawn_effect.PoofSpawnEffect;

public final class DNLSpawnEffects {
    private DNLSpawnEffects() {}

    public static SpawnTask createTask(String id, SpawnRequest req) {
        String key = (id == null || id.isBlank()) ? "none" : id;

        return switch (key) {
            case "none" -> new InstantSpawnEffect(req);
            case "poof" -> new PoofSpawnEffect(req);
            case "poof_delay_spread" -> new PoofDelaySpreadSpawnEffect(req, 20, 2, 4);
            default -> new InstantSpawnEffect(req);
        };
    }
}
