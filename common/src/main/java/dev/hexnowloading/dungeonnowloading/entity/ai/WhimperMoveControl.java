package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class WhimperMoveControl extends MoveControl {
    
    private final WhimperEntity whimper;

    public WhimperMoveControl(WhimperEntity whimper) {
        super(whimper);
        this.whimper = whimper;
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            double dx = this.wantedX - this.whimper.getX();
            double dy = this.wantedY - this.whimper.getY();
            double dz = this.wantedZ - this.whimper.getZ();

            double dist3 = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            // Old behavior stopped early when horizontally aligned, which can make a flyer hover above targets.
            // For Whimper, we prefer to keep moving through the point and let the attack goal decide when it's "close enough" to hit.
            double stopDistance = Math.max(0.25D, this.whimper.getBoundingBox().getSize());
            if (dist3 < stopDistance && !this.whimper.IsCharging()) {
                this.operation = MoveControl.Operation.WAIT;
                this.whimper.setDeltaMovement(this.whimper.getDeltaMovement().scale(0.5));
                return;
            }

            // If we're charging, never stop just because XZ is close; keep pushing so we don't hover-lock.
            if (!this.whimper.IsCharging() && distXZ < stopDistance && Math.abs(dy) < stopDistance) {
                this.operation = MoveControl.Operation.WAIT;
                this.whimper.setDeltaMovement(this.whimper.getDeltaMovement().scale(0.5));
                return;
            }

            Vec3 vec3 = new Vec3(dx, dy, dz);

            double accel = this.speedModifier * 0.05D;
            Vec3 add = vec3.scale(accel / dist3);

            // When charging, allow more vertical authority so we don't get stuck over the target.
            if (this.whimper.IsCharging()) {
                add = new Vec3(add.x, Mth.clamp(add.y, -0.20D, 0.20D), add.z);
            }

            this.whimper.setDeltaMovement(this.whimper.getDeltaMovement().add(add));

            if (this.whimper.getTarget() == null) {
                Vec3 deltaMovement = this.whimper.getDeltaMovement();
                this.whimper.setYRot(-((float) Mth.atan2(deltaMovement.x, deltaMovement.z)) * 57.295776F);
                this.whimper.yBodyRot = this.whimper.getYRot();
            } else {
                double lookX = this.whimper.getTarget().getX() - this.whimper.getX();
                double lookZ = this.whimper.getTarget().getZ() - this.whimper.getZ();
                this.whimper.setYRot(-((float) Mth.atan2(lookX, lookZ)) * 57.295776F);
                this.whimper.yBodyRot = this.whimper.getYRot();
            }
        }
    }
}