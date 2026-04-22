package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.block.DungeonWallTorch;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class WispLightBlocksGoal extends Goal {
    public static final int HORIZONTAL_SEARCH_RANGE = 16;
    public static final int VERTICAL_SEARCH_RANGE = 8;
    private static final int TARGET_REFRESH_INTERVAL = 20;
    private static final int RETRY_DELAY_TICKS = 30;
    private static final double TARGET_REACHED_DISTANCE_SQR = 1.3D * 1.3D;
    private static final double FINAL_APPROACH_REPATH_DISTANCE_SQR = 2.0D * 2.0D;
    private static final float TURN_SPEED = 12.0F;
    private static final double PATH_SPEED_MODIFIER = 0.16D;
    private static final double FINAL_APPROACH_SPEED_MODIFIER = 0.12D;

    private final WispEntity wisp;
    @Nullable
    private BlockPos targetBlock;
    private int retargetCooldown;
    private int retryCooldown;

    public WispLightBlocksGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.wisp.isAlive() || this.wisp.getTarget() != null) {
            return false;
        }

        if (this.retryCooldown > 0) {
            this.retryCooldown--;
            return false;
        }

        this.targetBlock = this.findNearestLightableBlock(null);
        return this.targetBlock != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.wisp.isAlive()
                && this.wisp.getTarget() == null
                && this.targetBlock != null
                && this.isValidLightTarget(this.wisp.level(), this.targetBlock);
    }

    @Override
    public void start() {
        this.retargetCooldown = 0;
        this.moveToTarget();
    }

    @Override
    public void stop() {
        this.targetBlock = null;
        this.retargetCooldown = 0;
        this.wisp.getNavigation().stop();
        this.wisp.setDeltaMovement(this.wisp.getDeltaMovement().scale(0.6D));
    }

    @Override
    public void tick() {
        if (this.targetBlock == null) {
            return;
        }

        if (--this.retargetCooldown <= 0) {
            this.retargetCooldown = TARGET_REFRESH_INTERVAL;
            BlockPos nearest = this.findNearestLightableBlock(null);
            if (nearest != null) {
                this.targetBlock = nearest;
                this.moveToTarget();
            }
        }

        if (!this.isValidLightTarget(this.wisp.level(), this.targetBlock)) {
            this.targetBlock = null;
            this.wisp.getNavigation().stop();
            this.retryCooldown = RETRY_DELAY_TICKS;
            return;
        }

        Vec3 targetPos = this.getApproachPos(this.targetBlock);
        if (this.wisp.getNavigation().isDone()) {
            double distanceToTargetSqr = this.wisp.position().distanceToSqr(targetPos);
            if (distanceToTargetSqr > TARGET_REACHED_DISTANCE_SQR) {
                this.wisp.getMoveControl().setWantedPosition(
                        targetPos.x,
                        targetPos.y,
                        targetPos.z,
                        FINAL_APPROACH_SPEED_MODIFIER
                );
            } else {
                this.moveToTarget();
            }
        }

        this.wisp.rotateTowardMotion(this.wisp.getDeltaMovement(), TURN_SPEED);

        if (Vec3.atCenterOf(this.targetBlock).distanceToSqr(this.wisp.position()) <= TARGET_REACHED_DISTANCE_SQR) {
            if (this.lightTarget(this.targetBlock)) {
                BlockPos litBlock = this.targetBlock;
                this.disengageFromLitBlock(litBlock);
                BlockPos nextTarget = this.findNearestLightableBlock(litBlock);
                if (nextTarget != null) {
                    this.targetBlock = nextTarget;
                    this.retargetCooldown = TARGET_REFRESH_INTERVAL;
                    this.moveToTarget();
                } else {
                    this.targetBlock = null;
                    this.retryCooldown = 10;
                }
            }
        }
    }

    @Nullable
    private BlockPos findNearestLightableBlock(@Nullable BlockPos excludedPos) {
        BlockPos origin = this.wisp.blockPosition();
        BlockPos best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (int dx = -HORIZONTAL_SEARCH_RANGE; dx <= HORIZONTAL_SEARCH_RANGE; dx++) {
            for (int dy = -VERTICAL_SEARCH_RANGE; dy <= VERTICAL_SEARCH_RANGE; dy++) {
                for (int dz = -HORIZONTAL_SEARCH_RANGE; dz <= HORIZONTAL_SEARCH_RANGE; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (excludedPos != null && candidate.equals(excludedPos)) {
                        continue;
                    }
                    if (!this.isValidLightTarget(this.wisp.level(), candidate)) {
                        continue;
                    }
                    if (!this.hasLineOfSight(candidate)) {
                        continue;
                    }
                    if (!this.canPathTo(candidate)) {
                        continue;
                    }

                    double distanceSqr = candidate.distToCenterSqr(this.wisp.position());
                    if (distanceSqr < bestDistanceSqr) {
                        bestDistanceSqr = distanceSqr;
                        best = candidate.immutable();
                    }
                }
            }
        }

        return best;
    }

    public static boolean isValidLightTarget(Level level, BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (CampfireBlock.canLight(state)) {
            return true;
        }

        if (CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
            return true;
        }

        if (block instanceof DungeonWallTorch) {
            return state.hasProperty(BlockStateProperties.LIT)
                    && !state.getValue(BlockStateProperties.LIT)
                    && (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !state.getValue(BlockStateProperties.WATERLOGGED));
        }

        return false;
    }

    private boolean hasLineOfSight(BlockPos pos) {
        Vec3 from = this.wisp.getEyePosition();
        Vec3 to = this.getApproachPos(pos);
        if (this.hasWaterBetween(from, to, pos)) {
            return false;
        }
        Vec3 start = from;

        for (int i = 0; i < 8; i++) {
            HitResult hitResult = this.wisp.level().clip(new ClipContext(start, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.wisp));
            if (hitResult.getType() == HitResult.Type.MISS) {
                return true;
            }
            if (hitResult.getType() != HitResult.Type.BLOCK) {
                return false;
            }

            net.minecraft.world.phys.BlockHitResult blockHitResult = (net.minecraft.world.phys.BlockHitResult) hitResult;
            BlockPos hitPos = blockHitResult.getBlockPos();
            if (hitPos.equals(pos)) {
                return true;
            }
            if (!this.isIgnorableSightBlock(hitPos)) {
                return false;
            }

            start = blockHitResult.getLocation().add(to.subtract(from).normalize().scale(0.05D));
        }

        return false;
    }

    private boolean hasWaterBetween(Vec3 from, Vec3 to, BlockPos targetPos) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-6D) {
            return false;
        }

        Vec3 step = delta.scale(0.25D / length);
        Vec3 cursor = from;
        int steps = Math.max(1, (int) Math.ceil(length / 0.25D));
        for (int i = 0; i <= steps; i++) {
            BlockPos samplePos = BlockPos.containing(cursor);
            if (!samplePos.equals(targetPos) && this.wisp.level().getBlockState(samplePos).getFluidState().is(FluidTags.WATER)) {
                return true;
            }
            cursor = cursor.add(step);
        }
        return false;
    }

    private boolean isIgnorableSightBlock(BlockPos pos) {
        return this.wisp.level().getBlockState(pos).getBlock() instanceof FireBlock;
    }

    public static Vec3 getApproachPos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof CampfireBlock) {
            return Vec3.atBottomCenterOf(pos).add(0.0D, 0.65D, 0.0D);
        }

        if (block instanceof CandleBlock || block instanceof CandleCakeBlock) {
            return Vec3.atBottomCenterOf(pos).add(0.0D, 0.8D, 0.0D);
        }

        if (block instanceof DungeonWallTorch && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction flameSide = facing.getOpposite();
            return Vec3.atCenterOf(pos).add(flameSide.getStepX() * 0.28D, 0.12D, flameSide.getStepZ() * 0.28D);
        }

        return Vec3.atCenterOf(pos).add(0.0D, 0.25D, 0.0D);
    }

    private Vec3 getApproachPos(BlockPos pos) {
        return getApproachPos(this.wisp.level(), pos);
    }

    private boolean canPathTo(BlockPos pos) {
        Vec3 approachPos = this.getApproachPos(pos);
        var path = this.wisp.getNavigation().createPath(approachPos.x, approachPos.y, approachPos.z, 1);
        return path != null && path.canReach();
    }

    private void disengageFromLitBlock(BlockPos litBlock) {
        this.wisp.getNavigation().stop();

        Vec3 away = this.wisp.position().subtract(Vec3.atCenterOf(litBlock));
        Vec3 horizontalAway = new Vec3(away.x, 0.0D, away.z);
        Vec3 pushDirection;
        if (horizontalAway.lengthSqr() > 1.0E-4D) {
            pushDirection = horizontalAway.normalize();
        } else {
            pushDirection = Vec3.directionFromRotation(0.0F, this.wisp.getYRot()).scale(-1.0D);
        }

        Vec3 retreatTarget = Vec3.atCenterOf(litBlock).add(pushDirection.scale(1.2D)).add(0.0D, 0.6D, 0.0D);
        this.wisp.getMoveControl().setWantedPosition(
                retreatTarget.x,
                retreatTarget.y,
                retreatTarget.z,
                FINAL_APPROACH_SPEED_MODIFIER
        );
        this.wisp.setDeltaMovement(this.wisp.getDeltaMovement().add(pushDirection.scale(0.08D)).add(0.0D, 0.04D, 0.0D));
    }

    private void moveToTarget() {
        if (this.targetBlock == null) {
            return;
        }

        Vec3 approachPos = this.getApproachPos(this.targetBlock);
        if (this.wisp.position().distanceToSqr(approachPos) > FINAL_APPROACH_REPATH_DISTANCE_SQR) {
            this.wisp.getNavigation().moveTo(approachPos.x, approachPos.y, approachPos.z, PATH_SPEED_MODIFIER);
        }
    }

    private boolean lightTarget(BlockPos pos) {
        if (!(this.wisp.level() instanceof ServerLevel server)) {
            return false;
        }

        BlockState state = server.getBlockState(pos);
        Block block = state.getBlock();
        boolean lit = false;

        if (CampfireBlock.canLight(state)) {
            server.setBlock(pos, state.setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL);
            lit = true;
        } else if (CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
            server.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), Block.UPDATE_ALL);
            lit = true;
        } else if (block instanceof DungeonWallTorch
                && state.hasProperty(BlockStateProperties.LIT)
                && !state.getValue(BlockStateProperties.LIT)
                && (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !state.getValue(BlockStateProperties.WATERLOGGED))) {
            server.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), Block.UPDATE_ALL);
            lit = true;
        }

        if (!lit) {
            return false;
        }

        server.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.7F, 1.0F + this.wisp.getRandom().nextFloat() * 0.2F);
        server.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 6, 0.12D, 0.12D, 0.12D, 0.01D);
        server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 3, 0.08D, 0.08D, 0.08D, 0.0D);
        return true;
    }
}
