package dev.hexnowloading.dungeonnowloading.util;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class DNLLevelUtil {

    private static Set<SoundType> pendingBlockBreakSounds = null;

    public static void beginMultiDestroySoundPending() {
        pendingBlockBreakSounds = new HashSet<>();
    }

    public static boolean destroyBlockMulti(Level level, BlockPos pos, boolean dropBlock, @Nullable Entity entity, int flags) {
        BlockDestructionManager.reset();
        level.gameEvent(DNLGameEvents.holder(DNLGameEvents.BLOCK_DESTROY_EARLY), pos, GameEvent.Context.of(entity, level.getBlockState(pos)));
        if (BlockDestructionManager.shouldCancel()) {
            return false;
        }

        boolean success;
        BlockState blockState = level.getBlockState(pos);
        if (blockState.isAir()) {
            return false;
        }
        FluidState fluidState = level.getFluidState(pos);

        if (dropBlock) {
            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(blockState, level, pos, blockEntity, entity, ItemStack.EMPTY);
        }

        if (pendingBlockBreakSounds != null) {
            pendingBlockBreakSounds.add(blockState.getSoundType());
            spawnDestroyParticles(level, pos, blockState); // spawn manual particles
        } else {
            // fallback: normal block break event (sound + particle)
            level.levelEvent(2001, pos, Block.getId(blockState));
        }

        success = level.setBlock(pos, fluidState.createLegacyBlock(), 3, flags);
        if (success) {
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(entity, blockState));
        }
        return success;
    }

    public static void endMultiDestroySoundPending(Level level, @Nullable Entity soundSourceEntity) {
        if (pendingBlockBreakSounds != null) {
            for (SoundType soundType : pendingBlockBreakSounds) {
                playBreakSound(level, soundSourceEntity, soundType);
            }
            pendingBlockBreakSounds = null;
        }
    }

    public static void spawnDestroyParticles(Level level, BlockPos blockPos, BlockState blockState) {
        if (blockState.isAir() || !blockState.shouldSpawnParticlesOnBreak()) {
            return;
        }
        VoxelShape shape = blockState.getShape(level, blockPos);
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double sizeX = Math.min(1.0, maxX - minX);
            double sizeY = Math.min(1.0, maxY - minY);
            double sizeZ = Math.min(1.0, maxZ - minZ);
            int stepsX = Math.max(2, Mth.ceil(sizeX / 0.25));
            int stepsY = Math.max(2, Mth.ceil(sizeY / 0.25));
            int stepsZ = Math.max(2, Mth.ceil(sizeZ / 0.25));
            for (int ix = 0; ix < stepsX; ++ix) {
                for (int iy = 0; iy < stepsY; ++iy) {
                    for (int iz = 0; iz < stepsZ; ++iz) {
                        double px = (ix + 0.5) / stepsX;
                        double py = (iy + 0.5) / stepsY;
                        double pz = (iz + 0.5) / stepsZ;
                        double worldX = minX + px * sizeX;
                        double worldY = minY + py * sizeY;
                        double worldZ = minZ + pz * sizeZ;
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                                    blockPos.getX() + worldX,
                                    blockPos.getY() + worldY,
                                    blockPos.getZ() + worldZ,
                                    1,
                                    px - 0.5, py - 0.5, pz - 0.5,
                                    0.0
                            );
                        }
                    }
                }
            }
        });
    }

    public static void playBreakSound(Level level, @Nullable Entity entity, SoundType soundType) {
        if (entity != null) {
            level.playSound(
                    entity,
                    entity.blockPosition(),
                    soundType.getBreakSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F,
                    soundType.getPitch() * 0.8F
            );
        }
    }
}
