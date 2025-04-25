package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.DNLClientSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class S2CFadeInTickingSoundPacket implements DNLPacket {

    private final int entityId;
    private final List<ResourceLocation> soundIds;
    private final float maxVolume;

    public S2CFadeInTickingSoundPacket(int entityId, ResourceLocation singleSoundId, float maxVolume) {
        this.entityId = entityId;
        this.soundIds = List.of(singleSoundId);
        this.maxVolume = maxVolume;
    }

    public S2CFadeInTickingSoundPacket(int entityId, List<ResourceLocation> soundIds, float maxVolume) {
        this.entityId = entityId;
        this.soundIds = soundIds;
        this.maxVolume = maxVolume;
    }

    public S2CFadeInTickingSoundPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.maxVolume = buffer.readFloat();
        int count = buffer.readVarInt();
        this.soundIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.soundIds.add(buffer.readResourceLocation());
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeFloat(maxVolume);
        buffer.writeVarInt(soundIds.size());
        for (ResourceLocation id : soundIds) {
            buffer.writeResourceLocation(id);
        }
    }

    public static S2CFadeInTickingSoundPacket decode(FriendlyByteBuf buffer) {
        return new S2CFadeInTickingSoundPacket(buffer);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft.getInstance().execute(() -> {
            for (ResourceLocation soundId : soundIds) {
                DNLClientSoundHandler.fadeInTickingSound(soundId, entityId, maxVolume);
            }
        });
    }
}
