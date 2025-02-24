package dev.hexnowloading.dungeonnowloading.game_event_listener;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.PreserverBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.BlockDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class BlockPlaceBreakListener implements GameEventListener {

    public static final Codec<BlockPlaceBreakListener> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BlockPlaceBreakListener::getBlockPos),
            Codec.INT.fieldOf("radius").forGetter(BlockPlaceBreakListener::getListenerRadius)
    ).apply(instance, BlockPlaceBreakListener::new));

    private final PositionSource listenerSource;
    private final int listenerRadius;
    private final BlockPos blockPos;

    public BlockPlaceBreakListener(BlockPos blockPos, int radius) {
        this.blockPos = blockPos;
        this.listenerSource = new BlockPositionSource(blockPos);
        this.listenerRadius = radius - 1;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public PositionSource getListenerSource() {
        return this.listenerSource;
    }

    @Override
    public int getListenerRadius() {
        return (int) Math.ceil(this.listenerRadius * Math.sqrt(3));
    }

    @Override
    public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 pos) {

        System.out.println("Block Break");

        BlockPos eventBlockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));

        Vec3 centerPos = this.getListenerSource().getPosition(serverLevel).orElseThrow();
        BlockPos centerBlockPos = new BlockPos((int) Math.floor(centerPos.x), (int) Math.floor(centerPos.y) - 1, (int) Math.floor(centerPos.z));
        if (Math.abs(eventBlockPos.getX() - centerBlockPos.getX()) <= this.listenerRadius && Math.abs(eventBlockPos.getY() - centerBlockPos.getY()) <= this.listenerRadius && Math.abs(eventBlockPos.getZ() - centerBlockPos.getZ()) <= this.listenerRadius) {
            if (gameEvent == DNLGameEvents.BLOCK_DESTROY_EARLY.get()) {

                if (context.sourceEntity() instanceof Player player && player.getAbilities().instabuild) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof PreserverBlock) {
                    return false;
                }

                BlockDestructionManager.cancelBlockDestruction();

                serverLevel.setBlock(eventBlockPos, DNLBlocks.MENDING_AURA.get().defaultBlockState(), Block.UPDATE_CLIENTS);

                return true;
            }
        }
        return false;
    }
}
