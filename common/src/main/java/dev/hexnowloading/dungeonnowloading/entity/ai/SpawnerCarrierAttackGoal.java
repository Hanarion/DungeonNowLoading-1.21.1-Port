package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.AABB;

public class SpawnerCarrierAttackGoal extends MeleeAttackGoal {
    private final SpawnerCarrierEntity spawnerCarrierEntity;

    public SpawnerCarrierAttackGoal(SpawnerCarrierEntity spawnerCarrierEntity, float speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(spawnerCarrierEntity, speedModifier, followingTargetEvenIfNotSeen);
        this.spawnerCarrierEntity = spawnerCarrierEntity;
    }

    /** 1.21 removed getAttackReachSqr; preserve the widened reach via an inflated attack box. */
    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.isTimeToAttack() && this.mob.getSensing().hasLineOfSight(target) && isWithinWidenedRange(target)) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
        }
    }

    private boolean isWithinWidenedRange(LivingEntity target) {
        float reach = (spawnerCarrierEntity.getBbWidth() - 0.1F) * 1.3F;
        AABB attackBox = this.mob.getBoundingBox().inflate(reach, 0.0D, reach);
        return attackBox.intersects(target.getBoundingBox());
    }
}
