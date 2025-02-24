package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class DNLGameEvents {

    public static final Supplier<GameEvent> BLOCK_DESTROY_EARLY = register("block_destroy_early", () -> new GameEvent("block_destroy_early", 16));

    public static <T extends GameEvent> Supplier<T> register(String name, Supplier<T> featureSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.GAME_EVENT, name, featureSupplier);
    }

    public static void init() {
    }

}
