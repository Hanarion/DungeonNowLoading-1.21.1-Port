package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.RecallableDef;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static dev.hexnowloading.dungeonnowloading.DungeonNowLoading.id;

public final class DNLRecallables {


    public static void registerAll() {
        register(id("chaos_spawner"), new RecallableDef(() -> DNLItems.SKULL_OF_CHAOS.get(), Component.translatable("entity.dungeonnowloading.chaos_spawner"), ChaosSpawnerEntity::spawnRecalledStatic, ChaosSpawnerEntity::disperseStatic));
        register(id("fairkeeper_serpent_caller"), new RecallableDef(() -> DNLItems.REDSTONE_IDOL.get(), Component.translatable("entity.dungeonnowloading.fairkeeper_serpent_caller"), FairkeeperSerpentCallerEntity::spawnRecalled, FairkeeperSerpentCallerEntity::disperse));
    }

    private static final Map<ResourceLocation, RecallableDef> BY_ID = new HashMap<>();

    public static void register(ResourceLocation id, RecallableDef def) {
        BY_ID.put(id, def);
    }

    public static RecallableDef get(ResourceLocation id) {
        return BY_ID.get(id);
    }
}
