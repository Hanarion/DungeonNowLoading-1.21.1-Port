package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 1.21 NeoForge networking is CustomPacketPayload-based. This generic wrapper carries an
 * existing {@link DNLPacket} so the common packet classes don't need rewriting; each
 * registered packet name gets its own {@link Type} (see {@link ForgeNetworkHelper}).
 */
public record DNLPayload(DNLPacket packet, CustomPacketPayload.Type<DNLPayload> type) implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return type;
    }
}
