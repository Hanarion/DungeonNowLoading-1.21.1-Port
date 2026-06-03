package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.screen.ClientScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class S2CWispwardLanternCartOpenConfigPacket implements DNLPacket {
    private final int entityId;
    private final int timerSeconds;

    public S2CWispwardLanternCartOpenConfigPacket(int entityId, int timerSeconds) {
        this.entityId = entityId;
        this.timerSeconds = timerSeconds;
    }

    public S2CWispwardLanternCartOpenConfigPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.timerSeconds = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeVarInt(this.timerSeconds);
    }

    public static S2CWispwardLanternCartOpenConfigPacket decode(FriendlyByteBuf buf) {
        return new S2CWispwardLanternCartOpenConfigPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        var minecraft = ClientUtil.getClient();
        if (minecraft == null) {
            return;
        }

        minecraft.execute(() -> ClientScreens.openWispwardLanternCartConfig(this.entityId, this.timerSeconds));
    }
}
