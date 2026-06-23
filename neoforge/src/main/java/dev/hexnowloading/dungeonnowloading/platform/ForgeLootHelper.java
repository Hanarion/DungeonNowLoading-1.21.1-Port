package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.LootHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = "dungeonnowloading", bus = EventBusSubscriber.Bus.GAME)
public class ForgeLootHelper implements LootHelper {

    private static final Map<ResourceLocation, LootPool> POOLS = new HashMap<>();

    @Override
    public void injectLoot(ResourceLocation id, LootPool pool) {
        POOLS.put(id, pool);
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootPool pool = POOLS.get(event.getName());
        if (pool != null) {
            event.getTable().addPool(pool);
        }
    }
}
