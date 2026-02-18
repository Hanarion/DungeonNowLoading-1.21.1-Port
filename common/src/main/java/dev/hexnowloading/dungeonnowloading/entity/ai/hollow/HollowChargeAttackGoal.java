package dev.hexnowloading.dungeonnowloading.entity.ai.hollow;

import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class HollowChargeAttackGoal extends Goal {

    private final HollowEntity hollowEntity;

    public HollowChargeAttackGoal(HollowEntity hollowEntity) {
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.hollowEntity = hollowEntity;
    }

    @Override
    public boolean canUse() {
        if (!hollowEntity.canStartCharge()) return false;

        LivingEntity target = hollowEntity.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (hollowEntity.getMoveControl().hasWanted()) return false;
        if (hollowEntity.getRandom().nextInt(reducedTickDelay(7)) != 0) return false;

        return hollowEntity.distanceToSqr(target) > 4.0;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = hollowEntity.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (!hollowEntity.IsCharging()) return false;

        return true;
    }

    @Override
    public void start() {
        LivingEntity target = hollowEntity.getTarget();
        if (target != null) {
            Vec3 eye = target.getEyePosition();
            hollowEntity.getMoveControl().setWantedPosition(eye.x, eye.y, eye.z, 1.0);
        }
        hollowEntity.setCharging(true);
    }

    @Override
    public void stop() {
        // If we were charging and got stopped for any reason, enter cooldown
        if (hollowEntity.IsCharging()) {
            hollowEntity.startChargeCooldownDefault();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = hollowEntity.getTarget();
        if (target == null) {
            hollowEntity.startChargeCooldownDefault();
            return;
        }

        if (hollowEntity.getBoundingBox().intersects(target.getBoundingBox())) {
            hollowEntity.doHurtTarget(target);
            hollowEntity.startChargeCooldown(HollowEntity.CHARGE_HIT_COOLDOWN_TICKS); // 2s
            return;
        }


        // keep steering while charging
        Vec3 eye = target.getEyePosition();
        hollowEntity.getMoveControl().setWantedPosition(eye.x, eye.y, eye.z, 1.0);
    }
}
