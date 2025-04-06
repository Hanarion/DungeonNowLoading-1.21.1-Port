package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;

public class FairkeeperBorosEatVertexProjectilesGoal extends StoppableGoal {

    private final FairkeeperBorosEntity boros;
    private final FairkeeperBorosEntity.FairkeeperBorosState state;
    private final double speed;
    private Entity targetProjectile;

    private static final int ORB_HEAL_AMOUNT = 1;
    private static final int DOMAIN_HEAL_AMOUNT = 5;

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
    public void stop() {
        this.boros.stopAttacking(10);
    }

    @Override
    public void tick() {
        if (this.targetProjectile == null || !this.targetProjectile.isAlive()) {
            this.boros.stopAttacking(10);
            return;
        }

        this.boros.getMoveControl().setWantedPosition(targetProjectile.getX(), this.boros.getY(), targetProjectile.getZ(), this.speed);

        float consumeRange = targetProjectile instanceof VertexDomainProjectileEntity ? 3.0F : 1.5F;

        if (this.boros.distanceTo(targetProjectile) < consumeRange) {
            absorbProjectile(targetProjectile);
        }

    }

    private void absorbProjectile(Entity projectile) {
        float maxHealth = boros.getMaxHealth();
        float healMultiplier = 1;

        if (projectile instanceof VertexDomainProjectileEntity) {
            healMultiplier = 5;
        }

        if (healMultiplier / DOMAIN_HEAL_AMOUNT > this.boros.getRandom().nextFloat()) {
            if (!this.boros.hasArmor()) {
                this.boros.setArmor(true);
                this.boros.setArmorHealth(150f);
            } else {
                FairkeeperBorosPartEntity currentPart = (FairkeeperBorosPartEntity) this.boros.getChild();
                if (currentPart != null) {
                    for (int i = 0; i < 14; i++) {
                        if (!currentPart.hasArmor()) {
                            currentPart.setArmor(true);
                            currentPart.heal(currentPart.getMaxHealth());
                            break;
                        }
                        currentPart = (FairkeeperBorosPartEntity) currentPart.getChild();
                        if (currentPart == null) {
                            break;
                        }
                    }
                }
            }
        }

        boros.heal(maxHealth * 0.03F * healMultiplier);
        this.boros.playHealSound(this.boros.getX(), this.boros.getY(), this.boros.getZ());
        projectile.remove(Entity.RemovalReason.DISCARDED);
        for (int i = 0; i < healMultiplier; i++) {
            RandomSource randomSource = this.boros.getRandom();
            double offsetX = (randomSource.nextDouble() - 0.5);
            double offsetY = (randomSource.nextDouble() - 0.5);
            double offsetZ = (randomSource.nextDouble() - 0.5);
            ((ServerLevel) this.boros.level()).sendParticles(ParticleTypes.HEART, this.boros.getX() + offsetX, this.boros.getY() + 3 + offsetY, this.boros.getZ() + offsetZ, 1, 0, 0, 0, 0);
        }
        // Reset goal to find a new target
        this.getTargetProjectile();
    }

    private void getTargetProjectile() {
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.boros.getCaller();
        if (caller == null) {
            this.stopGoal();
            return;
        }
        int halfSize = caller.getArenaSize();
        AABB arenaAABB = new AABB(
                caller.getX() - halfSize, caller.getY() - halfSize, caller.getZ() - halfSize,
                caller.getX() + halfSize, caller.getY() + halfSize, caller.getZ() + halfSize
        );

        // Find the closest valid projectile
        targetProjectile = boros.level().getEntities((Entity) null, arenaAABB, entity ->
                (entity instanceof VertexOrbProjectileEntity vertexOrb && vertexOrb.getLife() > 0 && vertexOrb.getLife() < VertexOrbProjectileEntity.DURATION_ON_GROUND * 0.5F) || (entity instanceof VertexDomainProjectileEntity vertexDomain && vertexDomain.getLife() > 0 && vertexDomain.getLife() < VertexDomainProjectileEntity.DURATION_ON_GROUND * 0.5F)
                ).stream()
                .filter(entity -> entity.getY() < boros.getY() + 3)
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(boros)))
                .orElse(null);

        if (targetProjectile == null || !targetProjectile.isAlive()) {
            this.stopGoal();
        }
    }
}
