package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.events.DNLFabricBlockEvents;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLCommands;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.server.entity.DNLFabricEntities;
import dev.hexnowloading.dungeonnowloading.supporter.PatronRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class DNLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DungeonNowLoading.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {DNLCommands.register(dispatcher);});

        registerEvents();
        registerEntityAttributes();
        registerPackets();
        DNLFabricEntities.registerSpawnPlacements();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // Wire the cached registry access used by common code (e.g. ItemNbt NBT
            // (de)serialization, DNLEnchantments.holder) that has no Level at hand. Mirrors the
            // NeoForge ServerStartingEvent listener in DNLForge.
            DungeonNowLoading.setRegistryAccess(server.registryAccess());
            PatronRegistry.initOrReload(server);
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, rm, success) -> {
            DungeonNowLoading.setRegistryAccess(server.registryAccess());
            PatronRegistry.initOrReload(server);
        });

        //DungeonNowLoading.LOGGER.info("Hello Fabric world!");
    }

    private void registerEvents() {
        DNLFabricBlockEvents.init();
    }

    private void registerEntityAttributes() {
        for (EntityType<? extends LivingEntity> type : DNLEntityTypes.getAllAttributes().keySet()) {
            FabricDefaultAttributeRegistry.register(type, DNLEntityTypes.getAllAttributes().get(type));
        }
    }

    private void registerPackets() { }
}
