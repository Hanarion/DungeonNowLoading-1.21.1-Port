package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.events.DNLFabricBlockEvents;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.server.entity.DNLFabricEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class DNLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DungeonNowLoading.init();
        registerEvents();
        registerEntityAttributes();
        registerPackets();
        DNLFabricEntities.registerSpawnPlacements();
        DungeonNowLoading.LOGGER.info("Hello Fabric world!");
    }

    private void registerEvents() {
        DNLFabricBlockEvents.init();
    }

    private void registerEntityAttributes() {
        for (EntityType<? extends LivingEntity> type : DNLEntityTypes.getAllAttributes().keySet()) {
            FabricDefaultAttributeRegistry.register(type, DNLEntityTypes.getAllAttributes().get(type));
        }
    }

    private void registerPackets() {
    }
}
