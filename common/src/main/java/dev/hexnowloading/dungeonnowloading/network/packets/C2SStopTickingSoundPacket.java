package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class C2SStopTickingSoundPacket implements DNLPacket {

    private final ResourceLocation soundId;
    private final double radius;

    public C2SStopTickingSoundPacket(ResourceLocation soundId, double radius) {
        this.soundId = soundId;
        this.radius = radius;
    }

    public C2SStopTickingSoundPacket(FriendlyByteBuf buf) {
        this.soundId = buf.readResourceLocation();
        this.radius = buf.readDouble();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(soundId);
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

        for (ServerPlayer player : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(
                    new S2CStopTickingSoundPacket(sender.getId(), soundId, 20, true),
                    player
            );
        }
    }
}
