package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.FairkeeperFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperAwakenGoal extends Goal {
    private final FairkeeperBorosEntity fairkeeper;
    private BlockPos initialTarget;
    private BlockPos finalTarget;
    private boolean movingHorizontally;

    private static final double VERTICAL_SPEED = 0.4F;
    private static final double THRESHOLD = 2.0;

    public FairkeeperAwakenGoal(FairkeeperBorosEntity fairkeeper) {
        this.fairkeeper = fairkeeper;
        this.movingHorizontally = false;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.fairkeeper.isState(FairkeeperBorosEntity.FairkeeperState.AWAKENING);
    }

    @Override
    public void start() {
        this.fairkeeper.setNoGravity(true);
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.fairkeeper.getCaller();
        int verticalOffset = caller.getVerticalOffset();
        int horizontalOffset = caller.getHorizontalOffset();
        BlockPos callerPos = caller.blockPosition();
        Direction direction = caller.getDirection();
        this.initialTarget = new BlockPos(callerPos.relative(direction.getCounterClockWise(), horizontalOffset).below(verticalOffset));
        this.finalTarget = new BlockPos(callerPos.relative(direction.getClockWise(), horizontalOffset).below(verticalOffset));
        this.fairkeeper.setAwakenEndPos(this.finalTarget);
    }

    @Override
    public void stop() {
        this.fairkeeper.setState(FairkeeperBorosEntity.FairkeeperState.IDLE);
    }

    @Override
    public void tick() {
        if (!movingHorizontally) {
            fairkeeper.setDeltaMovement(new Vec3(0.0F, -VERTICAL_SPEED, 0.0F));
            double deltaY = this.fairkeeper.getY() - this.initialTarget.getY();
            if (deltaY * deltaY < THRESHOLD * THRESHOLD) {
                movingHorizontally = true;
                this.fairkeeper.setNoGravity(false);
            }
        } else {
            // Move horizontally toward the final target position
            this.fairkeeper.getMoveControl().setWantedPosition(this.finalTarget.getX(), this.finalTarget.getY(), this.finalTarget.getZ(), 1.0F);

            lookTowardTarget();
            // Check if the boss has reached the final target
            double deltaX = this.fairkeeper.getX() - finalTarget.getX();
            double deltaZ = this.fairkeeper.getZ() - finalTarget.getZ();

            if ((deltaX * deltaX + deltaZ * deltaZ) < THRESHOLD * THRESHOLD) {
                stop(); // Stop the goal when the final target is reached
            }
        }

        // Ensure the boss faces the movement direction
    }

    private void lookTowardTarget() {
        // Calculate the direction to the target position
        double directionX = this.finalTarget.getX() - this.fairkeeper.getX();
        double directionZ = this.finalTarget.getZ() - this.fairkeeper.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0; // Subtract 90 degrees for Minecraft's yaw convention

        // Update Boros's yaw to face the direction of movement
        this.fairkeeper.setYRot((float) yaw); // Horizontal rotation
        this.fairkeeper.yBodyRot = (float) yaw; // Body rotation
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true; // Ensure the goal is updated every tick
    }
}
