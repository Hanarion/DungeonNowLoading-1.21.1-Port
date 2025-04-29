package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.DNLClientSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class S2CStartTickingSoundPacket implements DNLPacket {

    private final int entityId;
    private final ResourceLocation soundId;
    private final SoundSource soundSource;
    private final int tagId;
    private final float maxVolume;
    private final float pitch;
    private final boolean stopWhenOutOfRange;
    private final float range;
    private final float fadeStartDistance;

    public S2CStartTickingSoundPacket(int entityId,ResourceLocation soundId, SoundSource soundSource, int tagId, float maxVolume, float pitch, boolean stopWhenOutOfRange, float range, float fadeStartDistance) {
        this.entityId = entityId;
        this.soundId = soundId;
        this.soundSource = soundSource;
        this.tagId = tagId;
        this.maxVolume = maxVolume;
        this.pitch = pitch;
        this.stopWhenOutOfRange = stopWhenOutOfRange;
        this.range = range;
        this.fadeStartDistance = fadeStartDistance;
    }

    public S2CStartTickingSoundPacket(int entityId, ResourceLocation soundId, SoundSource soundSource) {
        this(entityId, soundId, soundSource, -1, 1.0f, 1.0f, true, 32f, 0f);
    }

    public S2CStartTickingSoundPacket(int entityId, ResourceLocation soundId, SoundSource soundSource, float maxVolume, float pitch, boolean stopWhenOutOfRange, float range) {
        this(entityId, soundId, soundSource, -1, maxVolume, pitch, stopWhenOutOfRange, range, 0f);
    }

    public S2CStartTickingSoundPacket(int entityId, ResourceLocation soundId, SoundSource soundSource, float maxVolume, float pitch, boolean stopWhenOutOfRange, float range, float fadeStartDistance) {
        this(entityId, soundId, soundSource, -1, maxVolume, pitch, stopWhenOutOfRange, range, fadeStartDistance);
    }

    public S2CStartTickingSoundPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.soundId = buf.readResourceLocation();
        this.soundSource = buf.readEnum(SoundSource.class);
        this.tagId = buf.readInt();
        this.maxVolume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.stopWhenOutOfRange = buf.readBoolean();
        this.range = buf.readFloat();
        this.fadeStartDistance = buf.readFloat();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeResourceLocation(soundId);
        buf.writeEnum(soundSource);
        buf.writeInt(tagId);
        buf.writeFloat(maxVolume);
        buf.writeFloat(pitch);
        buf.writeBoolean(stopWhenOutOfRange);
        buf.writeFloat(range);
        buf.writeFloat(fadeStartDistance);
    }

    public static S2CStartTickingSoundPacket decode(FriendlyByteBuf buf) {
        return new S2CStartTickingSoundPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        Entity entity = level.getEntity(entityId);
        if (entity == null) return;

        DNLClientSoundHandler.playTickingSound(soundId, soundSource, entity, tagId, maxVolume, pitch, stopWhenOutOfRange, range, fadeStartDistance);
    }
}
