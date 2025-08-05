package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class S2CStructureDetectionPacket implements DNLPacket {

    private final boolean insideStructure;
    private final int playerId;

    private static final Map<UUID, Boolean> playerStructureStatus = new HashMap<>();


    public S2CStructureDetectionPacket(boolean insideStructure, int playerId) {
        this.insideStructure = insideStructure;
        this.playerId = playerId;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(insideStructure);
        buffer.writeInt(playerId);
    }

    public static S2CStructureDetectionPacket decode(FriendlyByteBuf buffer) {
        return new S2CStructureDetectionPacket(buffer.readBoolean(), buffer.readInt());
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender != null) return; // Only run client-side

        if (ClientUtil.onClient()) {
            if (ClientUtil.getClientLevel() == null) return;

            Entity entity = ClientUtil.getClientLevel().getEntity(playerId);
            if (entity != null) {
                playerStructureStatus.put(entity.getUUID(), insideStructure);
            }
        }
    }

    public static boolean isClientInStructure() {
        if (ClientUtil.getClientPlayer() == null) {
            return false;
        }
        UUID clientUUID = ClientUtil.getClientPlayer().getUUID();
        return playerStructureStatus.getOrDefault(clientUUID, false);
    }
}
