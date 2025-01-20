package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.FairkeeperFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperOurosAwakenGoal extends Goal {
    private final FairkeeperOurosEntity ouros;
    private BlockPos initialTarget;
    private BlockPos finalTarget;
    private boolean movingHorizontally;

    private static final double VERTICAL_SPEED = 0.335F;
    private static final double THRESHOLD = 1.0;

    public FairkeeperOurosAwakenGoal(FairkeeperOurosEntity ouros) {
        this.ouros = ouros;
        this.movingHorizontally = false;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.ouros.isState(FairkeeperOurosEntity.FairkeeperOurosState.AWAKENING);
    }

    @Override
    public void start() {
        this.ouros.setNoGravity(true);
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        int verticalOffset = caller.getVerticalOffset();
        int horizontalOffset = caller.getHorizontalOffset();
        BlockPos callerPos = caller.blockPosition();
        Direction direction = caller.getDirection();
        this.initialTarget = new BlockPos(callerPos.relative(direction.getClockWise(), horizontalOffset).above(verticalOffset));
        this.finalTarget = new BlockPos(callerPos.relative(direction.getCounterClockWise(), horizontalOffset).above(verticalOffset));
        this.ouros.setAwakenEndPos(this.finalTarget);
    }

    @Override
    public void stop() {
        this.ouros.setState(FairkeeperOurosEntity.FairkeeperOurosState.IDLE);
    }

    @Override
    public void tick() {
        if (!movingHorizontally) {
            ouros.setDeltaMovement(new Vec3(0.0F, VERTICAL_SPEED, 0.0F));
            double deltaY = this.ouros.getBoundingBox().maxY - this.initialTarget.getY();
            if (deltaY * deltaY < THRESHOLD * THRESHOLD) {
                movingHorizontally = true;
                ouros.setNoGravity(false);
            }
        } else {
            // Move horizontally toward the final target position
            this.ouros.getMoveControl().setWantedPosition(this.finalTarget.getX(), this.finalTarget.getY(), this.finalTarget.getZ(), 1.0F);
            //System.out.println("Horizontal");
            //this.ouros.setZza((float) this.ouros.getAttributeValue(Attributes.MOVEMENT_SPEED));

            lookTowardTarget();
            // Check if the boss has reached the final target
            double deltaX = this.ouros.getX() - finalTarget.getX();
            double deltaZ = this.ouros.getZ() - finalTarget.getZ();

            if ((deltaX * deltaX + deltaZ * deltaZ) < THRESHOLD * THRESHOLD) {
                stop(); // Stop the goal when the final target is reached
            }
        }

        // Ensure the boss faces the movement direction
    }

    private void lookTowardTarget() {
        // Calculate the direction to the target position
        double directionX = this.finalTarget.getX() - this.ouros.getX();
        double directionZ = this.finalTarget.getZ() - this.ouros.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0; // Subtract 90 degrees for Minecraft's yaw convention

        // Update Boros's yaw to face the direction of movement
        this.ouros.setYRot((float) yaw); // Horizontal rotation
        this.ouros.yBodyRot = (float) yaw; // Body rotation
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true; // Ensure the goal is updated every tick
    }
}
