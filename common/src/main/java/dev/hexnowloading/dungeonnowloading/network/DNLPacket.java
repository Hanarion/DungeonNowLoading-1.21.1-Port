package dev.hexnowloading.dungeonnowloading.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface DNLPacket {
    void encode(FriendlyByteBuf buffer);
    void handle(@Nullable ServerPlayer sender);
}
