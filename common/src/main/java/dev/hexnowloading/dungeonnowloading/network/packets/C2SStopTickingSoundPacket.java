package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class C2SStopTickingSoundPacket implements DNLPacket {

    private final List<ResourceLocation> soundIds;
    private final double radius;

    public C2SStopTickingSoundPacket(ResourceLocation singleSoundId, double radius) {
        this.soundIds = List.of(singleSoundId);
        this.radius = radius;
    }

    public C2SStopTickingSoundPacket(List<ResourceLocation> soundIds, double radius) {
        this.soundIds = soundIds;
        this.radius = radius;
    }

    public C2SStopTickingSoundPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.soundIds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.soundIds.add(buf.readResourceLocation());
        }
        this.radius = buf.readDouble();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(soundIds.size());
        for (ResourceLocation id : soundIds) {
            buf.writeResourceLocation(id);
        }
        buf.writeDouble(radius);
    }

    public static C2SStopTickingSoundPacket decode(FriendlyByteBuf buf) {
        return new C2SStopTickingSoundPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null) return;

        AABB area = sender.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = sender.level().getEntitiesOfClass(ServerPlayer.class, area);

        for (ResourceLocation soundId : soundIds) {
            S2CStopTickingSoundPacket packet = new S2CStopTickingSoundPacket(sender.getId(), soundId);
            for (ServerPlayer player : nearbyPlayers) {
                Services.NETWORK.sendToPlayer(packet, player);
            }
        }
    }
}
