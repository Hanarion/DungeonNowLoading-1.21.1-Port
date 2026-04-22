package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.HoveringFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class WispLanternWanderGoal extends Goal {
    private static final int MIN_REPATH_TICKS = 30;
    private static final int MAX_REPATH_TICKS = 70;

    private final WispLanternEntity lantern;
    private int repathCooldown;

    public WispLanternWanderGoal(WispLanternEntity lantern) {
        this.lantern = lantern;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.lantern.isAlive() && this.lantern.getTarget() == null && !this.lantern.isSummoningWisp();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
    }

    @Override
    public void stop() {
        this.repathCooldown = 0;
        this.lantern.setDeltaMovement(this.lantern.getDeltaMovement().scale(0.85D));
    }

    @Override
    public void tick() {
        if (!(this.lantern.getMoveControl() instanceof HoveringFlyingMoveControl moveControl)) {
            return;
        }

        if (this.repathCooldown-- <= 0) {
            moveControl.setWantedPosition();
            this.repathCooldown = MIN_REPATH_TICKS + this.lantern.getRandom().nextInt(MAX_REPATH_TICKS - MIN_REPATH_TICKS + 1);
        }
    }
}
