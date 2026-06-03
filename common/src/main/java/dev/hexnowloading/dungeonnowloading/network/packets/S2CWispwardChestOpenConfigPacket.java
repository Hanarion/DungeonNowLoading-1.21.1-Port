package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.screen.ClientScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class S2CWispwardChestOpenConfigPacket implements DNLPacket {
    private final BlockPos pos;
    private final ResourceLocation lootTable;
    private final int requiredLitLanterns;

    public S2CWispwardChestOpenConfigPacket(BlockPos pos, ResourceLocation lootTable, int requiredLitLanterns) {
        this.pos = pos.immutable();
        this.lootTable = lootTable;
        this.requiredLitLanterns = requiredLitLanterns;
    }

    public S2CWispwardChestOpenConfigPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.lootTable = buf.readResourceLocation();
        this.requiredLitLanterns = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeResourceLocation(this.lootTable);
        buffer.writeVarInt(this.requiredLitLanterns);
    }

    public static S2CWispwardChestOpenConfigPacket decode(FriendlyByteBuf buf) {
        return new S2CWispwardChestOpenConfigPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        var minecraft = ClientUtil.getClient();
        if (minecraft == null) {
            return;
        }

        minecraft.execute(() -> ClientScreens.openWispwardChestConfig(this.pos, this.lootTable, this.requiredLitLanterns));
    }
}
