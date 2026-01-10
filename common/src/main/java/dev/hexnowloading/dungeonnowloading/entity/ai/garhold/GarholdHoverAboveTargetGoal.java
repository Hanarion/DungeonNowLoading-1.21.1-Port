package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GarholdHoverAboveTargetGoal extends Goal {

    private static final float LOCK_SPEED = 0.22F;

    private final GarholdEntity mob;

    private final double speed;
    private final double hoverHeight; // 6 blocks
    private double lastTargetX;
    private double lastTargetZ;
    private double lastHorDistSq;
    private final double minHorDist;  // don’t try to sit exactly above (looks jittery)
    private final double maxHorDist;

    private boolean locked = false;
    private int repathCooldown = 0;

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
        if (t == null || !t.isAlive()) return false;

        // Only do flying behavior when in FLYING state
        return mob.getGarholdState() == GarholdState.FLYING;
        // ^ if STATE is private, add a getter like mob.getGarholdState()
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

        // Desired: XZ exact, Y aims for hoverHeight
        double desiredX = target.getX();
        double desiredZ = target.getZ();
        double desiredY = target.getY() + hoverHeight;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double dx = desiredX - mob.getX();
        double dz = desiredZ - mob.getZ();
        double dy = desiredY - mob.getY();

        double horDistSq = dx * dx + dz * dz;

        // Target horizontal speed estimate (blocks/tick)
        double tdx = desiredX - lastTargetX;
        double tdz = desiredZ - lastTargetZ;
        double targetHorSpeed = Math.sqrt(tdx * tdx + tdz * tdz);

        lastTargetX = desiredX;
        lastTargetZ = desiredZ;

        // Hysteresis (squared distances)
        final double LOCK_DIST = 1.1;
        final double UNLOCK_DIST = 1.7;
        final double LOCK_DIST_SQ = LOCK_DIST * LOCK_DIST;
        final double UNLOCK_DIST_SQ = UNLOCK_DIST * UNLOCK_DIST;

        // If the gap is increasing while target is moving, we’re falling behind -> unlock
        boolean gapIncreasing = horDistSq > lastHorDistSq + 0.02; // tiny buffer to ignore jitter
        boolean targetMovingAway = targetHorSpeed > 0.06;         // player actually moving
        boolean tooFar = horDistSq >= UNLOCK_DIST_SQ;

        // Update for next tick comparisons (do this before returns)
        lastHorDistSq = horDistSq;

        if (!locked) {
            // PATHFIND MODE
            if (--repathCooldown <= 0) {
                repathCooldown = 5;
                mob.getNavigation().moveTo(desiredX, desiredY, desiredZ, speed);
            }

            if (horDistSq <= LOCK_DIST_SQ) {
                locked = true;
                mob.getNavigation().stop();
            }

            return;
        }

        // LOCK MODE
        mob.getNavigation().stop();

        // --- Make lock mode obey the same "speed" ---
        // Navigation speed is "blocks per tick-ish" scaled by attributes, but for lock-mode
        // we just cap horizontal acceleration/velocity using speed so it feels consistent.
        // Good rule of thumb:
        // - maxVel ≈ speed * 0.35..0.45
        // - accel ≈ maxVel * 0.8..1.2
        double maxXZVel = LOCK_SPEED * speed;
        double xzAccel = maxXZVel * 1.0;

        // For Y, keep it slightly weaker
        double maxYVel = LOCK_SPEED * speed;
        double yAccel = maxYVel * 1.0;

        // Damping: use the same damping regardless of speed
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

        // --- Unlock conditions ---
        // 1) Actually too far
        // 2) Falling behind while player is moving (gap increasing)
        // 3) If collisions push you off-center for a moment, you’ll pathfind around it
        if (tooFar || (gapIncreasing && targetMovingAway)) {
            locked = false;
            repathCooldown = 0;
        }
    }

    private Vec3 nudgeToAir(Vec3 pos, int maxUp) {
        BlockPos bp = BlockPos.containing(pos);
        for (int i = 0; i < maxUp; i++) {
            if (mob.level().getBlockState(bp).isAir() && mob.level().getBlockState(bp.above()).isAir()) {
                return new Vec3(pos.x, bp.getY() + 0.1, pos.z);
            }
            bp = bp.above();
        }
        // fallback: keep original Y (pathfinder may still handle)
        return pos;
    }
}
