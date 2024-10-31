package dev.hexnowloading.dungeonnowloading.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class AllRangeMeleeAttackGoal extends MeleeAttackGoal {

    private final float rangeInBbPercentage;

    public AllRangeMeleeAttackGoal(PathfinderMob $$0, double $$1, boolean $$2, float rangeInBbPercentage) {
        super($$0, $$1, $$2);
        this.rangeInBbPercentage = rangeInBbPercentage;
    }

    @Override
    protected double getAttackReachSqr(LivingEntity livingEntity) {
        return (double)(this.mob.getBbWidth() * this.rangeInBbPercentage * this.mob.getBbWidth() * this.rangeInBbPercentage + livingEntity.getBbWidth());
    }
}
