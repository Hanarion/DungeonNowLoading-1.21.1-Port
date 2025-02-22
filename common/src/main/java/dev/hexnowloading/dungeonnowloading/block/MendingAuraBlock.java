package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MendingAuraBlock extends Block {

    public MendingAuraBlock(Properties $$0) {
        super($$0);
    }

    @Override
    public boolean skipRendering(BlockState p_53972_, BlockState p_53973_, Direction p_53974_) {
        return p_53973_.is(this) ? true : super.skipRendering(p_53972_, p_53973_, p_53974_);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        super.animateTick(blockState, level, blockPos, randomSource);

        if (randomSource.nextInt(60) == 0) {
            List<Vec3> possibleDirections = new ArrayList<>();

            if (level.isEmptyBlock(blockPos.north())) possibleDirections.add(new Vec3(0.0, 0.0, -1.0)); // Move North (-Z)
            if (level.isEmptyBlock(blockPos.south())) possibleDirections.add(new Vec3(0.0, 0.0, 1.0));  // Move South (+Z)
            if (level.isEmptyBlock(blockPos.east())) possibleDirections.add(new Vec3(1.0, 0.0, 0.0));   // Move East (+X)
            if (level.isEmptyBlock(blockPos.west())) possibleDirections.add(new Vec3(-1.0, 0.0, 0.0));  // Move West (-X)
            if (level.isEmptyBlock(blockPos.above())) possibleDirections.add(new Vec3(0.0, 1.0, 0.0));  // Move Up (+Y)
            if (level.isEmptyBlock(blockPos.below())) possibleDirections.add(new Vec3(0.0, -1.0, 0.0)); // Move Down (-Y)

            Vec3 velocity = new Vec3(0.0, 0.0, 0.0);
            Vec3 spawnOffset = new Vec3(0.0, 0.0, 0.0);

            if (!possibleDirections.isEmpty()) {
                Vec3 chosenDirection = possibleDirections.get(randomSource.nextInt(possibleDirections.size()));
                velocity = chosenDirection.scale(0.08);
                spawnOffset = chosenDirection;
            }

            double x = blockPos.getX() + randomSource.nextDouble() + spawnOffset.x * (0.5F + randomSource.nextDouble() * 1.5F);
            double y = blockPos.getY() + randomSource.nextDouble() + spawnOffset.y * (0.5F + randomSource.nextDouble() * 1.5F);
            double z = blockPos.getZ() + randomSource.nextDouble() + spawnOffset.z * (0.5F + randomSource.nextDouble() * 1.5F);

            level.addParticle(DNLParticleTypes.MENDING_POP_PARTICLE.get(), true, x, y, z, -velocity.x, -velocity.y, -velocity.z);
        }
    }
}
