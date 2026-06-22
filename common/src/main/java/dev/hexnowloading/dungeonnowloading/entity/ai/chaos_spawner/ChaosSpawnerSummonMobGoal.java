package dev.hexnowloading.dungeonnowloading.entity.ai.chaos_spawner;

import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.util.WeightedRandomBag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.function.Consumer;

public class ChaosSpawnerSummonMobGoal extends Goal {

    private final ChaosSpawnerEntity boss;

    private int summonCount;
    private int maxSummonLimit;

    private static final int SUMMON_ANIM_TICKS = 100;
    private static final int POST_ATTACK_COOLDOWN_TICKS = 60;

    private static final ImmutableList<BlockPos> MOB_SUMMON_POS = ImmutableList.of(
            new BlockPos(5, 1, 0),
            new BlockPos(-5, 1, 0),
            new BlockPos(0, 1, 5),
            new BlockPos(0, 1, -5),
            new BlockPos(5, 1, 2),
            new BlockPos(-5, 1, 2),
            new BlockPos(2, 1, 5),
            new BlockPos(2, 1, -5),
            new BlockPos(5, 1, -2),
            new BlockPos(-5, 1, -2),
            new BlockPos(-2, 1, 5),
            new BlockPos(-2, 1, -5)
    );

    /**
     * A robust summon entry:
     * - type: which mob to spawn
     * - post: setup after finalizeSpawn (gear/effects/baby/no-drop etc.)
     */
    public record SummonEntry(EntityType<? extends Mob> type, Consumer<Mob> post) {
        public SummonEntry {
            if (post == null) post = m -> {};
        }
    }

    // Prebuilt bags so we don't allocate/rebuild every summon
    private static final WeightedRandomBag<SummonEntry> PHASE_1_BAG = buildPhase1Bag();
    private static final WeightedRandomBag<SummonEntry> PHASE_2_BAG = buildPhase2Bag();

    public ChaosSpawnerSummonMobGoal(ChaosSpawnerEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); // optional
    }

    @Override
    public boolean canUse() {
        if (!boss.isAttacking(ChaosSpawnerEntity.State.SUMMON_MOB)) return false;
        if (boss.getTarget() == null) return false;
        if (!(boss.level() instanceof ServerLevel serverLevel)) return false;

        if (!withinSummonLimit(serverLevel)) {
            boss.stopAttacking(0);
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return boss.isAttacking(ChaosSpawnerEntity.State.SUMMON_MOB) && boss.getTarget() != null;
    }

    @Override
    public void start() {
        boss.triggerSummonAnimation();
        boss.setAttackTick(SUMMON_ANIM_TICKS);
    }

    @Override
    public void tick() {
        if (!(boss.level() instanceof ServerLevel serverLevel)) return;

        // Fire once at the beginning of the summon animation
        if (boss.getAttackTick() == SUMMON_ANIM_TICKS) {
            boss.playSound(boss.getScreechSound(), 2.0F, 1.0F);

            boss.cullSummonsOutsideArena(serverLevel);
            int currentMobs = boss.countSummonedMobs(serverLevel);
            maxSummonLimit = computeSummonLimit();
            summonCount = Math.max(0, maxSummonLimit - currentMobs);

            // particles at boss
            double x = boss.getX();
            double y = boss.getY();
            double z = boss.getZ();
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 20, 3.0D, 3.0D, 3.0D, 0.0D);

            WeightedRandomBag<SummonEntry> bag = (boss.getPhase() == 1) ? PHASE_1_BAG : PHASE_2_BAG;

            for (int i = 0; i < summonCount; i++) {
                BlockPos offset = MOB_SUMMON_POS.get(Math.min(i, MOB_SUMMON_POS.size() - 1));
                BlockPos spawnPos = boss.blockPosition().offset(offset);
                summonMob(bag.getRandom(), spawnPos, serverLevel);
            }
        }

        // End of attack
        if (boss.getAttackTick() == 0) {
            boss.stopAttacking(POST_ATTACK_COOLDOWN_TICKS);
        }
    }

    private boolean withinSummonLimit(ServerLevel level) {
        boss.cullSummonsOutsideArena(level);     // boss method
        int current = boss.countSummonedMobs(level); // boss method
        int limit = computeSummonLimit();
        return current < limit;
    }

    private int computeSummonLimit() {
        return Math.min(3 + boss.getParticipatingPlayerCount() * 2, 12);
    }

    private void summonMob(SummonEntry entry, BlockPos summonPos, ServerLevel serverLevel) {
        Level level = boss.level();

        double px = summonPos.getX() + 0.5D;
        double py = summonPos.getY() + 1.0D;
        double pz = summonPos.getZ() + 0.5D;
        serverLevel.sendParticles(ParticleTypes.CLOUD, px, py, pz, 10, 0.5D, 0.5D, 0.5D, 0.0D);

        // Special case entries that need to spawn multiple mobs (e.g. jockey)
        if (entry.type() == EntityType.SPIDER && entry.post() == POST_SPIDER_JOCKEY) {
            spawnSpiderJockey(serverLevel, summonPos);
            return;
        }

        Mob mob = entry.type().create(level);
        if (mob == null) return;

        mob.moveTo(summonPos, 0.0F, 0.0F);
        mob.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(summonPos), MobSpawnType.MOB_SUMMONED, null);

        entry.post().accept(mob);
        level.addFreshEntity(mob);
        boss.trackSummon(mob);
    }

    private void spawnSpiderJockey(ServerLevel serverLevel, BlockPos summonPos) {
        Level level = boss.level();

        Spider spider = EntityType.SPIDER.create(level);
        Skeleton skeleton = EntityType.SKELETON.create(level);
        if (spider == null || skeleton == null) return;

        spider.moveTo(summonPos, 0.0F, 0.0F);
        skeleton.moveTo(summonPos, 0.0F, 0.0F);

        spider.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(summonPos), MobSpawnType.MOB_SUMMONED, null);
        skeleton.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(summonPos), MobSpawnType.MOB_SUMMONED, null);

        // no drop chance on rider (and optionally spider)
        noDropChance(skeleton);

        level.addFreshEntity(spider);
        level.addFreshEntity(skeleton);
        skeleton.startRiding(spider, true);
        boss.trackSummon(spider);
        boss.trackSummon(skeleton);
    }

    // --------------------------
    // Bag building + post actions
    // --------------------------

    private static WeightedRandomBag<SummonEntry> buildPhase1Bag() {
        WeightedRandomBag<SummonEntry> bag = new WeightedRandomBag<>();
        bag.addEntry(new SummonEntry(EntityType.ZOMBIE, m -> noDropChance((Monster) m)), 3);
        bag.addEntry(new SummonEntry(EntityType.SKELETON, m -> noDropChance((Monster) m)), 2);
        bag.addEntry(new SummonEntry(EntityType.SPIDER, m -> noDropChance((Monster) m)), 2);
        bag.addEntry(new SummonEntry(EntityType.CREEPER, m -> noDropChance((Monster) m)), 2);
        bag.addEntry(new SummonEntry(DNLEntityTypes.HOLLOW.get(), m -> noDropChance((Monster) m)), 2);
        return bag;
    }

    // A marker consumer for jockey (we intercept it in summonMob)
    private static final Consumer<Mob> POST_SPIDER_JOCKEY = m -> {};

    private static WeightedRandomBag<SummonEntry> buildPhase2Bag() {
        WeightedRandomBag<SummonEntry> bag = new WeightedRandomBag<>();

        // Basics
        bag.addEntry(new SummonEntry(EntityType.ZOMBIE, m -> noDropChance((Monster) m)), 6);
        bag.addEntry(new SummonEntry(EntityType.SKELETON, m -> noDropChance((Monster) m)), 4);
        bag.addEntry(new SummonEntry(EntityType.SPIDER, m -> noDropChance((Monster) m)), 4);
        bag.addEntry(new SummonEntry(EntityType.CREEPER, m -> noDropChance((Monster) m)), 4);
        bag.addEntry(new SummonEntry(DNLEntityTypes.HOLLOW.get(), m -> noDropChance((Monster) m)), 4);

        // Diamond Zombie
        bag.addEntry(new SummonEntry(EntityType.ZOMBIE, m -> {
            if (m instanceof Zombie z) {
                z.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
                z.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                z.getItemBySlot(EquipmentSlot.MAINHAND).enchant(DNLEnchantments.holder(z.level(), Enchantments.SHARPNESS), 2);
                noDropChance(z);
            }
        }), 6);

        // Diamond Skeleton
        bag.addEntry(new SummonEntry(EntityType.SKELETON, m -> {
            if (m instanceof Skeleton s) {
                s.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
                s.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                s.getItemBySlot(EquipmentSlot.MAINHAND).enchant(DNLEnchantments.holder(s.level(), Enchantments.POWER_ARROWS), 2);
                noDropChance(s);
            }
        }), 6);

        // Invisible Spider (use a large positive duration instead of -1)
        bag.addEntry(new SummonEntry(EntityType.SPIDER, m -> {
            m.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }), 6);

        // Spider Jockey
        bag.addEntry(new SummonEntry(EntityType.SPIDER, POST_SPIDER_JOCKEY), 3);

        bag.addEntry(new SummonEntry(DNLEntityTypes.SPAWNER_CARRIER.get(), m -> {
            if (m instanceof SpawnerCarrierEntity carrier) {
                carrier.setBrokenSpawnerCarrier();
            }
            if (m instanceof Monster mon) noDropChance(mon);
        }), 6);

        // Baby Zombie (geared)
        bag.addEntry(new SummonEntry(EntityType.ZOMBIE, m -> {
            if (m instanceof Zombie z) {
                z.setBaby(true);
                z.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
                z.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
                z.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
                z.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
                z.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
                z.getItemBySlot(EquipmentSlot.MAINHAND).enchant(DNLEnchantments.holder(z.level(), Enchantments.SHARPNESS), 4);
                noDropChance(z);
            }
        }), 1);

        return bag;
    }

    private static void noDropChance(Monster monster) {
        monster.setDropChance(EquipmentSlot.HEAD, 0.0F);
        monster.setDropChance(EquipmentSlot.CHEST, 0.0F);
        monster.setDropChance(EquipmentSlot.LEGS, 0.0F);
        monster.setDropChance(EquipmentSlot.FEET, 0.0F);
        monster.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        monster.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }
}
