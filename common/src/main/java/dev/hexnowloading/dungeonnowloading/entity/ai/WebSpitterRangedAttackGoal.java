package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.WebSpitterEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
public class WebSpitterRangedAttackGoal extends Goal {

    private enum CombatState {
        CHASE,
        SHOOT,
        BACK_UP
    }

    private final WebSpitterEntity mob;
    private final double moveSpeed;

    private final int attackIntervalTicks;
    private final int windupDurationTicks;

    private final float minAttackDistanceSq;
    private final float maxAttackDistanceSq;

    private int attackCooldown;
    private int windupTicks;
    private int seeTime;

    private CombatState state = CombatState.CHASE;

    public WebSpitterRangedAttackGoal(WebSpitterEntity mob,
                                      double moveSpeed,
                                      int attackInterval,
                                      float minAttackDistance,
                                      float maxAttackDistance) {
        this.mob = mob;
        this.moveSpeed = moveSpeed;

        int baseWindupDuration = 20; // 1 second

        this.attackIntervalTicks = reducedTickDelay(attackInterval);
        this.windupDurationTicks = reducedTickDelay(baseWindupDuration);

        this.minAttackDistanceSq = minAttackDistance * minAttackDistance;
        this.maxAttackDistanceSq = maxAttackDistance * maxAttackDistance;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public float getMinAttackDistanceSq() {
        return minAttackDistanceSq;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return !mob.getNavigation().isDone()
                || mob.distanceToSqr(target) <= maxAttackDistanceSq * 1.5F;
    }

    @Override
    public void start() {
        attackCooldown = 0;
        windupTicks = 0;
        seeTime = 0;
        state = CombatState.CHASE;
        mob.setAggressive(false);
    }

    @Override
    public void stop() {
        attackCooldown = 0;
        windupTicks = 0;
        seeTime = 0;
        mob.setAggressive(false);
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        double distSq = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSee = mob.getSensing().hasLineOfSight(target);

        // LOS timer
        if (canSee) {
            seeTime = Math.min(seeTime + 1, 60);
        } else {
            seeTime = Math.max(seeTime - 1, -60);
        }

        updateState(distSq, canSee);

        switch (state) {
            case CHASE -> tickChase(target);
            case SHOOT -> tickShoot(target);
            case BACK_UP -> tickBackUp(target);
        }
    }

    private void updateState(double distSq, boolean canSee) {
        CombatState prev = state;

        if (!canSee) {
            state = CombatState.CHASE;
        } else if (distSq > maxAttackDistanceSq) {
            state = CombatState.CHASE;
        } else if (distSq >= minAttackDistanceSq) {
            state = CombatState.SHOOT;
        } else {
            state = CombatState.BACK_UP;
        }

        // CHASE -> SHOOT transition: cancel active pathing immediately
        if (prev != state && state == CombatState.SHOOT) {
            mob.getNavigation().moveTo(mob, 0.0D); // “navigate to self”
        }
    }


    // --- shared shooting logic ---

    private void handleRangedAttack(LivingEntity target) {
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (seeTime <= 0) {
            windupTicks = 0;
            mob.setAggressive(false);
            return;
        }

        if (windupTicks < windupDurationTicks) {
            windupTicks++;
            mob.setAggressive(true);
            return;
        }

        mob.performRangedAttack(target, 1.0F);
        mob.setAggressive(false);

        windupTicks = 0;
        attackCooldown = attackIntervalTicks;
    }

    // --- state behaviours ---

    // CHASING: move in normal moveSpeed towards the target
    private void tickChase(LivingEntity target) {
        mob.getNavigation().moveTo(target, moveSpeed);
        mob.setAggressive(false);
    }

    private void tickShoot(LivingEntity target) {
        mob.getNavigation().stop();

        // rotate body + head toward player
        faceTargetBody(target);

        // "Strafe around target": sideways magnitude 0.5, move control picks left/right
        mob.getMoveControl().strafe(0.0F, 0.5F);

        handleRangedAttack(target);
    }

    private void tickBackUp(LivingEntity target) {
        mob.getNavigation().stop();

        // rotate body + head toward player
        faceTargetBody(target);

        // Pure backpedal: move control treats this as backward, no sideways component
        mob.getMoveControl().strafe(-0.5F, 0.0F);

        handleRangedAttack(target);
    }



    private void faceTargetBody(LivingEntity target) {
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();

        // angle from mob -> target
        float targetYaw = (float)(Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;

        float currentYaw = mob.getYRot();
        float maxTurnPerTick = 20.0F; // how fast the body can turn (deg per tick)

        float newYaw = Mth.approachDegrees(currentYaw, targetYaw, maxTurnPerTick);

        mob.setYRot(newYaw);       // main yaw
        mob.setYHeadRot(newYaw);   // head yaw
        mob.yBodyRot = newYaw;     // body render yaw
        mob.yBodyRotO = newYaw;    // previous body yaw (helps avoid jitter)
    }
}

