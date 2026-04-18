package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class GarholdMoveControl extends MoveControl {
    private final int maxTurn;
    private final boolean hoversInPlace;

    public GarholdMoveControl(Mob mob, int maxTurn, boolean hoversInPlace) {
        super(mob);
        this.maxTurn = maxTurn;
        this.hoversInPlace = hoversInPlace;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;

            mob.setNoGravity(true);

            double dx = wantedX - mob.getX();
            double dy = wantedY - mob.getY();
            double dz = wantedZ - mob.getZ();

            double distSq = dx*dx + dy*dy + dz*dz;
            if (distSq < 2.5e-7) {
                mob.setZza(0);
                mob.setYya(0);
                return;
            }

            float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            mob.setYRot(rotlerp(mob.getYRot(), yaw, 90.0F));

            float speed = (float)(this.speedModifier * mob.getAttributeValue(Attributes.FLYING_SPEED));
            mob.setSpeed(speed);

            // ✅ THIS is what makes "speed" actually move the mob via travel()
            mob.setZza(1.0F);          // forward intent (can also use speed, but 1.0 is typical)
            mob.setXxa(0.0F);

            // vertical intent
            double xz = Math.sqrt(dx*dx + dz*dz);
            mob.setYya(dy > 0 ? 1.0F : -1.0F);
        } else {
            if (!this.hoversInPlace) this.mob.setNoGravity(false);
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
        }
    }
}