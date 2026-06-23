package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CMendingAuraSyncPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

import java.util.Random;

public class MendingAuraBlockEntity extends BlockEntity {

    private BlockState storedBlockState;
    private CompoundTag storedBlockNbt;
    private int restoreTime;

    public MendingAuraBlockEntity(BlockPos blockPos, BlockState state) {
        super(DNLBlockEntityTypes.MENDING_AURA.get(), blockPos, state);
        this.restoreTime = new Random().nextInt(100) + 100;
    }

    public void setStoredBlock(BlockState blockState, CompoundTag blockNbt) {
        this.storedBlockState = blockState;
        this.storedBlockNbt = blockNbt;
        this.setChanged();
    }

    public void setStoredBlockStateClient(BlockState blockState) {
        this.storedBlockState = blockState;
    }

    public void syncToClients(ServerLevel level, BlockState auraState) {
        if (this.storedBlockState == null) {
            return;
        }
        S2CMendingAuraSyncPacket packet = new S2CMendingAuraSyncPacket(this.worldPosition, auraState, this.storedBlockState);
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(this.worldPosition, 128.0D)) {
                Services.NETWORK.sendToPlayer(packet, player);
            }
        }
    }

    public BlockState getStoredBlockState() {
        return this.storedBlockState;
    }

    public CompoundTag getStoredBlockNbt() {
        return this.storedBlockNbt != null ? this.storedBlockNbt.copy() : null;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);

        if (storedBlockState != null) {
            compoundTag.put("StoredBlockState", BlockState.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, storedBlockState).result().orElseThrow());
        }

        if (storedBlockNbt != null) {
            compoundTag.put("StoredBlockNBT", storedBlockNbt.copy());
        }

        compoundTag.putInt("RestoreTime", restoreTime);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);

        if (compoundTag.contains("StoredBlockState")) {
            storedBlockState = BlockState.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, compoundTag.getCompound("StoredBlockState")).result().orElse(null);
        }

        if (compoundTag.contains("StoredBlockNBT")) {
            storedBlockNbt = compoundTag.getCompound("StoredBlockNBT").copy();
        }

        restoreTime = compoundTag.getInt("RestoreTime");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag compoundTag = new CompoundTag();
        if (storedBlockState != null) {
            compoundTag.put("StoredBlockState", BlockState.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, storedBlockState).result().orElseThrow());
        }
        compoundTag.putInt("RestoreTime", restoreTime);
        return compoundTag;
    }

    public int getRestoreTime() { return restoreTime; }

    public void restoreBlock(Level level, BlockPos pos, BlockState state) {
        if (this.storedBlockState == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        level.setBlock(pos, this.storedBlockState, Block.UPDATE_CLIENTS);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null && this.storedBlockNbt != null) {
                blockEntity.load(this.storedBlockNbt);
        }
    }
}
