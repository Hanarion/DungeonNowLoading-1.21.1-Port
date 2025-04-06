package dev.hexnowloading.dungeonnowloading.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;

public interface WeightedTargetProvider {
    Map<UUID, Double> getDamageMap();
    Map<UUID, LivingEntity> getAttackers();
    Map<UUID, Double> getThreatScoreMap();

    BlockPos getArenaCenter();
    int getArenaSize();

    default void recordDamage(LivingEntity source, double amount) {
        getAttackers().put(source.getUUID(), source);
        getDamageMap().merge(source.getUUID(), amount, Double::sum);
    }
}
