package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.ChaosSpawnerProjectileEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SealedChaosAttackGoal extends Goal {

    private final SealedChaosEntity sealedChaosEntity;
    private final int shootIntervalTick;
    private int attackTick;

    // State for pulse (rapid) firing
    private int remainingPulseShots;
    private int pulseCooldown;
    private Vec3 lastPulseDirection; // direction used for remaining pulse shots
    private static final int PULSE_INTERVAL_TICKS = 2; // faster spacing between rapid shots

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
        }

        // Handle ongoing pulse sequence if any (continues even while attackTick is > 0)
        if (this.remainingPulseShots > 0) {
            if (this.pulseCooldown > 0) {
                this.pulseCooldown--;
            } else {
                // Fire next pulse shot straight ahead using stored direction
                firePulseShot();
                this.remainingPulseShots--;
                this.pulseCooldown = PULSE_INTERVAL_TICKS;
                // When we've just fired the last pulse shot, start the main cooldown now
                if (this.remainingPulseShots == 0) {
                    this.attackTick = this.shootIntervalTick;
                    this.lastPulseDirection = null;
                }
            }
        }

        if (this.attackTick > 0) {
            this.attackTick--;
        } else if (this.remainingPulseShots == 0) {
            // Only start a new attack if no pulse burst is currently active
            if (target != null && this.sealedChaosEntity.hasLineOfSight(target)) {
                performEnchantedShot(target);
            }
        }
    }

    private void performEnchantedShot(LivingEntity target) {
        int arcLevel = this.sealedChaosEntity.getArcShotLevel();
        int pulseLevel = this.sealedChaosEntity.getPulseShotLevel();

        // Determine total bullets based on highest relevant level
        int effectiveLevel = Math.max(arcLevel, pulseLevel);
        int totalBullets = bulletsForLevel(effectiveLevel);

        // Play the shot sound once per attack, before spawning any projectiles
        playShotSound();

        if (arcLevel > 0 && effectiveLevel > 0) {
            // Arc pattern: fire all bullets at once in an arc, then start cooldown immediately
            performArcShot(totalBullets, target, false);
            this.attackTick = this.shootIntervalTick;
        } else if (pulseLevel > 0 && effectiveLevel > 0) {
            // Pulse pattern: fire one now, queue the rest; cooldown will start after last pulse
            if (performStraightShotIfValid(target, false)) {
                this.remainingPulseShots = totalBullets - 1;
                this.pulseCooldown = PULSE_INTERVAL_TICKS;
                // Capture the direction we used for the first pulse so the rest continue even if target dies
                this.lastPulseDirection = this.sealedChaosEntity.getViewVector(1.0F).normalize();
            } else {
                // If we couldn't fire the first shot, just fall back to normal single shot + cooldown
                performSingleShot(target, false);
                this.attackTick = this.shootIntervalTick;
            }
        } else {
            // No relevant enchantment, keep original single-shot behavior
            performSingleShot(target, false);
            this.attackTick = this.shootIntervalTick;
        }
    }

    private void playShotSound() {
        this.sealedChaosEntity.level().playSound(
                null,
                this.sealedChaosEntity.getX(),
                this.sealedChaosEntity.getY(),
                this.sealedChaosEntity.getZ(),
                SoundEvents.WITHER_SHOOT,
                this.sealedChaosEntity.getSoundSource(),
                1.5F,
                1.0F + (this.sealedChaosEntity.getRandom().nextFloat() - this.sealedChaosEntity.getRandom().nextFloat()) * 0.2F
        );
    }

    private int bulletsForLevel(int level) {
        return switch (level) {
            case 1 -> 3;
            case 2 -> 5;
            default -> 1;
        };
    }

    private void performSingleShot(LivingEntity target, boolean playSound) {
        if (target.distanceTo(this.sealedChaosEntity) < this.sealedChaosEntity.getAttributeValue(Attributes.FOLLOW_RANGE)
                && this.sealedChaosEntity.getLookControl().isLookingAtTarget()) {
            if (playSound) {
                playShotSound();
            }
            vecFromCenterToFrontOfFace(0.0F, null);
        }
    }

    private boolean performStraightShotIfValid(LivingEntity target, boolean playSound) {
        if (target.distanceTo(this.sealedChaosEntity) < this.sealedChaosEntity.getAttributeValue(Attributes.FOLLOW_RANGE)
                && this.sealedChaosEntity.getLookControl().isLookingAtTarget()) {
            if (playSound) {
                playShotSound();
            }
            vecFromCenterToFrontOfFace(0.0F, null);
            return true;
        }
        return false;
    }

    private void performArcShot(int totalBullets, LivingEntity target, boolean playSound) {
        if (!(target.distanceTo(this.sealedChaosEntity) < this.sealedChaosEntity.getAttributeValue(Attributes.FOLLOW_RANGE)
                && this.sealedChaosEntity.getLookControl().isLookingAtTarget())) {
            return;
        }
        if (playSound) {
            playShotSound();
        }
        // Symmetric angles around 0
        float stepDegrees = 10.0F;
        float center = (totalBullets - 1) / 2.0F;
        for (int i = 0; i < totalBullets; i++) {
            float offsetIndex = i - center;
            float angle = offsetIndex * stepDegrees;
            vecFromCenterToFrontOfFace(angle, null);
        }
    }

    private void firePulseShot() {
        // Use stored direction if available; otherwise fall back to current view
        Vec3 direction = this.lastPulseDirection != null
                ? this.lastPulseDirection
                : this.sealedChaosEntity.getViewVector(1.0F).normalize();
        vecFromCenterToFrontOfFace(0.0F, direction);
    }

    private void vecFromCenterToFrontOfFace(float angle, Vec3 overrideDirection) {
        // Use the owner-based constructor so the projectile gets a clean origin, then offset spawn and direction
        double viewDistance = 0.35F; // a bit closer to really feel like it comes from inside/just in front
        Vec3 viewVector = overrideDirection != null ? overrideDirection : this.sealedChaosEntity.getViewVector(1.0F);
        if (angle != 0.0F) {
            float offset = (float) Math.toRadians(angle);
            viewVector = viewVector.yRot(offset);
        }

        // Direction vector for the projectile's motion
        Vec3 direction = viewVector.normalize();

        // Create projectile at the Sealed Chaos position
        ChaosSpawnerProjectileEntity projectile = new ChaosSpawnerProjectileEntity(
                this.sealedChaosEntity,
                direction.x,
                direction.y,
                direction.z
        );

        // Move it slightly forward and at roughly mid-body height (below eye level)
        double baseY = this.sealedChaosEntity.getY() + this.sealedChaosEntity.getBbHeight() * 0.5F;
        Vec3 spawnOffsetHorizontal = direction.scale(viewDistance);
        projectile.setPos(
                this.sealedChaosEntity.getX() + spawnOffsetHorizontal.x,
                baseY,
                this.sealedChaosEntity.getZ() + spawnOffsetHorizontal.z
        );

        // Give it an initial velocity along the direction vector; 0.8F is a moderate speed, inaccuracy 0 keeps it straight
        projectile.shoot(direction.x, direction.y, direction.z, 0.8F, 0.0F);

        this.sealedChaosEntity.level().addFreshEntity(projectile);
    }
}