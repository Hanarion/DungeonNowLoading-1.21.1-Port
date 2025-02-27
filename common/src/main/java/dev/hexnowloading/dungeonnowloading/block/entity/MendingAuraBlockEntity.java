package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Random;

public class MendingAuraBlockEntity extends BlockEntity {

    private BlockState storedBlockState;
    private CompoundTag storedBlockNbt;
    private int restoreTime;
    private boolean persistent;

    public MendingAuraBlockEntity(BlockPos blockPos, BlockState state) {
        super(DNLBlockEntityTypes.MENDING_AURA.get(), blockPos, state);
        this.restoreTime = new Random().nextInt(100) + 100;
        this.persistent = true;
    }

    public void setStoredBlock(BlockState blockState, CompoundTag blockNbt) {
        this.storedBlockState = blockState;
        this.storedBlockNbt = blockNbt;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        if (storedBlockState != null) {
            compoundTag.put("StoredBlockState", BlockState.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, storedBlockState).result().orElseThrow());
        }

        if (storedBlockNbt != null) {
            compoundTag.put("StoredBlockNBT", storedBlockNbt.copy());
        }

        compoundTag.putInt("RestoreTime", restoreTime);
        compoundTag.putBoolean("Persistent", persistent);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        if (compoundTag.contains("StoredBlockState")) {
            storedBlockState = BlockState.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, compoundTag.getCompound("StoredBlockState")).result().orElse(null);
        }

        if (compoundTag.contains("StoredBlockNBT")) {
            storedBlockNbt = compoundTag.getCompound("StoredBlockNBT").copy();
        }

        restoreTime = compoundTag.getInt("RestoreTime");
        persistent = compoundTag.getBoolean("Persistent");
    }

    public void setPersistent(boolean b) {
        persistent = b;
    }

    public int getRestoreTime() { return restoreTime; }

    public void restoreBlock(Level level, BlockPos pos, BlockState state) {
        if (this.storedBlockState == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (this.storedBlockState.getBlock() instanceof WallBlock) {
            BlockState blockState = this.storedBlockState
                    .setValue(BlockStateProperties.NORTH_WALL, state.getValue(BlockStateProperties.NORTH_WALL))
                    .setValue(BlockStateProperties.EAST_WALL, state.getValue(BlockStateProperties.EAST_WALL))
                    .setValue(BlockStateProperties.SOUTH_WALL, state.getValue(BlockStateProperties.SOUTH_WALL))
                    .setValue(BlockStateProperties.WEST_WALL, state.getValue(BlockStateProperties.WEST_WALL));
            level.setBlock(pos, blockState, Block.UPDATE_CLIENTS);
        } else if (this.storedBlockState.getBlock() instanceof FenceBlock) {
            BlockState blockState = this.storedBlockState
                    .setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.NORTH))
                    .setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.EAST))
                    .setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.SOUTH))
                    .setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.WEST));
            level.setBlock(pos, blockState, Block.UPDATE_CLIENTS);
        } else {
            level.setBlock(pos, this.storedBlockState, Block.UPDATE_CLIENTS);
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null && this.storedBlockNbt != null) {
            blockEntity.load(this.storedBlockNbt);
        }
    }
}
