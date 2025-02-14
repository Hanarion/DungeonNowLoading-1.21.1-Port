package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;

public class FairkeeperBorosShootPoisonArrowGoal extends Goal {

    private final FairkeeperBorosEntity boros;
    private FairkeeperBorosEntity.FairkeeperBorosState state;
    private int attackTicks;
    private FairkeeperBorosPartEntity currentPart;
    private int loopCount;

    private final int SHOOTING_INTERVAL = 10;
    private final int START_UP_DELAY = 40;
    private final int TOTAL_LOOP = 3;

    public FairkeeperBorosShootPoisonArrowGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros) {
        this.boros = boros;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        return this.boros.getTarget() != null && this.boros.getTarget().isAlive() && this.boros.isState(state);
    }

    @Override
    public void start() {
        this.attackTicks = reducedTickDelay(SHOOTING_INTERVAL + START_UP_DELAY);
        this.currentPart = (FairkeeperBorosPartEntity) this.boros.getChild();
        this.loopCount = 0;
    }

    @Override
    public void tick() {

        if (this.currentPart == null || this.currentPart.isTail()) {
            if (loopCount == TOTAL_LOOP) {
                this.boros.stopAttacking(20);
            } else {
                loopCount++;
                this.currentPart = (FairkeeperBorosPartEntity) this.boros.getChild();
            }
            return;
        }

        if (this.attackTicks > 0) {
            this.attackTicks--;
            return;
        }
        this.attackTicks = reducedTickDelay(SHOOTING_INTERVAL);

        while (this.currentPart.hasArmor()) {
            this.currentPart = (FairkeeperBorosPartEntity) this.currentPart.getChild();
            if (this.currentPart == null) {
                this.boros.stopAttacking(20);
                return;
            }
        }

        this.shootArrow(90.0F);
        this.shootArrow(-90.0F);

        this.currentPart = (FairkeeperBorosPartEntity) this.currentPart.getChild();

    }

    private void shootArrow(float angle) {
        double viewDistance = 2.0F;
        Vec3 viewVector = this.currentPart.getViewVector(1.0F);
        double dx = viewVector.x;
        double dz = viewVector.z;
        double angleRadians = Math.toRadians(angle);
        double rx = dx * Math.cos(angleRadians) - dz * Math.sin(angleRadians);
        double rz = dx * Math.sin(angleRadians) + dz * Math.cos(angleRadians);
        Arrow arrow = new Arrow(this.currentPart.level(), this.currentPart);
        arrow.addEffect(new MobEffectInstance(MobEffects.POISON, 600, 2));
        arrow.setPos(this.currentPart.getX() + rx * viewDistance, this.currentPart.getY() + this.currentPart.getBoundingBox().getYsize() / 2, this.currentPart.getZ() + rz * viewDistance);
        arrow.shootFromRotation(this.currentPart, this.currentPart.getXRot(), this.currentPart.getYRot() + (float) Math.toDegrees(angleRadians), 0.0F, 2.0F, 1.0F);
        arrow.setBaseDamage(this.boros.getAttackDamage() * 0.3F);
        this.currentPart.level().addFreshEntity(arrow);
    }
}
