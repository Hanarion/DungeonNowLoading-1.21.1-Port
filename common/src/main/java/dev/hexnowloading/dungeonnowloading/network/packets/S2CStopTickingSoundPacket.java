package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.DNLClientSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class S2CStopTickingSoundPacket implements DNLPacket {

    private final int entityId;
    private final List<ResourceLocation> soundIds;

    public S2CStopTickingSoundPacket(int entityId, ResourceLocation singleSoundId) {
        this.entityId = entityId;
        this.soundIds = List.of(singleSoundId);
    }

    public S2CStopTickingSoundPacket(int entityId, List<ResourceLocation> soundIds) {
        this.entityId = entityId;
        this.soundIds = soundIds;
    }

    public S2CStopTickingSoundPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        int count = buf.readVarInt();
        this.soundIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.soundIds.add(buf.readResourceLocation());
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(soundIds.size());
        for (ResourceLocation id : soundIds) {
            buf.writeResourceLocation(id);
        }
    }

    public static S2CStopTickingSoundPacket decode(FriendlyByteBuf buf) {
        return new S2CStopTickingSoundPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft.getInstance().execute(() -> {
            for (ResourceLocation soundId : soundIds) {
                DNLClientSoundHandler.fadeOutLoopingSound(soundId, entityId);
            }
        });
    }
}