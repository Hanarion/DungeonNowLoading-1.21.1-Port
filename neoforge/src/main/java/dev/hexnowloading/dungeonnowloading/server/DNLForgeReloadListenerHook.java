package dev.hexnowloading.dungeonnowloading.server;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.ForgeReloadListenerPlatform;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DungeonNowLoading.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DNLForgeReloadListenerHook {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        for (ForgeReloadListenerPlatform.Entry e : ForgeReloadListenerPlatform.drainPending()) {
            event.addListener(e.listener()); // Forge doesn't require an ID
        }
    }
}
