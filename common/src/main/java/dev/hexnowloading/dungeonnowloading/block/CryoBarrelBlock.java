package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.entity.misc.PayloadEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CryoBarrelBlock extends GenericExplosiveBarrelBlock {
    public CryoBarrelBlock(Properties props) {
        super(props);
    }

    @Override
    protected void onDetonate(Level level, BlockPos pos, LivingEntity owner) {
        if (!level.isClientSide) {
            ServerLevel sl = (ServerLevel) level;

            // Spew several cryo payloads outward (powdered snow impact) with reduced speeds
            int count = 14 + sl.getRandom().nextInt(6); // 14..19 pieces
            for (int i = 0; i < count; i++) {
                int dx = sl.getRandom().nextInt(3) - 1; // -1..1
                int dz = sl.getRandom().nextInt(3) - 1; // -1..1
                double sx = pos.getX() + dx + 0.5D;
                double sy = pos.getY() + 0.9D;
                double sz = pos.getZ() + dz + 0.5D;
                double vx = (sl.getRandom().nextDouble() - 0.5D) * 0.35D;
                double vy = 0.45D + sl.getRandom().nextDouble() * 0.2D;
                double vz = (sl.getRandom().nextDouble() - 0.5D) * 0.35D;
                // Snow-like particles to sell motion
                for (int p = 0; p < 4; p++) {
                    double px = sx + vx * 0.2D + (sl.getRandom().nextDouble() - 0.5D) * 0.2D;
                    double py = sy + vy * 0.2D;
                    double pz = sz + vz * 0.2D;
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, px, py, pz, 1, 0, 0, 0, 0.0D);
                }
                PayloadEntity blob = new PayloadEntity(sl, sx, sy, sz, PayloadEntity.Kind.CRYO);
                blob.setPhysics(0.04D, 0.98D);
                blob.setDeltaMovement(vx, vy, vz);
                sl.addFreshEntity(blob);
            }

            // After firing, spread powdered snow: within 1-block radius, 2 blocks tall; within 2-block radius, 1 block tall
            spreadPowderSnow(sl, pos, 1, 2);
            spreadPowderSnow(sl, pos, 2, 1);
        }
    }

    private void spreadPowderSnow(ServerLevel level, BlockPos center, int radius, int height) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    for (int dy = 0; dy < height; dy++) {
                        mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        if (level.isEmptyBlock(mutable)) {
                            level.setBlock(mutable, Blocks.POWDER_SNOW.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
