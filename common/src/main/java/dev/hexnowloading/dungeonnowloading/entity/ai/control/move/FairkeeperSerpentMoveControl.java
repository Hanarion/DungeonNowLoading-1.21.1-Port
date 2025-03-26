package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class FairkeeperSerpentMoveControl extends MoveControl {
    private final Mob mob; // The entity using this control
    private final float rotationSpeed; // How quickly the entity rotates towards the target

    public FairkeeperSerpentMoveControl(Mob mob, float rotationSpeed) {
        super(mob);
        this.mob = mob;
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            double dx = this.wantedX - this.mob.getX();
            double dz = this.wantedZ - this.mob.getZ();
            double dy = this.wantedY - this.mob.getY();

            // Calculate the distance and desired angle
            double distance = Math.sqrt(dx * dx + dz * dz);
            float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;

            // Gradually rotate the entity towards the target
            this.mob.setYRot(rotateTowards(this.mob.getYRot(), targetYaw, this.rotationSpeed));
            this.mob.yBodyRot = this.mob.getYRot();
            this.mob.yHeadRot = rotateTowards(this.mob.getYHeadRot(), targetYaw, this.rotationSpeed);

            // Move the entity forward in the current facing direction
            if (distance > 0.1) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                /*this.mob.setDeltaMovement(
                        this.mob.getDeltaMovement().add(
                                -Math.sin(Math.toRadians(this.mob.getYRot())) * this.speedModifier,
                                dy * this.speedModifier,
                                Math.cos(Math.toRadians(this.mob.getYRot())) * this.speedModifier
                        )
                );*/
            } else {
                this.operation = Operation.WAIT; // Stop moving when close enough
            }
        }
    }

    private float rotateTowards(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees(target - current);
        return current + Mth.clamp(delta, -maxDelta, maxDelta);
    }
}
