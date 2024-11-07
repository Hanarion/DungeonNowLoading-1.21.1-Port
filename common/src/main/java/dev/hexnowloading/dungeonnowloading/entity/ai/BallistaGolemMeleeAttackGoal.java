package dev.hexnowloading.dungeonnowloading.entity.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;

public class BallistaGolemMeleeAttackGoal extends MeleeAttackGoal {
    private final float rangeInBbPercentage;
    private final float MAX_DISTANCE = 10.0F;

    public BallistaGolemMeleeAttackGoal(PathfinderMob pathfinderMob, double $$1, boolean $$2, float rangeInBbPercentage) {
        super(pathfinderMob, $$1, $$2);
        this.rangeInBbPercentage = rangeInBbPercentage;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.mob.getTarget().distanceTo(this.mob) < MAX_DISTANCE;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity livingEntity, double distanceToTarget) {
        double attackReachSqr = this.getAttackReachSqr(livingEntity);
        if (distanceToTarget <= attackReachSqr && this.getTicksUntilNextAttack() <= 0) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(livingEntity);
            if (livingEntity instanceof Player player && player.isBlocking()) {
                player.disableShield(true);
            }
        }
    }

    @Override
    protected double getAttackReachSqr(LivingEntity livingEntity) {
        return (double)(this.mob.getBbWidth() * this.rangeInBbPercentage * this.mob.getBbWidth() * this.rangeInBbPercentage + livingEntity.getBbWidth());
    }
}
