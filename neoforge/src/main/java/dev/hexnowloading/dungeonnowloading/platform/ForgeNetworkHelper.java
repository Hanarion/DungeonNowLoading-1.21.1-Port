package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.platform.services.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class ForgeNetworkHelper implements NetworkHelper {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            DungeonNowLoading.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private int id = 0;

    @Override
    public <T extends DNLPacket> void register(String name, Class<T> clazz, Function<FriendlyByteBuf, T> constructor) {
        INSTANCE.registerMessage(id++, clazz,
                DNLPacket::encode,
                constructor,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() -> packet.handle(ctx.get().getSender()));
                    ctx.get().setPacketHandled(true);
                });
    }

    @Override
    public void sendToPlayer(DNLPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @Override
    public void sendToAllPlayers(DNLPacket packet, MinecraftServer server) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    @Override
    public void sendToServer(DNLPacket packet) {
        INSTANCE.sendToServer(packet);
    }
}
