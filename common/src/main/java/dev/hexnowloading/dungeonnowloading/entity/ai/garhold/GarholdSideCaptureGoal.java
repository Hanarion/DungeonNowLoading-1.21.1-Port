package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class GarholdSideCaptureGoal extends Goal {

    // Spec:
    // - at 0.75s (15 ticks) start moving
    // - move 3 blocks over 0.25s (5 ticks)
    private static final int WINDUP_TICKS = 15;
    private static final int DASH_TICKS = 5;
    private static final double DASH_BLOCKS = 6.0;
    private static final double DASH_PER_TICK = DASH_BLOCKS / DASH_TICKS;

    // how generous the grab feels
    private static final double HIT_INFLATE = 0.35;

    private final GarholdEntity mob;

    private int ticks;
    private boolean dashing;

    private LivingEntity capturedPlayer;

    private float lockedYaw;
    private boolean yawLocked;

    public GarholdSideCaptureGoal(GarholdEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.getGarholdState() == GarholdState.SIDE_CAPTURE;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getGarholdState() == GarholdState.SIDE_CAPTURE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        ticks = 0;
        dashing = false;
        capturedPlayer = null;
        yawLocked = false;

        mob.getNavigation().stop();
        mob.setDeltaMovement(Vec3.ZERO);

        // Start the animation immediately when entering SIDE_CAPTURE
        mob.playSideCaptureAnimation();

        LivingEntity t = mob.getTarget();
        if (t != null) {
            // lock to where the player was when the goal began
            double dx = t.getX() - mob.getX();
            double dz = t.getZ() - mob.getZ();

            if (dx * dx + dz * dz > 1.0e-6) {
                lockedYaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            } else {
                lockedYaw = mob.getYRot();
            }
        } else {
            lockedYaw = mob.getYRot();
        }

        yawLocked = true;

// enforce it immediately
        mob.setYRot(lockedYaw);
        mob.setYHeadRot(lockedYaw);
        mob.yBodyRot = lockedYaw;
        mob.yRotO = lockedYaw;
        mob.yHeadRotO = lockedYaw;
    }

    @Override
    public void tick() {
        ticks++;

        mob.getNavigation().stop();

// While locked, force it every tick (prevents look control / body from drifting)
        if (yawLocked) {
            mob.setYRot(lockedYaw);
            mob.setYHeadRot(lockedYaw);
            mob.yBodyRot = lockedYaw;
            mob.yRotO = lockedYaw;       // optional anti-jitter
            mob.yHeadRotO = lockedYaw;   // optional anti-jitter
        }

        // Windup (first 15 ticks) - keep stable
        if (!dashing) {
            mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);

            if (ticks >= WINDUP_TICKS) {
                dashing = true;
            }
            return;
        }

        // Dash phase (5 ticks)
        if (ticks < WINDUP_TICKS + DASH_TICKS) {
            dashForwardAndTryCapture();
            return;
        }

        // Dash ended and animation is still finishing; do nothing here.
        // The animation end callback should decide closing gate vs ascend.
        mob.setDeltaMovement(Vec3.ZERO);
    }

    private void dashForwardAndTryCapture() {

        Vec3 dir = Vec3.directionFromRotation(0.0F, lockedYaw); // pitch 0, yaw locked
        Vec3 fwd = new Vec3(dir.x, 0.0, dir.z);
        if (fwd.lengthSqr() < 1.0e-6) return;

        Vec3 step = fwd.normalize().scale(DASH_PER_TICK);

        // Swept AABB capture (avoid tunneling)
        if (capturedPlayer == null && !mob.hasCapturedPlayer()) {
            LivingEntity hit = findHitTargetSwept(step);
            if (hit != null) {
                boolean captured = mob.beginCapture(hit);
                if (captured) {
                    capturedPlayer = hit;
                    mob.setDeltaMovement(Vec3.ZERO);
                    dashing = false;
                    return;
                }
            }
        }

        // Block collision-aware movement: if we can't fit, stop the dash early
        AABB movedBox = mob.getBoundingBox().move(step);
        if (mob.level().noCollision(mob, movedBox)) {
            mob.setDeltaMovement(step.x, mob.getDeltaMovement().y, step.z);
            mob.hasImpulse = true;
            mob.move(MoverType.SELF, mob.getDeltaMovement());
            mob.setDeltaMovement(Vec3.ZERO);
        } else {
            dashing = false;
        }

        mob.setDeltaMovement(Vec3.ZERO);
    }

    private LivingEntity findHitTargetSwept(Vec3 step) {

        AABB now = mob.getBoundingBox().inflate(HIT_INFLATE);
        AABB next = now.move(step).inflate(HIT_INFLATE);
        AABB swept = now.minmax(next);

        List<LivingEntity> entities = mob.level().getEntitiesOfClass(
                LivingEntity.class,
                swept,
                this::isValidCaptureTarget
        );

        return entities.isEmpty() ? null : entities.get(0);
    }

    private boolean isValidCaptureTarget(LivingEntity e) {
        if (e instanceof GarholdEntity) return false;
        if (e == mob) return false;
        if (!e.isAlive()) return false;
        if (e.isSpectator()) return false;
        if (!isSmallEnoughToCapture(e)) return false;

        if (e instanceof Player p) {
            return !p.isCreative() && !p.isSpectator();
        }

        return true;
    }

    private boolean isSmallEnoughToCapture(LivingEntity target) {

        EntityDimensions selfDims = mob.getDimensions(mob.getPose());
        EntityDimensions targetDims = target.getDimensions(target.getPose());

        float maxWidth  = selfDims.width  * 1.5f;
        float maxHeight = selfDims.height * 1.5f;

        return targetDims.width  <= maxWidth
                && targetDims.height <= maxHeight;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.setDeltaMovement(Vec3.ZERO);

        // If something interrupts SIDE_CAPTURE, return to flying
        if (mob.getGarholdState() == GarholdState.SIDE_CAPTURE) {
            mob.setGarholdState(GarholdState.FLYING);
        }

        capturedPlayer = null;
        dashing = false;
        yawLocked = false;
        lockedYaw = 0.0f;
        ticks = 0;
    }
}
