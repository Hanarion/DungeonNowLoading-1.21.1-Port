package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class WispWanderGoal extends Goal {
    private static final int HORIZONTAL_RANGE = 7;
    private static final int VERTICAL_RANGE = 4;
    private static final int MAX_ATTEMPTS = 15;
    private static final double TARGET_REACHED_DISTANCE_SQR = 1.5D * 1.5D;
    private static final double XZ_DRIFT_SPEED = 0.18D;
    private static final double Y_DRIFT_SPEED = 0.14D;
    private static final double STEER_STRENGTH = 0.08D;
    private static final float TURN_SPEED = 10.0F;

    private final WispEntity wisp;
    @Nullable
    private BlockPos wanderTarget;

    public WispWanderGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.wisp.getTarget() == null && this.wisp.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.wisp.getTarget() == null && this.wisp.isAlive();
    }

    @Override
    public void start() {
        this.wanderTarget = null;
    }

    @Override
    public void stop() {
        this.wanderTarget = null;
        this.wisp.setDeltaMovement(this.wisp.getDeltaMovement().scale(0.6D));
    }

    @Override
    public void tick() {
        if (this.shouldPickNewTarget()) {
            this.wanderTarget = this.findWanderTarget();
        }

        if (this.wanderTarget == null) {
            this.wisp.setDeltaMovement(this.wisp.getDeltaMovement().scale(0.9D));
            return;
        }

        Vec3 targetCenter = Vec3.atCenterOf(this.wanderTarget);
        this.wisp.steerTowards(targetCenter, XZ_DRIFT_SPEED, Y_DRIFT_SPEED, STEER_STRENGTH, TURN_SPEED);
    }

    private boolean shouldPickNewTarget() {
        if (this.wanderTarget == null) {
            return true;
        }

        if (!this.isValidWanderPos(this.wanderTarget)) {
            return true;
        }

        if (this.wanderTarget.closerToCenterThan(this.wisp.position(), Math.sqrt(TARGET_REACHED_DISTANCE_SQR))) {
            return true;
        }

        return this.wisp.getRandom().nextInt(30) == 0;
    }

    @Nullable
    private BlockPos findWanderTarget() {
        BlockPos origin = this.wisp.blockPosition();

        if (this.wisp.onGround()) {
            BlockPos liftTarget = origin.above(2 + this.wisp.getRandom().nextInt(2));
            if (this.isValidWanderPos(liftTarget)) {
                return liftTarget;
            }
        }

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int dx = this.wisp.getRandom().nextInt(-HORIZONTAL_RANGE, HORIZONTAL_RANGE + 1);
            int dy = this.wisp.getRandom().nextInt(-VERTICAL_RANGE, VERTICAL_RANGE + 1);
            int dz = this.wisp.getRandom().nextInt(-HORIZONTAL_RANGE, HORIZONTAL_RANGE + 1);
            if (dx == 0 && dy == 0 && dz == 0) {
                continue;
            }

            BlockPos candidate = origin.offset(dx, dy, dz);
            if (this.isValidWanderPos(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private boolean isValidWanderPos(BlockPos pos) {
        if (!this.wisp.level().isInWorldBounds(pos) || !this.wisp.level().getWorldBorder().isWithinBounds(pos)) {
            return false;
        }

        BlockState state = this.wisp.level().getBlockState(pos);
        if (!state.getFluidState().isEmpty()) {
            return false;
        }

        if (!state.getCollisionShape(this.wisp.level(), pos).isEmpty()) {
            return false;
        }

        return this.wisp.level().noCollision(this.wisp, this.wisp.getBoundingBox().move(
                pos.getX() + 0.5D - this.wisp.getX(),
                pos.getY() + 0.5D - this.wisp.getY(),
                pos.getZ() + 0.5D - this.wisp.getZ()
        ));
    }
}
