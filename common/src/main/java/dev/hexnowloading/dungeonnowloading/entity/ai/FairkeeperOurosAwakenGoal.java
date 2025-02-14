package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperOurosAwakenGoal extends Goal {
    private final FairkeeperOurosEntity ouros;
    private Vec3 initialTarget;
    private Vec3 finalTarget;
    private boolean movingHorizontally;

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
        //this.ouros.setNoGravity(true);
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        int verticalOffset = caller.getVerticalOffset();
        int horizontalOffset = caller.getHorizontalOffset();
        BlockPos callerPos = caller.blockPosition();
        Direction direction = caller.getDirection();
        this.initialTarget = (new BlockPos(callerPos.relative(direction.getClockWise(), horizontalOffset).above(verticalOffset))).getCenter().add(0.0f, 0.5f, 0.0f);
        this.finalTarget = (new BlockPos(callerPos.relative(direction.getCounterClockWise(), horizontalOffset).above(verticalOffset))).getCenter().add(0.0f, 0.5f, 0.0f);
        this.ouros.setAwakenEndPos(this.finalTarget);
    }

    @Override
    public void tick() {
        if (!movingHorizontally) {
            //double deltaY = this.ouros.getBoundingBox().maxY - initialTarget.y;
            if (ouros.onCieling()) {
                movingHorizontally = true;
            }
        } else {
            this.ouros.getMoveControl().setWantedPosition(this.finalTarget.x, this.ouros.getBoundingBox().maxY, this.finalTarget.z, 1.0F);

            lookTowardTarget();

            double deltaX = this.ouros.getX() - finalTarget.x;
            double deltaZ = this.ouros.getZ() - finalTarget.z;

            if ((deltaX * deltaX + deltaZ * deltaZ) < THRESHOLD * THRESHOLD) {
                this.ouros.targetRandomPlayer();
                this.ouros.stopAttacking(20);
            }
        }
    }

    private void lookTowardTarget() {
        double directionX = this.finalTarget.x - this.ouros.getX();
        double directionZ = this.finalTarget.z - this.ouros.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0;

        this.ouros.setYRot((float) yaw);
        this.ouros.yBodyRot = (float) yaw;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
