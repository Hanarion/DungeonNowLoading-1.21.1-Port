package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.logging.LogUtils;
import dev.hexnowloading.dungeonnowloading.game_event_listener.PreserverBlockDestructionSystem;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.slf4j.Logger;

public class PreserverBlockEntity extends BlockEntity implements GameEventListener.Holder<PreserverBlockDestructionSystem.Listener>, PreserverBlockDestructionSystem {

    private static final Logger LOGGER = LogUtils.getLogger();
    private PreserverBlockDestructionSystem.User user;
    private PreserverBlockDestructionSystem.Listener gameEventListener;

    private int squareRange;
    private int thickness;

    public PreserverBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.PRESERVER_BLOCK.get(), blockPos, blockState);
        this.user = new PreserverBlockDestructionSystem.User(this.getBlockPos(), PreserverBlockDestructionSystem.Listener.squareRegionCalculation(10), PreserverBlockDestructionSystem.User.PreserverPlane.XZ, 1);
        this.gameEventListener = new PreserverBlockDestructionSystem.Listener(this);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        PreserverBlockDestructionSystem.User.CODEC.encodeStart(NbtOps.INSTANCE, this.user)
                .resultOrPartial(LOGGER::error)
                .ifPresent(tag -> compoundTag.put("listener", tag));
    }

    @Override
    public void load(CompoundTag compoundTag) {
        if (compoundTag.contains("listener", 10)) { // 10 means it's a CompoundTag
            PreserverBlockDestructionSystem.User.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("listener"))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(listener -> {
                        this.user = listener;
                    });
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
}
