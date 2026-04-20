package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class WispFlyingMoveControl extends MoveControl {

    public WispFlyingMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double deltaX = wantedX - this.mob.getX();
            double deltaY = wantedY - this.mob.getY();
            double deltaZ = wantedZ - this.mob.getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (distance < 2.5000003E-7F) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5));
                return;
            }
            double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED);
            Vec3 movement = new Vec3(deltaX / distance, deltaY / distance, deltaZ / distance).scale(speed);
            this.mob.setDeltaMovement(movement);
            lookTowardTarget();
        }
    }

    private void lookTowardTarget() {
        double directionX = this.wantedX - this.mob.getX();
        double directionY = this.wantedY - this.mob.getY();
        double directionZ = this.wantedZ - this.mob.getZ();
        double horizontalDistance = Math.sqrt(directionX * directionX + directionZ * directionZ);
        double yaw = Math.toDegrees(Math.atan2(directionX, directionZ));
        double pitch = Math.toDegrees(Math.atan2(directionY, horizontalDistance));

        this.mob.setYRot((float) yaw);
        this.mob.yBodyRot = (float) yaw;
        this.mob.yHeadRot = (float) yaw;
        this.mob.setXRot((float) pitch);
    }
}
