package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FairkeeperOurosCircleAroundGoal extends Goal {
    private final FairkeeperOurosEntity ouros;          // The mob executing the goal
    private Entity circlingTarget;      // The player or entity at the center
    private final double radius;      // Radius of the circle
    private final double speed;       // Speed of movement
    private final boolean clockwise;
    private final boolean circlePlayer; // Direction of movement
    private double angle;             // Current angle in degrees
    private double targetX, targetY, targetZ;  // Current target position (X, Z only)
    private int arenaSize;
    private BlockPos arenaCenter;
    private FairkeeperOurosEntity.FairkeeperOurosState state;

    private static final double THRESHOLD = 2.0; // Distance threshold to "reach" target

    public FairkeeperOurosCircleAroundGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros, double radius, double speed, boolean clockwise, boolean circlePlayer) {
        this.state = state;
        this.ouros = ouros;
        this.radius = radius;
        this.speed = speed;
        this.clockwise = clockwise;
        this.angle = 0;
        this.circlePlayer = circlePlayer;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        boolean b;
        if (this.circlePlayer) {
            this.circlingTarget = this.ouros.getTarget();
            b = this.circlingTarget != null && this.circlingTarget.isAlive();
        } else {
            this.circlingTarget = this.ouros.getCaller();
            b = this.circlingTarget != null;
        }
        return b && this.ouros.isState(state);
    }

    @Override
    public void start() {
        FairkeeperSerpentCallerEntity caller = ((FairkeeperSerpentCallerEntity) ouros.getCaller());
        this.arenaSize = caller != null ? caller.getArenaSize() : 0;
        this.arenaCenter = caller != null ? caller.blockPosition() : BlockPos.ZERO;

        if (this.circlingTarget != null) {
            double deltaX = this.ouros.getX() - this.circlingTarget.getX();
            double deltaZ = this.ouros.getZ() - this.circlingTarget.getZ();

            this.angle = Math.toDegrees(Math.atan2(deltaZ, deltaX));

            this.angle = (this.angle + 360) % 360;
        } else {
            this.angle = 0;
        }
        updateTargetPosition();
    }

    @Override
    public void tick() {
        if (this.circlingTarget != null) {
            double deltaX = this.ouros.getX() - this.targetX;
            double deltaZ = this.ouros.getZ() - this.targetZ;
            if ((deltaX * deltaX + deltaZ * deltaZ) < THRESHOLD * THRESHOLD) {
                this.angle += this.clockwise ? -10 : 10;
                this.angle = this.angle % 360;

                updateTargetPosition();
            }

            this.ouros.getMoveControl().setWantedPosition(this.targetX, this.targetY, this.targetZ, this.speed);
        }
    }

    private void updateTargetPosition() {
        double angleRad = Math.toRadians(this.angle);

        // Calculate the potential target position on the circle
        double potentialX = this.circlingTarget.getX() + this.radius * Math.cos(angleRad);
        double potentialZ = this.circlingTarget.getZ() + this.radius * Math.sin(angleRad);

        // Define arena boundaries
        double minX = this.arenaCenter.getX() - this.arenaSize + 1;
        double maxX = this.arenaCenter.getX() + this.arenaSize - 1;
        double minZ = this.arenaCenter.getZ() - this.arenaSize + 1;
        double maxZ = this.arenaCenter.getZ() + this.arenaSize - 1;

        // Clamp the target position within the arena boundaries
        this.targetX = Math.max(minX, Math.min(maxX, potentialX));
        this.targetZ = Math.max(minZ, Math.min(maxZ, potentialZ));

        // Maintain the circling target's Y-coordinate
        this.targetY = this.ouros.getBoundingBox().maxY;
    }
}
