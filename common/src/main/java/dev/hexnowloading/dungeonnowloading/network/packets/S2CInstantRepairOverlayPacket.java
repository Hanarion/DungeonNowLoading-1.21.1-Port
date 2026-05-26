package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.client.render.MendingAuraOverlayRenderer;
import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class S2CInstantRepairOverlayPacket implements DNLPacket {
    private final BlockPos pos;

    public S2CInstantRepairOverlayPacket(BlockPos pos) {
        this.pos = pos.immutable();
    }

    private S2CInstantRepairOverlayPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public static S2CInstantRepairOverlayPacket decode(FriendlyByteBuf buffer) {
        return new S2CInstantRepairOverlayPacket(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender != null || !ClientUtil.onClient()) {
            return;
        }

        var minecraft = ClientUtil.getClient();
        if (minecraft == null) {
            return;
        }

        minecraft.execute(() -> MendingAuraOverlayRenderer.add(this.pos));
    }
}
