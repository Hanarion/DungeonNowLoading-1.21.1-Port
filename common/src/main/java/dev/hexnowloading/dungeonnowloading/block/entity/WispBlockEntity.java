package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WispBlockEntity extends BlockEntity {
    public static final int LIFETIME_TICKS = 20 * 60;
    public static final int MAX_WISPS_PER_PLAYER = 5;
    private static final String OWNER_TAG = "Owner";
    private static final String PLACED_GAME_TIME_TAG = "PlacedGameTime";

    @Nullable
    private UUID owner;
    private long placedGameTime;

    public WispBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.WISP_BLOCK.get(), pos, state);
    }

    public void setOwner(UUID owner, long placedGameTime) {
        this.owner = owner;
        this.placedGameTime = placedGameTime;
        this.setChanged();
    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public long getPlacedGameTime() {
        return this.placedGameTime;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WispBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (blockEntity.placedGameTime <= 0L) {
            blockEntity.placedGameTime = serverLevel.getGameTime();
            blockEntity.setChanged();
        }

        if (serverLevel.getGameTime() - blockEntity.placedGameTime >= LIFETIME_TICKS) {
            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            tag.putUUID(OWNER_TAG, this.owner);
        }
        tag.putLong(PLACED_GAME_TIME_TAG, this.placedGameTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.owner = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
        this.placedGameTime = tag.getLong(PLACED_GAME_TIME_TAG);
    }
}
