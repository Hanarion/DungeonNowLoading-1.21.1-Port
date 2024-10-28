package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;

public class OverchargedRedstoneBlock extends Block {
    public OverchargedRedstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        // Check if there are any redstone components around the block
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            if (adjacentState.is(Blocks.REDSTONE_WIRE) || adjacentState.isSignalSource() || adjacentState.is(DNLTags.OVERCHARGED_REDSTONE_BLOCK_NEIGHBOUR_EXPLOSIVE) || adjacentState.is(Blocks.TNT)) {
                return 0; // No signal if there is any redstone or redstone component around
            }
        }
        return 15; // Provide full signal if there are no redstone components around
    }

    @Override
    public boolean isSignalSource(BlockState $$0) {
        return true;
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        if (entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity) && !(entity instanceof ScuttleEntity)) {
            entity.hurt(level.damageSources().hotFloor(), 6.0F);
            if (!entity.fireImmune()) {
                entity.setSecondsOnFire(5);
            }
        }
        super.stepOn(level, blockPos, blockState, entity);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState1, boolean b) {
        explodePoweredRedstone(level, blockPos);
        explodeDirectlyConnectedRepeatersAndComparators(level, blockPos);
        super.onPlace(blockState, level, blockPos, blockState1, b);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        explodePoweredRedstone(world, pos);
        explodeDirectlyConnectedRepeatersAndComparators(world, pos);
    }

    private void explodePoweredRedstone(Level world, BlockPos pos) {
        // Use a queue to perform a breadth-first search to find and destroy connected redstone wires up to 15 blocks in chain length
        Queue<BlockPos> toCheck = new LinkedList<>();
        Set<BlockPos> checked = new HashSet<>();
        toCheck.add(pos);
        int chainLength = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            if (adjacentState.is(Blocks.REPEATER) || adjacentState.is(Blocks.COMPARATOR)) {
                explodeComponents(world, adjacentPos, 0.5f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
            }
        }

        while (!toCheck.isEmpty() && chainLength < 16) {
            int levelSize = toCheck.size();
            for (int i = 0; i < levelSize; i++) {
                BlockPos currentPos = toCheck.poll();
                if (checked.contains(currentPos)) {
                    continue;
                }
                checked.add(currentPos);

                BlockState currentState = world.getBlockState(currentPos);

                if (currentState.getBlock() == Blocks.REDSTONE_WIRE) {

                    world.playSound(null, currentPos, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_DUST_COMBUSTION.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    world.setBlock(currentPos, Blocks.FIRE.defaultBlockState(), 3);

                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(DustParticleOptions.REDSTONE, currentPos.getX() + 0.5f, currentPos.getY() + 0.5f, currentPos.getZ() + 0.5f, 5, 0.5f, 0.5f, 0.5f, 0.0);
                    }
                }

                for (Direction direction : Direction.values()) {
                    BlockPos adjacentPos = currentPos.relative(direction);
                    BlockState adjacentState = world.getBlockState(adjacentPos);
                    if (!checked.contains(adjacentPos) && adjacentState.getBlock() == Blocks.REDSTONE_WIRE) {
                        toCheck.add(adjacentPos);
                    }
                    if (adjacentState.is(Blocks.REPEATER) || adjacentState.is(Blocks.COMPARATOR)) {
                        explodeRepeaterAndComparator(world, adjacentPos, adjacentState, direction);
                    } else if (adjacentState.is(Blocks.ACTIVATOR_RAIL) || adjacentState.is(Blocks.POWERED_RAIL)) {
                        explodeComponents(world, adjacentPos, 0.5f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
                    } else if (adjacentState.is(Blocks.DISPENSER) || adjacentState.is(Blocks.DROPPER) || adjacentState.is(Blocks.PISTON) || adjacentState.is(Blocks.STICKY_PISTON)) {
                        explodeComponents(world, adjacentPos, 1.0f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
                    } else if (adjacentState.is(Blocks.NOTE_BLOCK) || adjacentState.is(Blocks.REDSTONE_LAMP) || adjacentState.is(Blocks.OBSERVER) || adjacentState.is(DNLBlocks.SIGNAL_GATE.get())) {
                        explodeComponents(world, adjacentPos, 1.5f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
                    } else if (adjacentState.is(Blocks.TNT)) {
                        explodeComponents(world, adjacentPos, 6.0f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_TNT_EXPLOSION.get());
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
    }

    private void explodeDirectlyConnectedRepeatersAndComparators(Level world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            if (adjacentState.is(Blocks.REPEATER) || adjacentState.is(Blocks.COMPARATOR)) {
                explodeRepeaterAndComparator(world, adjacentPos, adjacentState, direction);
            } else if (adjacentState.is(Blocks.ACTIVATOR_RAIL) || adjacentState.is(Blocks.POWERED_RAIL)) {
                explodeComponents(world, adjacentPos, 0.5f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
            } else if (adjacentState.is(Blocks.DISPENSER) || adjacentState.is(Blocks.DROPPER) || adjacentState.is(Blocks.PISTON) || adjacentState.is(Blocks.STICKY_PISTON)) {
                explodeComponents(world, adjacentPos, 1.0f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
            } else if (adjacentState.is(Blocks.NOTE_BLOCK) || adjacentState.is(Blocks.REDSTONE_LAMP) || adjacentState.is(Blocks.OBSERVER) || adjacentState.is(DNLBlocks.SIGNAL_GATE.get())) {
                explodeComponents(world, adjacentPos, 1.5f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
            } else if (adjacentState.is(Blocks.TNT)) {
                explodeComponents(world, adjacentPos, 6.0f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_TNT_EXPLOSION.get());
            }
        }
    }

    private void explodeRepeaterAndComparator(Level world, BlockPos adjacentPos, BlockState adjacentState, Direction direction) {
        Direction repeaterDirection = adjacentState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (repeaterDirection == direction.getOpposite()) {
            explodeComponents(world, adjacentPos, 0.5f, DNLSounds.OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION.get());
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(), adjacentPos.getX() + 0.5, adjacentPos.getY() + 0.5, adjacentPos.getZ() + 0.5, 1, 0.0f, 0.0f, 0.0f, 0.0f);
            }
        }
    }

    private void explodeComponents(Level level, BlockPos blockPos, float strength, SoundEvent soundEvent) {
        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        level.explode(null, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, strength, true, Level.ExplosionInteraction.BLOCK);
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(), blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 1, 0.0f, 0.0f, 0.0f, 0.0f);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextFloat() > 0.9f) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = blockPos.relative(direction);
                if (level.getBlockState(adjacentPos).isAir()) {
                    double x = blockPos.getX() + 0.5 + 0.5 * direction.getStepX() + (randomSource.nextFloat() - 0.5) * 0.6;
                    double y = blockPos.getY() + 0.5 + 0.5 * direction.getStepY() + (randomSource.nextFloat() - 0.5) * 0.6;
                    double z = blockPos.getZ() + 0.5 + 0.5 * direction.getStepZ() + (randomSource.nextFloat() - 0.5) * 0.6;
                    level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
                }
            }
        }
        if (randomSource.nextFloat() > 0.7f) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = blockPos.relative(direction);
                if (level.getBlockState(adjacentPos).isAir()) {
                    double x = blockPos.getX() + 0.5 + 0.5 * direction.getStepX() + (randomSource.nextFloat() - 0.5) * 0.6;
                    double y = blockPos.getY() + 0.5 + 0.5 * direction.getStepY() + (randomSource.nextFloat() - 0.5) * 0.6;
                    double z = blockPos.getZ() + 0.5 + 0.5 * direction.getStepZ() + (randomSource.nextFloat() - 0.5) * 0.6;
                    level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}