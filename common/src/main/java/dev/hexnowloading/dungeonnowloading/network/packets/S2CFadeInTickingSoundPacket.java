package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.DNLClientSoundHandler;
import dev.hexnowloading.dungeonnowloading.sound.TickingSoundTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class S2CFadeInTickingSoundPacket implements DNLPacket {

    private final int entityId;
    private final ResourceLocation soundId;
    private final TickingSoundTarget target;
    private final int tagId;
    private final float maxVolume;
    private final int fadeInTicks; // 🔥 NEW!

    // === Constructors ===

    public S2CFadeInTickingSoundPacket(int entityId, ResourceLocation soundId) {
        this(entityId, soundId, TickingSoundTarget.OLDEST, -1, 1.0f, 20);
    }

    public S2CFadeInTickingSoundPacket(int entityId, ResourceLocation soundId, float maxVolume, int fadeInTicks) {
        this(entityId, soundId, TickingSoundTarget.OLDEST, -1, maxVolume, fadeInTicks);
    }

    public S2CFadeInTickingSoundPacket(int entityId, ResourceLocation soundId, TickingSoundTarget target, float maxVolume, int fadeInTicks) {
        this(entityId, soundId, target, -1, maxVolume, fadeInTicks);
    }

    public S2CFadeInTickingSoundPacket(int entityId, ResourceLocation soundId, TickingSoundTarget target, int tagId, float maxVolume, int fadeInTicks) {
        this.entityId = entityId;
        this.soundId = soundId;
        this.target = target;
        this.tagId = tagId;
        this.maxVolume = maxVolume;
        this.fadeInTicks = fadeInTicks;
    }

    // === Encoding/Decoding ===

    public S2CFadeInTickingSoundPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.soundId = buf.readResourceLocation();
        this.target = buf.readEnum(TickingSoundTarget.class);
        this.tagId = buf.readVarInt();
        this.maxVolume = buf.readFloat();
        this.fadeInTicks = buf.readVarInt(); // 🔥 NEW
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeResourceLocation(soundId);
        buf.writeEnum(target);
        buf.writeVarInt(tagId);
        buf.writeFloat(maxVolume);
        buf.writeVarInt(fadeInTicks); // 🔥 NEW
    }

    public static S2CFadeInTickingSoundPacket decode(FriendlyByteBuf buf) {
        return new S2CFadeInTickingSoundPacket(buf);
    }

    // === Handle ===

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft.getInstance().execute(() -> {
            DNLClientSoundHandler.fadeInTickingSound(soundId, entityId, target, tagId, maxVolume, fadeInTicks);
        });
    }
}
