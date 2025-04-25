package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.DNLClientSoundHandler;
import dev.hexnowloading.dungeonnowloading.sound.TickingSoundTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class S2CStopTickingSoundPacket implements DNLPacket {

    private final int entityId;
    private final ResourceLocation soundId;
    private final TickingSoundTarget target;
    private final int tagId;
    private final int fadeTicks;
    private final boolean shouldStop;

    // === Constructors ===

    // OLDEST (default)
    public S2CStopTickingSoundPacket(int entityId, ResourceLocation soundId, int fadeTicks, boolean shouldStop) {
        this(entityId, soundId, TickingSoundTarget.OLDEST, -1, fadeTicks, shouldStop);
    }

    public S2CStopTickingSoundPacket(int entityId, ResourceLocation soundId, TickingSoundTarget target,  int fadeTicks, boolean shouldStop) {
        this(entityId, soundId, target, -1, fadeTicks, shouldStop);
    }

    // SPECIFIC tagId
    public S2CStopTickingSoundPacket(int entityId, ResourceLocation soundId, int tagId, int fadeTicks, boolean shouldStop) {
        this(entityId, soundId, TickingSoundTarget.SPECIFIC, tagId, fadeTicks, shouldStop);
    }

    // Explicit target
    public S2CStopTickingSoundPacket(int entityId, ResourceLocation soundId, TickingSoundTarget target, int tagId, int fadeTicks, boolean shouldStop) {
        this.entityId = entityId;
        this.soundId = soundId;
        this.target = target;
        this.tagId = tagId;
        this.fadeTicks = fadeTicks;
        this.shouldStop = shouldStop;
    }

    // === Encoding / Decoding ===

    public S2CStopTickingSoundPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.soundId = buf.readResourceLocation();
        this.target = buf.readEnum(TickingSoundTarget.class);
        this.tagId = buf.readVarInt();
        this.fadeTicks = buf.readVarInt();
        this.shouldStop = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeResourceLocation(soundId);
        buf.writeEnum(target);
        buf.writeVarInt(tagId);
        buf.writeVarInt(fadeTicks);
        buf.writeBoolean(shouldStop);
    }

    public static S2CStopTickingSoundPacket decode(FriendlyByteBuf buf) {
        return new S2CStopTickingSoundPacket(buf);
    }

    // === Handler ===

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft.getInstance().execute(() -> {
            DNLClientSoundHandler.fadeOutTickingSound(soundId, entityId, target, tagId, fadeTicks, shouldStop);
        });
    }
}
