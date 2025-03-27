package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class BallistaGolemMeleeAttackGoal extends Goal {
    private final float rangeInBbPercentage;
    protected final PathfinderMob mob;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;

    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private long lastCanUseCheck;
    private Path path;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;


    public BallistaGolemMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen, float rangeInBbPercentage) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.rangeInBbPercentage = rangeInBbPercentage;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < 20L) return false;
        this.lastCanUseCheck = gameTime;

        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        this.path = this.mob.getNavigation().createPath(target, 0);
        if (this.path != null) return true;

        return this.getAttackReachSqr(target) >= this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (!this.followingTargetEvenIfNotSeen) return !this.mob.getNavigation().isDone();

        if (!this.mob.isWithinRestriction(target.blockPosition())) return false;

        return target instanceof Player player && !player.isSpectator() && !player.isCreative();
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    public void stop() {
        LivingEntity $$0 = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test($$0)) {
            this.mob.setTarget((LivingEntity)null);
        }

        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return;

        double distanceSq = mob.distanceToSqr(target);
        double attackRangeSq = getAttackReachSqr(target);

        float distanceMultiplier = distanceSq > BallistaGolemEntity.BALLISTA_GOLEM_MELEE_RANGE ? 0.7f : 1.0f;
        double moveSpeed = speedModifier * distanceMultiplier;

        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(target)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0 || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0 || this.mob.getRandom().nextFloat() < 0.05F)) {
            this.pathedTargetX = target.getX();
            this.pathedTargetY = target.getY();
            this.pathedTargetZ = target.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            if (distanceSq > 1024.0) {
                this.ticksUntilNextPathRecalculation += 10;
            } else if (distanceSq > 256.0) {
                this.ticksUntilNextPathRecalculation += 5;
            }

            if (!this.mob.getNavigation().moveTo(target, moveSpeed)) {
                this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
        }

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);

        if (distanceSq <= attackRangeSq && --this.ticksUntilNextAttack <= 0) {
            this.ticksUntilNextAttack = this.adjustedTickDelay(20);
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
            if (target instanceof Player player && player.isBlocking()) {
                player.disableShield(true);
            }
        }
    }

    private double getAttackReachSqr(LivingEntity livingEntity) {
        return (double)(this.mob.getBbWidth() * this.rangeInBbPercentage * this.mob.getBbWidth() * this.rangeInBbPercentage + livingEntity.getBbWidth());
    }
}
