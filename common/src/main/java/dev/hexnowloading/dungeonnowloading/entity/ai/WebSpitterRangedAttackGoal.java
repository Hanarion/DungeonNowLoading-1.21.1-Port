package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.WebSpitterEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WebSpitterRangedAttackGoal extends Goal {

    private final WebSpitterEntity mob;
    private final double speedModifier;
    private final int attackInterval;
    private final float attackRadius;
    private final float attackRadiusSqr;

    private int attackTime = -1;
    private int seeTime;

    public WebSpitterRangedAttackGoal(WebSpitterEntity mob,
                                      double speedModifier,
                                      int attackInterval,
                                      float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackInterval = attackInterval;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone());
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double distSq = this.mob.distanceToSqr(target);
        boolean canSee = this.mob.getSensing().hasLineOfSight(target);

        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }

        // Look at target
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // --- Anchored behaviour ---
        if (this.mob.isAnchored()) {
            // Only break anchor if player gets close enough
            if (this.mob.shouldBreakAnchorTo(target)) {
                this.mob.setAnchored(false);
            } else {
                // Stay put, no chasing / kiting
                this.mob.getNavigation().stop();
            }
        } else {
            // --- Range management ---
            // If inside 7 blocks, back up while firing
            if (distSq < 49.0D) { // 7 * 7
                Vec3 away = new Vec3(
                        this.mob.getX() - target.getX(),
                        0.0D,
                        this.mob.getZ() - target.getZ()
                ).normalize().scale(1.0D);

                this.mob.getNavigation().moveTo(
                        this.mob.getX() + away.x,
                        this.mob.getY(),
                        this.mob.getZ() + away.z,
                        this.speedModifier
                );
            }
            // If too far (but within "engage" radius), close in a bit
            else if (distSq > this.attackRadiusSqr * 0.75F && distSq <= this.attackRadiusSqr * 1.5F) {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
            } else {
                // In the sweet spot – stay roughly where we are
                this.mob.getNavigation().stop();
            }
        }

        // --- Firing logic (16-block range + LOS) ---
        if (this.attackTime > 0) {
            this.attackTime--;
        }

        if (canSee && distSq <= this.attackRadiusSqr) {
            if (this.attackTime <= 0) {
                float dist = (float) Math.sqrt(distSq);
                float distanceFactor = dist / this.attackRadius; // 0.0 – 1.0
                this.mob.performRangedAttack(target, distanceFactor);
                this.attackTime = this.attackInterval;
            }
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
