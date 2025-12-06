package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.client.preview.PreviewOverlayForge;
import dev.hexnowloading.dungeonnowloading.registry.DNLPackets;
import net.minecraftforge.common.MinecraftForge;

public class DNLForgeClient {
    public static void init() {
        DNLPackets.registerClientbound();
        DNLPackets.registerServerbound();
        // register hologram preview overlay
        MinecraftForge.EVENT_BUS.addListener(PreviewOverlayForge::onRenderLevelStage);
    }

}
