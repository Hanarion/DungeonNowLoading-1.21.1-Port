package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GarholdHoverAboveTargetGoal extends Goal {

    private static final float LOCK_SPEED = 0.07F;
    private static final int DIVE_LOCK_TICKS = reducedTickDelay(40); // 2 seconds

    private static final double SIDE_RANGE = 2.0;
    private static final double SIDE_RANGE_SQ = SIDE_RANGE * SIDE_RANGE;

    private static final double Y_LOCK_TOL = 0.75;

    private static final double VERTICAL_SWEEP_STEP = 0.25;


    private final GarholdEntity mob;

    private final double speed;
    private final double hoverHeight;

    private double lastTargetX;
    private double lastTargetZ;
    private double lastHorDistSq;

    @SuppressWarnings("unused")
    private final double minHorDist;
    @SuppressWarnings("unused")
    private final double maxHorDist;

    private boolean locked = false;
    private int repathCooldown = 0;
    private int lockedTicks = 0;

    public GarholdHoverAboveTargetGoal(GarholdEntity mob,
                                       double speed,
                                       double hoverHeight,
                                       double minHorDist,
                                       double maxHorDist) {
        this.mob = mob;
        this.speed = speed;
        this.hoverHeight = hoverHeight;
        this.minHorDist = minHorDist;
        this.maxHorDist = maxHorDist;
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

        Desired desired = computeDesired(target);

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

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

        if (tryTriggerDive(target)) {
            return;
        }

        if (tooFar || (gapIncreasing && desiredMoving)) {
            locked = false;
            repathCooldown = 0;
        }

    }

    private boolean tryTriggerDive(LivingEntity target) {
        // Only count time when we're truly locked and we're in hover-above mode (not side capture)
        boolean hoveringAbove = !shouldSideCapture(target);

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
        if (!shouldSideCapture(target)) {
            return Desired.lockOk(new Vec3(target.getX(), target.getY() + hoverHeight, target.getZ()));
        }

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

    // Side capture if the vertical space from playerY -> hoverY at player's XZ is blocked for Garhold's full hitbox.
    private boolean shouldSideCapture(LivingEntity target) {
        double x = target.getX();
        double z = target.getZ();

        double y0 = target.getY();
        double y1 = target.getY() + hoverHeight;

        return !isVerticalSweepClearForAabbAtXZ(x, z, y0, y1);
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

        AABB cur = mob.getBoundingBox();
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
            repathCooldown = 5;
            mob.getNavigation().moveTo(x, y, z, speed);
        }

        if (horDistSq <= lockDistSq && Math.abs(dy) <= Y_LOCK_TOL) {
            locked = true;
            mob.getNavigation().stop();
        }
    }

    private void tickLock(double dx, double dy, double dz) {
        mob.getNavigation().stop();

        double maxXZVel = LOCK_SPEED * speed;
        double xzAccel = maxXZVel;

        double maxYVel = LOCK_SPEED * speed;
        double yAccel = maxYVel * 0.5;

        double xzDamping = 0.55;
        double yDamping = 0.70;

        Vec3 vel = mob.getDeltaMovement();

        double addX = Mth.clamp(dx * xzAccel, -maxXZVel, maxXZVel);
        double addZ = Mth.clamp(dz * xzAccel, -maxXZVel, maxXZVel);
        double addY = Mth.clamp(dy * yAccel, -maxYVel, maxYVel);

        mob.setDeltaMovement(
                vel.x * xzDamping + addX,
                vel.y * yDamping + addY,
                vel.z * xzDamping + addZ
        );
    }

    private record Desired(Vec3 pos, boolean forcePath) {
        static Desired lockOk(Vec3 pos) { return new Desired(pos, false); }
        static Desired forcePath(Vec3 pos) { return new Desired(pos, true); }
    }
}
