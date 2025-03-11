package dev.hexnowloading.dungeonnowloading.platform.services;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

public interface NetworkHelper {
    <T extends DNLPacket> void register(String name, Class<T> clazz, Function<FriendlyByteBuf, T> constructor);

    void sendToPlayer(DNLPacket packet, ServerPlayer player);

    void sendToAllPlayers(DNLPacket packet, MinecraftServer server);

    void sendToServer(DNLPacket packet);

}
