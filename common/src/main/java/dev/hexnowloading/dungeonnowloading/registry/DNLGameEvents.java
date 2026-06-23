package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class DNLGameEvents {

    public static final Supplier<GameEvent> BLOCK_DESTROY_EARLY = register("block_destroy_early", () -> new GameEvent(32));
    public static final Supplier<GameEvent> PLAYER_BLOCK_DESTROY_EARLY = register("player_block_destroy_early", () -> new GameEvent(32));
    public static final Supplier<GameEvent> BLOCK_DESTROYED_BY_EXPLOSION = register("block_destroyed_by_explosion", () -> new GameEvent(32));
    public static final Supplier<GameEvent> BLOCK_CONTENT_DROPPING = register("block_content_dropping", () -> new GameEvent(32));
    public static final Supplier<GameEvent> BLOCK_BURNED = register("block_burned", () -> new GameEvent(32));
    public static final Supplier<GameEvent> BLOCK_PUSHED_EARLY = register("block_pushed", () -> new GameEvent(32));
    public static final Supplier<GameEvent> BLOCK_PUSHED_EARLY_FAILED = register("block_push_failed", () -> new GameEvent(32));

    public static <T extends GameEvent> Supplier<T> register(String name, Supplier<T> featureSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.GAME_EVENT, name, featureSupplier);
    }

    /** 1.21 GameEventListener/Level.gameEvent take Holder&lt;GameEvent&gt;; wrap a registered event. */
    public static net.minecraft.core.Holder<GameEvent> holder(Supplier<GameEvent> event) {
        return BuiltInRegistries.GAME_EVENT.wrapAsHolder(event.get());
    }

    public static void init() {
    }

}
