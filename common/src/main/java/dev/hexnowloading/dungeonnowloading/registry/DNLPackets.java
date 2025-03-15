package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.network.packets.C2SItemAnimationPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CScorcherHeatPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStructureDetectionPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CItemAnimationPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;

public class DNLPackets {
    public static void register() {
        Services.NETWORK.register("structure_detection", S2CStructureDetectionPacket.class, S2CStructureDetectionPacket::decode);
        Services.NETWORK.register("server_bound_item_animation_packet", C2SItemAnimationPacket.class, C2SItemAnimationPacket::decode);
        Services.NETWORK.register("client_bound_item_animation_packet", S2CItemAnimationPacket.class, S2CItemAnimationPacket::decode);
        Services.NETWORK.register("client_bound_scorcher_packet", S2CScorcherHeatPacket.class, S2CScorcherHeatPacket::decode);
    }
}
