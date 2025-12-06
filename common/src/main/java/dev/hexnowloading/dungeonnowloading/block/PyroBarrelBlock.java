package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.entity.misc.PayloadEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Supplier;

public class PyroBarrelBlock extends GenericExplosiveBarrelBlock {
    public static final IntegerProperty MODE = IntegerProperty.create("pyro_mode", 0, 3); // 0=idle,1=normal,2=flaming,3=fall

    public PyroBarrelBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(FUSE, 0)
                .setValue(MODE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MODE);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        int mode = state.getValue(MODE);
        int fuse = state.getValue(FUSE);
        if (mode > 0 && fuse > 0) {
            if (mode == 3) {
                // FALL mode: two behaviors
                // - fast staged spread when fuse <= 4 (radius 1..4 with slight delay)
                // - legacy gradual spread when fuse > 4 (threshold-based)
                if (fuse <= 4) {
                    int radius = 5 - fuse; // 4->1, 3->2, 2->3, 1->4
                    placeOilRing(level, pos, radius);
                    // decrement stage and re-schedule with slight delay (2 ticks)
                    BlockState next = state.setValue(FUSE, fuse - 1);
                    level.setBlock(pos, next, Block.UPDATE_ALL);
                    if (fuse - 1 > 0) {
                        level.scheduleTick(pos, this, 2);
                    } else {
                        // finished all rings: burst and remove barrel
                        onDetonate(level, pos, null);
                        level.removeBlock(pos, false);
                    }
                } else {
                    // legacy gradual spread (~60 ticks)
                    int radius = fuse >= 46 ? 1 : (fuse >= 31 ? 2 : (fuse >= 16 ? 3 : 4));
                    placeOilRing(level, pos, radius);
                    level.scheduleTick(pos, this, 1);
                }
            } else {
                // NORMAL/FLAMING: only spew oil payload blobs
                spewOilBlobs(level, pos);
                level.scheduleTick(pos, this, 1);
            }
        }
    }

    @Override
    protected boolean replaceWithFireOnDetonate(BlockState state) {
        // Flaming arrow or flint-and-steel: place fire at barrel when done
        return state.getValue(MODE) == 2;
    }

    @Override
    protected void onDetonate(Level level, BlockPos pos, LivingEntity owner) {
        if (!(level instanceof ServerLevel sl)) return;
        // Final burst: spew OIL payloads
        int blobs = 16 + sl.getRandom().nextInt(6);
        for (int i = 0; i < blobs; i++) {
            int dx = sl.getRandom().nextInt(3) - 1;
            int dz = sl.getRandom().nextInt(3) - 1;
            double sx = pos.getX() + dx + 0.5D;
            double sy = pos.getY() + 0.9D;
            double sz = pos.getZ() + dz + 0.5D;
            double vx = (sl.getRandom().nextDouble() - 0.5D) * 0.42D;
            double vy = 0.45D + sl.getRandom().nextDouble() * 0.25D;
            double vz = (sl.getRandom().nextDouble() - 0.5D) * 0.42D;
            sl.sendParticles(ParticleTypes.SMOKE, sx, sy, sz, 2, 0.05, 0.02, 0.05, 0.0D);
            PayloadEntity blob = new PayloadEntity(sl, sx, sy, sz, PayloadEntity.Kind.OIL);
            blob.setPhysics(0.04D, 0.98D);
            blob.setDeltaMovement(vx, vy, vz);
            sl.addFreshEntity(blob);
        }
    }

    @Override
    public void onImmediateTrigger(Level level, BlockPos pos, LivingEntity owner, TriggerCause cause) {
        if (!(level instanceof ServerLevel sl)) return;
        switch (cause) {
            case PROJECTILE_FLAMING_ARROW, GENTLY_LIT_ON_FIRE -> {
                BlockState s = this.defaultBlockState();
                s = s.setValue(MODE, 2).setValue(FUSE, 40);
                sl.setBlock(pos, s, Block.UPDATE_ALL);
                sl.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                sl.scheduleTick(pos, this, 1);
            }
            case PROJECTILE_NORMAL_ARROW -> {
                BlockState s = this.defaultBlockState();
                s = s.setValue(MODE, 1).setValue(FUSE, 40);
                sl.setBlock(pos, s, Block.UPDATE_ALL);
                sl.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.8F, 0.9F);
                sl.scheduleTick(pos, this, 1);
            }
            case FALL_IMPACT, IMPACT_BY_FALLING_BLOCK -> {
                // Fast staged ring: start at radius 1 and finish to 4 with slight delay between stages
                BlockState s = this.defaultBlockState().setValue(MODE, 3).setValue(FUSE, 4);
                sl.setBlock(pos, s, Block.UPDATE_ALL);
                sl.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 0.8F);
                // immediate projectile burst
                onDetonate(sl, pos, owner);
                // schedule first staged tick (slight delay)
                sl.scheduleTick(pos, this, 2);
            }
            default -> super.onImmediateTrigger(level, pos, owner, cause);
        }
    }

    // Place a ring following terrain at or below barrel level (never above)
    private void placeOilRing(ServerLevel level, BlockPos center, int radius) {
        Block spillBlock = getOilSpillBlockSafe();
        if (!(spillBlock instanceof OilSpillBlock)) return;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= radius * radius && d2 > (radius - 1) * (radius - 1)) {
                    BlockPos base = center.offset(dx, 0, dz);
                    // Only consider base (same Y) and one block below, never above
                    BlockPos[] candidates = new BlockPos[] { base, base.below() };
                    for (BlockPos c : candidates) {
                        mutable.set(c);
                        BlockState s = level.getBlockState(mutable);
                        if ((level.isEmptyBlock(mutable) || s.canBeReplaced()) && !(s.getBlock() instanceof OilSpillBlock)) {
                            level.setBlock(mutable, spillBlock.defaultBlockState(), Block.UPDATE_ALL);
                            break;
                        }
                    }
                }
            }
        }
    }

    // --- helpers ---
    private Block getOilSpillBlockSafe() {
        if (!DNLBlocks.blocksRegistered) return null;
        Supplier<Block> sup = DNLBlocks.OIL_SPILL;
        return sup == null ? null : sup.get();
    }

    private void spewOilBlobs(ServerLevel level, BlockPos pos) {
        int blobs = 2 + level.getRandom().nextInt(3);
        for (int i = 0; i < blobs; i++) {
            double sx = pos.getX() + 0.5D;
            double sy = pos.getY() + 0.9D;
            double sz = pos.getZ() + 0.5D;
            double vx = (level.getRandom().nextDouble() - 0.5D) * 0.35D;
            double vy = 0.35D + level.getRandom().nextDouble() * 0.30D;
            double vz = (level.getRandom().nextDouble() - 0.5D) * 0.35D;
            level.sendParticles(ParticleTypes.SMOKE, sx, sy, sz, 1, 0.02, 0.02, 0.02, 0.0D);
            PayloadEntity blob = new PayloadEntity(level, sx, sy, sz, PayloadEntity.Kind.OIL);
            blob.setPhysics(0.04D, 0.98D);
            blob.setDeltaMovement(vx, vy, vz);
            level.addFreshEntity(blob);
        }
    }

    private void gradualSpill(ServerLevel level, BlockPos center, int radius, RandomSource random) {
        Block spillBlock = getOilSpillBlockSafe();
        if (!(spillBlock instanceof OilSpillBlock)) return;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        // Place a handful of random positions within radius each tick to simulate slow spill
        int attempts = 6;
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            if (dx * dx + dz * dz <= radius * radius) {
                // try ground, then down one, then up one to follow terrain
                BlockPos[] candidates = new BlockPos[] {
                        center.offset(dx, 0, dz), center.offset(dx, -1, dz), center.offset(dx, 1, dz)
                };
                for (BlockPos c : candidates) {
                    mutable.set(c);
                    BlockState s = level.getBlockState(mutable);
                    if ((level.isEmptyBlock(mutable) || s.canBeReplaced()) && !(s.getBlock() instanceof OilSpillBlock)) {
                        level.setBlock(mutable, spillBlock.defaultBlockState(), Block.UPDATE_ALL);
                        break;
                    }
                }
            }
        }
    }
}
