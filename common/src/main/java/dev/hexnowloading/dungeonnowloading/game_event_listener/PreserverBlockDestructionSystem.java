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

public interface PreserverBlockDestructionSystem {

    public User getUser();

    public static class User {
        public static final Codec<User> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(User::getBlockPos),
                Codec.INT.fieldOf("radius").forGetter(User::getListenerRadius),
                PreserverPlane.CODEC.fieldOf("plane").forGetter(User::getPlane),
                Codec.INT.fieldOf("thickness").forGetter(User::getThickness)
        ).apply(instance, User::new));

        private final BlockPos blockPos;
        private int listenerRadius;
        private int thickness;
        private PreserverPlane plane;

        public User(BlockPos blockPos, int radius, PreserverPlane plane, int thickness) {
            this.blockPos = blockPos;
            this.listenerRadius = radius;
            this.thickness = thickness;
            this.plane = plane;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public int getListenerRadius() {
            return listenerRadius;
        }

        public void setListenerRadius(int radius) {
            this.listenerRadius = radius;
        }

        public int getThickness() {
            return thickness;
        }

        public void setThickness(int thickness) {
            this.thickness = thickness;
        }

        public PreserverPlane getPlane() {
            return plane;
        }

        public void setPlane(PreserverPlane plane) {
            this.plane = plane;
        }

        public PositionSource getPositionSource() {
            return new BlockPositionSource(blockPos);
        }

        public enum PreserverPlane {
            XZ, XY, ZY;

            public static final Codec<PreserverPlane> CODEC = Codec.STRING.xmap(PreserverPlane::valueOf, PreserverPlane::name);
        }
    }

    public static class Listener implements GameEventListener {
        private final PreserverBlockDestructionSystem system;

        public Listener(PreserverBlockDestructionSystem system) {
            this.system = system;
        }

        public BlockPos getBlockPos() {
            return system.getUser().getBlockPos();
        }

        @Override
        public PositionSource getListenerSource() {
            return system.getUser().getPositionSource();
        }

        @Override
        public int getListenerRadius() {
            return system.getUser().getListenerRadius();
        }

        public void setListenerRadius(int radius) {
            system.getUser().setListenerRadius(radius);
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
            int radius = reverseSquareRegionCalculation(this.getListenerRadius());

            Vec3 centerPos = this.getListenerSource().getPosition(serverLevel).orElseThrow();
            BlockPos centerBlockPos = new BlockPos((int) Math.floor(centerPos.x), (int) Math.floor(centerPos.y), (int) Math.floor(centerPos.z));

            // Calculate bounds based on the selected plane
            int minX = centerBlockPos.getX();
            int minY = centerBlockPos.getY();
            int minZ = centerBlockPos.getZ();
            int maxX = centerBlockPos.getX();
            int maxY = centerBlockPos.getY();
            int maxZ = centerBlockPos.getZ();

            User user = system.getUser();

            switch (user.getPlane()) {
                case XZ -> {
                    minX -= radius;
                    maxX += radius;
                    minZ -= radius;
                    maxZ += radius;
                    minY -= user.getThickness() / 2;
                    maxY += user.getThickness() / 2;
                }
                case XY -> {
                    minX -= radius;
                    maxX += radius;
                    minY -= radius;
                    maxY += radius;
                    minZ -= user.getThickness() / 2;
                    maxZ += user.getThickness() / 2;
                }
                case ZY -> {
                    minZ -= radius;
                    maxZ += radius;
                    minY -= radius;
                    maxY += radius;
                    minX -= user.getThickness() / 2;
                    maxX += user.getThickness() / 2;
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
    }
}
