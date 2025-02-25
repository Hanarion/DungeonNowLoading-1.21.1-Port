package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MendingAuraBlockEntity entity) {
        if (!level.isClientSide) {
            entity.restoreTime--;

            if (entity.restoreTime <= 0) {
                if (entity.storedBlockState == null) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    return;
                }
                level.setBlock(pos, entity.storedBlockState, Block.UPDATE_CLIENTS);

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && entity.storedBlockNbt != null) {
                    blockEntity.load(entity.storedBlockNbt);
                }
            }
        }
    }

}
