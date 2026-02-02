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
    private boolean hasAimedOnce; // ensure at least one aiming tick before first shot

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

    @Override
    public void start() {
        // Warm-up before first possible shot.
        // Important: do NOT clear any in-progress pulse sequence here, otherwise retargeting
        // (which restarts goals) will wipe the remaining burst and feel broken.
        this.attackTick = 5;
        this.hasAimedOnce = false;
    }

    @Override
    public void stop() {
        // Don't hard-reset pulse state on stop; it causes pulse bursts to be lost on retarget.
        this.hasAimedOnce = false;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.sealedChaosEntity.getTarget();

        // If we fully lose the target (or it died), cancel any stored pulse burst.
        // We still preserve burst state across retargets (target swap while still non-null),
        // but not across "no target" periods.
        if (target == null || !target.isAlive()) {
            this.remainingPulseShots = 0;
            this.pulseCooldown = 0;
            this.lastPulseDirection = null;
            this.hasAimedOnce = false;
            return;
        }

        if (target != null) {
            this.sealedChaosEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.hasAimedOnce = true; // we've spent at least one tick aiming at the current target
        }

        // Handle ongoing pulse sequence.
        if (this.remainingPulseShots > 0) {
            if (this.pulseCooldown > 0) {
                this.pulseCooldown--;
            } else {
                firePulseShot();
                this.remainingPulseShots--;

                int pulseInterval = Math.max(1, PULSE_INTERVAL_TICKS - (this.sealedChaosEntity.getOverworkedLevel() >= 3 ? 1 : 0));
                this.pulseCooldown = pulseInterval;

                // When the burst ends, keep whatever attackTick is currently set to (it was set when the burst started).
                if (this.remainingPulseShots == 0) {
                    this.lastPulseDirection = null;
                }
            }
        }

        if (this.attackTick > 0) {
            this.attackTick--;
            return;
        }

        if (!this.hasAimedOnce) {
            // ensure we've aimed for at least one tick before any shot
            this.attackTick = 1;
            return;
        }

        if (this.remainingPulseShots == 0 && target != null && this.sealedChaosEntity.hasLineOfSight(target)) {
            performEnchantedShot(target);
        }
    }

    private int getEffectiveShootInterval() {
        int base = this.shootIntervalTick;
        int overworkedLevel = this.sealedChaosEntity.getOverworkedLevel();
        if (overworkedLevel <= 0) {
            return base;
        }

        // Overworked speeds up firing: -12% interval per level.
        // L5 => ~0.4x interval (2.5x fire rate), clamped to at least 2 ticks.
        float factor = 1.0F - 0.12F * overworkedLevel;
        return Math.max(2, (int) (base * factor));
    }

    private void performEnchantedShot(LivingEntity target) {
        int arcLevel = this.sealedChaosEntity.getArcShotLevel();
        int pulseLevel = this.sealedChaosEntity.getPulseShotLevel();

        int effectiveLevel = Math.max(arcLevel, pulseLevel);
        int totalBullets = bulletsForLevel(effectiveLevel);

        int interval = getEffectiveShootInterval();

        if (arcLevel > 0 && effectiveLevel > 0) {
            // Arc pattern: fire all bullets at once; sound per bullet.
            performArcShot(totalBullets, target, true);
            this.attackTick = interval;
        } else if (pulseLevel > 0 && effectiveLevel > 0) {
            // Pulse pattern: sound per bullet.
            if (performStraightShotIfValid(target, true)) {
                this.remainingPulseShots = totalBullets - 1;
                // Overworked also speeds up pulse spacing slightly.
                int pulseInterval = Math.max(1, PULSE_INTERVAL_TICKS - (this.sealedChaosEntity.getOverworkedLevel() >= 3 ? 1 : 0));
                this.pulseCooldown = pulseInterval;
                this.lastPulseDirection = this.sealedChaosEntity.getViewVector(1.0F).normalize();

                // IMPORTANT: set the interval *now* so it's preserved even if goals restart mid-burst.
                this.attackTick = interval;
            } else {
                performSingleShot(target, true);
                this.attackTick = interval;
            }
        } else {
            performSingleShot(target, true);
            this.attackTick = interval;
        }
    }

    private void playShotSound() {
        this.sealedChaosEntity.level().playSound(
                null,
                this.sealedChaosEntity.getX(),
                this.sealedChaosEntity.getY(),
                this.sealedChaosEntity.getZ(),
                DNLSounds.SEALED_CHAOS_BULLET_SHOOT.get(),
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
        // Symmetric angles around 0
        float stepDegrees = 10.0F;
        float center = (totalBullets - 1) / 2.0F;
        for (int i = 0; i < totalBullets; i++) {
            if (playSound) {
                playShotSound();
            }
            float offsetIndex = i - center;
            float angle = offsetIndex * stepDegrees;
            vecFromCenterToFrontOfFace(angle, null);
        }
    }

    private void firePulseShot() {
        Vec3 direction = this.lastPulseDirection != null
                ? this.lastPulseDirection
                : this.sealedChaosEntity.getViewVector(1.0F).normalize();

        // Each pulse shot should produce sound.
        playShotSound();

        vecFromCenterToFrontOfFace(0.0F, direction);
    }

    private void vecFromCenterToFrontOfFace(float angle, Vec3 overrideDirection) {
        // Use the owner-based constructor so the projectile gets a clean origin, then offset spawn and direction
        double viewDistance = 0.15F; // spawn much closer to the body so it feels like it comes from inside
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

        // Move it slightly forward and at roughly chest height (below eye level)
        double baseY = this.sealedChaosEntity.getY() + this.sealedChaosEntity.getBbHeight() * 0.25F;
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
