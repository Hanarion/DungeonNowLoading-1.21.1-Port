package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SilverfishBarrelBlock extends GenericExplosiveBarrelBlock {

    public SilverfishBarrelBlock(Properties props) {
        super(props);
    }

    @Override
    protected boolean replaceWithFireOnDetonate(BlockState state) {
        return false;
    }

    @Override
    public void onImmediateTrigger(Level level, BlockPos pos, LivingEntity owner, TriggerCause cause) {
        if (!(level instanceof ServerLevel sl)) return;
        // Immediate triggers: detonate behavior + clear the barrel block
        detonateNow(sl, pos, owner);
        sl.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void onDetonate(Level level, BlockPos pos, LivingEntity owner) {
        if (!(level instanceof ServerLevel sl)) return;
        detonateNow(sl, pos, owner);
    }

    private void detonateNow(ServerLevel level, BlockPos pos, LivingEntity owner) {
        level.explode(null,
                pos.getX() + 0.5D,
                pos.getY() + 0.9D,
                pos.getZ() + 0.5D,
                0.8F,
                Level.ExplosionInteraction.NONE);

        emitSilverfishBurst(level, pos, level.getRandom(), 10);

        level.playSound(null, pos, SoundEvents.SILVERFISH_HURT, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        level.sendParticles(ParticleTypes.SMOKE,
                pos.getX() + 0.5D,
                pos.getY() + 0.9D,
                pos.getZ() + 0.5D,
                18,
                0.25,
                0.25,
                0.25,
                0.03D);
    }

    private void emitSilverfishBurst(ServerLevel level, BlockPos pos, RandomSource random, int count) {
        double sx = pos.getX() + 0.5D;
        double sy = pos.getY() + 0.1D;
        double sz = pos.getZ() + 0.5D;

        for (int i = 0; i < count; i++) {
            Silverfish fish = EntityType.SILVERFISH.create(level);
            if (fish == null) continue;

            fish.moveTo(sx, sy, sz, random.nextFloat() * 360.0F, 0.0F);


            Vec3 dir = randomSphereDirection(random, 0.35D);
            double speed = 0.55D + random.nextDouble() * 0.25D;
            fish.setDeltaMovement(dir.scale(speed));

            level.addFreshEntity(fish);


            level.sendParticles(ParticleTypes.POOF, sx, sy + 0.5D, sz, 1, 0.02, 0.02, 0.02, 0.0D);
        }
    }

    private static Vec3 randomSphereDirection(RandomSource random, double upBias) {
        double x = random.nextDouble() * 2.0D - 1.0D;
        double y = random.nextDouble() * 2.0D - 1.0D + upBias;
        double z = random.nextDouble() * 2.0D - 1.0D;
        double len = Math.sqrt(x * x + y * y + z * z);
        if (len < 1.0E-4D) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }
        return new Vec3(x / len, y / len, z / len);
    }
}