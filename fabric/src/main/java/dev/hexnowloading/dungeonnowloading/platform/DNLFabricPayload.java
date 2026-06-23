package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 1.21 Fabric networking is CustomPacketPayload-based (same vanilla type NeoForge uses). This
 * generic wrapper carries an existing {@link DNLPacket} so the common packet classes don't need
 * rewriting; each registered packet name gets its own {@link Type} (see FabricNetworkHelper).
 */
public record DNLFabricPayload(DNLPacket packet, Type<DNLFabricPayload> payloadType) implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return payloadType;
    }
}
