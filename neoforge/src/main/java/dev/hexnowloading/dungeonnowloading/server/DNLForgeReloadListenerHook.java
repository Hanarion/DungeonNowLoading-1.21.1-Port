package dev.hexnowloading.dungeonnowloading.server;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.ForgeReloadListenerPlatform;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = DungeonNowLoading.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class DNLForgeReloadListenerHook {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        for (ForgeReloadListenerPlatform.Entry e : ForgeReloadListenerPlatform.drainPending()) {
            event.addListener(e.listener()); // Forge doesn't require an ID
        }
    }
}
