package dev.hexnowloading.dungeonnowloading.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

public class SmoothBodyRotationControl extends BodyRotationControl {
    private final Mob mob;
    private final float rotationPerTick;
    private static final int HEAD_STABLE_ANGLE = 15;
    private static final int DELAY_UNTIL_STARTING_TO_FACE_FORWARD = 100;
    private static final int HOW_LONG_IT_TAKES_TO_FACE_FORWARD = 100;

    public SmoothBodyRotationControl(Mob mob, float rotationPerTick) {
        super(mob);
        this.mob = mob;
        this.rotationPerTick = rotationPerTick;
    }

    public void clientTick() {
        this.mob.yHeadRot = this.mob.yBodyRot;
        this.mob.yBodyRot = interpolateRotation(this.mob.yBodyRot, this.mob.getYRot(), this.rotationPerTick);
    }

    private float interpolateRotation(float current, float target, float maxChange) {
        float difference = Mth.wrapDegrees(target - current);
        if (difference > maxChange) difference = maxChange;
        if (difference < -maxChange) difference = -maxChange;
        return current + difference;
    }
}
