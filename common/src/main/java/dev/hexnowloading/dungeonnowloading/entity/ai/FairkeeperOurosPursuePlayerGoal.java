package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperOurosPursuePlayerGoal extends Goal {
    private final FairkeeperOurosEntity ouros;
    private final FairkeeperOurosEntity.FairkeeperOurosState state;
    private final double speed;
    private LivingEntity target;
    private Vec3 targetPosition;

    public FairkeeperOurosPursuePlayerGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros, double speed) {
        this.state = state;
        this.ouros = ouros;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = this.ouros.getTarget();
        return this.target != null && this.target.isAlive() && this.ouros.isState(state);
    }

    @Override
    public void start() {
        this.targetPosition = this.target.getPosition(1.0F);
    }

    @Override
    public void tick() {
        targetPosition = this.target.getPosition(1.0f);
        this.ouros.getMoveControl().setWantedPosition(this.targetPosition.x, this.ouros.getBoundingBox().maxY, this.targetPosition.z, this.speed);
    }
}
