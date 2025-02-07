package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperBorosPursuePlayerGoal extends Goal {

    private final FairkeeperBorosEntity boros;
    private final FairkeeperBorosEntity.FairkeeperBorosState state;
    private final double speed;
    private LivingEntity target;
    private Vec3 targetPosition;

    public FairkeeperBorosPursuePlayerGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, double speed, double tackleRange, double addSpeedBy) {
        this.state = state;
        this.boros = boros;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public FairkeeperBorosPursuePlayerGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, double speed) {
        this(state, boros, speed, 0.0F, 0.0F);
    }

    @Override
    public boolean canUse() {
        this.target = this.boros.getTarget();
        return this.target != null && this.target.isAlive() && this.boros.isState(state);
    }

    @Override
    public void start() {
        this.targetPosition = this.target.getPosition(1.0F);
    }

    @Override
    public void tick() {
        targetPosition = this.target.getPosition(1.0f);
        this.boros.getMoveControl().setWantedPosition(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, this.speed);
    }
}
