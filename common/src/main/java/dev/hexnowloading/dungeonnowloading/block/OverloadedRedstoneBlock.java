package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class OverloadedRedstoneBlock extends PoweredBlock {
    public OverloadedRedstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (world.hasNeighborSignal(pos)) {
                explodePoweredRedstone(world, pos);
            }
        }
    }

    private void explodePoweredRedstone(Level world, BlockPos pos) {
        // Use a queue to perform a breadth-first search to find and destroy connected redstone wires up to 15 blocks in chain length
        Queue<BlockPos> toCheck = new LinkedList<>();
        Set<BlockPos> checked = new HashSet<>();
        toCheck.add(pos);
        int chainLength = 0;

        while (!toCheck.isEmpty() && chainLength < 15) {
            int levelSize = toCheck.size();
            for (int i = 0; i < levelSize; i++) {
                BlockPos currentPos = toCheck.poll();
                if (checked.contains(currentPos)) {
                    continue;
                }
                checked.add(currentPos);

                BlockState currentState = world.getBlockState(currentPos);
                if (currentState.getBlock() == Blocks.REDSTONE_WIRE) {
                    // Non-destructive explosion
                    world.explode(null, currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
                    world.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                    // Set fire with a chance
                    if (world.random.nextFloat() < 0.3F) {
                        world.setBlock(currentPos, Blocks.FIRE.defaultBlockState(), 3);
                    }
                    // Spawn fire particles
                    spawnFireParticles(world, currentPos);
                }

                // Add all possible connected redstone positions to the queue to check
                for (Direction direction : Direction.values()) {
                    BlockPos adjacentPos = currentPos.relative(direction);
                    if (!checked.contains(adjacentPos) && world.getBlockState(adjacentPos).getBlock() == Blocks.REDSTONE_WIRE) {
                        toCheck.add(adjacentPos);
                    }
                }

                // Check diagonal connections (steps up or down)
                for (Direction horizontal : Direction.Plane.HORIZONTAL) {
                    BlockPos upStep = currentPos.relative(horizontal).above();
                    BlockPos downStep = currentPos.relative(horizontal).below();
                    if (!checked.contains(upStep) && world.getBlockState(upStep).getBlock() == Blocks.REDSTONE_WIRE) {
                        toCheck.add(upStep);
                    }
                    if (!checked.contains(downStep) && world.getBlockState(downStep).getBlock() == Blocks.REDSTONE_WIRE) {
                        toCheck.add(downStep);
                    }
                }
            }
            chainLength++;
        }

        // Check and destroy the last redstone at the end of the chain
        while (!toCheck.isEmpty()) {
            BlockPos lastPos = toCheck.poll();
            if (!checked.contains(lastPos) && world.getBlockState(lastPos).getBlock() == Blocks.REDSTONE_WIRE) {
                world.explode(null, lastPos.getX() + 0.5, lastPos.getY() + 0.5, lastPos.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
                world.setBlock(lastPos, Blocks.AIR.defaultBlockState(), 3);
                if (world.random.nextFloat() < 0.3F) {
                    world.setBlock(lastPos, Blocks.FIRE.defaultBlockState(), 3);
                }
                spawnFireParticles(world, lastPos);
            }
        }
    }

    private void spawnFireParticles(Level world, BlockPos pos) {
        RandomSource random = RandomSource.create();
        for (int i = 0; i < 20; i++) {
            double d0 = pos.getX() + 0.5 + random.nextDouble() * 0.6 - 0.3;
            double d1 = pos.getY() + 1 + random.nextDouble() * 0.6 - 0.3;
            double d2 = pos.getZ() + 0.5 + random.nextDouble() * 0.6 - 0.3;
            world.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0, 0, 0);
        }
    }
}

