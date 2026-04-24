package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ReaperSpiderRecoveryGoal extends Goal {
    private static final int RECOVERY_TICKS = reducedTickDelay(100);
    private static final double REENTER_RANGE = 5.0D;

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
        this.mob.getNavigation().stop();
        this.mob.setRevealed(true);
        this.mob.setInvisible(false);
        this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
    }

    @Override
    public void stop() {
        this.mob.setLocomotionLocked(false);
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        this.mob.getNavigation().stop();
        this.mob.setRevealed(true);
        this.mob.setInvisible(false);
        this.mob.setBackingUp(false);

        if (target != null && target.isAlive()) {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
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
}
