package dev.hexnowloading.dungeonnowloading.entity.util;

import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

public record RecallableDef(
        Item recallItem,
        Component displayName,
        RecallSpawner spawner,
        SoulDisperseHandler disperseHandler
) {
    @FunctionalInterface public interface RecallSpawner {
        void spawn(ServerLevel level, SeepingSoulEntity soul, int defeatedCount);
    }
    @FunctionalInterface public interface SoulDisperseHandler {
        void handle(ServerLevel level, SeepingSoulEntity soul, int defeatedCount);
    }
}
