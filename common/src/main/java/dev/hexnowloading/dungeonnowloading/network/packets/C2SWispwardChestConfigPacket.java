package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.block.entity.WispwardChestBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public class C2SWispwardChestConfigPacket implements DNLPacket {
    private static final double MAX_EDIT_DISTANCE_SQ = 64.0D * 64.0D;

    private final BlockPos pos;
    private final ResourceLocation lootTable;
    private final int requiredLitLanterns;
    private final boolean resetReward;

    public C2SWispwardChestConfigPacket(BlockPos pos, ResourceLocation lootTable, int requiredLitLanterns) {
        this(pos, lootTable, requiredLitLanterns, false);
    }

    public C2SWispwardChestConfigPacket(BlockPos pos, ResourceLocation lootTable, int requiredLitLanterns, boolean resetReward) {
        this.pos = pos.immutable();
        this.lootTable = lootTable;
        this.requiredLitLanterns = requiredLitLanterns;
        this.resetReward = resetReward;
    }

    public C2SWispwardChestConfigPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.lootTable = buf.readResourceLocation();
        this.requiredLitLanterns = buf.readVarInt();
        this.resetReward = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeResourceLocation(this.lootTable);
        buffer.writeVarInt(this.requiredLitLanterns);
        buffer.writeBoolean(this.resetReward);
    }

    public static C2SWispwardChestConfigPacket decode(FriendlyByteBuf buf) {
        return new C2SWispwardChestConfigPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null || !sender.getAbilities().instabuild) {
            return;
        }

        sender.server.execute(() -> {
            ServerLevel level = sender.serverLevel();
            if (!level.isLoaded(this.pos) || sender.distanceToSqr(this.pos.getCenter()) > MAX_EDIT_DISTANCE_SQ) {
                return;
            }

            if (level.getBlockEntity(this.pos) instanceof WispwardChestBlockEntity chest) {
                chest.applyCreativeConfig(level, this.lootTable, this.requiredLitLanterns);
                if (this.resetReward) {
                    chest.resetReward(level);
                }
            }
        });
    }
}
