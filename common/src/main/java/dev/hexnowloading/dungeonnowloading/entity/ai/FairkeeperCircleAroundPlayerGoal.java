package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FairkeeperCircleAroundPlayerGoal extends Goal {
    private final FairkeeperBorosEntity boros;          // The mob executing the goal
    private LivingEntity target;      // The player or entity at the center
    private final double radius;      // Radius of the circle
    private final double speed;       // Speed of movement
    private final boolean clockwise;  // Direction of movement
    private double angle;             // Current angle in degrees
    private double targetX, targetY, targetZ;  // Current target position (X, Z only)

    private static final double THRESHOLD = 2.0; // Distance threshold to "reach" target

    public FairkeeperCircleAroundPlayerGoal(FairkeeperBorosEntity boros, double radius, double speed, boolean clockwise) {
        this.boros = boros;
        this.radius = radius;
        this.speed = speed;
        this.clockwise = clockwise;
        this.angle = 0; // Start angle
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Activate the goal if the Boros has a valid target
        this.target = this.boros.getTarget();
        return this.target != null && this.target.isAlive() && this.boros.isState(FairkeeperBorosEntity.FairkeeperState.CIRCLING);
    }

    @Override
    public boolean canContinueToUse() {
        // Continue circling as long as the target exists and is alive
        return this.target != null && this.target.isAlive();
    }

    @Override
    public void start() {
        // Initialize the angle and the first target position
        this.angle = 0;
        updateTargetPosition();
    }

    @Override
    public void tick() {
        if (this.target != null) {
            // Check if Boros has reached the current target position (X, Z only)
            double deltaX = this.boros.getX() - this.targetX;
            double deltaZ = this.boros.getZ() - this.targetZ;
            if ((deltaX * deltaX + deltaZ * deltaZ) < THRESHOLD * THRESHOLD) {
                // Increment the angle for the next position
                this.angle += this.clockwise ? -10 : 10; // Adjust angle increment for direction
                this.angle = this.angle % 360;           // Keep angle within 0-360 degrees

                // Update the next target position on the circle
                updateTargetPosition();
            }

            // Continue moving towards the current target position
            this.boros.getMoveControl().setWantedPosition(this.targetX, this.targetY, this.targetZ, this.speed);

            // Adjust Boros's yaw to face the moving direction
            lookTowardTarget();
        }
    }

    private void updateTargetPosition() {
        // Convert angle to radians for calculations
        double angleRad = Math.toRadians(this.angle);

        // Calculate the target position on the circle with the player as the center (X, Z only)
        this.targetX = this.target.getX() + this.radius * Math.cos(angleRad);
        this.targetZ = this.target.getZ() + this.radius * Math.sin(angleRad);
        this.targetY = this.target.getY();
    }

    private void lookTowardTarget() {
        // Calculate the direction to the target position
        double directionX = this.targetX - this.boros.getX();
        double directionZ = this.targetZ - this.boros.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0; // Subtract 90 degrees for Minecraft's yaw convention

        // Update Boros's yaw to face the direction of movement
        this.boros.setYRot((float) yaw); // Horizontal rotation
        this.boros.yBodyRot = (float) yaw; // Body rotation
    }
}