package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class ReaperSpiderStalkGoal extends Goal {
    private final ReaperSpiderEntity mob;
    private LivingEntity target;

    public ReaperSpiderStalkGoal(ReaperSpiderEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (!(livingEntity instanceof Player player) || !player.isAlive() || player.getAbilities().instabuild) {
            return false;
        }

        this.target = livingEntity;
        return this.mob.getBehaviorState() == ReaperSpiderEntity.BehaviorState.STALKING;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target instanceof Player player
                && player.isAlive()
                && !player.getAbilities().instabuild
                && this.mob.getBehaviorState() == ReaperSpiderEntity.BehaviorState.STALKING;
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        this.mob.getNavigation().moveTo(this.target, this.mob.getChaseSpeedMultiplier());
    }
}
