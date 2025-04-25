package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.DNLClientSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class S2CStartTickingSoundPacket implements DNLPacket {

    private final int entityId;
    private final List<ResourceLocation> soundIds;
    private final boolean stopOnRecall;
    private final float maxVolume;
    private final float pitch;
    private final boolean stopWhenOutOfRange;
    private final float range;

    public S2CStartTickingSoundPacket(int entityId, ResourceLocation singleSoundId, boolean stopOnRecall) {
        this(entityId, List.of(singleSoundId), stopOnRecall, 1, 1, true, 32);
    }

    public S2CStartTickingSoundPacket(int entityId, List<ResourceLocation> soundIds, boolean stopOnRecall) {
        this(entityId, soundIds, stopOnRecall, 1, 1, true, 32);
    }

    public S2CStartTickingSoundPacket(int entityId, List<ResourceLocation> soundIds, boolean stopOnRecall, float maxVolume, float pitch, boolean stopWhenOutOfRange, float range) {
        this.entityId = entityId;
        this.soundIds = soundIds;
        this.stopOnRecall = stopOnRecall;
        this.maxVolume = maxVolume;
        this.pitch = pitch;
        this.stopWhenOutOfRange = stopWhenOutOfRange;
        this.range = range;
    }

    public S2CStartTickingSoundPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        int count = buf.readVarInt();
        this.soundIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.soundIds.add(buf.readResourceLocation());
        }
        this.stopOnRecall = buf.readBoolean();
        this.maxVolume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.stopWhenOutOfRange = buf.readBoolean();
        this.range = buf.readFloat();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(soundIds.size());
        for (ResourceLocation id : soundIds) {
            buf.writeResourceLocation(id);
        }
        buf.writeBoolean(stopOnRecall);
        buf.writeFloat(maxVolume);
        buf.writeFloat(pitch);
        buf.writeBoolean(stopWhenOutOfRange);
        buf.writeFloat(range);
    }

    public static S2CStartTickingSoundPacket decode(FriendlyByteBuf buf) {
        return new S2CStartTickingSoundPacket(buf);
    }


    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (mc.level == null) return;

        Entity entity = level.getEntity(entityId);

        for (ResourceLocation soundId : soundIds) {
            DNLClientSoundHandler.playTickingSound(soundId, entity, stopOnRecall, maxVolume, pitch, stopWhenOutOfRange, range);
        }
    }
}
