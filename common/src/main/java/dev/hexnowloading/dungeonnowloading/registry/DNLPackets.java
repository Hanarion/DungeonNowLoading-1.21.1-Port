package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.network.packets.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;

public class DNLPackets {
    public static void registerServerbound() {
        System.out.println("[DNLPackets] registerServerbound()");
        Services.NETWORK.register("serverbound_stop_ticking_sound", C2SStopTickingSoundPacket.class, C2SStopTickingSoundPacket::decode);
        Services.NETWORK.register("serverbound_pedestal_edit", C2SPedestalEditPacket.class, C2SPedestalEditPacket::decode);
        Services.NETWORK.register("serverbound_pedestal_update", C2SPedestalUpdatePacket.class, C2SPedestalUpdatePacket::decode);
        Services.NETWORK.register("serverbound_wispward_chest_config", C2SWispwardChestConfigPacket.class, C2SWispwardChestConfigPacket::decode);
        Services.NETWORK.register("serverbound_wispward_lantern_config", C2SWispwardLanternConfigPacket.class, C2SWispwardLanternConfigPacket::decode);
        Services.NETWORK.register("serverbound_wispward_lantern_cart_config", C2SWispwardLanternCartConfigPacket.class, C2SWispwardLanternCartConfigPacket::decode);
    }

    public static void registerClientbound() {
        Services.NETWORK.register("clientbound_structure_detection", S2CStructureDetectionPacket.class, S2CStructureDetectionPacket::decode);
        Services.NETWORK.register("clientbound_start_ticking_sound", S2CStartTickingSoundPacket.class, S2CStartTickingSoundPacket::decode);
        Services.NETWORK.register("clientbound_stop_ticking_sound", S2CStopTickingSoundPacket.class, S2CStopTickingSoundPacket::decode);
        Services.NETWORK.register("clientbound_fade_in_ticking_sound", S2CFadeInTickingSoundPacket.class, S2CFadeInTickingSoundPacket::decode);
        Services.NETWORK.register("clientbound_fade_out_background_music", S2CFadeOutBackgroundMusicSoundPacket.class, S2CFadeOutBackgroundMusicSoundPacket::decode);
        Services.NETWORK.register("clientbound_open_pedestal_editor", S2CPedestalOpenEditorPacket.class, S2CPedestalOpenEditorPacket::decode);
        Services.NETWORK.register("clientbound_open_wispward_chest_config", S2CWispwardChestOpenConfigPacket.class, S2CWispwardChestOpenConfigPacket::decode);
        Services.NETWORK.register("clientbound_open_wispward_lantern_config", S2CWispwardLanternOpenConfigPacket.class, S2CWispwardLanternOpenConfigPacket::decode);
        Services.NETWORK.register("clientbound_open_wispward_lantern_cart_config", S2CWispwardLanternCartOpenConfigPacket.class, S2CWispwardLanternCartOpenConfigPacket::decode);
        Services.NETWORK.register("clientbound_mending_aura_sync", S2CMendingAuraSyncPacket.class, S2CMendingAuraSyncPacket::decode);
        Services.NETWORK.register("clientbound_instant_repair_overlay", S2CInstantRepairOverlayPacket.class, S2CInstantRepairOverlayPacket::decode);
    }
}
