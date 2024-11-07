package dev.hexnowloading.dungeonnowloading.potion;

import dev.hexnowloading.dungeonnowloading.components.VertexNode;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

    public VertexTransmissionEffect() {
        super(MobEffectCategory.HARMFUL, 13458603); // Set the effect type and color
    }

    public VertexNode getVertexNode(UUID uuid) {
        return entityVertexNodeMap.get(uuid);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // TODO: Make uses of the effect amplifier

        UUID uuid = entity.getUUID();
        entityVertexNodeMap.computeIfAbsent(uuid, id -> new VertexNode(entity));
        damageTickCountMap.putIfAbsent(uuid, 0);

        VertexNode vertexNode = entityVertexNodeMap.get(uuid);
        int damageTickCount = damageTickCountMap.get(uuid);

        if (damageTickCount == 0) {
            float damageAmount = BASE_DAMAGE * (DAMAGE_MULTIPLIER_PER_CONNECTION * vertexNode.getConnectionCount() + 1);
            entity.hurt(entity.level().damageSources().magic(), damageAmount);
            damageTickCountMap.put(uuid, DAMAGE_TICK_INTERVAL);
        } else {
            damageTickCountMap.put(uuid, damageTickCount-1);
        }

        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, vertexNode.getConnectionCount()));

        if (!entity.isDeadOrDying()
                && !vertexNode.connectionLimitReached()
                && !vertexNode.attemptedConnection()) {
            vertexNode.connectToNearbyNodes(entity);
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

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0;
    }
}
