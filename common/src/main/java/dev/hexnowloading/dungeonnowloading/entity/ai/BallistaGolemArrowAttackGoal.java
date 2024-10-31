package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BallistaArrowEntity;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BallistaGolemArrowAttackGoal extends Goal {

    private final BallistaGolemEntity ballistaGolemEntity;
    private int nextScanTick;
    private int attackTicks;

    public BallistaGolemArrowAttackGoal(BallistaGolemEntity ballistaGolemEntity) {
        this.ballistaGolemEntity = ballistaGolemEntity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private int nextStartTick() { return reducedTickDelay(10); }

    private int nextCooldownStartTick() { return reducedTickDelay(60 + ballistaGolemEntity.getRandom().nextInt(40)); }

    @Override
    public boolean canUse() {
        if (nextScanTick > 0) {
            --this.nextScanTick;
            return false;
        } else {
            double DETECTION_RANGE = this.ballistaGolemEntity.getFollowDistance();
            boolean hasTargetInRange = this.ballistaGolemEntity.getTarget() != null && this.ballistaGolemEntity.getTarget().distanceTo(this.ballistaGolemEntity) < DETECTION_RANGE;
            this.nextScanTick = this.nextStartTick();
            return this.ballistaGolemEntity.isState(BallistaGolemEntity.BallistaGolemState.IDLE) && hasTargetInRange && this.ballistaGolemEntity.getBallistaArrowCount() > 0;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.ballistaGolemEntity.isState(BallistaGolemEntity.BallistaGolemState.SHOOT);
    }

    @Override
    public void start() {
        this.ballistaGolemEntity.setState(BallistaGolemEntity.BallistaGolemState.SHOOT);
        this.ballistaGolemEntity.triggerShootAnimation();
        this.attackTicks = reducedTickDelay(69);
    }

    @Override
    public void tick() {
        if (this.attackTicks > 0) {
            this.attackTicks--;
        } else {
            this.ballistaGolemEntity.setState(BallistaGolemEntity.BallistaGolemState.IDLE);
            this.ballistaGolemEntity.triggerIdleAnimation();
            this.nextScanTick = this.nextCooldownStartTick();
            return;
        }
        if (this.attackTicks == reducedTickDelay(69 - 25)) {
            double viewDistance = 1.5F;
            Vec3 viewVector = this.ballistaGolemEntity.getViewVector(1.0F);
            double d0 = viewVector.x * viewDistance;
            double d1 = viewVector.y * viewDistance;
            double d2 = viewVector.z * viewDistance;
            BallistaArrowEntity projectile = new BallistaArrowEntity(this.ballistaGolemEntity, this.ballistaGolemEntity.level());
            projectile.setPos(this.ballistaGolemEntity.getX() + d0, this.ballistaGolemEntity.getEyeY() - 0.28 + d1, this.ballistaGolemEntity.getZ() + d2);
            projectile.shootFromRotation(this.ballistaGolemEntity, this.ballistaGolemEntity.getXRot(), this.ballistaGolemEntity.getYRot(), 0.0F, 2.0F, 0.1F);
            this.ballistaGolemEntity.level().addFreshEntity(projectile);
            this.ballistaGolemEntity.setBallistaArrowCount(this.ballistaGolemEntity.getBallistaArrowCount() - 1);
        }
    }
}
