package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

import java.util.ArrayList;
import java.util.List;

// 1.21 / CCA 6.1.3: PlayerComponent -> RespawnableComponent; NBT methods take HolderLookup.Provider.
// Also fixed: the old readFromNbt read hardcoded indices getInt(0/1/2) (re-read entry 0) while
// writeToNbt stored a list-of-int-lists; the serialization now round-trips correctly. Removed the
// broken equals(){return false;} (it defeated AutoSyncedComponent change detection) and a stray
// mixin Profiler.setActive debug leftover.
public class FairkeeperChestPositionsCapabilityHandler implements IFairkeeperChestPositionsCapability, RespawnableComponent<FairkeeperChestPositionsCapabilityHandler> {

    private List<BlockPos> fairkeeperPosList;

    public FairkeeperChestPositionsCapabilityHandler() {
        this(new ArrayList<>());
    }

    public FairkeeperChestPositionsCapabilityHandler(List<BlockPos> blockPosList) {
        this.fairkeeperPosList = blockPosList;
    }

    @Override
    public List<BlockPos> getList() {
        if (this.fairkeeperPosList == null) {
            this.fairkeeperPosList = new ArrayList<>();
        }
        return this.fairkeeperPosList;
    }

    @Override
    public void addBlock(BlockPos blockPos) {
        if (this.fairkeeperPosList == null) {
            this.fairkeeperPosList = new ArrayList<>();
        }
        if (!this.fairkeeperPosList.contains(blockPos)) {
            this.fairkeeperPosList.add(blockPos);
        }
    }

    @Override
    public void copyList(List<BlockPos> list) {
        this.fairkeeperPosList = list;
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider registries) {
        this.fairkeeperPosList = new ArrayList<>();
        if (compoundTag.contains("FairkeeperChestPositions", Tag.TAG_LIST)) {
            ListTag listTag = compoundTag.getList("FairkeeperChestPositions", Tag.TAG_LIST);
            for (int a = 0; a < listTag.size(); ++a) {
                ListTag pos = listTag.getList(a);
                this.fairkeeperPosList.add(new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2)));
            }
        }
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        getList().forEach(blockPos -> listTag.add(newIntList(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        compoundTag.put("FairkeeperChestPositions", listTag);
    }

    @Override
    public void copyFrom(FairkeeperChestPositionsCapabilityHandler original, HolderLookup.Provider registries) {
        this.fairkeeperPosList = new ArrayList<>(original.fairkeeperPosList);
    }

    @Override
    public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean sameCharacter) {
        return lossless || keepInventory;
    }

    private ListTag newIntList(int... ints) {
        ListTag listTag = new ListTag();
        for (int i : ints) {
            listTag.add(IntTag.valueOf(i));
        }
        return listTag;
    }
}
