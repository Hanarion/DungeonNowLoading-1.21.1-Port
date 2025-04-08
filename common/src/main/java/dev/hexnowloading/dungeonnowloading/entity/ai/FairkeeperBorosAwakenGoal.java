package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperBorosAwakenGoal extends Goal {
    private final FairkeeperBorosEntity fairkeeper;
    private Vec3 initialTarget;
    private Vec3 finalTarget;
    private boolean movingHorizontally;

    private static final double THRESHOLD = 2.0;

    public FairkeeperBorosAwakenGoal(FairkeeperBorosEntity fairkeeper) {
        this.fairkeeper = fairkeeper;
        this.movingHorizontally = false;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.fairkeeper.isState(FairkeeperBorosEntity.FairkeeperBorosState.AWAKENING);
    }

    @Override
    public void start() {
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.fairkeeper.getCaller();
        int verticalOffset = caller.getVerticalOffset();
        int horizontalOffset = caller.getHorizontalOffset();
        BlockPos callerPos = caller.blockPosition();
        Direction direction = caller.getDirection();
        this.initialTarget = (new BlockPos(callerPos.relative(direction.getCounterClockWise(), horizontalOffset).below(verticalOffset))).getCenter().add(0.0f, -0.5f, 0.0f);
        this.finalTarget = (new BlockPos(callerPos.relative(direction.getClockWise(), horizontalOffset).below(verticalOffset))).getCenter().add(0.0f, -0.5f, 0.0f);
        this.fairkeeper.setAwakenEndPos(this.initialTarget);
        this.fairkeeper.transitionTo(FairkeeperBorosEntity.FairkeeperBorosAnimationState.MOUTH_OPEN);
        this.fairkeeper.noPhysics = true;
    }

    @Override
    public void tick() {
        if (!movingHorizontally) {
            double deltaY = this.fairkeeper.getY() - this.initialTarget.y;
            //BlockState blockState = this.fairkeeper.level().getBlockState(this.fairkeeper.blockPosition().below(2));
            if (deltaY * deltaY < 5.0F * 5.0F) {
                if (this.fairkeeper.noPhysics) {
                    this.fairkeeper.noPhysics = false;
                }
                this.fairkeeper.addDeltaMovement(this.fairkeeper.getLookAngle().scale(0.05F));
            }
            if (fairkeeper.onGround()) {
                movingHorizontally = true;
                fairkeeper.setCanDestroyBlocks(true);
            }
        } else {
            this.fairkeeper.getMoveControl().setWantedPosition(this.finalTarget.x, this.finalTarget.y, this.finalTarget.z, 1.1F);

            lookTowardTarget();
            double deltaX = this.fairkeeper.getX() - finalTarget.x;
            double deltaZ = this.fairkeeper.getZ() - finalTarget.z;

            if ((deltaX * deltaX + deltaZ * deltaZ) < THRESHOLD * THRESHOLD) {
                this.fairkeeper.transitionTo(FairkeeperBorosEntity.FairkeeperBorosAnimationState.MOUTH_CLOSE);
                this.fairkeeper.stopAttacking(20);
            }
        }

    }

    private void lookTowardTarget() {
        double directionX = this.finalTarget.x - this.fairkeeper.getX();
        double directionZ = this.finalTarget.z - this.fairkeeper.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0;

        this.fairkeeper.setYRot((float) yaw);
        this.fairkeeper.yBodyRot = (float) yaw;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
