package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.logging.LogUtils;
import dev.hexnowloading.dungeonnowloading.game_event_listener.PreserverBlockDestructionSystem;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class PreserverBlockEntity extends BlockEntity implements GameEventListener.Holder<PreserverBlockDestructionSystem.Listener>, PreserverBlockDestructionSystem {

    private static final Logger LOGGER = LogUtils.getLogger();
    private PreserverBlockDestructionSystem.User user;
    private PreserverBlockDestructionSystem.Listener gameEventListener;

    private final Set<BlockPos> playerPlacedBlocks = new HashSet<>();


    public PreserverBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.PRESERVER_BLOCK.get(), blockPos, blockState);
        this.user = new PreserverBlockDestructionSystem.User(this.getBlockPos(), new BlockPos(10, 10, 10), new BlockPos(-10, -10, -10), Direction.NORTH);
        this.gameEventListener = new PreserverBlockDestructionSystem.Listener(this);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        PreserverBlockDestructionSystem.User.CODEC.encodeStart(NbtOps.INSTANCE, this.user)
                .resultOrPartial(LOGGER::error)
                .ifPresent(tag -> compoundTag.put("listener", tag));
        compoundTag.put("PlayerPlacedBlocks", saveBlockPosSet(playerPlacedBlocks));
    }

    @Override
    public void load(CompoundTag compoundTag) {
        if (compoundTag.contains("listener", 10)) { // 10 means it's a CompoundTag
            PreserverBlockDestructionSystem.User.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("listener"))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(listener -> {
                        this.user = new User(this.getBlockPos(), listener.getCornerA(), listener.getCornerB(), listener.getFacing());
                    });
        }
        if (compoundTag.contains("PlayerPlacedBlocks")) {
            playerPlacedBlocks.clear();
            playerPlacedBlocks.addAll(loadBlockPosSet(compoundTag.getList("PlayerPlacedBlocks", 10)));
        }
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public Listener getListener() {
        return this.gameEventListener;
    }

    public void addPlayerPlacedBlock(BlockPos pos) {
        playerPlacedBlocks.add(pos);
    }

    // Remove when broken
    public void removePlayerPlacedBlock(BlockPos pos) {
        playerPlacedBlocks.remove(pos);
    }

    // Check if the block is player-placed
    public boolean isPlayerPlaced(BlockPos pos) {
        return playerPlacedBlocks.contains(pos);
    }

    private static ListTag saveBlockPosSet(Set<BlockPos> positions) {
        ListTag listTag = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            listTag.add(tag);
        }
        return listTag;
    }

    private static Set<BlockPos> loadBlockPosSet(ListTag listTag) {
        Set<BlockPos> positions = new HashSet<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            positions.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
        return positions;
    }
}
