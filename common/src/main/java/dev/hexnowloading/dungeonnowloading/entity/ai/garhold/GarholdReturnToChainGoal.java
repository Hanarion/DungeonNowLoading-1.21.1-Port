package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GarholdReturnToChainGoal extends Goal {

    private final GarholdEntity mob;

    // 47x7x47 scan
    private static final int RADIUS_XZ = 23;
    private static final int RADIUS_Y = 3;

    private static final int BELOW_CHAIN = 3;

    private static final float LOCK_SPEED = 0.07F;
    private static final double LOCK_DIST = 2.0;

    private static final int VALIDATE_INTERVAL = 10;

    private int validateCooldown = 0;
    private int repathCooldown = 0;
    private BlockPos chainBottom = null;
    private boolean locked = false;

    public GarholdReturnToChainGoal(GarholdEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(mob.isGarholdState(GarholdState.FLYING))) return false;
        if (mob.getTarget() != null) return false;

        this.chainBottom = findNearestChainBottom(mob.level(), mob.blockPosition());
        return this.chainBottom != null;
    }

    @Override
    public boolean canContinueToUse() {
        return (mob.isGarholdState(GarholdState.FLYING)) && mob.getTarget() == null && chainBottom != null;
    }

    @Override
    public void start() {
        repathCooldown = 0;
        validateCooldown = 0;
        locked = false;
    }

    @Override
    public void tick() {
        if (chainBottom == null) return;

        if (--validateCooldown <= 0) {
            validateCooldown = VALIDATE_INTERVAL;

            if (!isTargetStillValid(chainBottom)) {
                reacquireTarget();
                if (chainBottom == null) {
                    // nothing to return to
                    stop();
                    return;
                }
            }
        }

        double tx = chainBottom.getX() + 0.5;
        double ty = (chainBottom.getY() - BELOW_CHAIN) + 0.5;
        double tz = chainBottom.getZ() + 0.5;

        mob.getLookControl().setLookAt(tx, ty, tz);

        double dx = tx - mob.getX();
        double dz = tz - mob.getZ();
        double dy = ty - mob.getY();

        double distSq = dx * dx + dy * dy + dz * dz;
        double lockDistSq = LOCK_DIST * LOCK_DIST;

        if (!locked) {
            tickPathfind(tx, ty, tz, distSq, lockDistSq);
            return;
        }

        tickLock(dx, dy, dz);

        if (distSq <= 0.02) { // now finish is also 3D
            mob.setGarholdState(GarholdState.ATTACHING);
            mob.setDeltaMovement(Vec3.ZERO);
            mob.getNavigation().stop();
            this.chainBottom = null;
            return;
        }

// drift reset (also 3D)
        if (distSq > lockDistSq * 2.25) {
            locked = false;
            repathCooldown = 0;
        }
    }

    private void tickPathfind(double x, double y, double z, double distSq, double lockDistSq) {
        if (--repathCooldown <= 0) {
            repathCooldown = 5;
            mob.getNavigation().moveTo(x, y, z, 1.2);
        }

        if (distSq <= lockDistSq) {
            locked = true;
            mob.getNavigation().stop();
        }
    }

    private void tickLock(double dx, double dy, double dz) {
        mob.getNavigation().stop();

        double maxXZVel = LOCK_SPEED * 1.2; // 1.2 = your nav speed, keep consistent
        double xzAccel = maxXZVel;

        double maxYVel = LOCK_SPEED * 1.2;
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

        mob.hasImpulse = true;
    }



    @Override
    public void stop() {
        chainBottom = null;
        mob.getNavigation().stop();
    }

    private BlockPos findNearestChainBottom(Level level, BlockPos center) {
        BlockPos best = null;
        double bestDist2 = Double.MAX_VALUE;

        for (int dy = -RADIUS_Y; dy <= RADIUS_Y; dy++) {
            int y = center.getY() + dy;

            for (int dx = -RADIUS_XZ; dx <= RADIUS_XZ; dx++) {
                int x = center.getX() + dx;

                for (int dz = -RADIUS_XZ; dz <= RADIUS_XZ; dz++) {
                    int z = center.getZ() + dz;

                    BlockPos p = new BlockPos(x, y, z);
                    if (!mob.isChainBlock(p)) continue;

                    BlockPos bottom = findBottomOfChainColumn(p);

                    // Desired destination point (centered) = 3 blocks below bottom chain
                    double tx = bottom.getX() + 0.5;
                    double ty = (bottom.getY() - BELOW_CHAIN) + 0.5;
                    double tz = bottom.getZ() + 0.5;

                    // Check if Garhold's hitbox would fit there (blocks only)
                    if (!canFitAt(tx, ty, tz)) continue;

                    if (isOccupiedByEntity(tx, ty, tz)) continue;

                    BlockPos destPos = BlockPos.containing(tx, ty, tz);

                    if (!mob.level().getFluidState(destPos).isEmpty()) continue;


                    double d2 = mob.distanceToSqr(tx, ty, tz);
                    if (d2 < bestDist2) {
                        bestDist2 = d2;
                        best = bottom;
                    }
                }
            }
        }

        return best;
    }

    private BlockPos findBottomOfChainColumn(BlockPos anyChainBlock) {
        BlockPos.MutableBlockPos cur = anyChainBlock.mutable();
        int safety = 128;

        while (safety-- > 0) {
            BlockPos below = cur.below();
            if (!mob.isChainBlock(below)) break;
            cur.set(below);
        }
        return cur.immutable();
    }

    private boolean canFitAt(double x, double y, double z) {
        // Build Garhold’s bounding box as if it were positioned at (x,y,z)
        AABB box = mob.getBoundingBox().move(x - mob.getX(), y - mob.getY(), z - mob.getZ());

        // Blocks-only collision check (recommended for "can I occupy this spot")
        return mob.level().noCollision(mob, box);
    }

    private boolean isTargetStillValid(BlockPos bottom) {
        if (bottom == null) return false;

        // 1) chain no longer exists at (or above) bottom
        if (!mob.isChainBlock(bottom)) return false;

        // destination = 3 blocks below bottom chain
        double tx = bottom.getX() + 0.5;
        double ty = (bottom.getY() - BELOW_CHAIN) + 0.5;
        double tz = bottom.getZ() + 0.5;

        // 2) no space below chain (blocks collision)
        if (!canFitAt(tx, ty, tz)) return false;

        // 3) already occupied by another entity (including other Garhold)
        if (isOccupiedByEntity(tx, ty, tz)) return false;

        // 4) path is blocked / impossible right now (navigation can't make a path)
        // This is a *soft* invalidation: only fail if we are far and path is null.
        if (mob.distanceToSqr(tx, ty, tz) > 4.0) { // only care when not basically there
            var path = mob.getNavigation().createPath(tx, ty, tz, 0);
            if (path == null || !path.canReach()) return false;
        }

        return true;
    }

    private boolean isOccupiedByEntity(double x, double y, double z) {
        // Use Garhold's box at the target spot and see if it intersects other entities' boxes
        AABB targetBox = mob.getBoundingBox().move(x - mob.getX(), y - mob.getY(), z - mob.getZ());

        // ignore self; if anything else intersects, consider occupied
        return !mob.level().getEntities(mob, targetBox.inflate(0.05),
                e -> e.isAlive() && e.isPickable() && e.getBoundingBox().intersects(targetBox)
        ).isEmpty();
    }

    private void reacquireTarget() {
        this.chainBottom = findNearestChainBottom(mob.level(), mob.blockPosition());
        this.locked = false;
        this.repathCooldown = 0;
        mob.getNavigation().stop();
    }
}
