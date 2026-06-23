package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.platform.services.NetworkHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 1.21 Fabric networking. Each {@link DNLPacket} is wrapped in a {@link DNLFabricPayload}
 * (a {@link CustomPacketPayload}). Registration happens in {@link #register} at common mod init;
 * the payload codec is registered with Fabric's {@link PayloadTypeRegistry} and the receiver with
 * Server/ClientPlayNetworking. Mirrors the NeoForge side.
 */
public class FabricNetworkHelper implements NetworkHelper {

    private final Map<Class<? extends DNLPacket>, CustomPacketPayload.Type<DNLFabricPayload>> ids = new HashMap<>();

    private CustomPacketPayload.Type<DNLFabricPayload> getType(DNLPacket packet) {
        return Objects.requireNonNull(ids.get(packet.getClass()), "Used unregistered DNL packet: " + packet.getClass());
    }

    @Override
    public <T extends DNLPacket> void register(String name, Class<T> clazz, Function<FriendlyByteBuf, T> constructor) {
        CustomPacketPayload.Type<DNLFabricPayload> type = new CustomPacketPayload.Type<>(DungeonNowLoading.id(name));
        ids.put(clazz, type);

        // StreamCodec: encode by delegating to the DNLPacket, decode by running the ctor into a payload.
        StreamCodec<RegistryFriendlyByteBuf, DNLFabricPayload> codec = StreamCodec.of(
                (buf, payload) -> payload.packet().encode(buf),
                buf -> new DNLFabricPayload(constructor.apply(buf), type)
        );

        // Register the codec on both directions (a payload that only travels one way just has an
        // unused codec in the other; Fabric requires the codec registered for any direction it's sent).
        PayloadTypeRegistry.playC2S().register(type, codec);
        PayloadTypeRegistry.playS2C().register(type, codec);

        // Server receives serverbound (C2S) payloads.
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                context.player().server.execute(() -> payload.packet().handle(context.player())));

        // Client receives clientbound (S2C) payloads — only on the client env (dedicated-server safe).
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    context.client().execute(() -> payload.packet().handle(null)));
        }
    }

    @Override
    public void sendToPlayer(DNLPacket packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, wrap(packet));
    }

    @Override
    public void sendToAllPlayers(DNLPacket packet, MinecraftServer server) {
        DNLFabricPayload payload = wrap(packet);
        for (ServerPlayer player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToServer(DNLPacket packet) {
        ClientPlayNetworking.send(wrap(packet));
    }

    private DNLFabricPayload wrap(DNLPacket packet) {
        return new DNLFabricPayload(packet, getType(packet));
    }
}
