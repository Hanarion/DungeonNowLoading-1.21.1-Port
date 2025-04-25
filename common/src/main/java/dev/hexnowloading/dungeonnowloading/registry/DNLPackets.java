package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.network.packets.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;

public class DNLPackets {
    public static void register() {
        Services.NETWORK.register("clientbound_structure_detection", S2CStructureDetectionPacket.class, S2CStructureDetectionPacket::decode);
        Services.NETWORK.register("clientbound_start_ticking_sound", S2CStartTickingSoundPacket.class, S2CStartTickingSoundPacket::decode);
        Services.NETWORK.register("clientbound_stop_ticking_sound", S2CStopTickingSoundPacket.class, S2CStopTickingSoundPacket::decode);
        Services.NETWORK.register("clientbound_fade_in_ticking_sound", S2CFadeInTickingSoundPacket.class, S2CFadeInTickingSoundPacket::decode);
        Services.NETWORK.register("serverbound_stop_ticking_sound", C2SStopTickingSoundPacket.class, C2SStopTickingSoundPacket::decode);
    }
}
