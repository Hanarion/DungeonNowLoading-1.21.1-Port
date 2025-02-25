package dev.hexnowloading.dungeonnowloading.game_event_listener;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.PreserverBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.PreserverBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import dev.hexnowloading.dungeonnowloading.util.event_managers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

                    if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                        preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                        return false;
                    }

                    if (context.sourceEntity() instanceof Player player && player.getAbilities().instabuild) {
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof MendingAuraBlock) {
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof PreserverBlock) {
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                        return false;
                    }

                    BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                    BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                    CompoundTag compoundTag = new CompoundTag();
                    if (originalBlockEntity != null) {
                        compoundTag = originalBlockEntity.saveWithFullMetadata();
                    }

                    BlockDestructionManager.cancel();
                    ContainerDropManager.cancel(eventBlockPos);

                    serverLevel.setBlock(eventBlockPos, DNLBlocks.MENDING_AURA.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                        blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    }

                    if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                        preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                        serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    return true;
                }
                if (gameEvent == DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION.get()) {
                    if (serverLevel.getBlockState(eventBlockPos).isAir()) {
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof MendingAuraBlock) {
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                        ExplosionDestructionManager.cancel();
                        preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                        return false;
                    }

                    if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                        preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                        return false;
                    }

                    BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                    BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                    CompoundTag compoundTag = new CompoundTag();
                    if (originalBlockEntity != null) {
                        compoundTag = originalBlockEntity.saveWithFullMetadata();
                    }

                    ExplosionDestructionManager.cancel();
                    ContainerDropManager.cancel(eventBlockPos);

                    serverLevel.setBlock(eventBlockPos, DNLBlocks.MENDING_AURA.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                        blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    }

                    if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                        preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                        serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    return true;
                }
                if (gameEvent == DNLGameEvents.BLOCK_BURNED.get()) {
                    if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                        preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE_ON_FIRE)) {
                        return false;
                    }

                    BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                    BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                    CompoundTag compoundTag = new CompoundTag();
                    if (originalBlockEntity != null) {
                        compoundTag = originalBlockEntity.saveWithFullMetadata();
                    }

                    BlockBurnManager.cancel();
                    ContainerDropManager.cancel(eventBlockPos);

                    serverLevel.setBlock(eventBlockPos, DNLBlocks.MENDING_AURA.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                        blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    }

                    if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                        preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                        serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    return true;
                }
                if (gameEvent == DNLGameEvents.BLOCK_PUSHED_EARLY.get()) {

                    if (serverLevel.getBlockState(eventBlockPos).isAir()) {
                        return false;
                    }

                    if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                        return false;
                    }

                    BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                    BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                    CompoundTag compoundTag = new CompoundTag();
                    if (originalBlockEntity != null) {
                        compoundTag = originalBlockEntity.saveWithFullMetadata();
                    }

                    PistonPushManager.cancel();

                    serverLevel.setBlock(eventBlockPos, DNLBlocks.MENDING_AURA.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                        blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    }

                    if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                        preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                        serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    return true;
                }
                if (gameEvent == GameEvent.BLOCK_PLACE) {
                    if (context.sourceEntity() instanceof Player player && player.getAbilities().instabuild) {
                        return false;
                    }

                    if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity blockEntity) {
                        blockEntity.addPlayerPlacedBlock(eventBlockPos);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
