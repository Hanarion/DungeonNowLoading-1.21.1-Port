package dev.hexnowloading.dungeonnowloading.entity.ai.control.look;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class BallistaGolemSmoothLookControl extends LookControl {

    private final float customYawSpeed;
    private final float customPitchSpeed;
    private final float maxYawOffset; // how far the head can turn from the body before the body follows

    public BallistaGolemSmoothLookControl(Mob mob, float yawSpeed, float pitchSpeed, float maxYawOffset) {
        super(mob);
        this.customYawSpeed = yawSpeed;
        this.customPitchSpeed = pitchSpeed;
        this.maxYawOffset = maxYawOffset;
    }

    @Override
    public void tick() {
        if (this.lookAtCooldown > 0) {
            --this.lookAtCooldown;

            // Smoothly rotate head toward target YAW + optional offset
            this.getYRotD().ifPresent(desiredYaw -> {
                this.mob.yHeadRot = this.rotateTowards(
                        this.mob.yHeadRot,
                        desiredYaw, // + optional offset if you want
                        this.customYawSpeed
                );
            });

            // Smoothly rotate head PITCH
            this.getXRotD().ifPresent(desiredPitch -> {
                this.mob.setXRot(this.rotateTowards(
                        this.mob.getXRot(),
                        desiredPitch, // + optional tilt if desired
                        this.customPitchSpeed
                ));
            });

        } else {
            // Look straight forward (when idle)
            this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), 0.0F, this.customPitchSpeed));
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, this.customYawSpeed);
        }

        // Adjust body rotation slightly if the head is too far off
        float yawDiff = Mth.wrapDegrees(this.mob.yHeadRot - this.mob.yBodyRot);

        if (yawDiff < -this.maxYawOffset) {
            this.mob.yBodyRot -= 4.0F;
        } else if (yawDiff > this.maxYawOffset) {
            this.mob.yBodyRot += 4.0F;
        }
    }

    @Override
    protected void clampHeadRotationToBody() {
        // Disable clamping so head can rotate freely
    }

    @Override
    protected boolean resetXRotOnTick() {
        return false; // Don’t force XRot to zero unless we're idle
    }
}
