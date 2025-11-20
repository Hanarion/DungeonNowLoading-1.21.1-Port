package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WispAttackGoal extends Goal {
    private final WispEntity wisp;
    private LivingEntity target;

    // release config
    private static final double RELEASE_DISTANCE = 5.0;
    private static final double MIN_CRUISE_SPEED = 0.15;

    // release state
    private boolean released = false;
    private Vec3 cruiseDir = Vec3.ZERO;
    private double cruiseSpeed = 0.0;

    public WispAttackGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = wisp.getTarget();
        if (t == null || !t.isAlive()) return false;
        this.target = t;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // keep going while target is alive; release glide is handled inside tick()
        return (target != null && target.isAlive());
    }

    @Override
    public void start() {
        released = false;
        cruiseDir = Vec3.ZERO;
        cruiseSpeed = 0.0;
        wisp.setNoGravity(true);
    }

    @Override
    public void stop() {
        target = null;
        released = false;
        cruiseDir = Vec3.ZERO;
        cruiseSpeed = 0.0;
        // stop steering
        wisp.getMoveControl().setWantedPosition(wisp.getX(), wisp.getY(), wisp.getZ(), 0);
    }

    @Override
    public void tick() {
        if (target == null) return;

        if (released) {
            // fly straight with constant speed; no more homing
            if (cruiseSpeed > 0 && !cruiseDir.equals(Vec3.ZERO)) {
                wisp.setDeltaMovement(cruiseDir.scale(cruiseSpeed));
            }
            return;
        }

        // homing phase
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 to = targetPos.subtract(wisp.position());
        double dist = to.length();
        if (dist < 1.0e-6) return;

        // if close enough -> RELEASE: capture current travel vector & speed, then stop steering
        if (dist <= RELEASE_DISTANCE) {
            released = true;

            Vec3 v = wisp.getDeltaMovement();
            if (v.lengthSqr() < 1.0e-6) {
                // fallback to current direction toward target if we're nearly stationary
                v = to.normalize().scale(wisp.getAttributeValue(Attributes.FLYING_SPEED));
            }

            cruiseDir = v.normalize();
            // clamp cruise to something reasonable around the flying speed
            double base = wisp.getAttributeValue(Attributes.FLYING_SPEED);
            cruiseSpeed = Math.max(MIN_CRUISE_SPEED, Math.min(v.length(), base * 1.25));

            // stop giving MoveControl targets so it won’t re-steer
            wisp.getMoveControl().setWantedPosition(wisp.getX(), wisp.getY(), wisp.getZ(), 0);
            return;
        }

        // keep homing while far
        wisp.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 1.0);
    }
}
