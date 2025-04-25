package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.BackgroundMusicHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class S2CFadeOutBackgroundMusicSoundPacket implements DNLPacket {

    private final int fadeOutDurationTicks;

    public S2CFadeOutBackgroundMusicSoundPacket(int fadeOutDurationTicks) {
        this.fadeOutDurationTicks = fadeOutDurationTicks;
    }

    public S2CFadeOutBackgroundMusicSoundPacket(FriendlyByteBuf buf) {
        this.fadeOutDurationTicks = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(fadeOutDurationTicks);
    }

    public static S2CFadeOutBackgroundMusicSoundPacket decode(FriendlyByteBuf buffer) {
        return new S2CFadeOutBackgroundMusicSoundPacket(buffer);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft.getInstance().execute(() -> {
            BackgroundMusicHandler.fadeOutCurrentMusic(fadeOutDurationTicks);
        });
    }
}
