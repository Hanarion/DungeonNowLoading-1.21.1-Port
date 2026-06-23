package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.platform.services.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 1.21 NeoForge networking. Common registers packets via {@link #register} at mod init;
 * those are buffered and flushed onto a {@link PayloadRegistrar} during
 * RegisterPayloadHandlersEvent (mod bus). Each DNLPacket is wrapped in {@link DNLPayload}.
 */
public class ForgeNetworkHelper implements NetworkHelper {

    private static final String PROTOCOL_VERSION = "1";

    private record Entry<T extends DNLPacket>(CustomPacketPayload.Type<DNLPayload> type,
                                              Function<FriendlyByteBuf, T> constructor) {}

    private static final List<Entry<?>> ENTRIES = new ArrayList<>();
    private static final java.util.Map<Class<?>, CustomPacketPayload.Type<DNLPayload>> TYPES = new java.util.HashMap<>();
    private static boolean flushed = false;

    @Override
    public <T extends DNLPacket> void register(String name, Class<T> clazz, Function<FriendlyByteBuf, T> constructor) {
        CustomPacketPayload.Type<DNLPayload> type =
                new CustomPacketPayload.Type<>(DungeonNowLoading.id(name));
        ENTRIES.add(new Entry<>(type, constructor));
        TYPES.put(clazz, type);
    }

    /**
     * Flush buffered registrations; call from RegisterPayloadHandlersEvent (mod bus).
     * NeoForge can fire this event again on a later NetworkRegistry.setup (resource reload) within
     * the same JVM, and payloads are registered process-globally — flushing twice throws
     * "already registered". Guard so we register exactly once.
     */
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        if (flushed) {
            return;
        }
        flushed = true;
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        for (Entry<?> entry : ENTRIES) {
            registerEntry(registrar, entry);
        }
    }

    private static <T extends DNLPacket> void registerEntry(PayloadRegistrar registrar, Entry<T> entry) {
        StreamCodec<RegistryFriendlyByteBuf, DNLPayload> codec = StreamCodec.of(
                (buf, payload) -> payload.packet().encode(buf),
                buf -> new DNLPayload(entry.constructor().apply(buf), entry.type())
        );
        registrar.playBidirectional(entry.type(), codec, (payload, context) ->
                context.enqueueWork(() -> payload.packet().handle(
                        context.player() instanceof ServerPlayer sp ? sp : null)));
    }

    private DNLPayload wrap(DNLPacket packet) {
        return new DNLPayload(packet, TYPES.get(packet.getClass()));
    }

    @Override
    public void sendToPlayer(DNLPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, wrap(packet));
    }

    @Override
    public void sendToAllPlayers(DNLPacket packet, MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(wrap(packet));
    }

    @Override
    public void sendToServer(DNLPacket packet) {
        PacketDistributor.sendToServer(wrap(packet));
    }
}
