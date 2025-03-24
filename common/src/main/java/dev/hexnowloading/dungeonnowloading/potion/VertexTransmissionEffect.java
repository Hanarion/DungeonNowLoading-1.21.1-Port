package dev.hexnowloading.dungeonnowloading.potion;

import dev.hexnowloading.dungeonnowloading.components.VertexNode;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.util.DNLMath;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.HashMap;
import java.util.UUID;

public class VertexTransmissionEffect extends MobEffect {
    private static final int DAMAGE_TICK_INTERVAL = 10;
    private static final float BASE_DAMAGE = 1.0f;
    private static final float DAMAGE_MULTIPLIER_PER_CONNECTION = 1.0f;

    private static final HashMap<UUID, VertexNode> entityVertexNodeMap = new HashMap<>();
    private static final HashMap<UUID, Integer> damageTickCountMap = new HashMap<>();
    private static final HashMap<UUID, Boolean> isReconnectionCaseMap = new HashMap<>();
    private static final HashMap<UUID, Boolean> isNoConnectionBeamDamageCaseMap = new HashMap<>();

    public VertexTransmissionEffect() {
        super(MobEffectCategory.HARMFUL, 15073280);
    }

    public VertexNode getVertexNode(UUID uuid) {
        return entityVertexNodeMap.get(uuid);
    }

    public void markAsReconnectionCase(UUID uuid) {
        isReconnectionCaseMap.put(uuid, true);
    }

    public void setNoConnectionBeamDamageCase(UUID uuid, boolean isCase) {
        isNoConnectionBeamDamageCaseMap.put(uuid, isCase);
    }


    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        UUID uuid = entity.getUUID();
        entityVertexNodeMap.computeIfAbsent(uuid, id -> new VertexNode(entity));
        damageTickCountMap.putIfAbsent(uuid, 5);
        isReconnectionCaseMap.putIfAbsent(uuid, false);
        isNoConnectionBeamDamageCaseMap.putIfAbsent(uuid, false);

        VertexNode vertexNode = entityVertexNodeMap.get(uuid);
        int damageTickCount = damageTickCountMap.get(uuid);
        boolean isNoConnectionBeamDamageCase = isNoConnectionBeamDamageCaseMap.get(uuid);

        if (damageTickCount == 0) {
            float damageAmount = BASE_DAMAGE * (DAMAGE_MULTIPLIER_PER_CONNECTION * vertexNode.getConnectionCount());

            if (damageAmount > 0 || isNoConnectionBeamDamageCase) {
                // Recalculate damage amount for isNoConnectionBeamDamageCase
                if (isNoConnectionBeamDamageCase) {
                    damageAmount = BASE_DAMAGE * DAMAGE_MULTIPLIER_PER_CONNECTION;
                    this.setNoConnectionBeamDamageCase(uuid, false);        // Resets it back
                }

                entity.hurt(entity.level().damageSources().magic(), damageAmount);

                entity.level().playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        DNLSounds.VERTEX_ARROW_DAMAGE.get(),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.2F / (DNLMath.randomRange(0.0f, 1.0f) * 0.2F + 0.9F)
                );
            }

            damageTickCountMap.put(uuid, DAMAGE_TICK_INTERVAL);
        } else {
            damageTickCountMap.put(uuid, damageTickCount-1);
        }

        if (!entity.isDeadOrDying()
                && !vertexNode.connectionLimitReached()
                && !vertexNode.attemptedConnection()) {
            vertexNode.connectToNearbyNodes(entity, isReconnectionCaseMap.get(uuid));
        }

        vertexNode.tick(entity);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        UUID uuid = entity.getUUID();

        VertexNode vertexNode = entityVertexNodeMap.get(uuid);
        if (vertexNode != null) {
            vertexNode.disconnect_all();
        }

        entityVertexNodeMap.remove(uuid);
        damageTickCountMap.remove(uuid);
        isReconnectionCaseMap.remove(uuid);

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0;
    }
}
