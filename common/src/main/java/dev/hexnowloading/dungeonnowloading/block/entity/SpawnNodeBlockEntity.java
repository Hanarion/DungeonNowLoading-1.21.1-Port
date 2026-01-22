package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnNodeBlockEntity extends BlockEntity {

    // Example: "dnl:temple/garhold_guard"
    private String spawnDefId = "dnl:default";

    // Optional later: patch NBT
    private CompoundTag spawnPatch = new CompoundTag();

    public SpawnNodeBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.SPAWN_NODE.get(), pos, state);
    }

    public String getSpawnDefId() { return spawnDefId; }
    public void setSpawnDefId(String id) { this.spawnDefId = id; setChanged(); }

    public CompoundTag getSpawnPatch() { return spawnPatch; }
    public void setSpawnPatch(CompoundTag patch) { this.spawnPatch = patch == null ? new CompoundTag() : patch; setChanged(); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("SpawnDefId", spawnDefId);
        tag.put("SpawnPatch", spawnPatch.copy());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.spawnDefId = tag.getString("SpawnDefId");
        this.spawnPatch = tag.contains("SpawnPatch") ? tag.getCompound("SpawnPatch").copy() : new CompoundTag();
    }
}
