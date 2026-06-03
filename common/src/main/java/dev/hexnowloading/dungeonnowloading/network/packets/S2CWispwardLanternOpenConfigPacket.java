package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.screen.ClientScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class S2CWispwardLanternOpenConfigPacket implements DNLPacket {
    private final BlockPos pos;
    private final int timerSeconds;

    public S2CWispwardLanternOpenConfigPacket(BlockPos pos, int timerSeconds) {
        this.pos = pos.immutable();
        this.timerSeconds = timerSeconds;
    }

    public S2CWispwardLanternOpenConfigPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.timerSeconds = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.timerSeconds);
    }

    public static S2CWispwardLanternOpenConfigPacket decode(FriendlyByteBuf buf) {
        return new S2CWispwardLanternOpenConfigPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        var minecraft = ClientUtil.getClient();
        if (minecraft == null) {
            return;
        }

        minecraft.execute(() -> ClientScreens.openWispwardLanternConfig(this.pos, this.timerSeconds));
    }
}
