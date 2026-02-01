package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Arrays;
import java.util.EnumSet;

public class ChaosSpawnerLookAtTargetGoal extends Goal {

    private final ChaosSpawnerEntity chaosSpawnerEntity;
    private LivingEntity lookAt;
    private final boolean onlyHorizontal;

    // states where you want to actively aim at target
    private final ChaosSpawnerEntity.State[] lookOn = {
            ChaosSpawnerEntity.State.IDLE,
            ChaosSpawnerEntity.State.SHOOT_GHOST_BULLET_SINGLE
            // add more if you want (BURST, SUMMON, etc)
    };

    public ChaosSpawnerLookAtTargetGoal(ChaosSpawnerEntity chaosSpawnerEntity, boolean onlyHorizontal) {
        this.chaosSpawnerEntity = chaosSpawnerEntity;
        this.onlyHorizontal = onlyHorizontal;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Only run when we have a target
        LivingEntity target = chaosSpawnerEntity.getTarget();
        if (target == null || !target.isAlive()) return false;

        // Optional: only look during certain states
        if (!Arrays.stream(lookOn).anyMatch(state -> state == chaosSpawnerEntity.getState())) return false;

        this.lookAt = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Keep going as long as the target exists and is alive
        LivingEntity target = chaosSpawnerEntity.getTarget();
        if (target == null || !target.isAlive()) return false;

        // Optional: keep respecting your state gate
        if (!Arrays.stream(lookOn).anyMatch(state -> state == chaosSpawnerEntity.getState())) return false;

        this.lookAt = target; // keep updated in case target swaps
        return true;
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if (lookAt == null) return;

        double y = onlyHorizontal ? chaosSpawnerEntity.getEyeY() : lookAt.getEyeY();
        chaosSpawnerEntity.getLookControl().setLookAt(lookAt.getX(), y, lookAt.getZ());

        // if you want body to follow head:
        chaosSpawnerEntity.setYBodyRot(chaosSpawnerEntity.yHeadRot);
    }
}
