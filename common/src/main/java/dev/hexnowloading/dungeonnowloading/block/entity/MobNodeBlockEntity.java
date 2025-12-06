package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MobNodeBlockEntity extends BlockEntity {
    private ResourceLocation id;
    private String summonCommand = "";
    private int spawnCount = 1;

    public MobNodeBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.MOB_NODE.get(), pos, state);
    }

    // Getters
    public String getId() {
        return id == null ? "" : id.toString();
    }

    public String getSummonCommand() {
        return summonCommand;
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public void apply(String idStr, String summonCommand, int spawnCount, double randomSpread,
                      double mpHealthIncreasePerPlayer, double mpAttackIncreasePerPlayer,
                      int additionalSpawnPerRequiredPlayer, int requiredPlayers, int spawnCap,
                      double additionalSpreadPerRequiredPlayer) {
        try {
            this.id = idStr == null || idStr.isBlank() ? null : new ResourceLocation(idStr);
        } catch (Exception ignored) {
            this.id = null;
        }
        this.summonCommand = summonCommand == null ? "" : summonCommand;
        this.spawnCount = Math.max(1, spawnCount);
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("id")) {
            try {
                id = new ResourceLocation(tag.getString("id"));
            } catch (Exception ignored) {
                id = null;
            }
        } else id = null;
        summonCommand = tag.getString("summonCommand");
        spawnCount = tag.getInt("spawnCount");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (id != null) tag.putString("id", id.toString());
        tag.putString("summonCommand", summonCommand);
        tag.putInt("spawnCount", spawnCount);
    }
}

