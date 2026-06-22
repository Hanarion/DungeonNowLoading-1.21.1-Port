package dev.hexnowloading.dungeonnowloading.entity.util;

import dev.hexnowloading.dungeonnowloading.config.BossConfig;
import dev.hexnowloading.dungeonnowloading.config.MobConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Objects;
import java.util.UUID;

public class EntityScale {

    private static final UUID SCALED_HEALTH_MODIFIER_UUID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID SCALED_ATTACK_MODIFIER_UUID = UUID.fromString("3a284fc3-6c5a-43d7-93ec-d96423e0f34f");
    private static final double bossHealthScale = BossConfig.BOSS_HEALTH_MODIFIER.get();
    private static final double bossAttackDamageScale = BossConfig.BOSS_DAMAGE_MODIFIER.get();
    private static final double bossExhaustionScale = BossConfig.BOSS_EXHAUSTION_MODIFIER.get();
    private static final double multiplayerBossHealthScale = BossConfig.TOGGLE_MULTIPLAYER_SCALING.get() ? BossConfig.MULTIPLAYER_BOSS_HEALTH_SCALE.get() : 0;
    private static final double multiplayerBossAttackScale = BossConfig.TOGGLE_MULTIPLAYER_SCALING.get() ? BossConfig.MULTIPLAYER_BOSS_ATTACK_SCALE.get() : 0;
    private static final double multiplayerBossExhaustionScale = BossConfig.TOGGLE_MULTIPLAYER_SCALING.get() ? BossConfig.MULTIPLAYER_BOSS_EXHAUSTION_SCALE.get() : 0;
    private static final double recallBossHealthScale = BossConfig.RECALL_BOSS_HEALTH_SCALE.get();
    private static final double recallBossAttackScale = BossConfig.RECALL_BOSS_ATTACK_SCALE.get();
    private static final double recallBossExhaustionScale = BossConfig.RECALL_BOSS_EXHAUSTION_SCALE.get();

    public static void scaleBossHealth(LivingEntity entityType, int playerCount, int defeatedCount) {
        double baseBonus = bossHealthScale - 1;
        double mpBonus = Math.max(0, playerCount - 1) * multiplayerBossHealthScale;
        double recallBonus = recallHpBonus(defeatedCount);

        double healthMultiplier = baseBonus + mpBonus + recallBonus;

        AttributeModifier SCALED_HEALTH_MODIFIER = new AttributeModifier(SCALED_HEALTH_MODIFIER_UUID, "Scaled health", healthMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        Objects.requireNonNull(entityType.getAttribute(Attributes.MAX_HEALTH)).removeModifier(SCALED_HEALTH_MODIFIER);
        Objects.requireNonNull(entityType.getAttribute(Attributes.MAX_HEALTH)).addPermanentModifier(SCALED_HEALTH_MODIFIER);
        entityType.setHealth(entityType.getMaxHealth());
    }

    public static void scaleBossAttack(LivingEntity entityType, int playerCount, int defeatedCount) {
        double baseBase = bossAttackDamageScale - 1;
        double mpBonus = Math.max(0, playerCount - 1) * multiplayerBossAttackScale;
        double recallBonus = recallAtkBonus(defeatedCount);

        double attackMultiplier = baseBase + mpBonus + recallBonus;

        AttributeModifier SCALED_ATTACK_MODIFIER = new AttributeModifier(SCALED_ATTACK_MODIFIER_UUID, "Scaled attack", attackMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        Objects.requireNonNull(entityType.getAttribute(Attributes.ATTACK_DAMAGE)).removeModifier(SCALED_ATTACK_MODIFIER);
        Objects.requireNonNull(entityType.getAttribute(Attributes.ATTACK_DAMAGE)).addPermanentModifier(SCALED_ATTACK_MODIFIER);
    }

    public static void scaleBossExhaustion(LivingEntity entityType, int playerCount, ExhaustionTracker exhaustionTracker, int defeatedCount) {
        double baseBase = bossExhaustionScale - 1;
        double mpBonus = Math.max(0, playerCount - 1) * multiplayerBossExhaustionScale;
        double recallBonus = recallExhaustionBonus(defeatedCount);

        double exhaustionMultiplier = 1.0 + baseBase + mpBonus + recallBonus;

        exhaustionTracker.setMaxExhaustion((float) (exhaustionTracker.getMaxExhaustion() * (exhaustionMultiplier)));
    }

    public static void scaleMobAttributes(LivingEntity entity) {
        var maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double scaledHealth = maxHealthAttr.getBaseValue() * MobConfig.DUNGEON_MOB_HEALTH_MODIFIER.get();
            maxHealthAttr.setBaseValue(scaledHealth);
            entity.setHealth((float) entity.getMaxHealth());
        }

        var attackAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            double scaledAttack = attackAttr.getBaseValue() * MobConfig.DUNGEON_MOB_ATTACK_MODIFIER.get();
            attackAttr.setBaseValue(scaledAttack);
        }
    }

    public static double recallHpBonus(int defeatedCount) {
        int lvl = RecallUtil.clampCount(defeatedCount);
        return recallBossHealthScale * lvl;
    }

    public static double recallAtkBonus(int defeatedCount) {
        int lvl = RecallUtil.clampCount(defeatedCount);
        return recallBossAttackScale * lvl;
    }

    public static double recallExhaustionBonus(int defeatedCount) {
        int lvl = RecallUtil.clampCount(defeatedCount);
        return recallBossExhaustionScale * lvl;
    }
}
