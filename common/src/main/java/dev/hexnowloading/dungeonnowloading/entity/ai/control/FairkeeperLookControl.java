package dev.hexnowloading.dungeonnowloading.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;

public class FairkeeperLookControl extends LookControl {
    private final Mob mob;
    private float targetYaw;   // Target yaw
    private boolean hasYawTarget; // Whether there's a yaw target
    private float yawSpeed;    // Transition speed for yaw
    private float pitchSpeed;  // Transition speed for pitch

    public FairkeeperLookControl(Mob mob, float yawSpeed, float pitchSpeed) {
        super(mob);
        this.mob = mob;
        this.targetYaw = mob.getYRot();
        this.hasYawTarget = false;
        this.yawSpeed = yawSpeed;
        this.pitchSpeed = pitchSpeed;
    }

    @Override
    public void tick() {
        // Gradually adjust yaw to look at the target if present
        if (this.hasYawTarget) {
            float currentYaw = this.mob.getYRot();
            float smoothedYaw = Mth.lerp(this.yawSpeed, currentYaw, this.targetYaw);
            this.mob.setYRot(smoothedYaw);
            this.mob.yHeadRot = smoothedYaw; // Sync head rotation
            this.mob.yBodyRot = smoothedYaw; // Sync body rotation
        }

        // Always adjust pitch based on the vertical motion of the entity
        Vec3 motion = this.mob.getDeltaMovement();
        if (!motion.equals(Vec3.ZERO)) {
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z); // Horizontal movement magnitude
            float targetPitch = (float) Math.toDegrees(Math.atan2(-motion.y, horizontalSpeed)); // Calculate pitch

            // Gradually adjust pitch
            float currentPitch = this.mob.getXRot();
            float smoothedPitch = Mth.lerp(this.pitchSpeed, currentPitch, targetPitch);
            this.mob.setXRot(smoothedPitch);
        }
    }

    /**
     * Sets the yaw target for the entity to look at.
     */
    public void setYawTarget(float targetYaw) {
        this.targetYaw = targetYaw;
        this.hasYawTarget = true;
    }

    /**
     * Sets the target position for the entity to look at.
     */
    public void setLookAt(double x, double y, double z) {
        double dx = x - this.mob.getX();
        double dz = z - this.mob.getZ();
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        setYawTarget(targetYaw);
    }

    /**
     * Updates the transition speeds for yaw and pitch.
     */
    public void setTransitionSpeeds(float yawSpeed, float pitchSpeed) {
        this.yawSpeed = yawSpeed;
        this.pitchSpeed = pitchSpeed;
    }
}