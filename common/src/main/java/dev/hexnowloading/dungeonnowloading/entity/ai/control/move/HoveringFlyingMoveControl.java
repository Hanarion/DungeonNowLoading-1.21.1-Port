package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class HoveringFlyingMoveControl extends MoveControl {
    private static final int RANGE = 7;        // horizontal range
    private static final int V_RANGE = 3;      // vertical +/- range
    private static final int MAX_ATTEMPTS = 20;
    private static final int SIDE_RAYS = 2;    // center + 2 side rays each axis (lightweight)

    private static final double ARRIVE_EPS = 0.25;     // stop within ~0.5 blocks
    private static final double BASE_ACCEL = 0.06;     // base acceleration per tick
    private static final float  MAX_YAW_CHANGE = 90.0f;// max yaw change per tick
    private static final double IDLE_DAMP = 0.9;

    private final Mob mob;
    private final Level level;
    private final RandomSource rng;

    public HoveringFlyingMoveControl(Mob mob) {
        super(mob);
        this.mob = mob;
        this.level = mob.level();
        this.rng = mob.getRandom();
    }

    /**
     * Picks a random reachable point within RANGE and sets it as the wanted position.
     * Call this from your goal (you already do) to make it wander intelligently.
     */
    public void setWantedPosition() {

        // 1. If touching the ground, rise upwards a bit
        if (mob.onGround() || isTouchingGround()) {
            Vec3 up = mob.position().add(0, 2 + rng.nextInt(2), 0); // +2 to +3 blocks up
            this.setWantedPosition(up.x, up.y, up.z, 1.0);
            return;
        }

        // 2. Prefer moving relative to target, if one exists
        LivingEntity target = mob.getTarget();
        if (target != null) {
            double distSq = mob.distanceToSqr(target);
            double FAR_RANGE_SQ = 16.0 * 16.0;
            double NEAR_RANGE_SQ = 8.0 * 8.0;

            // 2a. If far away (>16 blocks) → move toward the player
            if (distSq > FAR_RANGE_SQ) {
                Vec3 basePos = mob.position();
                Vec3 toward = target.position()
                        .subtract(basePos)
                        .normalize()
                        .scale(6.0)           // move about 6 blocks toward the player
                        .add(basePos);

                if (pathIsClear(basePos, toward) && noCollisionAt(toward)) {
                    this.setWantedPosition(toward.x, toward.y, toward.z, 1.0);
                    return;
                }
            }
            // 2b. If too close (<8 blocks) → move away from the player
            else if (distSq < NEAR_RANGE_SQ) {
                Vec3 basePos = mob.position();

                // Direction away from player
                Vec3 awayDir = basePos.subtract(target.position());
                if (awayDir.lengthSqr() < 1.0E-4) {
                    // fallback if somehow same position
                    awayDir = new Vec3(1, 0, 0);
                }
                awayDir = awayDir.normalize();

                // Add a little sideways jitter so it doesn't move in a perfectly straight line
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 side = awayDir.cross(up);
                if (side.lengthSqr() < 1.0E-4) {
                    side = new Vec3(0, 0, 1);
                }
                side = side.normalize();

                double forwardDist = 4.0 + rng.nextDouble() * 4.0;     // 4–8 blocks away
                double sideOffset  = (rng.nextDouble() - 0.5) * 4.0;   // -2 to +2 blocks sideways
                int   verticalJitter = rng.nextInt(-1, 2);             // -1 to +1 Y

                Vec3 candidate = basePos
                        .add(awayDir.scale(forwardDist))
                        .add(side.scale(sideOffset))
                        .add(0, verticalJitter, 0);

                if (pathIsClear(basePos, candidate) && noCollisionAt(candidate)) {
                    this.setWantedPosition(candidate.x, candidate.y, candidate.z, 1.0);
                    return;
                }
            }
        }

        // 3. Otherwise use natural wandering
        Vec3 targetPos = pickRandomReachableTarget();
        if (targetPos != null) {
            double speed = Mth.clamp(mob.getAttributeValue(Attributes.FLYING_SPEED), 0.5D, 1.5D);
            this.setWantedPosition(targetPos.x, targetPos.y, targetPos.z, speed);
        }
    }


    private boolean isTouchingGround() {
        BlockPos below = mob.blockPosition().below();
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }


    @Nullable
    private Vec3 pickRandomReachableTarget() {
        Vec3 base = mob.position();
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int dx = rng.nextInt(-RANGE, RANGE + 1);
            int dz = rng.nextInt(-RANGE, RANGE + 1);
            int dy = rng.nextInt(-V_RANGE, V_RANGE + 1);

            // Keep it inside the cube and avoid zero-length
            if (dx == 0 && dy == 0 && dz == 0) continue;

            BlockPos pos = BlockPos.containing(base.x + dx, base.y + dy, base.z + dz);
            if (!isAirLikeAndFree(pos)) continue;

            // Center of block is a good spot for small flyers
            Vec3 to = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            if (pathIsClear(base, to)) {
                // Also ensure at the destination our AABB won’t collide
                if (noCollisionAt(to)) {
                    return to;
                }
            }
        }
        return null;
    }

    private boolean isAirLikeAndFree(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getFluidState().isEmpty()) return false; // reject water/lava
        if (!state.getCollisionShape(level, pos).isEmpty()) return false; // must be non-solid (air/grass/flowers OK)
        // Also avoid spawning inside world border or below min build height
        if (!level.getWorldBorder().isWithinBounds(pos)) return false;
        return level.isInWorldBounds(pos);
    }

    private boolean noCollisionAt(Vec3 to) {
        AABB box = mob.getDimensions(mob.getPose()).makeBoundingBox(to.x, to.y, to.z);
        return level.noCollision(mob, box);
    }

    /**
     * Uses a central ray + a couple of side rays (offset by entity radius) to ensure no solid blocks
     * obstruct the straight line flight path.
     */
    private boolean pathIsClear(Vec3 from, Vec3 to) {
        // Center ray
        if (rayHitsBlock(from, to)) return false;

        // Side rays to approximate thickness
        double r = Math.max(0.15, mob.getBbWidth() * 0.35); // small radius for narrow flyers
        Vec3 dir = to.subtract(from).normalize();

        // Build two roughly perpendicular vectors for offsets
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 sideA = dir.cross(up);
        if (sideA.lengthSqr() < 1e-4) sideA = new Vec3(1, 0, 0); // fallback if dir nearly vertical
        sideA = sideA.normalize();
        Vec3 sideB = dir.cross(sideA).normalize();

        for (int i = 1; i <= SIDE_RAYS; i++) {
            double off = r * i;
            if (rayHitsBlock(from.add(sideA.scale(off)), to.add(sideA.scale(off)))) return false;
            if (rayHitsBlock(from.add(sideA.scale(-off)), to.add(sideA.scale(-off)))) return false;
            if (rayHitsBlock(from.add(sideB.scale(off)), to.add(sideB.scale(off)))) return false;
            if (rayHitsBlock(from.add(sideB.scale(-off)), to.add(sideB.scale(-off)))) return false;
        }
        return true;
    }

    private boolean rayHitsBlock(Vec3 from, Vec3 to) {
        HitResult hit = level.clip(new ClipContext(
                from, to,
                ClipContext.Block.COLLIDER,   // collide with solid blocks
                ClipContext.Fluid.NONE,       // ignore fluids
                mob
        ));
        return hit.getType() != HitResult.Type.MISS;
    }

    @Override
    public void tick() {
        // When we have no move target, gently damp and keep facing velocity
        if (this.operation != Operation.MOVE_TO) {
            Vec3 v = mob.getDeltaMovement();
            if (!v.equals(Vec3.ZERO)) {
                mob.setDeltaMovement(v.scale(IDLE_DAMP));
                faceVelocity();
            }
            return;
        }

        // Vector to target
        double dx = this.wantedX - mob.getX();
        double dy = this.wantedY - mob.getY();
        double dz = this.wantedZ - mob.getZ();
        double distSq = dx*dx + dy*dy + dz*dz;

        // Arrived?
        if (distSq < ARRIVE_EPS * ARRIVE_EPS) {
            this.operation = Operation.WAIT;
            return;
        }

        double dist = Math.sqrt(distSq);
        // Direction unit vector
        double ux = dx / dist;
        double uy = dy / dist;
        double uz = dz / dist;

        // Speed & acceleration
        double speedAttr = mob.getAttributeValue(Attributes.FLYING_SPEED); // usually ~0.75
        double accel = BASE_ACCEL * this.speedModifier * Mth.clamp(speedAttr, 0.4, 2.0);

        // Accelerate toward target
        Vec3 vel = mob.getDeltaMovement().add(ux * accel, uy * accel, uz * accel);

        // Clamp top speed to something sane relative to attribute (prevents crazy overshoot)
        double maxSpeed = Math.max(0.2, speedAttr * 1.5);
        double vLen = vel.length();
        if (vLen > maxSpeed) {
            vel = vel.scale(maxSpeed / vLen);
        }

        mob.setDeltaMovement(vel);

        // Rotate to face movement
        faceVelocity();

        // Keep moving until we arrive; do NOT reset operation here
        // (vanilla FlyingMoveControl keeps MOVE_TO until close enough)
    }

    private void faceVelocity() {
        Vec3 v = mob.getDeltaMovement();
        if (v.lengthSqr() > 1.0E-4) {
            float targetYaw = (float)(Mth.atan2(v.z, v.x) * (180.0 / Math.PI)) - 90.0F;
            mob.setYRot(rotlerp(mob.getYRot(), targetYaw, MAX_YAW_CHANGE));
            mob.yBodyRot = mob.getYRot();
            mob.yHeadRot = mob.getYRot();
        }
    }
}
