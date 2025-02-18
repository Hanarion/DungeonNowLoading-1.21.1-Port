package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;

public class FairkeeperBorosEatVertexProjectilesGoal extends Goal {

    private final FairkeeperBorosEntity boros;
    private final FairkeeperBorosEntity.FairkeeperBorosState state;
    private final double speed;
    private Entity targetProjectile;

    public FairkeeperBorosEatVertexProjectilesGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, double speed) {
        this.state = state;
        this.boros = boros;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.boros.isState(state);
    }

    @Override
    public void start() {
        this.getTargetProjectile();
    }

    @Override
    public void tick() {
        if (this.targetProjectile == null || !this.targetProjectile.isAlive()) {
            this.boros.stopAttacking(10);
            return;
        }

        this.boros.getMoveControl().setWantedPosition(targetProjectile.getX(), this.boros.getY(), targetProjectile.getZ(), this.speed);

        if (this.boros.distanceTo(targetProjectile) < 1.5) {
            absorbProjectile(targetProjectile);
        }

    }

    private void absorbProjectile(Entity projectile) {
        float maxHealth = boros.getMaxHealth();
        float healMultiplier = 1;

        if (projectile instanceof VertexDomainProjectileEntity) {
            healMultiplier = 5; // Heal 15% max HP
        }

        boros.heal(maxHealth * 0.03F * healMultiplier);
        this.boros.playHealSound(this.boros.getX(), this.boros.getY(), this.boros.getZ());
        projectile.remove(Entity.RemovalReason.DISCARDED);
        for (int i = 0; i < healMultiplier; i++) {
            RandomSource randomSource = this.boros.getRandom();
            double offsetX = (randomSource.nextDouble() - 0.5); // Random value between -1 and +1
            double offsetY = (randomSource.nextDouble() - 0.5); // Random value between -0.75 and +0.75
            double offsetZ = (randomSource.nextDouble() - 0.5);
            ((ServerLevel) this.boros.level()).sendParticles(ParticleTypes.HEART, this.boros.getX() + offsetX, this.boros.getY() + 3 + offsetY, this.boros.getZ() + offsetZ, 1, 0, 0, 0, 0);
        }
        // Reset goal to find a new target
        this.getTargetProjectile();
    }

    private void getTargetProjectile() {
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.boros.getCaller();
        if (caller == null) {
            this.boros.stopAttacking(10);
            return;
        }
        int halfSize = caller.getArenaSize();
        AABB arenaAABB = new AABB(
                caller.getX() - halfSize, caller.getY() - halfSize, caller.getZ() - halfSize,
                caller.getX() + halfSize, caller.getY() + halfSize, caller.getZ() + halfSize
        );

        // Find the closest valid projectile
        targetProjectile = boros.level().getEntities((Entity) null, arenaAABB, entity ->
                        entity instanceof VertexOrbProjectileEntity || entity instanceof VertexDomainProjectileEntity
                ).stream()
                .filter(entity -> entity.getY() < boros.getY() + 3) // Filter out unreachable projectiles
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(boros))) // Get the closest one
                .orElse(null);

        if (targetProjectile == null || !targetProjectile.isAlive()) {
            this.boros.stopAttacking(10);
        }
    }
}
