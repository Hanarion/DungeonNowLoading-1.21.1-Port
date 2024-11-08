package dev.hexnowloading.dungeonnowloading.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.player.Player;

public class SmoothBodyRotationControl extends BodyRotationControl {
    private final Mob mob;
    private final float rotationPerTick;
    private boolean shouldRotate = true; // Flag to control rotation
    private int rotationDelayCounter = 0;
    private static final int DELAY_UNTIL_STARTING_TO_FACE_FORWARD = 100;
    private static final int HOW_LONG_IT_TAKES_TO_FACE_FORWARD = 100;

    public SmoothBodyRotationControl(Mob mob, float rotationPerTick) {
        super(mob);
        this.mob = mob;
        this.rotationPerTick = rotationPerTick;
    }

    @Override
    public void clientTick() {
        if (shouldRotate) {
            // Smoothly interpolate body rotation to match the head rotation direction
            this.mob.yHeadRot = this.mob.getYRot(); // Set head rotation to match YRot (direction to target)
            this.mob.yBodyRot = interpolateRotation(this.mob.yBodyRot, this.mob.yHeadRot, rotationPerTick);
        } else {
            // Gradually return to forward-facing after delay
            if (++rotationDelayCounter >= DELAY_UNTIL_STARTING_TO_FACE_FORWARD) {
                this.mob.yBodyRot = interpolateRotation(this.mob.yBodyRot, this.mob.getYRot(), rotationPerTick / HOW_LONG_IT_TAKES_TO_FACE_FORWARD);
            }
        }
    }

    private float interpolateRotation(float current, float target, float maxChange) {
        float difference = Mth.wrapDegrees(target - current);
        if (difference > maxChange) difference = maxChange;
        if (difference < -maxChange) difference = -maxChange;
        return current + difference;
    }

    // Enable rotation towards the target
    public void enableRotation() {
        this.shouldRotate = true;
        this.rotationDelayCounter = 0;
    }

    // Disable rotation (e.g., when attacking)
    public void disableRotation() {
        this.shouldRotate = false;
    }
}