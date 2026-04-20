package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.WispAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.WispProjectileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WispAttackGoal extends Goal {
    private final WispEntity wisp;
    private LivingEntity target;

    private static final int WINDUP_DURATION_TICKS = reducedTickDelay((int) ((WispAnimationDuration.FLARE_UP + WispAnimationDuration.TACKLE_START) * 20));

    private int windupTicks = 0;

    public WispAttackGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = wisp.getTarget();
        if (t == null || !t.isAlive()) return false;
        this.target = t;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        windupTicks = 0;
        wisp.setNoGravity(true);
    }

    @Override
    public void stop() {
        target = null;
        windupTicks = 0;
        wisp.getMoveControl().setWantedPosition(wisp.getX(), wisp.getY(), wisp.getZ(), 0);
        wisp.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (target == null) return;

        wisp.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (windupTicks < WINDUP_DURATION_TICKS) {
            if (windupTicks <= 0) {
                this.wisp.playFlareUpAnimation();
            }
            windupTicks++;

            wisp.getMoveControl().setWantedPosition(wisp.getX(), wisp.getY(), wisp.getZ(), 0.0D);
            wisp.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5D, 0);
        Vec3 direction = targetPos.subtract(wisp.position());
        if (direction.lengthSqr() < 1.0e-6D) return;

        double speed = wisp.getAttributeValue(Attributes.FLYING_SPEED);
        this.spawnProjectileWisp(direction.normalize(), speed);
    }

    private void spawnProjectileWisp(Vec3 direction, double speed) {
        if (wisp.level().isClientSide) {
            return;
        }

        Entity owner = wisp.getOwner();
        LivingEntity projectileOwner = owner instanceof LivingEntity livingOwner ? livingOwner : wisp;
        WispProjectileEntity projectile = new WispProjectileEntity(wisp.level(), projectileOwner);
        projectile.setPos(wisp.getX(), wisp.getY(), wisp.getZ());
        projectile.setDamage((float) wisp.getAttributeValue(Attributes.ATTACK_DAMAGE));
        projectile.setHomingTarget(target);
        projectile.shoot(direction.x, direction.y, direction.z, (float) speed, 0.0F);

        wisp.level().addFreshEntity(projectile);
        this.spawnTransitionBurst();
        wisp.discard();
    }

    private void spawnTransitionBurst() {
        if (!(wisp.level() instanceof ServerLevel server)) {
            return;
        }

        double x = wisp.getX();
        double y = wisp.getY() + wisp.getBbHeight() * 0.5D;
        double z = wisp.getZ();

        server.sendParticles(ParticleTypes.FLAME, x, y, z, 18, 0.22D, 0.22D, 0.22D, 0.05D);
        server.sendParticles(ParticleTypes.SMOKE, x, y, z, 8, 0.18D, 0.18D, 0.18D, 0.02D);
        server.sendParticles(ParticleTypes.POOF, x, y, z, 6, 0.12D, 0.12D, 0.12D, 0.02D);
    }
}
