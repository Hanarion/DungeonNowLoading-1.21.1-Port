package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.registry.DNLPackets;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class DNLForgeClient {
    public static void init(FMLClientSetupEvent event) {
        DNLPackets.registerClientbound();
        DNLPackets.registerServerbound();
        event.enqueueWork(DNLClient::registerMenuScreens);
    }

}
