package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class DNLGameEvents {

    public static final Supplier<GameEvent> BLOCK_DESTROY_EARLY = register("block_destroy_early", () -> new GameEvent("block_destroy_early", 32));
    public static final Supplier<GameEvent> PLAYER_BLOCK_DESTROY_EARLY = register("player_block_destroy_early", () -> new GameEvent("player_block_destroy_early", 32));
    public static final Supplier<GameEvent> BLOCK_DESTROYED_BY_EXPLOSION = register("block_destroyed_by_explosion", () -> new GameEvent("block_destroyed_by_explosion", 32));
    public static final Supplier<GameEvent> BLOCK_CONTENT_DROPPING = register("block_content_dropping", () -> new GameEvent("block_content_dropped", 32));
    public static final Supplier<GameEvent> BLOCK_BURNED = register("block_burned", () -> new GameEvent("block_burned", 32));
    public static final Supplier<GameEvent> BLOCK_PUSHED_EARLY = register("block_pushed", () -> new GameEvent("block_pushed", 32));
    public static final Supplier<GameEvent> BLOCK_PUSHED_EARLY_FAILED = register("block_push_failed", () -> new GameEvent("block_push_failed", 32));

    public static <T extends GameEvent> Supplier<T> register(String name, Supplier<T> featureSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.GAME_EVENT, name, featureSupplier);
    }

    public static void init() {
    }

}
