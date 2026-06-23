package dev.hexnowloading.dungeonnowloading.entity.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.AABB;

public class AllRangeMeleeAttackGoal extends MeleeAttackGoal {

    private final float rangeInBbPercentage;

    public AllRangeMeleeAttackGoal(PathfinderMob $$0, double $$1, boolean $$2, float rangeInBbPercentage) {
        super($$0, $$1, $$2);
        this.rangeInBbPercentage = rangeInBbPercentage;
    }

    /**
     * 1.21 removed MeleeAttackGoal.getAttackReachSqr; melee reach is now an AABB
     * ({@code Mob#getAttackBoundingBox}). Preserve the old widened reach by checking an
     * inflated attack box here instead.
     */
    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.isTimeToAttack() && this.mob.getSensing().hasLineOfSight(target) && isWithinWidenedRange(target)) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
        }
    }

    private boolean isWithinWidenedRange(LivingEntity target) {
        float reach = this.mob.getBbWidth() * this.rangeInBbPercentage;
        AABB attackBox = this.mob.getBoundingBox().inflate(reach, 0.0D, reach);
        return attackBox.intersects(target.getBoundingBox());
    }
}
