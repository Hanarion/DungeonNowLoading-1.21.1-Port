package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WhimperChargeAttackGoal extends Goal {
    private final WhimperEntity whimper;
    private boolean hasHitThisCharge;

    public WhimperChargeAttackGoal(WhimperEntity whimper) {
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.whimper = whimper;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.whimper.getTarget();
        if (target == null || !target.isAlive() || this.whimper.getMoveControl().hasWanted()) {
            return false;
        }

        int baseDelay = 5;
        int level = this.whimper.getOverworkedLevel();
        if (level > 0) {
            float factor = 1.0F - 0.2F * level; // 20% reduction per level
            if (factor < 0.2F) factor = 0.2F;   // cap at 80% reduction
            baseDelay = Math.max(1, (int)(baseDelay * factor));
        }

        if (this.whimper.getRandom().nextInt(reducedTickDelay(baseDelay)) != 0) {
            return false;
        }

        return this.whimper.distanceToSqr(target) > 3.0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.whimper.getMoveControl().hasWanted()
                && this.whimper.getTarget() != null
                && this.whimper.getTarget().isAlive();
    }

    @Override
    public void start() {
        LivingEntity target = this.whimper.getTarget();
        if (target != null) {
            Vec3 eyePosition = target.getEyePosition();
            // Charge quickly toward the target
            this.whimper.getMoveControl().setWantedPosition(eyePosition.x, eyePosition.y, eyePosition.z, 1.6);
        }

        this.hasHitThisCharge = false;
        this.whimper.setCharging(true);
    }

    @Override
    public void stop() {
        this.whimper.setCharging(false);
        this.hasHitThisCharge = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.whimper.getTarget();
        if (target == null || !target.isAlive()) {
            this.whimper.setCharging(false);
            return;
        }


        if (!this.hasHitThisCharge && this.whimper.getBoundingBox().intersects(target.getBoundingBox())) {
            this.whimper.doHurtTarget(target);
            this.hasHitThisCharge = true;

        }

        double d2 = this.whimper.distanceToSqr(target);

        if (d2 < 16.0) {
            Vec3 eye = target.getEyePosition();
            this.whimper.getMoveControl().setWantedPosition(eye.x, eye.y, eye.z, 1.6);
        }
    }
}
