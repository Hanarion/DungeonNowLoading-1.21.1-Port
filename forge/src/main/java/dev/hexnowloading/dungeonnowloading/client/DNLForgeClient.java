package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.registry.DNLPackets;

public class DNLForgeClient {
    public static void init() {
        DNLPackets.registerClientbound();
    }
}
