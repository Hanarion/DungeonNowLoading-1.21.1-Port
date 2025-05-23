package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.FlameProjectileEntity;
import net.minecraft.world.phys.Vec3;

public class FairkeeperBorosFlameThrowerGoal extends StoppableGoal {

    private final FairkeeperBorosEntity boros;
    private final FairkeeperBorosEntity.FairkeeperBorosState state;
    private final int startUpDelay;
    private int startUpDelayTick;
    private int durationTick;

    private static final int DURATION = reducedTickDelay(200);

    public FairkeeperBorosFlameThrowerGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, int startUpDelay) {
        this.boros = boros;
        this.state = state;
        this.startUpDelay = reducedTickDelay(startUpDelay);
    }

    @Override
    public boolean canUse() {
        return this.boros.getTarget() != null && this.boros.getTarget().isAlive() && this.boros.isState(state);
    }

    @Override
    public void start() {
        this.startUpDelayTick = startUpDelay;
        this.durationTick = DURATION;
        this.boros.playFlameShootingSound();
    }

    @Override
    public void tick() {
        if (this.startUpDelayTick > 0) {
            this.startUpDelayTick--;
            return;
        }

        if (this.durationTick > 0) {
            this.durationTick--;
            this.shootFlame(90);
            this.shootFlame(-90);
        }

    }

    private void shootFlame(float angle) {
        double viewDistance = 2.0F;
        Vec3 viewVector = this.boros.getViewVector(1.0F);
        double dx = viewVector.x;
        double dz = viewVector.z;
        double angleRadians = Math.toRadians(angle);
        double rx = dx * Math.cos(angleRadians) - dz * Math.sin(angleRadians);
        double rz = dx * Math.sin(angleRadians) + dz * Math.cos(angleRadians);
        FlameProjectileEntity flame = new FlameProjectileEntity(this.boros, this.boros.level());
        flame.setOwner(this.boros);
        flame.setPos(this.boros.getX() + rx * viewDistance, this.boros.getY() + this.boros.getBoundingBox().getYsize() / 2, this.boros.getZ() + rz * viewDistance);
        flame.shootFromRotation(this.boros, this.boros.getXRot(), this.boros.getYRot() + (float) Math.toDegrees(angleRadians), 0.0F, 0.3F, 1.0F);
        this.boros.level().addFreshEntity(flame);
    }
}
