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

public class PreserverBlockDestructionListener implements GameEventListener {

    public static final Codec<PreserverBlockDestructionListener> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PreserverBlockDestructionListener::getBlockPos),
            Codec.INT.fieldOf("radius").forGetter(PreserverBlockDestructionListener::getListenerRadius),
            PreserverPlane.CODEC.fieldOf("plane").forGetter(PreserverBlockDestructionListener::getPlane),
            Codec.INT.fieldOf("thickness").forGetter(PreserverBlockDestructionListener::getThickness)
    ).apply(instance, PreserverBlockDestructionListener::new));

    private final PositionSource listenerSource;
    private final int listenerRadius;
    private final BlockPos blockPos;
    private final PreserverPlane plane;
    private final int thickness;

    public PreserverBlockDestructionListener(BlockPos blockPos, int radius, PreserverPlane plane, int thickness) {
        this.blockPos = blockPos;
        this.listenerSource = new BlockPositionSource(blockPos);
        this.listenerRadius = radius;
        this.plane = plane;
        this.thickness = thickness;
    }

    public int getThickness() {
        return this.thickness;
    }

    public PreserverPlane getPlane() {
        return this.plane;
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
        return this.listenerRadius;
    }

    public static int squareRegionCalculation(int centerToSideDistance) {
        return (int) Math.ceil((centerToSideDistance - 1) * Math.sqrt(3));
    }

    public static int reverseSquareRegionCalculation(int y) {
        return (int) Math.floor((y / Math.sqrt(3)));
    }

    @Override
    public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 pos) {

        BlockPos eventBlockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
        int radius = reverseSquareRegionCalculation(this.listenerRadius);

        Vec3 centerPos = this.getListenerSource().getPosition(serverLevel).orElseThrow();
        BlockPos centerBlockPos = new BlockPos((int) Math.floor(centerPos.x), (int) Math.floor(centerPos.y), (int) Math.floor(centerPos.z));

        // Calculate bounds based on the selected plane
        int minX = centerBlockPos.getX();
        int minY = centerBlockPos.getY();
        int minZ = centerBlockPos.getZ();
        int maxX = centerBlockPos.getX();
        int maxY = centerBlockPos.getY();
        int maxZ = centerBlockPos.getZ();

        switch (this.plane) {
            case XZ -> {
                minX -= radius;
                maxX += radius;
                minZ -= radius;
                maxZ += radius;
                minY -= thickness / 2;
                maxY += thickness / 2;
            }
            case XY -> {
                minX -= radius;
                maxX += radius;
                minY -= radius;
                maxY += radius;
                minZ -= thickness / 2;
                maxZ += thickness / 2;
            }
            case ZY -> {
                minZ -= radius;
                maxZ += radius;
                minY -= radius;
                maxY += radius;
                minX -= thickness / 2;
                maxX += thickness / 2;
            }
        }

        // Check if the event block is within this defined region
        if (eventBlockPos.getX() >= minX && eventBlockPos.getX() <= maxX &&
                eventBlockPos.getY() >= minY && eventBlockPos.getY() <= maxY &&
                eventBlockPos.getZ() >= minZ && eventBlockPos.getZ() <= maxZ) {
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

    public enum PreserverPlane {
        XZ, XY, ZY;

        public static final Codec<PreserverPlane> CODEC = Codec.STRING.xmap(PreserverPlane::valueOf, PreserverPlane::name);
    }
}
