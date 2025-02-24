package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.logging.LogUtils;
import dev.hexnowloading.dungeonnowloading.game_event_listener.BlockPlaceBreakListener;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.slf4j.Logger;

public class PreserverBlockEntity extends BlockEntity implements GameEventListener.Holder<GameEventListener> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private DynamicGameEventListener<BlockPlaceBreakListener> dynamicBlockPlaceBreakListener;

    private boolean isListenerRegistered;
    private SectionPos lastSection;


    private BlockPos maxBlockPos;
    private BlockPos minBlockPos;
    private int range;

    public PreserverBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.PRESERVER_BLOCK.get(), blockPos, blockState);
        this.dynamicBlockPlaceBreakListener = new DynamicGameEventListener<>(new BlockPlaceBreakListener(this.getBlockPos(), 10));
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        /*if (this.maxBlockPos != null) {
            compoundTag.put("MaxBlockPos", NbtHelper.newIntList(this.maxBlockPos.getX(), this.maxBlockPos.getY(), this.maxBlockPos.getZ()));
        }
        if (this.minBlockPos != null) {
            compoundTag.put("MinBlockPos", NbtHelper.newIntList(this.minBlockPos.getX(), this.minBlockPos.getY(), this.minBlockPos.getZ()));
        }*/
        compoundTag.putInt("Range", this.range);
        BlockPlaceBreakListener.CODEC.encodeStart(NbtOps.INSTANCE, this.dynamicBlockPlaceBreakListener.getListener())
                .resultOrPartial(LOGGER::error)
                .ifPresent(tag -> compoundTag.put("listener", tag));

    }

    @Override
    public void load(CompoundTag compoundTag) {
        /*if (compoundTag.contains("MaxBlockPos", CompoundTag.TAG_LIST)) {
            this.minBlockPos = new BlockPos(compoundTag.getList("MaxBlockPos", CompoundTag.TAG_INT).getInt(0), compoundTag.getList("MaxBlockPos", CompoundTag.TAG_INT).getInt(1), compoundTag.getList("MaxBlockPos", CompoundTag.TAG_INT).getInt(2));
        }
        if (compoundTag.contains("MinBlockPos", CompoundTag.TAG_LIST)) {
            this.minBlockPos = new BlockPos(compoundTag.getList("MinBlockPos", CompoundTag.TAG_INT).getInt(0), compoundTag.getList("MinBlockPos", CompoundTag.TAG_INT).getInt(1), compoundTag.getList("MinBlockPos", CompoundTag.TAG_INT).getInt(2));
        }*/
        this.range = compoundTag.getInt("Range");
        this.isListenerRegistered = false;
        if (compoundTag.contains("listener", 10)) { // 10 means it's a CompoundTag
            BlockPlaceBreakListener.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("listener"))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(listener -> {
                        this.dynamicBlockPlaceBreakListener = new DynamicGameEventListener<>(listener);
                    });
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PreserverBlockEntity blockEntity) {

        /*if (level instanceof ServerLevel serverLevel) {
            Vec3 vec3 = blockEntity.getDynamicGameEventListener().getListener().getListenerSource().getPosition(serverLevel).orElse(null);
            System.out.println(vec3);
            if (vec3 == null) {
                blockEntity.registerListener(serverLevel);
            }
            //BlockPos blockPos = new BlockPos((int) Math.floor(vec3.x), (int) Math.floor(vec3.y) - 1, (int) Math.floor(vec3.z));
        }*/
    }

    public void registerListener(ServerLevel serverLevel) {
        this.dynamicBlockPlaceBreakListener.add(serverLevel);
        this.isListenerRegistered = true;
        System.out.println("reg");
    }

    public void unregisterListener(ServerLevel serverLevel) {
        this.dynamicBlockPlaceBreakListener.remove(serverLevel);
        this.isListenerRegistered = false;
        System.out.println("unreg");
    }

    public DynamicGameEventListener<?> getDynamicGameEventListener() {
        return this.dynamicBlockPlaceBreakListener;
    }

    @Override
    public GameEventListener getListener() {
        return this.dynamicBlockPlaceBreakListener.getListener();
    }
}
