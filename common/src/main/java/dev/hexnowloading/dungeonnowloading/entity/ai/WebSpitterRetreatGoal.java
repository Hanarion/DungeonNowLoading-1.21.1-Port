package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.WebSpitterEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class WebSpitterRetreatGoal extends Goal {

    private final WebSpitterEntity mob;
    private final double baseMoveSpeed;
    private final float safeDistanceSq;

    private boolean retreatRequested = false;
    private int safeTicks = 0;

    private int retreatTicks = 0;
    private static final int MAX_RETREAT_TICKS = reducedTickDelay(100);
    private static final int REQUIRED_SAFE_TICKS = reducedTickDelay(40);


    public WebSpitterRetreatGoal(WebSpitterEntity mob,
                                 double baseMoveSpeed,
                                 float minAttackDistance) {
        this.mob = mob;
        this.baseMoveSpeed = baseMoveSpeed;
        this.safeDistanceSq = minAttackDistance * minAttackDistance;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // Called by the entity when hit at close range
    public void requestRetreat() {
        this.retreatRequested = true;
    }

    @Override
    public boolean canUse() {
        if (!retreatRequested) return false;

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            retreatRequested = false;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Force end if max duration hit
        if (retreatTicks >= MAX_RETREAT_TICKS) {
            return false;
        }

        double distSq = mob.distanceToSqr(target);
        if (distSq > safeDistanceSq) {
            return safeTicks < REQUIRED_SAFE_TICKS;
        } else {
            return true; // still too close, keep retreating
        }
    }


    @Override
    public void start() {
        safeTicks = 0;
        retreatTicks = 0;
        mob.setAggressive(false);
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        retreatRequested = false;
        safeTicks = 0;
        retreatTicks = 0;
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        retreatTicks++;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distSq = mob.distanceToSqr(target);
        if (distSq > safeDistanceSq) {
            safeTicks++;
        } else {
            safeTicks = 0;
        }

        moveAwayFrom(target, 6.0D, baseMoveSpeed * 1.3D);
    }


    private void moveAwayFrom(LivingEntity target, double distance, double speed) {
        double dx = mob.getX() - target.getX();
        double dz = mob.getZ() - target.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0E-4D) {
            return;
        }

        dx /= dist;
        dz /= dist;

        double destX = mob.getX() + dx * distance;
        double destZ = mob.getZ() + dz * distance;

        mob.getNavigation().moveTo(destX, mob.getY(), destZ, speed);
    }
}
