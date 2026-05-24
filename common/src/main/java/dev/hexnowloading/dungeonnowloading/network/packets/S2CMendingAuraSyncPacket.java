package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class S2CMendingAuraSyncPacket implements DNLPacket {

    private final BlockPos pos;
    private final BlockState auraState;
    private final BlockState storedState;

    public S2CMendingAuraSyncPacket(BlockPos pos, BlockState auraState, BlockState storedState) {
        this.pos = pos.immutable();
        this.auraState = auraState;
        this.storedState = storedState;
    }

    public S2CMendingAuraSyncPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.auraState = BlockState.CODEC.parse(NbtOps.INSTANCE, buffer.readNbt()).result().orElse(null);
        this.storedState = BlockState.CODEC.parse(NbtOps.INSTANCE, buffer.readNbt()).result().orElse(null);
    }

    public static S2CMendingAuraSyncPacket decode(FriendlyByteBuf buffer) {
        return new S2CMendingAuraSyncPacket(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(encodeBlockState(auraState));
        buffer.writeNbt(encodeBlockState(storedState));
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender != null) return;

        var minecraft = ClientUtil.getClient();
        Level level = ClientUtil.getClientLevel();
        if (minecraft == null || level == null || auraState == null || storedState == null) return;

        minecraft.execute(() -> {
            level.setBlock(pos, auraState, Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(pos) instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
                mendingAuraBlockEntity.setStoredBlockStateClient(storedState);
            }
            if (level.getBlockState(pos).getBlock() instanceof MendingAuraBlock) {
                level.sendBlockUpdated(pos, auraState, auraState, Block.UPDATE_CLIENTS);
                level.setBlocksDirty(pos, auraState, auraState);
            }
        });
    }

    private static CompoundTag encodeBlockState(BlockState state) {
        Tag tag = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, state).result().orElseThrow();
        return tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
    }
}
