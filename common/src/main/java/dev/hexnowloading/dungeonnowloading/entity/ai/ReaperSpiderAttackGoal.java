package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.ReaperSpiderMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ReaperSpiderAttackGoal extends Goal {

    private enum State {
        CHASE,
        STRAFE,
        ATTACK_WINDUP,
        ATTACK_DASH,
        RECOVER
    }

    private final ReaperSpiderEntity mob;

    private static final double CHASE_SPEED = 1.0D;
    private static final double STRAFE_SPEED = 1.0D;
    private static final double STRAFE_DISTANCE = 10.0D;
    private static final double DASH_SPEED = 1.8D;

    private static final double CHASE_START_RANGE = 10.0D;
    private static final double CHASE_STOP_RANGE = 7.0D;
    private static final double ATTACK_RANGE = 1.5D;

    private static final int WINDUP_TICKS  = reducedTickDelay(15);  // 1 second
    private static final int DASH_TICKS    = reducedTickDelay(60);  // base dash cap
    private static final int RECOVER_TICKS = reducedTickDelay(30);  // 2 seconds stop
    private static final int SINGLE_ATTACK_LOCK_TICKS = reducedTickDelay(20);

    private State state = State.CHASE;
    private LivingEntity target;

    // dash
    private Vec3 dashStartPos;
    private int dashMaxTicks;
    private Vec3 memorizedTargetPos;
    private Vec3 dashDirection;
    private boolean dashEnteredStrikeZone;

    // strafing
    private int strafesToDo;
    private int strafesDone;
    private int currentStrafeTicks;
    private int maxStrafeTicks;
    private int strafeDirection; // +1 right, -1 left
    private Vec3 strafeStartPos;

    // strafe slash
    private int strafeSlashCooldown;
    private int singleAttackLockTicks;

    // attack
    private int windupTickCounter;
    private int recoverTickCounter;

    public ReaperSpiderAttackGoal(ReaperSpiderEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity tgt = this.mob.getTarget();
        if (tgt == null || !tgt.isAlive()) return false;
        this.target = tgt;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive();
    }

    @Override
    public void start() {
        this.state = State.CHASE;
        this.strafesDone = 0;
        this.strafeSlashCooldown = 0;
        this.singleAttackLockTicks = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
        this.state = State.CHASE;
        this.singleAttackLockTicks = 0;
    }

    @Override
    public void tick() {
        if (this.target == null) return;

        if (this.singleAttackLockTicks > 0) {
            this.singleAttackLockTicks--;
        }

        controlLooking();

        double distSq = this.mob.distanceToSqr(this.target);

        switch (this.state) {
            case CHASE -> tickChase(distSq);
            case STRAFE -> tickStrafe(distSq);
            case ATTACK_WINDUP -> tickAttackWindup();
            case ATTACK_DASH -> tickAttackDash();
            case RECOVER -> tickRecover();
        }
    }

    private void controlLooking() {
        if (Objects.requireNonNull(this.state) == State.ATTACK_DASH) {
            if (this.memorizedTargetPos != null) {
                this.mob.getLookControl().setLookAt(
                        memorizedTargetPos.x,
                        memorizedTargetPos.y,
                        memorizedTargetPos.z,
                        30.0F, 30.0F
                );
            } else if (this.target != null) {
                this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            }
        } else if (this.target != null) {
            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }
    }

    private void tickChase(double distSq) {
        double chaseStartSq = CHASE_START_RANGE * CHASE_START_RANGE;
        double chaseStopSq = CHASE_STOP_RANGE * CHASE_STOP_RANGE;

        if (distSq > chaseStopSq) {
            this.mob.getNavigation().moveTo(this.target, CHASE_SPEED);
        } else {
            this.mob.getNavigation().stop();
        }

        // enter strafing if close enough and we’re not in RECOVER (RECOVER never calls tickChase)
        if (distSq <= chaseStopSq && this.mob.getSensing().hasLineOfSight(this.target)) {
            startStrafingPhase();
        }
    }

    private void startStrafingPhase() {
        this.state = State.STRAFE;
        this.strafesDone = 0;
        this.strafesToDo = 2 + this.mob.getRandom().nextInt(2); // 2–3 side steps

        // clear any existing "chase" path by navigating to self once
        this.mob.getNavigation().moveTo(this.mob, 0.0D);

        startSingleStrafe();
    }

    private void startSingleStrafe() {
        this.currentStrafeTicks = 0;
        this.maxStrafeTicks = 6; // cap ~6 ticks; tuned vs STRAFE_SPEED
        this.strafeStartPos = this.mob.position();
        this.strafeDirection = this.mob.getRandom().nextBoolean() ? 1 : -1;
    }

    private void tickStrafe(double distSq) {
        double chaseStartSq = CHASE_START_RANGE * CHASE_START_RANGE; // 10-block radius

        // If the player runs away, go back to chasing
        if (distSq > chaseStartSq || !this.mob.getSensing().hasLineOfSight(this.target)) {
            this.state = State.CHASE;
            this.mob.getNavigation().stop();
            return;
        }

        // we fully control movement here
        this.mob.getNavigation().stop();

        // 1) always look at the player while strafing
        faceTargetBody(this.target);

        // --- radial distance control using *horizontal* distance only ---
        double dx = this.target.getX() - this.mob.getX();
        double dz = this.target.getZ() - this.mob.getZ();
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        // NEW: quick swipe if inside attack zone and off cooldown
        if (horizDist <= ATTACK_RANGE && this.strafeSlashCooldown <= 0 && this.singleAttackLockTicks <= 0) {
            performFrontalSlash(0.75F, false);                       // 75% damage slash
            this.strafeSlashCooldown = reducedTickDelay(20);  // ~1 second cooldown
        }

        float forward = 0.0F;

        double tooClose = ATTACK_RANGE;         // 3 blocks
        double ideal     = CHASE_STOP_RANGE;    // 7 blocks (your current setting)
        double innerBand = ideal - 0.5;         // 6.5
        double outerBand = ideal + 0.5;         // 7.5

        if (horizDist < tooClose) {
            forward = -1.0F;
        } else if (horizDist < innerBand) {
            forward = -0.5F;
        } else if (horizDist > outerBand) {
            forward = 0.5F;
        }

        float right = this.strafeDirection > 0 ? 1.0F : -1.0F;

        if (this.mob.getMoveControl() instanceof ReaperSpiderMoveControl rmc) {
            rmc.setStrafe(forward, right, STRAFE_SPEED);
        } else {
            this.mob.getMoveControl().strafe(forward, right);
        }

        this.currentStrafeTicks++;

        Vec3 now = this.mob.position();
        double sx = now.x - this.strafeStartPos.x;
        double sz = now.z - this.strafeStartPos.z;
        double horizontalDist = Math.sqrt(sx * sx + sz * sz);

        boolean reachedDistance = horizontalDist >= STRAFE_DISTANCE;
        boolean timeout = this.currentStrafeTicks >= this.maxStrafeTicks;

        if (reachedDistance || timeout) {
            this.strafesDone++;
            if (this.strafesDone >= this.strafesToDo) {
                startAttackWindup();
            } else {
                startSingleStrafe();
            }
        }
    }


    private void startAttackWindup() {
        this.state = State.ATTACK_WINDUP;
        this.windupTickCounter = WINDUP_TICKS;
        // do NOT memorize here; we want to lock in right before the dash
        this.mob.getNavigation().stop();
    }

    private void tickAttackWindup() {
        if (this.windupTickCounter-- <= 0) {
            startAttackDash();
        }
    }

    private void startAttackDash() {
        this.state = State.ATTACK_DASH;

        // lock in target position at the *moment* the dash starts
        LivingEntity t = this.target;
        if (t != null && t.isAlive()) {
            this.memorizedTargetPos = t.position();
        } else if (this.memorizedTargetPos == null) {
            this.memorizedTargetPos = this.mob.position().add(this.mob.getForward().scale(1.0D));
        }

        Vec3 from = this.mob.position();
        Vec3 to = this.memorizedTargetPos;

        // dash along horizontal plane
        Vec3 horiz = new Vec3(to.x - from.x, 0.0D, to.z - from.z);
        if (horiz.lengthSqr() < 1.0E-4D) {
            float yawRad = this.mob.getYRot() * (Mth.PI / 180.0F);
            horiz = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        }

        this.dashDirection = horiz.normalize();
        this.dashStartPos = from;
        this.dashMaxTicks = DASH_TICKS;
        this.dashEnteredStrikeZone = false;

        this.mob.getNavigation().stop();

        this.mob.setFastClimb(true);
    }

    private void tickAttackDash() {
        if (this.dashDirection == null || this.dashStartPos == null) {
            startRecoverPhase();
            return;
        }

        // keep facing dash direction
        float yaw = (float) (Mth.atan2(this.dashDirection.z, this.dashDirection.x) * (180F / Math.PI)) - 90F;
        this.mob.setYRot(yaw);
        this.mob.yHeadRot = yaw;

        // apply motion
        Vec3 motion = this.dashDirection.scale(DASH_SPEED * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        this.mob.setDeltaMovement(motion.x, this.mob.getDeltaMovement().y, motion.z);

        // --- NEW: XZ strike-zone logic around memorizedTargetPos ---
        if (this.memorizedTargetPos != null) {
            // horizontal distance only (ignore Y)
            double dx = this.mob.getX() - this.memorizedTargetPos.x;
            double dz = this.mob.getZ() - this.memorizedTargetPos.z;
            double horizDistSq = dx * dx + dz * dz;

            double strikeRadius = ATTACK_RANGE; // same as your 3-block hit range
            double strikeRadiusSq = strikeRadius * strikeRadius;

            boolean inStrikeZone = horizDistSq <= strikeRadiusSq;

            // first time we enter the zone, flag it
            if (!this.dashEnteredStrikeZone && inStrikeZone) {
                this.dashEnteredStrikeZone = true;
            }

            // once we've entered and then *left* the zone again, attack at this spot
            if (this.dashEnteredStrikeZone && !inStrikeZone) {
                performFrontalSlash(1.0F, true); // full damage, heavy attack
                startRecoverPhase();
                return;
            }
        }

        // safety timeout: if something goes wrong, don't dash forever
        if (--this.dashMaxTicks <= 0) {
            performFrontalSlash(1.0F, true);
            startRecoverPhase();
        }
    }


    private void startRecoverPhase() {
        this.state = State.RECOVER;
        this.recoverTickCounter = RECOVER_TICKS;
        this.mob.setFastClimb(false);
        this.mob.getNavigation().stop();

        // only stop horizontal momentum if grounded
        if (this.mob.onGround()) {
            Vec3 current = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(0.0D, current.y, 0.0D); // keep Y
        }
    }

    private void tickRecover() {
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);

        this.recoverTickCounter--;
        if (this.recoverTickCounter <= 0) {
            this.state = State.CHASE;
            // prevent single (strafe) attack for ~2 seconds
            this.singleAttackLockTicks = SINGLE_ATTACK_LOCK_TICKS; // 2s at 20tps
        }
    }

    // main dash slash uses full damage
    private void performFrontalSlash() {
        performFrontalSlash(1.0F, true);
    }

    private void performFrontalSlash(float damageMultiplier, boolean heavyAttack) {
        if (this.target == null || !this.target.isAlive()) {
            return;
        }

        double range = ATTACK_RANGE;
        Vec3 forward = this.mob.getForward(); // normalized
        Vec3 origin = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);

        AABB box = this.mob.getBoundingBox()
                .inflate(range, 0.75D, range)
                .move(forward.scale(range * 0.5D));

        Predicate<LivingEntity> predicate = candidate ->
                candidate == this.target &&
                        candidate != this.mob &&
                        this.mob.canAttack(candidate);

        List<LivingEntity> hits = this.mob.level().getEntitiesOfClass(LivingEntity.class, box, predicate);

        for (LivingEntity hit : hits) {
            Vec3 toHit = hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D).subtract(origin);
            if (toHit.normalize().dot(forward) > 0.0D) {
                float baseDamage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage = baseDamage * damageMultiplier;

                hit.hurt(this.mob.damageSources().mobAttack(this.mob), damage);
                hit.push(forward.x * 0.5D, 0.1D, forward.z * 0.5D);

                if (hit instanceof Player player) {
                    var useStack = player.getUseItem();
                    if (!useStack.isEmpty() && useStack.getItem() instanceof ShieldItem) {
                        player.stopUsingItem();
                        int shieldDisableTicks = 120; // 6s
                        player.getCooldowns().addCooldown(useStack.getItem(), shieldDisableTicks);
                        player.level().broadcastEntityEvent(player, (byte) 30);
                    }
                }
            }
        }

        // particles
        if (this.mob.level() instanceof ServerLevel server) {
            int particleCount = heavyAttack ? 20 : 10;

            // choose particle by attack type
            var particleType = heavyAttack
                    ? ParticleTypes.SWEEP_ATTACK          // big slash
                    : ParticleTypes.CRIT;                  // lighter, still slashy-ish

            Vec3 sideVec = new Vec3(-forward.z, 0.0D, forward.x).normalize();

            for (int i = 0; i < particleCount; i++) {
                double t = this.mob.getRandom().nextDouble() * range;
                double sideOffset = (this.mob.getRandom().nextDouble() - 0.5D) * 1.5D;

                Vec3 p = origin
                        .add(forward.scale(t))
                        .add(sideVec.scale(sideOffset));

                server.sendParticles(
                        particleType,
                        p.x, p.y, p.z,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );
            }
        }
    }




    private void faceTargetBody(LivingEntity target) {
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();

        float targetYaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
        float currentYaw = mob.getYRot();
        float maxTurnPerTick = 30.0F;

        float newYaw = Mth.approachDegrees(currentYaw, targetYaw, maxTurnPerTick);

        mob.setYRot(newYaw);
        mob.setYHeadRot(newYaw);
        mob.yBodyRot = newYaw;
        mob.yBodyRotO = newYaw;
    }

}
