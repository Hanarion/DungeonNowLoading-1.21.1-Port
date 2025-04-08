package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.util.WeightedTargetProvider;
import dev.hexnowloading.dungeonnowloading.util.WeightedRandomBag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;

import java.util.*;

public class BossTargetSelectorGoal extends TargetGoal {

    private final Mob mob;
    private final WeightedTargetProvider targetProvider;
    private LivingEntity selectedTarget = null;

    public BossTargetSelectorGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        if (this.mob instanceof WeightedTargetProvider t) {
            this.targetProvider = t;
        } else {
            this.targetProvider = null;
        }
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() == null) {
            findTarget();
            return selectedTarget != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            livingEntity = this.targetMob;
        }

        if (livingEntity == null) return false;

        if (!this.mob.canAttack(livingEntity)) return false;

        Team teamMob = this.mob.getTeam();
        Team teamOther = livingEntity.getTeam();
        if (teamMob != null && teamOther == teamMob) {
            return false;
        }

        int arenaSize = this.targetProvider.getArenaSize();
        BlockPos arenaCenter = this.targetProvider.getArenaCenter();
        boolean withinX = Math.abs(livingEntity.getX() - arenaCenter.getX() + 0.5f) < arenaSize + 0.5F;
        boolean withinY = Math.abs(livingEntity.getY() - arenaCenter.getY() + 0.5f) < arenaSize + 0.5F;
        boolean withinZ = Math.abs(livingEntity.getZ() - arenaCenter.getZ() + 0.5f) < arenaSize + 0.5F;
        if (!withinX || !withinY || !withinZ) {
            return false;
        }

        mob.setTarget(livingEntity);
        return true;
    }

    @Override
    public void start() {
        if (selectedTarget != null) {
            mob.setTarget(selectedTarget);
            targetMob = selectedTarget;
            selectedTarget = null;
        }
        super.start();
    }

    public static void changeTarget(WeightedTargetProvider targetProvider) {
        Mob mob = (Mob) targetProvider;
        if (mob.getTarget() == null) {
            return;
        }

        mob.setTarget(weightTargets(targetProvider, mob));
    }

    private void findTarget() {
        selectedTarget = weightTargets(targetProvider, mob);
    }

    private static LivingEntity weightTargets(WeightedTargetProvider targetProvider, Mob mob) {
        BlockPos arenaCenter = targetProvider.getArenaCenter();
        int range = targetProvider.getArenaSize();
        AABB aabb = new AABB(arenaCenter).inflate(range);
        Set<UUID> seen = new HashSet<>();
        List<LivingEntity> candidates = new ArrayList<>();

        for (Player player : mob.level().getEntitiesOfClass(Player.class, aabb)) {
            if (player.isAlive() && mob.canAttack(player)) {
                candidates.add(player);
                seen.add(player.getUUID());
            }
        }

        for (Map.Entry<UUID, LivingEntity> entry : targetProvider.getAttackers().entrySet()) {
            LivingEntity attacker = entry.getValue();
            if (attacker != null && attacker.isAlive() && mob.canAttack(attacker) && seen.add(entry.getKey())) {
                candidates.add(attacker);
            }
        }

        targetProvider.getThreatScoreMap().clear();

        WeightedRandomBag<LivingEntity> weightedPool = new WeightedRandomBag<>();

        // Step 1: Sort candidates by distance (closest first)
        candidates.sort(Comparator.comparingDouble(mob::distanceToSqr));

        //System.out.println("========================================");

        for (int i = 0; i < candidates.size(); i++) {
            LivingEntity entity = candidates.get(i);

            // Step 2: Determine distance multiplier
            double distanceMultiplier = switch (i) {
                case 0 -> 2.0;
                case 1 -> 1.5;
                default -> 1.0;
            };

            // Step 3: Calculate damage-based score
            double damage = targetProvider.getDamageMap().getOrDefault(entity.getUUID(), 0.0);
            int damageScore = (int) Math.floor(damage / (mob.getMaxHealth() * 0.1F));

            // Step 4: Final weighted score
            int finalScore = (int) Math.floor(damageScore * distanceMultiplier + 1.0F);

            //System.out.println("   - " + entity.getType() + " : " + finalScore);

            targetProvider.getThreatScoreMap().put(entity.getUUID(), (double) finalScore);
            weightedPool.addEntry(entity, finalScore);
        }

        LivingEntity entity = weightedPool.getRandom();

        if (entity != null) {
            //System.out.println("Selected : " + entity.getType());
        }

        return entity;
    }
}