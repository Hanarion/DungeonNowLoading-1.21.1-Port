package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.DNLClient;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class DNLForgeClient {
    public static void init(FMLClientSetupEvent event) {
        // Packets are already registered by DungeonNowLoading.init() (common); do NOT re-register
        // them here — doing so doubled the buffered payload list and threw "already registered".
        event.enqueueWork(DNLClient::registerMenuScreens);
    }

}
