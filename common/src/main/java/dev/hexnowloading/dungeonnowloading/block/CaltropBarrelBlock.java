package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.entity.misc.PayloadEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CaltropBarrelBlock extends GenericExplosiveBarrelBlock {

    public CaltropBarrelBlock(Properties props) {
        super(props);
    }

    @Override
    protected boolean replaceWithFireOnDetonate(BlockState state) {
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
    }

    @Override
    public void onImmediateTrigger(Level level, BlockPos pos, LivingEntity owner, TriggerCause cause) {
        if (!(level instanceof ServerLevel sl)) return;
        // Instant ignition: tiny explosion and larger caltrop burst at lower speed, then remove barrel
        tinyExplosion(sl, pos);
        emitCaltropBurst(sl, pos, sl.getRandom(), 72 + sl.getRandom().nextInt(16), 0.8D);
        sl.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 0.7F, 1.25F);
        sl.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, 16, 0.25, 0.25, 0.25, 0.03D);
        sl.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void onDetonate(Level level, BlockPos pos, LivingEntity owner) {
        if (!(level instanceof ServerLevel sl)) return;
        // Detonation path (including falling impact): same instantaneous behavior
        tinyExplosion(sl, pos);
        emitCaltropBurst(sl, pos, sl.getRandom(), 72 + sl.getRandom().nextInt(16), 0.8D);
        sl.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 0.7F, 1.25F);
        sl.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, 16, 0.25, 0.25, 0.25, 0.03D);
        sl.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private void tinyExplosion(ServerLevel sl, BlockPos pos) {
        sl.explode(null, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, 0.8F, Level.ExplosionInteraction.NONE);
    }

    private void emitCaltropBurst(ServerLevel level, BlockPos pos, RandomSource random, int count, double speed) {
        double sx = pos.getX() + 0.5D;
        double sy = pos.getY() + 0.9D;
        double sz = pos.getZ() + 0.5D;
        for (int i = 0; i < count; i++) {
            // Only caltrops
            PayloadEntity.Kind kind = PayloadEntity.Kind.CALTROP;
            // Radial spherical direction with slight upward bias
            Vec3 dir = randomSphereDirection(random, 0.15D);
            Vec3 vel = dir.scale(speed);
            PayloadEntity payload = new PayloadEntity(level, sx, sy, sz, kind);
            payload.setPhysics(0.01D, 0.98D); // minimal gravity for fragment feel
            payload.setDeltaMovement(vel);
            level.addFreshEntity(payload);
            level.sendParticles(ParticleTypes.SMOKE, sx, sy, sz, 1, 0.02, 0.02, 0.02, 0.0D);
        }
    }

    private static Vec3 randomSphereDirection(RandomSource random, double upBias) {
        // Random unit vector with slight upward bias
        double x = random.nextDouble() * 2.0D - 1.0D;
        double y = random.nextDouble() * 2.0D - 1.0D + upBias;
        double z = random.nextDouble() * 2.0D - 1.0D;
        double len = Math.sqrt(x * x + y * y + z * z);
        if (len < 1.0E-4D) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }
        return new Vec3(x / len, y / len, z / len);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, net.minecraft.world.phys.BlockHitResult hit, Projectile projectile) {
        // Only flaming projectiles (e.g., flaming arrows) ignite the barrel
        if (!(level instanceof ServerLevel sl)) return;
        if (projectile.isOnFire()) {
            this.onImmediateTrigger(sl, hit.getBlockPos(), null, null);
        }
    }
}
