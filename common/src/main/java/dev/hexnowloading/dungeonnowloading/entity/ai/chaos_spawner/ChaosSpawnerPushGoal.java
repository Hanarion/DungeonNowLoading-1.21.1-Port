package dev.hexnowloading.dungeonnowloading.entity.ai.chaos_spawner;

import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.ChaosSpawnerProjectileEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ChaosSpawnerPushGoal extends Goal {
    private final ChaosSpawnerEntity chaosSpawnerEntity;

    public ChaosSpawnerPushGoal(ChaosSpawnerEntity chaosSpawnerEntity) {
        this.chaosSpawnerEntity = chaosSpawnerEntity;
    }

    @Override
    public boolean canUse() {
        return chaosSpawnerEntity.isAttacking(ChaosSpawnerEntity.State.PUSH) && chaosSpawnerEntity.getTarget() != null;
    }

    @Override
    public void start() {
        super.start();
        chaosSpawnerEntity.triggerSmashAttackAnimation();
        chaosSpawnerEntity.setAttackTick(100);
        chaosSpawnerEntity.playSound(DNLSounds.CHAOS_SPAWNER_BUILD_UP.get());
    }

    @Override
    public void tick() {
        if (chaosSpawnerEntity.getAttackTick() == 72) {
            if (chaosSpawnerEntity.getPhase() > 1) {
                shootGhostBurst(16);
            }
        }
        if (chaosSpawnerEntity.getAttackTick() == 66) { // Only even number tick works for some reason
            chaosSpawnerEntity.playSound(DNLSounds.CHAOS_SPAWNER_SHOCKWAVE.get(), 3.0F, 1.0F);
            ((ServerLevel) chaosSpawnerEntity.level()).sendParticles(ParticleTypes.POOF, chaosSpawnerEntity.getX(), chaosSpawnerEntity.getY(), chaosSpawnerEntity.getZ(), 50, 3.0D, 0.0D, 3.0D, 0.0D);
            AABB aabb = (new AABB(this.chaosSpawnerEntity.blockPosition())).inflate(8);
            List<LivingEntity> targets = this.chaosSpawnerEntity.level().getEntitiesOfClass(LivingEntity.class, aabb);
            for (LivingEntity mob : targets) {
                if (mob != this.chaosSpawnerEntity) {
                    if (mob instanceof Player player) {
                        this.pushNearbyPlayers(player);
                    } else {
                        this.pushNearbyMobs(mob);
                    }
                }
            }
        }
        if (chaosSpawnerEntity.getAttackTick() == 0) {
            chaosSpawnerEntity.stopAttacking(60);
        }
    }

    private void pushNearbyMobs(LivingEntity mob) {
        double knockbackStrength = 12.0D;
        int damageAmount = (int) (chaosSpawnerEntity.getAttackDamage() * 0.9F);
        double x = mob.getX() - chaosSpawnerEntity.getX();
        double z = mob.getZ() - chaosSpawnerEntity.getZ();
        double a = Math.max(x * x + z * z, 0.001);
        mob.push(x / a * knockbackStrength, 0.2, z / a * knockbackStrength);
        mob.hurt(chaosSpawnerEntity.damageSources().noAggroMobAttack(chaosSpawnerEntity), damageAmount);
    }

    private void pushNearbyPlayers(Player player) {
        double knockbackStrength = 12.0D;
        int damageAmount;
        if (player.isBlocking()) {
            player.disableShield();
            damageAmount = (int) (chaosSpawnerEntity.getAttackDamage() * 0.45F);
        } else {
            damageAmount = (int) (chaosSpawnerEntity.getAttackDamage() * 0.9F);
        }
        double x = player.getX() - chaosSpawnerEntity.getX();
        double z = player.getZ() - chaosSpawnerEntity.getZ();
        double a = Math.max(x * x + z * z, 0.001);
        player.push(x / a * knockbackStrength, 0.2, z / a * knockbackStrength);
        player.hurt(chaosSpawnerEntity.damageSources().mobAttack(chaosSpawnerEntity), damageAmount);
    }

    private void shootGhostBurst(int bullets) {
        float randomOffsetDeg = chaosSpawnerEntity.getRandom().nextFloat() * 360.0F;

        Vec3 base = chaosSpawnerEntity.getViewVector(0.5F)
                .yRot((float) Math.toRadians(randomOffsetDeg));

        final double spawnDistance = 2.0D;
        final float stepDeg = 360.0F / bullets;

        for (int i = 0; i < bullets; i++) {
            Vec3 dir = base.yRot((float) Math.toRadians(stepDeg * i));

            ChaosSpawnerProjectileEntity proj =
                    new ChaosSpawnerProjectileEntity(chaosSpawnerEntity, dir.x, dir.y, dir.z);

            proj.setPos(
                    chaosSpawnerEntity.getX() + dir.x * spawnDistance,
                    chaosSpawnerEntity.getY() + dir.y * spawnDistance - 1,
                    chaosSpawnerEntity.getZ() + dir.z * spawnDistance
            );

            chaosSpawnerEntity.level().addFreshEntity(proj);
        }
    }
}
