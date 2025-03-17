package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.network.packets.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;

public class DNLPackets {
    public static void register() {
        Services.NETWORK.register("structure_detection", S2CStructureDetectionPacket.class, S2CStructureDetectionPacket::decode);
        Services.NETWORK.register("client_bound_start_item_animation_packet", S2CStartItemAnimationPacket.class, S2CStartItemAnimationPacket::decode);
        Services.NETWORK.register("client_bound_stop_item_animation_packet", S2CStopItemAnimationPacket.class, S2CStopItemAnimationPacket::decode);
        Services.NETWORK.register("client_bound_scorcher_packet", S2CScorcherHeatPacket.class, S2CScorcherHeatPacket::decode);
    }
}
