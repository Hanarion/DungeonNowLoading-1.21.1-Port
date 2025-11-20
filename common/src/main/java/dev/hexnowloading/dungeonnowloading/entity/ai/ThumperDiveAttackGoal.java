package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.ThumperEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ThumperDiveAttackGoal extends Goal {

    private enum Phase {
        ASCEND_ABOVE_TARGET,
        SPIN_UP,
        DIVE,
        GROUNDED
    }

    private final ThumperEntity mob;
    private Phase phase = Phase.ASCEND_ABOVE_TARGET;
    private LivingEntity target;
    private int groundedTicks;
    private int diveCooldown; // time between attack cycles

    // Spin wind-up
    private int spinTicks;
    private static final int SPIN_DURATION_TICKS = reducedTickDelay(40); // 1 second
    private static final float SPIN_SPEED_DEGREES = 36.0F; // per tick
    private int diveTicks;
    private static final int MAX_DIVE_TICKS = reducedTickDelay(20);

    // Hover position (locked when we start spinning)
    private double hoverX, hoverY, hoverZ;

    // Tune these if needed
    private static final double ASCEND_HEIGHT_ABOVE_TARGET = 3.0D;
    private static final double EXTRA_RANDOM_HEIGHT = 1.0D;
    private static final double DIVE_SPEED = 1.3D;
    private static final int DIVE_COOLDOWN_TICKS = reducedTickDelay(20); // 1s between cycles

    // Ground
    private static final int GROUND_DURATION_TICKS = reducedTickDelay(40);

    public ThumperDiveAttackGoal(ThumperEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (diveCooldown > 0) {
            return false;
        }

        LivingEntity t = mob.getTarget();
        if (t == null || !t.isAlive() || !(t instanceof Player)) {
            return false;
        }

        this.target = t;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        phase = Phase.ASCEND_ABOVE_TARGET;
        groundedTicks = 0;
        diveTicks = 0;
        mob.resetSlamState();
    }

    @Override
    public void stop() {
        target = null;
        diveCooldown = DIVE_COOLDOWN_TICKS;
    }

    @Override
    public void tick() {
        if (diveCooldown > 0) {
            diveCooldown--;
        }

        if (target == null) {
            return;
        }

        switch (phase) {
            case ASCEND_ABOVE_TARGET -> tickAscend();
            case SPIN_UP -> tickSpinUp();
            case DIVE -> tickDive();
            case GROUNDED -> tickGrounded();
        }
    }

    private void tickAscend() {
        // We always want to hover ~3–4 blocks above the target
        double targetX = target.getX();
        double targetZ = target.getZ();
        double baseY = target.getY() + ASCEND_HEIGHT_ABOVE_TARGET;
        double desiredY = baseY + mob.getRandom().nextDouble() * EXTRA_RANDOM_HEIGHT;

        // Move towards a point directly above the target
        mob.getMoveControl().setWantedPosition(targetX, desiredY, targetZ, mob.getAttributeValue(Attributes.FLYING_SPEED));

        // Check if we are close enough horizontally and high enough vertically
        double dx = mob.getX() - targetX;
        double dz = mob.getZ() - targetZ;
        double horizontalDistSq = dx * dx + dz * dz;

        boolean closeHorizontally = horizontalDistSq < 1.5D * 1.5D;
        boolean highEnough = mob.getY() >= desiredY - 0.5D;

        if (closeHorizontally && highEnough) {
            // Lock the hover position where it will spin
            this.hoverX = targetX;
            this.hoverY = desiredY;
            this.hoverZ = targetZ;

            this.spinTicks = 0;
            // Stop drift
            mob.setDeltaMovement(Vec3.ZERO);
            phase = Phase.SPIN_UP;

            // 🔊 Play spin-up start sound once
            mob.level().playSound(
                    null,
                    mob.getX(), mob.getY(), mob.getZ(),
                    DNLSounds.SCORCHER_START.get(),
                    SoundSource.HOSTILE,
                    1.0F,
                    1.0F
            );
        }
    }

    private void tickSpinUp() {
        // Stay at the locked hover position
        mob.getMoveControl().setWantedPosition(hoverX, hoverY, hoverZ, 0.0D);
        mob.setDeltaMovement(Vec3.ZERO);

        // Spin in place
        float newRot = mob.getYRot() + SPIN_SPEED_DEGREES;
        mob.setYRot(newRot);
        mob.yBodyRot = newRot;
        mob.yHeadRot = newRot;

        spinTicks++;

        // After 1 second, begin the dive
        if (spinTicks >= SPIN_DURATION_TICKS) {
            phase = Phase.DIVE;
            diveTicks = 0;
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY() - 10, mob.getZ(), mob.getAttributeValue(Attributes.FLYING_SPEED) * 1.5D);
        }
    }

    private void tickDive() {
        // First: check if we should STOP diving
        if (this.mob.getDeltaMovement().length() < 0.001f || diveTicks >= MAX_DIVE_TICKS) {
            mob.setDeltaMovement(Vec3.ZERO);
            mob.onSlamImpact(); // sound + particles handled in entity
            phase = Phase.GROUNDED;
            groundedTicks = 0;
            return;
        }

        // Still in the air: continue the straight-down dive
        diveTicks++;

        // Stop any path / hover control fighting with us
        mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0D);

        // Pure vertical slam
        Vec3 diveVec = new Vec3(0.0D, -DIVE_SPEED, 0.0D);
        mob.setDeltaMovement(diveVec);
        mob.hasImpulse = true;
    }

    private void tickGrounded() {
        // Just sit here for a while
        mob.getNavigation().stop();
        groundedTicks++;

        if (groundedTicks >= GROUND_DURATION_TICKS) {
            // Next cycle: go back up again
            phase = Phase.ASCEND_ABOVE_TARGET;
            mob.resetSlamState();
        }
    }
}
