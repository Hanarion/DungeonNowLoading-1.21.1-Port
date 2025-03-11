package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.network.packets.ClientboundStructureDetectionPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;

public class DNLPackets {
    public static void register() {
        Services.NETWORK.register("structure_detection", ClientboundStructureDetectionPacket.class, ClientboundStructureDetectionPacket::decode);
    }
}
