package dev.hexnowloading.dungeonnowloading.server;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.ForgeCommonRegistryHelper;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = DungeonNowLoading.MOD_ID, bus= EventBusSubscriber.Bus.GAME)
public class CommonEventsForge {
    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        for (SimpleJsonResourceReloadListener loader : ForgeCommonRegistryHelper.dataLoaders)
            event.addListener(loader);
    }
}
