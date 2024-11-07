package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BallistaArrowEntity;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BallistaGolemArrowAttackGoal extends Goal {

    private final BallistaGolemEntity ballistaGolemEntity;
    private int nextScanTick;
    private int attackTicks;
    private static final float FACING_THRESHOLD = 15.0F;

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
            double MIN_DETECTION_RANGE = 10;
            boolean hasTargetInRange = this.ballistaGolemEntity.getTarget() != null && this.ballistaGolemEntity.getTarget().distanceTo(this.ballistaGolemEntity) < DETECTION_RANGE && this.ballistaGolemEntity.getTarget().distanceTo(this.ballistaGolemEntity) > MIN_DETECTION_RANGE;
            boolean isFacingTarget = this.ballistaGolemEntity.getTarget() != null && isFacingTarget(this.ballistaGolemEntity.getTarget());
            this.nextScanTick = this.nextStartTick();
            return this.ballistaGolemEntity.isState(BallistaGolemEntity.BallistaGolemState.IDLE)
                    && hasTargetInRange && this.ballistaGolemEntity.getBallistaArrowCount() > 0
                    && this.ballistaGolemEntity.getTarget().hasLineOfSight(this.ballistaGolemEntity)
                    && isFacingTarget;
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


    private boolean isFacingTarget(LivingEntity target) {
        if (target == null) return false;

        // Calculate the angle between the golem's facing and the target
        double deltaX = target.getX() - this.ballistaGolemEntity.getX();
        double deltaZ = target.getZ() - this.ballistaGolemEntity.getZ();
        float targetYaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);

        // Calculate the difference in rotation
        float angleDifference = Math.abs(Mth.wrapDegrees(this.ballistaGolemEntity.getYRot() - targetYaw));

        // Return true if within the threshold
        return angleDifference < FACING_THRESHOLD;
    }
}
