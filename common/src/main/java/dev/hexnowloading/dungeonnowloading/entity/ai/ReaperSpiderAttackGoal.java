package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.ReaperSpiderMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
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
    private static final double STRAFE_SPEED = 0.6D;
    private static final double STRAFE_DISTANCE = 10.0D;
    private static final double DASH_SPEED = 1.8D;

    private static final double CHASE_START_RANGE = 10.0D;
    private static final double CHASE_STOP_RANGE = 7.0D;
    private static final double ATTACK_RANGE = 1.5D;


    private static final int STRAFE_TO_DO  = 4;
    private static final int STRAFE_TO_DO_ADD_RANDOM = 3;
    private static final int STRAFE_TICKS  = reducedTickDelay(10);
    private static final int STRAFE_TICKS_ADD_RANDOM = reducedTickDelay(6);
    private static final int STRAFE_SLASH_COOLDOWN = reducedTickDelay(20);
    private static final int WINDUP_TICKS  = reducedTickDelay(10);  // 1 second
    private static final int DASH_TICKS    = reducedTickDelay(60);  // base dash cap
    private static final int RECOVER_TICKS = reducedTickDelay(20);  // 2 seconds stop
    private static final int SINGLE_ATTACK_LOCK_TICKS = reducedTickDelay(10);

    private State state = State.CHASE;
    private LivingEntity target;

    // dash
    private Vec3 dashStartPos;
    private int dashMaxTicks;
    private Vec3 memorizedTargetPos;
    private Vec3 dashDirection;
    private boolean dashEnteredStrikeZone;
    private boolean dashHasAttacked;

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

        if (tgt instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return false;
            }
        }

        if (!this.mob.canAttack(tgt)) {
            return false;
        }

        this.target = tgt;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        if (this.target instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return false;
            }
        }

        if (!this.mob.canAttack(this.target)) {
            return false;
        }

        return true;
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

        if (this.strafeSlashCooldown > 0) {
            this.strafeSlashCooldown--;
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
        if (this.state == State.RECOVER) {
            return;
        }

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
        this.strafesToDo = STRAFE_TO_DO + this.mob.getRandom().nextInt(STRAFE_TO_DO_ADD_RANDOM); // 2–3 side steps

        // clear any existing "chase" path by navigating to self once
        this.mob.getNavigation().moveTo(this.mob, 0.0D);

        startSingleStrafe();
    }

    private void startSingleStrafe() {
        this.currentStrafeTicks = 0;
        this.maxStrafeTicks = STRAFE_TICKS + this.mob.getRandom().nextInt(STRAFE_TICKS_ADD_RANDOM); // cap ~6 ticks; tuned vs STRAFE_SPEED
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

        if (this.strafeSlashCooldown <= 0
                && this.singleAttackLockTicks <= 0
                && isTargetInFrontalSlashRange()
                && !this.mob.isAttackAnimationRunning()) {

            this.mob.playSingleSlashAnimation(() -> performFrontalSlash(0.75F, false));
            this.strafeSlashCooldown = STRAFE_SLASH_COOLDOWN;
        }

        float forward;

        double tooClose = 2.5;
        double ideal     = CHASE_STOP_RANGE;
        double innerBand = ideal - 0.5;
        double outerBand = ideal + 0.5;

        if (horizDist < tooClose) {
            forward = -0.5F;
        } else if (horizDist < innerBand) {
            forward = -0.5F;
        } else if (horizDist > outerBand) {
            forward = 0.5F;
        } else {
            forward = 0.0F;
        }

        float right = this.strafeDirection > 0 ? 1.0F : -1.0F;

        double speed = STRAFE_SPEED;

        if (this.mob.getMoveControl() instanceof ReaperSpiderMoveControl rmc) {
            rmc.setStrafe(forward, right, speed);
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
                if (!this.mob.isAttackAnimationRunning()) {
                    startAttackWindup();
                } else {
                    this.strafesDone--;

                }
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
        this.mob.playWindUpAnimation();
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
        this.dashHasAttacked = false; // NEW

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

        // --- NEW: immediate frontal slash if target is in slash range during dash ---
        if (!this.dashHasAttacked && isTargetInFrontalSlashRange() && !this.mob.isAttackAnimationRunning()) {
            this.mob.playDoubleSlashAnimation(() -> performFrontalSlash(1.0F, true));
            this.dashHasAttacked = true;
            startRecoverPhase();
            return;
        }

        // --- existing XZ strike-zone logic around memorizedTargetPos ---
        if (this.memorizedTargetPos != null) {
            double dx = this.mob.getX() - this.memorizedTargetPos.x;
            double dz = this.mob.getZ() - this.memorizedTargetPos.z;
            double horizDistSq = dx * dx + dz * dz;

            double strikeRadius = ATTACK_RANGE;
            double strikeRadiusSq = strikeRadius * strikeRadius;

            boolean inStrikeZone = horizDistSq <= strikeRadiusSq;

            if (!this.dashEnteredStrikeZone && inStrikeZone) {
                this.dashEnteredStrikeZone = true;
            }

            if (this.dashEnteredStrikeZone && !inStrikeZone && !this.mob.isAttackAnimationRunning()) {
                this.mob.playDoubleSlashAnimation(() -> performFrontalSlash(1.0F, true));
                this.dashHasAttacked = true;     // ensure we mark it
                startRecoverPhase();
                return;
            }
        }

        // safety timeout: if something goes wrong, don't dash forever
        if (--this.dashMaxTicks <= 0 && !this.mob.isAttackAnimationRunning()) {
            this.mob.playDoubleSlashAnimation(() -> performFrontalSlash(1.0F, true));
            this.dashHasAttacked = true;
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

    private boolean isTargetInFrontalSlashRange() {
        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        double range = ATTACK_RANGE;
        Vec3 forward = this.mob.getForward(); // normalized-ish
        Vec3 origin = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);

        AABB box = this.mob.getBoundingBox()
                .inflate(range, 0.75D, range)
                .move(forward.scale(range * 0.5D));

        // quick reject if bounding boxes don't intersect
        if (!box.intersects(this.target.getBoundingBox())) {
            return false;
        }

        Vec3 toHit = this.target.position()
                .add(0.0D, this.target.getBbHeight() * 0.5D, 0.0D)
                .subtract(origin);

        // must be in front of the spider
        return toHit.normalize().dot(forward) > 0.0D;
    }


    private void performFrontalSlash(float damageMultiplier, boolean heavyAttack) {
        double range = ATTACK_RANGE;
        Vec3 forward = this.mob.getForward(); // normalized-ish
        Vec3 origin = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);

        // Hit box in front of the mob
        AABB box = this.mob.getBoundingBox()
                .inflate(range, 0.75D, range)
                .move(forward.scale(range * 0.5D));

        // We only care about players (all of them in range)
        List<Player> hits = this.mob.level().getEntitiesOfClass(
                Player.class,
                box,
                this.mob::canAttack
        );

        for (Player hit : hits) {
            // still enforce "in front of me", not behind
            Vec3 toHit = hit.position()
                    .add(0.0D, hit.getBbHeight() * 0.5D, 0.0D)
                    .subtract(origin);

            if (toHit.normalize().dot(forward) > 0.0D) {
                float baseDamage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage = baseDamage * damageMultiplier;

                hit.hurt(this.mob.damageSources().mobAttack(this.mob), damage);
                hit.push(forward.x * 0.5D, 0.1D, forward.z * 0.5D);

                // shield break stays the same
                var useStack = hit.getUseItem();
                if (!useStack.isEmpty() && useStack.getItem() instanceof ShieldItem) {
                    hit.stopUsingItem();
                    int shieldDisableTicks = 120; // 6s
                    hit.getCooldowns().addCooldown(useStack.getItem(), shieldDisableTicks);
                    hit.level().broadcastEntityEvent(hit, (byte) 30);
                }
            }
        }

        // particles unchanged if you re-enable them
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
