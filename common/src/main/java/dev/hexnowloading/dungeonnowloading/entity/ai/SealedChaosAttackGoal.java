package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.ChaosSpawnerProjectileEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SealedChaosAttackGoal extends Goal {

    private final SealedChaosEntity sealedChaosEntity;
    private final int shootIntervalTick;
    private int attackTick;

    public SealedChaosAttackGoal(SealedChaosEntity sealedChaosEntity, int shootIntervalTick) {
        this.sealedChaosEntity = sealedChaosEntity;
        this.shootIntervalTick = shootIntervalTick;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.sealedChaosEntity.getTarget();
        return target != null && target.isAlive();
    }

    /*@Override
    public boolean canContinueToUse() {
        LivingEntity target = this.sealedChaosEntity.getTarget();
        return target != null && target.isAlive();
    }*/

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.sealedChaosEntity.getTarget();
        if (target != null) {
            this.sealedChaosEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.attackTick > 0) {
                this.attackTick--;
            } else {
                if (this.sealedChaosEntity.hasLineOfSight(target)) {
                    performSingleShot(target);
                    this.attackTick = this.shootIntervalTick;
                }
            }
        }
    }

    private void performSingleShot(LivingEntity target) {
        if (target.distanceTo(this.sealedChaosEntity) < this.sealedChaosEntity.getAttributeValue(Attributes.FOLLOW_RANGE)
                && this.sealedChaosEntity.getLookControl().isLookingAtTarget()) {

            shootProjectile(0.0F);
        }
    }

    private void shootProjectile(float angle) {

        // 🔊 Play shoot sound HERE (server side safe)
        this.sealedChaosEntity.level().playSound(
                null,
                this.sealedChaosEntity.getX(),
                this.sealedChaosEntity.getY(),
                this.sealedChaosEntity.getZ(),
                DNLSounds.SEALED_CHAOS_BULLET_SHOOT.get(),
                //SoundEvents.WITHER_SHOOT,
                this.sealedChaosEntity.getSoundSource(),
                1.0F,
                0.9F + this.sealedChaosEntity.getRandom().nextFloat() * 0.2F
        );

        Vec3 viewVector = this.sealedChaosEntity.getViewVector(1.0F);

        if (angle != 0.0F) {
            viewVector = viewVector.yRot((float) Math.toRadians(angle));
        }

        double speed = 0.1F;

        double dx = viewVector.x * speed;
        double dy = viewVector.y * speed;
        double dz = viewVector.z * speed;

        ChaosSpawnerProjectileEntity projectile =
                new ChaosSpawnerProjectileEntity(this.sealedChaosEntity, dx, dy, dz);

        projectile.setPos(
                this.sealedChaosEntity.getX() + dx,
                this.sealedChaosEntity.getEyeY() - 0.1F,
                this.sealedChaosEntity.getZ() + dz
        );

        this.sealedChaosEntity.level().addFreshEntity(projectile);
    }

}
