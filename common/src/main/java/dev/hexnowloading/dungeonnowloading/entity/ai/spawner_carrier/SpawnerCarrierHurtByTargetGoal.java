package dev.hexnowloading.dungeonnowloading.entity.ai.spawner_carrier;

import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class SpawnerCarrierHurtByTargetGoal extends HurtByTargetGoal {
    private final SpawnerCarrierEntity carrier;

    public SpawnerCarrierHurtByTargetGoal(SpawnerCarrierEntity carrier) {
        // keep your “ignore other SpawnerCarrierEntity” behavior if you want:
        super(carrier, SpawnerCarrierEntity.class);
        this.carrier = carrier;
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (attacker != null && carrier.isSpawnedMinion(attacker)) {
            // Clear it so we don’t keep re-checking the same “hurt by minion” hit
            this.mob.setLastHurtByMob(null);
            return false;
        }
        return super.canUse();
    }

    @Override
    public void start() {
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (attacker != null && carrier.isSpawnedMinion(attacker)) {
            this.mob.setLastHurtByMob(null);
            return;
        }
        super.start();
    }
}
