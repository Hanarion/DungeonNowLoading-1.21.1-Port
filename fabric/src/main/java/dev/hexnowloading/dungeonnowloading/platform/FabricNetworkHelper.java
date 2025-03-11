package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.platform.services.NetworkHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class FabricNetworkHelper implements NetworkHelper {

    private final Map<Class<? extends DNLPacket>, ResourceLocation> ids = new HashMap<>();

    private ResourceLocation getId(DNLPacket packet) {
        return Objects.requireNonNull(ids.get(packet.getClass()), "Used unregistered message!");
    }

    @Override
    public <T extends DNLPacket> void register(String name, Class<T> clazz, Function<FriendlyByteBuf, T> constructor) {
        ResourceLocation id = DungeonNowLoading.id(name);
        ids.put(clazz, id);

        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, sender) -> {
            DNLPacket packet = constructor.apply(buffer);
            server.execute(() -> packet.handle(player));
        });

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            ClientProxy.register(id, constructor);
    }

    @Override
    public void sendToPlayer(DNLPacket packet, ServerPlayer player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buffer);
        ServerPlayNetworking.send(player, getId(packet), buffer);
    }

    @Override
    public void sendToAllPlayers(DNLPacket packet, MinecraftServer server) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buffer);

        for(ServerPlayer player : PlayerLookup.all(server))
            ServerPlayNetworking.send(player, getId(packet), buffer);
    }

    @Override
    public void sendToServer(DNLPacket packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buffer);
        ClientPlayNetworking.send(getId(packet), buffer);
    }


    // Proxy class to prevent dedicated servers from crashing
    private static final class ClientProxy {

        public static <T extends DNLPacket> void register(ResourceLocation id, Function<FriendlyByteBuf, T> constructor) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                DNLPacket packet = constructor.apply(buffer);
                client.execute(() -> packet.handle(null));
            });
        }

    }
}
