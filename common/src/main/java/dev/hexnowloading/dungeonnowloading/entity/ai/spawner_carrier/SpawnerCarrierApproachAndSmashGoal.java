package dev.hexnowloading.dungeonnowloading.entity.ai.spawner_carrier;

import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SpawnerCarrierApproachAndSmashGoal extends Goal {
    private final SpawnerCarrierEntity mob;
    private final double speed;

    private static final double RANGE = 3.0;
    private static final double RANGE_SQR = RANGE * RANGE;

    private int cooldown = 0;

    public SpawnerCarrierApproachAndSmashGoal(SpawnerCarrierEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        var target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        var target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        var target = mob.getTarget();
        if (target != null) {
            mob.getNavigation().moveTo(target, speed);
        }
    }

    @Override
    public void tick() {
        var target = mob.getTarget();
        if (target == null) return;

        if (cooldown > 0) cooldown--;

        mob.getLookControl().setLookAt(target, 30, 30);

        if (mob.isLocomotionLocked() || mob.isBusyAttacking()) {
            mob.getNavigation().stop();
            return;
        }

        double d2 = mob.distanceToSqr(target);

        if (d2 <= RANGE_SQR && cooldown == 0) {
            mob.getNavigation().stop();
            mob.playGroundSmashFromGoal();
            cooldown = 60;
            return;
        }

        if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(target, speed);
        } else if (mob.tickCount % 20 == 0) {
            mob.getNavigation().moveTo(target, speed);
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }
}
