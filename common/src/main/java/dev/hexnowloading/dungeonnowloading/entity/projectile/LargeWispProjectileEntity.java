package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LargeWispProjectileEntity extends WispProjectileEntity {
    private static final double IMPACT_HEAT_INFLATION = 0.5D;
    private static final int MAX_TRANSFORMED_BLOCKS = 5;
    private static final double PARTICLE_MULTIPLIER = 1.4D;

    public LargeWispProjectileEntity(EntityType<? extends LargeWispProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LargeWispProjectileEntity(Level level, LivingEntity owner) {
        super(DNLEntityTypes.LARGE_WISP_PROJECTILE.get(), level, owner);
    }

    @Override
    protected boolean shouldDiscardAfterHit(HitResult hitResult) {
        return hitResult.getType() == HitResult.Type.BLOCK;
    }

    @Override
    protected void handleEntityHit(EntityHitResult hitResult) {
        this.hitEntity = hitResult.getEntity();
        this.suppressFireAtBlockPos = null;
        if (this.hitEntity instanceof AbstractMinecart minecart) {
            this.handleMinecartHit(minecart);
        } else {
            this.applyEntityImpact(this.hitEntity);
        }
        this.hitEntity = null;
        this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, (BlockState) null));
    }

    @Override
    protected void spawnTrailParticles() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        Vec3 trailDirection = motion.lengthSqr() > 1.0E-7D ? motion.normalize() : Vec3.ZERO;
        double centerY = this.getY() + this.getBbHeight() * 0.5D;

        for (int i = 0; i < scaledCount(3); i++) {
            double distanceBack = 0.12D + i * 0.16D;
            double x = this.getX() - trailDirection.x * distanceBack + (this.random.nextDouble() - 0.5D) * 0.3D;
            double y = centerY - trailDirection.y * distanceBack + (this.random.nextDouble() - 0.5D) * 0.3D;
            double z = this.getZ() - trailDirection.z * distanceBack + (this.random.nextDouble() - 0.5D) * 0.3D;

            server.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.01D);
        }

        if (this.tickCount % 2 == 0) {
            double x = this.getX() - trailDirection.x * 0.35D;
            double y = centerY - trailDirection.y * 0.35D;
            double z = this.getZ() - trailDirection.z * 0.35D;
            server.sendParticles(ParticleTypes.SMOKE, x, y, z, scaledCount(1), 0.03D, 0.03D, 0.03D, 0.005D);
        }
    }

    @Override
    protected void handleBlockHit(BlockHitResult hitResult) {
        BlockPos impactPos = hitResult.getBlockPos();
        this.hitEntity = null;
        this.suppressFireAtBlockPos = null;

        if (this.breakThroughPowderSnow(impactPos)) {
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, impactPos, GameEvent.Context.of(this, Blocks.POWDER_SNOW.defaultBlockState()));
            return;
        }

        int transformedBlocks = this.tryHeatBlock(impactPos) ? 1 : 0;

        if (transformedBlocks < MAX_TRANSFORMED_BLOCKS && this.level() instanceof ServerLevel server) {
            AABB bounds = this.getBoundingBox().inflate(IMPACT_HEAT_INFLATION);
            BlockPos min = BlockPos.containing(bounds.minX + 1.0E-4D, bounds.minY + 1.0E-4D, bounds.minZ + 1.0E-4D);
            BlockPos max = BlockPos.containing(bounds.maxX - 1.0E-4D, bounds.maxY - 1.0E-4D, bounds.maxZ - 1.0E-4D);
            for (BlockPos blockPos : BlockPos.betweenClosed(min, max)) {
                if (transformedBlocks >= MAX_TRANSFORMED_BLOCKS) {
                    break;
                }
                if (blockPos.equals(impactPos)) {
                    continue;
                }
                BlockState blockState = server.getBlockState(blockPos);
                if (blockState.isAir() || blockState.getDestroySpeed(server, blockPos) < 0.0F) {
                    continue;
                }
                if (this.tryHeatBlock(blockPos.immutable())) {
                    transformedBlocks++;
                }
            }
        }

        this.level().gameEvent(GameEvent.PROJECTILE_LAND, impactPos, GameEvent.Context.of(this, this.level().getBlockState(impactPos)));
    }

    @Override
    protected void discardWithBurst() {
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }

        if (this.hitEntity != null) {
            this.applyEntityImpact(this.hitEntity);
        }

        ServerLevel server = (ServerLevel) this.level();
        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5D;
        double cz = this.getZ();

        server.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, scaledCount(1), 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLAME, cx, cy, cz, scaledCount(24), 0.35D, 0.35D, 0.35D, 0.08D);
        server.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, scaledCount(12), 0.30D, 0.30D, 0.30D, 0.03D);
        this.level().playSound(null, cx, cy, cz, DNLSounds.WISP_DEATH.get(), SoundSource.HOSTILE, 0.9F, 0.95F + this.random.nextFloat() * 0.1F);
        this.gameEvent(GameEvent.ENTITY_DIE);

        BlockPos center = this.blockPosition();
        RandomSource rand = server.getRandom();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos firePos = center.offset(dx, dy, dz);
                    BlockPos below = firePos.below();

                    boolean isCenter = dx == 0 && dy == 0 && dz == 0;
                    if (!isCenter && rand.nextFloat() > 0.5F) {
                        continue;
                    }

                    if (firePos.equals(this.suppressFireAtBlockPos)) {
                        continue;
                    }

                    if (server.isEmptyBlock(firePos)) {
                        BlockState belowState = server.getBlockState(below);
                        if (belowState.isFaceSturdy(server, below, Direction.UP)) {
                            server.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
                        }
                    }
                }
            }
        }

        this.discard();
    }

    private boolean tryHeatBlock(BlockPos blockPos) {
        if (!(this.level() instanceof ServerLevel server)) {
            return false;
        }

        BlockState beforeState = server.getBlockState(blockPos);
        if (beforeState.isAir() || beforeState.getDestroySpeed(server, blockPos) < 0.0F) {
            return false;
        }

        BlockPos previousSuppressPos = this.suppressFireAtBlockPos;
        BlockState previousSuppressState = previousSuppressPos != null ? server.getBlockState(previousSuppressPos) : null;

        this.heatBlock(blockPos);

        BlockState afterState = server.getBlockState(blockPos);
        if (!afterState.equals(beforeState)) {
            return true;
        }

        if (this.suppressFireAtBlockPos != null) {
            BlockState afterSuppressState = server.getBlockState(this.suppressFireAtBlockPos);
            if (previousSuppressPos == null || !this.suppressFireAtBlockPos.equals(previousSuppressPos)) {
                return true;
            }
            return previousSuppressState == null || !afterSuppressState.equals(previousSuppressState);
        }

        return false;
    }

    private static int scaledCount(int baseCount) {
        return Math.max(1, (int) Math.round(baseCount * PARTICLE_MULTIPLIER));
    }
}
