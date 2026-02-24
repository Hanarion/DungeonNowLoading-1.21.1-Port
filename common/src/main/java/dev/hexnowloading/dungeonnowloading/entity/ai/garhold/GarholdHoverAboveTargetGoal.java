package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GarholdHoverAboveTargetGoal extends Goal {

    private static final float LOCK_SPEED = 4.0F;
    private static final int DIVE_LOCK_TICKS = 10;

    private static final double SIDE_RANGE = 2.0;
    private static final double SIDE_RANGE_SQ = SIDE_RANGE * SIDE_RANGE;

    private static final double Y_LOCK_TOL = 0.75;

    private static final double VERTICAL_SWEEP_STEP = 0.25;

    private static final double SIDE_CAPTURE_FACE_DOT = 0.75; // ~41 degrees cone. 0.5=60deg, 0.9=25deg
    private static final double SIDE_CAPTURE_TRIGGER_XZ = 4.0;
    private static final double SIDE_CAPTURE_TRIGGER_XZ_SQ = SIDE_CAPTURE_TRIGGER_XZ * SIDE_CAPTURE_TRIGGER_XZ;

    private static final double SIDE_CAPTURE_TRIGGER_Y_TOL = 1.25;

    private static final int ABOVE_CLEAR_RELEASE_TICKS = 20 * 3;

    private static final double MAX_HOVER_HEIGHT = 6.0;
    private static final double MIN_HOVER_HEIGHT = 4.0;
    private static final double HOVER_HEIGHT_STEP = 1.0;

    private final GarholdEntity mob;

    private final double speed;

    private double lastTargetX;
    private double lastTargetZ;
    private double lastHorDistSq;

    private boolean locked = false;
    private int repathCooldown = 0;
    private int lockedTicks = 0;

    private boolean committedSideCapture = false;
    private int clearAboveTicks = 0;

    // Cached per-tick "above space" result
    private double bestHoverThisTick = MAX_HOVER_HEIGHT;
    private boolean aboveBlockedThisTick = false;

    // Optional debug/readback
    @SuppressWarnings("unused")
    private double effectiveHoverHeight = MAX_HOVER_HEIGHT;

    public GarholdHoverAboveTargetGoal(GarholdEntity mob,
                                       double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = mob.getTarget();
        return t != null && t.isAlive() && mob.getGarholdState() == GarholdState.FLYING;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = mob.getTarget();
        return t != null && t.isAlive() && mob.getGarholdState() == GarholdState.FLYING;
    }

    @Override
    public void start() {
        repathCooldown = 0;
        locked = false;
        lockedTicks = 0;

        committedSideCapture = false;
        clearAboveTicks = 0;

        aboveBlockedThisTick = false;
        bestHoverThisTick = MAX_HOVER_HEIGHT;
        effectiveHoverHeight = MAX_HOVER_HEIGHT;

        LivingEntity t = mob.getTarget();
        if (t != null) {
            lastTargetX = t.getX();
            lastTargetZ = t.getZ();
        }
        lastHorDistSq = Double.MAX_VALUE;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        // Cache hover selection once per tick: 6 -> 5 -> 4, else "blocked"
        double bestHover = findBestHoverHeight(target);
        aboveBlockedThisTick = bestHover < 0.0;
        bestHoverThisTick = aboveBlockedThisTick ? MAX_HOVER_HEIGHT : bestHover;
        effectiveHoverHeight = bestHoverThisTick; // optional debug

        boolean needsSide = aboveBlockedThisTick;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Immediate transition to SIDE_CAPTURE if we meet the "front + close + same Y" trigger.
        // (This can still happen even if above is clear; that's by design of your trigger.)
        if (canEnterSideCapture(target)) {
            mob.getNavigation().stop();
            mob.setDeltaMovement(Vec3.ZERO);
            mob.setGarholdState(GarholdState.SIDE_CAPTURE);
            mob.sideCaptureCooldownTicks = 10;
            return;
        }

        // Commit side capture while blocked; require sustained clearance to release the commit.
        if (needsSide) {
            committedSideCapture = true;
            clearAboveTicks = 0;
        } else if (committedSideCapture) {
            clearAboveTicks++;
            if (clearAboveTicks >= ABOVE_CLEAR_RELEASE_TICKS) {
                committedSideCapture = false;
                clearAboveTicks = 0;
            }
        }

        Desired desired = computeDesired(target);

        mob.getLookControl().setLookAt(target, 10.0F, 10.0F);

        double dx = desired.pos.x - mob.getX();
        double dz = desired.pos.z - mob.getZ();
        double dy = desired.pos.y - mob.getY();

        double horDistSq = dx * dx + dz * dz;

        double tdx = desired.pos.x - lastTargetX;
        double tdz = desired.pos.z - lastTargetZ;
        double desiredHorSpeed = Math.sqrt(tdx * tdx + tdz * tdz);

        lastTargetX = desired.pos.x;
        lastTargetZ = desired.pos.z;

        final double LOCK_DIST = 2.1;
        final double UNLOCK_DIST = 3.2;
        final double LOCK_DIST_SQ = LOCK_DIST * LOCK_DIST;
        final double UNLOCK_DIST_SQ = UNLOCK_DIST * UNLOCK_DIST;

        boolean gapIncreasing = horDistSq > lastHorDistSq + 0.02;
        boolean desiredMoving = desiredHorSpeed > 0.06;
        boolean tooFar = horDistSq >= UNLOCK_DIST_SQ;

        lastHorDistSq = horDistSq;

        if (desired.forcePath) {
            locked = false;
            tickPathfind(desired.pos.x, desired.pos.y, desired.pos.z, horDistSq, dy, LOCK_DIST_SQ);
            lockedTicks = 0;
            return;
        }

        if (!locked) {
            tickPathfind(desired.pos.x, desired.pos.y, desired.pos.z, horDistSq, dy, LOCK_DIST_SQ);
            lockedTicks = 0;
            return;
        }

        tickLock(dx, dy, dz);

        if (tryTriggerDive()) {
            return;
        }

        if (tooFar || (gapIncreasing && desiredMoving)) {
            locked = false;
            repathCooldown = 0;
        }
    }

    private boolean tryTriggerDive() {
        // Only dive when actually hovering-above (not committed side capture, and above isn't blocked this tick)
        boolean hoveringAbove = !(committedSideCapture || aboveBlockedThisTick);

        if (locked && hoveringAbove) {
            lockedTicks++;
            if (lockedTicks >= DIVE_LOCK_TICKS) {
                mob.getNavigation().stop();

                locked = false;
                repathCooldown = 0;
                lockedTicks = 0;

                mob.setGarholdState(GarholdState.DIVE);
                return true;
            }
        } else {
            lockedTicks = 0;
        }

        return false;
    }

    private Desired computeDesired(LivingEntity target) {
        boolean wantsSide = committedSideCapture || aboveBlockedThisTick;

        if (!wantsSide) {
            // Hover directly above target using bestHoverThisTick (6->5->4)
            return Desired.lockOk(new Vec3(
                    target.getX(),
                    target.getY() + bestHoverThisTick,
                    target.getZ()
            ));
        }

        // --- existing side behavior below ---
        double px = target.getX();
        double pz = target.getZ();

        double dx = mob.getX() - px;
        double dz = mob.getZ() - pz;
        double distSq = dx * dx + dz * dz;

        if (distSq <= SIDE_RANGE_SQ) {
            double targetY = target.getY();
            double dy = targetY - mob.getY();

            if (Math.abs(dy) <= Y_LOCK_TOL) {
                Vec3 pos = new Vec3(mob.getX(), targetY, mob.getZ());

                // When we're trying to sweep vertically at our current XZ to match targetY,
                // ensure the swept volume is clear; otherwise force pathing.
                if (!isVerticalSweepClearForAabb(targetY)) {
                    return Desired.forcePath(pos);
                }

                return Desired.lockOk(pos);
            }

            Vec3 ring = computeSideRingPoint(target, SIDE_RANGE);
            return Desired.forcePath(ring);
        }

        Vec3 ring = computeSideRingPoint(target, SIDE_RANGE);
        return Desired.lockOk(ring);
    }

    /**
     * Returns a hover height in [MAX_HOVER_HEIGHT..MIN_HOVER_HEIGHT] if there's enough vertical clearance
     * at the target's XZ from y0 -> targetY+height, else returns -1.
     */
    private double findBestHoverHeight(LivingEntity target) {
        double x = target.getX();
        double z = target.getZ();

        // Start slightly above the player's feet (your original choice)
        double y0 = target.getY() + 1.1;

        for (double h = MAX_HOVER_HEIGHT; h >= MIN_HOVER_HEIGHT; h -= HOVER_HEIGHT_STEP) {
            double y1 = target.getY() + h;
            if (isVerticalSweepClearForAabbAtXZ(x, z, y0, y1)) {
                return h;
            }
        }

        return -1.0;
    }

    private Vec3 computeSideRingPoint(LivingEntity target, double range) {
        double px = target.getX();
        double pz = target.getZ();

        double dx = mob.getX() - px;
        double dz = mob.getZ() - pz;

        double distSq = dx * dx + dz * dz;
        if (distSq < 1.0e-6) {
            dx = 1.0;
            dz = 0.0;
            distSq = 1.0;
        }

        double dist = Math.sqrt(distSq);
        double nx = dx / dist;
        double nz = dz / dist;

        return new Vec3(px + nx * range, target.getY(), pz + nz * range);
    }

    private boolean isVerticalSweepClearForAabb(double targetY) {
        return isVerticalSweepClearForAabbAtXZ(mob.getX(), mob.getZ(), mob.getY(), targetY);
    }

    private boolean isVerticalSweepClearForAabbAtXZ(double x, double z, double yA, double yB) {
        Level level = mob.level();

        AABB cur = mob.getBoundingBox().inflate(-0.3, 0.0, -0.3);
        double halfX = (cur.maxX - cur.minX) * 0.5;
        double halfZ = (cur.maxZ - cur.minZ) * 0.5;
        double height = (cur.maxY - cur.minY);

        AABB base = new AABB(
                x - halfX, yA, z - halfZ,
                x + halfX, yA + height, z + halfZ
        );

        if (Math.abs(yB - yA) < 1.0e-4) {
            return level.noCollision(mob, base);
        }

        double step = VERTICAL_SWEEP_STEP * Math.signum(yB - yA);
        double y = yA;

        while ((step > 0 && y < yB) || (step < 0 && y > yB)) {
            double nextY = y + step;
            if ((step > 0 && nextY > yB) || (step < 0 && nextY < yB)) nextY = yB;

            AABB moved = base.move(0.0, nextY - yA, 0.0);
            if (!level.noCollision(mob, moved)) return false;

            y = nextY;
        }

        return true;
    }

    private void tickPathfind(double x, double y, double z, double horDistSq, double dy, double lockDistSq) {
        if (--repathCooldown <= 0) {
            repathCooldown = 1; // you can increase this if you want less spammy moveTo
            mob.getNavigation().moveTo(x, y, z, speed);
        }

        if (horDistSq <= lockDistSq && Math.abs(dy) <= Y_LOCK_TOL) {
            locked = true;
            mob.getNavigation().stop();
        }
    }

    @SuppressWarnings("unused")
    private static float rotlerp(float from, float to, float maxTurn) {
        float d = Mth.wrapDegrees(to - from);
        if (d >  maxTurn) d =  maxTurn;
        if (d < -maxTurn) d = -maxTurn;
        return from + d;
    }

    private void tickLock(double dx, double dy, double dz) {
        mob.getNavigation().stop();
        mob.setNoGravity(true);

        float fly = (float) mob.getAttributeValue(Attributes.FLYING_SPEED);
        double vmax = this.speed * fly * LOCK_SPEED; // max correction speed

        Vec3 to = new Vec3(dx, dy, dz);
        double dist = to.length();

        if (dist < 0.08) { // deadzone
            mob.setDeltaMovement(mob.getDeltaMovement().scale(0.2));
            return;
        }

        // desired speed shrinks as we get close -> no overshoot, no orbit
        double v = Math.min(vmax, dist * 0.95);
        Vec3 desiredVel = to.scale(v / dist);

        Vec3 cur = mob.getDeltaMovement();
        mob.setDeltaMovement(
                Mth.lerp(0.25, cur.x, desiredVel.x),
                Mth.lerp(0.25, cur.y, desiredVel.y),
                Mth.lerp(0.25, cur.z, desiredVel.z)
        );
    }

    private record Desired(Vec3 pos, boolean forcePath) {
        static Desired lockOk(Vec3 pos) { return new Desired(pos, false); }
        static Desired forcePath(Vec3 pos) { return new Desired(pos, true); }
    }

    private boolean isFacingTarget(LivingEntity target) {
        Vec3 to = target.position().subtract(mob.position());
        Vec3 flatTo = new Vec3(to.x, 0.0, to.z);
        if (flatTo.lengthSqr() < 1.0e-6) return true;

        Vec3 look = mob.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0.0, look.z);
        if (flatLook.lengthSqr() < 1.0e-6) return false;

        flatTo = flatTo.normalize();
        flatLook = flatLook.normalize();
        return flatLook.dot(flatTo) >= SIDE_CAPTURE_FACE_DOT;
    }

    private boolean canEnterSideCapture(LivingEntity target) {
        if (mob.getGarholdState() != GarholdState.FLYING) return false; // only from flying
        if (mob.sideCaptureCooldownTicks > 0) return false;

        // XZ range <= 4 blocks
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();
        double xzDistSq = dx * dx + dz * dz;
        if (xzDistSq > SIDE_CAPTURE_TRIGGER_XZ_SQ) return false;

        // Y must be within +/- 1.25 blocks of target feet Y
        double dy = target.getY() - mob.getY();
        if (Math.abs(dy) > SIDE_CAPTURE_TRIGGER_Y_TOL) return false;

        // Must be facing the target
        return isFacingTarget(target);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}