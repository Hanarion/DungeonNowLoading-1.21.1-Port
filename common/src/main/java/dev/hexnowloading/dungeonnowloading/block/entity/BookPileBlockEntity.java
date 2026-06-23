package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class BookPileBlockEntity extends BlockEntity {

    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    public BookPileBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.BOOK_PILE.get(), pos, state);
    }

    @Nullable
    public ResourceLocation getLootTable() {
        return lootTable;
    }

    public void setLootTable(@Nullable ResourceLocation id, long seed) {
        this.lootTable = id;
        this.lootTableSeed = seed;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (lootTable != null) {
            tag.putString("LootTable", lootTable.toString());
            tag.putLong("LootTableSeed", lootTableSeed);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("LootTable", Tag.TAG_STRING)) {
            this.lootTable = ResourceLocation.parse(tag.getString("LootTable"));
            this.lootTableSeed = tag.getLong("LootTableSeed");
        } else {
            this.lootTable = null;
            this.lootTableSeed = 0L;
        }
    }
}

