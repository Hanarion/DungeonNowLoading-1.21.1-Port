package dev.hexnowloading.dungeonnowloading.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;

public abstract class StoppableGoal extends Goal {

    private boolean forceStop;

    @Override
    public boolean canContinueToUse() {
        if (this.forceStop) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.forceStop = false;
    }

    protected void stopGoal() {
        this.forceStop = true;
    }

    protected boolean isForceStopped() {
        return this.forceStop;
    }
}
