package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BrittlestoneBlock extends Block {

    private static final int BREAK_DELAY_TICKS = 5; // 1 second

    public BrittlestoneBlock(Properties properties) {
        super(properties);
    }

    /**
     * Called when the block should start its "brittle" reaction:
     * - spawns particles immediately
     * - schedules a tick in 1 second to actually break the block
     */
    private void triggerBrittle(Level level, BlockPos pos, BlockState state, Entity cause) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;

        // Particle cue when it gets triggered (crumbling dust)
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                8,      // count
                0.25,   // spread X
                0.15,   // spread Y
                0.25,   // spread Z
                0.02    // speed
        );

        // Schedule actual break in 1 second
        serverLevel.scheduleTick(pos, this, BREAK_DELAY_TICKS);
    }

    // This runs after the scheduled delay
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Break without dropping (you’re already using false)
        level.destroyBlock(pos, false);
        // Optional: play default block break effect (sound + particles)
        level.levelEvent(2001, pos, Block.getId(state));
    }

    /*// 1) Break when you RUN on it
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        if (entity instanceof Player player && player.isSprinting()) {
            triggerBrittle(level, pos, state, player);
        }
    }*/

    @Override
    public boolean canSurvive(BlockState $$0, LevelReader $$1, BlockPos $$2) {
        return super.canSurvive($$0, $$1, $$2);
    }

    // 2) Break when you LAND on it from 1+ block high
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);

        if (fallDistance >= 1.0F) {
            triggerBrittle(level, pos, state, entity);
        }
    }

    // 3) Break when an adjacent block breaks
    @Override
    public void neighborChanged(BlockState state,
                                Level level,
                                BlockPos pos,
                                Block neighborBlock,
                                BlockPos neighborPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.isClientSide) return;

        // Only care about neighbors that have just become air
        if (!level.isEmptyBlock(neighborPos)) {
            return;
        }

        // If the old block was air already, ignore (this was probably a placement/update)
        if (neighborBlock == Blocks.AIR) {
            return;
        }

        // Approximate the old state of the neighbor as its default state
        BlockState guessedOld = neighborBlock.defaultBlockState();

        // Only trigger if that block is a *full cube* in general
        // (torches, rails, buttons, etc. will fail this)
        boolean wasFullBlock = guessedOld.isCollisionShapeFullBlock(level, neighborPos);
        // If your mappings don't have isCollisionShapeFullBlock, you can use:
        // boolean wasFullBlock = Block.isShapeFullBlock(guessedOld.getCollisionShape(level, neighborPos));

        if (!wasFullBlock) {
            return; // Not a full block → no brittle trigger
        }

        // Full block neighbor got broken → trigger Brittlestone reaction
        triggerBrittle(level, pos, state, null);
    }

}
