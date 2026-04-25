package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class ReaperSpiderRecoveryGoal extends Goal {
    private static final int RECOVERY_TICKS = reducedTickDelay(100);
    private static final double REENTER_RANGE = 5.0D;
    private static final double STALKING_EXIT_RANGE = 15.0D;

    private final ReaperSpiderEntity mob;
    private int recoveryTicksRemaining;

    public ReaperSpiderRecoveryGoal(ReaperSpiderEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.mob.getBehaviorState() == ReaperSpiderEntity.BehaviorState.RECOVERY;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getBehaviorState() == ReaperSpiderEntity.BehaviorState.RECOVERY && this.recoveryTicksRemaining > 0;
    }

    @Override
    public void start() {
        this.recoveryTicksRemaining = RECOVERY_TICKS;
        this.mob.setLocomotionLocked(true);
        this.mob.setLockedBackpedaling(true);
        this.mob.getNavigation().stop();
        this.mob.setRevealed(true);
        this.mob.setInvisible(false);
        this.mob.setBackingUp(true);
    }

    @Override
    public void stop() {
        this.mob.setLockedBackpedaling(false);
        this.mob.setLocomotionLocked(false);
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
        this.mob.setSpeed(0.0F);
        this.mob.setZza(0.0F);
        this.mob.setXxa(0.0F);
        this.mob.setBackingUp(false);
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        this.mob.getNavigation().stop();
        this.mob.setRevealed(true);
        this.mob.setInvisible(false);

        if (target != null && target.isAlive()) {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.faceTargetBody(target);
            this.mob.setBackingUp(true);

            if (target instanceof Player player && !player.getAbilities().instabuild
                    && this.mob.distanceToSqr(target) > STALKING_EXIT_RANGE * STALKING_EXIT_RANGE) {
                this.enterStalking();
                return;
            }
        } else {
            this.mob.setLockedBackpedaling(false);
            this.mob.setBackingUp(false);
            this.mob.setSpeed(0.0F);
            this.mob.setZza(0.0F);
            this.mob.setXxa(0.0F);
        }

        if (--this.recoveryTicksRemaining > 0) {
            return;
        }

        if (target instanceof Player player && player.isAlive() && !player.getAbilities().instabuild) {
            if (this.mob.distanceToSqr(target) <= REENTER_RANGE * REENTER_RANGE) {
                this.mob.setBehaviorState(ReaperSpiderEntity.BehaviorState.CHASING);
                this.mob.setRevealed(true);
                this.mob.setInvisible(false);
                this.mob.queueRevealTackle();
            } else {
                this.mob.setBehaviorState(ReaperSpiderEntity.BehaviorState.STALKING);
                this.mob.setRevealed(false);
                this.mob.setInvisible(true);
            }
        } else {
            this.mob.setBehaviorState(ReaperSpiderEntity.BehaviorState.WANDER);
            this.mob.setRevealed(true);
            this.mob.setInvisible(false);
        }
    }

    private void enterStalking() {
        this.mob.setLockedBackpedaling(false);
        this.mob.setLocomotionLocked(false);
        this.mob.setBackingUp(false);
        this.mob.getNavigation().stop();
        this.mob.setBehaviorState(ReaperSpiderEntity.BehaviorState.STALKING);
        this.mob.setRevealed(false);
        this.mob.setInvisible(true);
    }

    private void faceTargetBody(LivingEntity target) {
        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0F / Math.PI)) - 90.0F;
        float newYaw = Mth.approachDegrees(this.mob.getYRot(), targetYaw, 30.0F);

        this.mob.setYRot(newYaw);
        this.mob.setYHeadRot(newYaw);
        this.mob.yBodyRot = newYaw;
        this.mob.yBodyRotO = newYaw;
    }
}
