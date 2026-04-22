package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
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
            Vec3 currentCenter = this.mob instanceof WispEntity wisp ? wisp.getNavigationAnchorPos() : this.mob.getBoundingBox().getCenter();
            double deltaX = wantedX - currentCenter.x;
            double deltaY = wantedY - currentCenter.y;
            double deltaZ = wantedZ - currentCenter.z;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (distance < 2.5000003E-7F) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5));
                return;
            }
            double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED);
            Vec3 movement = new Vec3(deltaX / distance, deltaY / distance, deltaZ / distance).scale(speed);
            this.mob.setDeltaMovement(movement);
        }

        Vec3 motion = this.mob.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6D) {
            lookTowardMotion(motion);
        }
    }

    private void lookTowardMotion(Vec3 motion) {
        double horizontalDistance = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        double yaw = Math.toDegrees(Math.atan2(motion.z, motion.x)) - 90.0D;
        double pitch = -Math.toDegrees(Math.atan2(motion.y, horizontalDistance));

        this.mob.setYRot((float) yaw);
        this.mob.setYHeadRot((float) yaw);
        this.mob.yRotO = (float) yaw;
        this.mob.yHeadRotO = (float) yaw;
        this.mob.setXRot((float) pitch);
        this.mob.xRotO = (float) pitch;
    }
}
