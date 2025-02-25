package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.logging.LogUtils;
import dev.hexnowloading.dungeonnowloading.game_event_listener.PreserverBlockDestructionListener;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.slf4j.Logger;

public class PreserverBlockEntity extends BlockEntity implements GameEventListener.Holder<GameEventListener> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private PreserverBlockDestructionListener gameEventListener;

    private int range;

    public PreserverBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.PRESERVER_BLOCK.get(), blockPos, blockState);
        this.gameEventListener = new PreserverBlockDestructionListener(this.getBlockPos(), PreserverBlockDestructionListener.squareRegionCalculation(10), PreserverBlockDestructionListener.PreserverPlane.XZ, 1);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        PreserverBlockDestructionListener.CODEC.encodeStart(NbtOps.INSTANCE, this.gameEventListener)
                .resultOrPartial(LOGGER::error)
                .ifPresent(tag -> compoundTag.put("listener", tag));

    }

    @Override
    public void load(CompoundTag compoundTag) {
        if (compoundTag.contains("listener", 10)) { // 10 means it's a CompoundTag
            PreserverBlockDestructionListener.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("listener"))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(listener -> {
                        this.gameEventListener = listener;
                    });
        }
    }

    @Override
    public GameEventListener getListener() {
        return this.gameEventListener;
    }

    public void setGameEventListener(PreserverBlockDestructionListener gameEventListener) {
        this.gameEventListener = gameEventListener;
    }
}
