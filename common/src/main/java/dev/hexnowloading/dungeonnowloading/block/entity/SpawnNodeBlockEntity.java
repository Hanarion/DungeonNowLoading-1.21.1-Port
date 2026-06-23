package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnNodeBlockEntity extends BlockEntity {

    // Example: "dnl:temple/garhold_guard"
    private String spawnPool = "dnl:default_pool";

    public SpawnNodeBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.SPAWN_NODE.get(), pos, state);
    }

    public String getSpawnPool() { return spawnPool; }
    public void setSpawnPool(String id) { this.spawnPool = id; setChanged(); }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("SpawnPool", spawnPool);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.spawnPool = tag.getString("SpawnPool");
    }
}
