package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispwardLanternCartEntity;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class C2SWispwardLanternCartConfigPacket implements DNLPacket {
    private static final double MAX_EDIT_DISTANCE_SQ = 64.0D * 64.0D;

    private final int entityId;
    private final int timerSeconds;

    public C2SWispwardLanternCartConfigPacket(int entityId, int timerSeconds) {
        this.entityId = entityId;
        this.timerSeconds = timerSeconds;
    }

    public C2SWispwardLanternCartConfigPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.timerSeconds = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeVarInt(this.timerSeconds);
    }

    public static C2SWispwardLanternCartConfigPacket decode(FriendlyByteBuf buf) {
        return new C2SWispwardLanternCartConfigPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null || !sender.getAbilities().instabuild) {
            return;
        }

        sender.server.execute(() -> {
            if (!(sender.serverLevel().getEntity(this.entityId) instanceof WispwardLanternCartEntity cart)
                    || !cart.isTimed()
                    || sender.distanceToSqr(cart) > MAX_EDIT_DISTANCE_SQ) {
                return;
            }

            cart.setTimerSeconds(this.timerSeconds);
            if (cart.isLit()) {
                cart.refreshTimedLight(sender.serverLevel().getGameTime());
            }
        });
    }
}
