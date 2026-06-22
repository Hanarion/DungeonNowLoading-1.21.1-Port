package dev.hexnowloading.dungeonnowloading.util;

import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Server-side helper for the Overworked enchantment tradeoff:
 * faster summon attack speed in exchange for reduced owner max health while any overworked summon is alive.
 */
public final class OverworkedPenaltyUtil {

    private OverworkedPenaltyUtil() {
    }

    /** Stable UUID so the modifier can be updated/removed safely. */
    public static final UUID OVERWORKED_HP_MODIFIER_ID =
            UUID.nameUUIDFromBytes("dnl_overworked_player_max_health".getBytes());

    /**
     * Refresh the owner's max-health penalty based on currently alive overworked summons.
     *
     * Rule: use the maximum Overworked level among the owner's currently loaded summons.
     */
    public static void refreshOwnerPenalty(ServerLevel level, Player owner) {
        if (level == null || owner == null) return;

        int maxLevel = 0;

        // "Loaded entities" correctness: this will count summons that are currently loaded.
        // That's consistent with how entity-based mechanics usually behave.
        for (WhimperEntity w : level.getEntitiesOfClass(
                WhimperEntity.class,
                owner.getBoundingBox().inflate(128.0D),
                e -> e.isAlive() && owner.getUUID().equals(e.getOwnerUUID()) && e.getOverworkedLevel() > 0
        )) {
            maxLevel = Math.max(maxLevel, w.getOverworkedLevel());
            if (maxLevel >= 5) break;
        }

        if (maxLevel < 5) {
            for (SealedChaosEntity s : level.getEntitiesOfClass(
                    SealedChaosEntity.class,
                    owner.getBoundingBox().inflate(128.0D),
                    e -> e.isAlive() && owner.getUUID().equals(e.getOwnerUUID()) && e.getOverworkedLevel() > 0
            )) {
                maxLevel = Math.max(maxLevel, s.getOverworkedLevel());
                if (maxLevel >= 5) break;
            }
        }

        // Copper Creep also counts toward the owner's overworked penalty.
        if (maxLevel < 5) {
            for (CopperCreepEntity c : level.getEntitiesOfClass(
                    CopperCreepEntity.class,
                    owner.getBoundingBox().inflate(128.0D),
                    e -> e.isAlive()
                            && e.getSummonerUUID().filter(uuid -> uuid.equals(owner.getUUID())).isPresent()
                            && e.getOverworkedLevel() > 0
            )) {
                maxLevel = Math.max(maxLevel, c.getOverworkedLevel());
                if (maxLevel >= 5) break;
            }
        }

        applyOwnerPenalty(owner, maxLevel);
    }

    public static void applyOwnerPenalty(Player owner, int overworkedLevel) {
        AttributeInstance maxHealthAttr = owner.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        // Remove any existing modifier first
        if (maxHealthAttr.getModifier(OVERWORKED_HP_MODIFIER_ID) != null) {
            maxHealthAttr.removeModifier(OVERWORKED_HP_MODIFIER_ID);
        }

        if (overworkedLevel <= 0) {
            return;
        }

        // Each Overworked level reduces max HP by 2.0 (one heart), but never below 1 HP total
        double baseMax = maxHealthAttr.getBaseValue();
        double penalty = 2.0D * overworkedLevel;
        double minAllowed = 1.0D;

        if (baseMax - penalty < minAllowed) {
            penalty = baseMax - minAllowed;
            if (penalty < 0.0D) penalty = 0.0D;
        }

        if (penalty > 0.0D) {
            maxHealthAttr.addPermanentModifier(new AttributeModifier(
                    OVERWORKED_HP_MODIFIER_ID,
                    "dnl_overworked_player_max_health",
                    -penalty,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        float newMax = (float) owner.getAttributeValue(Attributes.MAX_HEALTH);
        if (owner.getHealth() > newMax) {
            owner.setHealth(newMax);
        }
    }

    /**
     * Utility for entity removal hooks.
     */
    public static void refreshOwnerPenaltyIfPossible(net.minecraft.world.level.Level level, UUID ownerUuid) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (ownerUuid == null) return;
        Player owner = serverLevel.getPlayerByUUID(ownerUuid);
        if (owner == null) return;
        refreshOwnerPenalty(serverLevel, owner);
    }
}