package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class FairkeeperOurosLaserGoal extends Goal {

    private final FairkeeperOurosEntity ouros;
    private final FairkeeperOurosEntity.FairkeeperOurosState state;

    public FairkeeperOurosLaserGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros) {
        this.ouros = ouros;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        return this.ouros.getTarget() != null && this.ouros.getTarget().isAlive() && this.ouros.isState(state);
    }


}
