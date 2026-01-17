package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
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
    private static final double DASH_BLOCKS = 3.0;
    private static final double DASH_PER_TICK = DASH_BLOCKS / DASH_TICKS;

    // how generous the grab feels
    private static final double HIT_INFLATE = 0.35;

    private final GarholdEntity mob;

    private int ticks;
    private boolean dashing;

    private Player capturedPlayer;

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
    public void start() {
        ticks = 0;
        dashing = false;
        capturedPlayer = null;

        mob.getNavigation().stop();
        mob.setDeltaMovement(Vec3.ZERO);

        // Start the animation immediately when entering SIDE_CAPTURE
        mob.playSideCaptureAnimation();
    }

    @Override
    public void tick() {
        ticks++;

        mob.getNavigation().stop();

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
        // Move forward based on current facing. Usually best to keep it flat in XZ.
        Vec3 look = mob.getLookAngle();
        Vec3 fwd = new Vec3(look.x, 0.0, look.z);
        if (fwd.lengthSqr() < 1.0e-6) {
            fwd = new Vec3(0.0, 0.0, 1.0);
        }
        Vec3 step = fwd.normalize().scale(DASH_PER_TICK);

        // Swept AABB capture (avoid tunneling)
        if (capturedPlayer == null && !mob.hasCapturedPlayer()) {
            Player hit = findHitPlayerSwept(step);
            if (hit != null) {
                capturedPlayer = hit;
                mob.beginCapture(hit);

                // Stop dash immediately after capture
                mob.setDeltaMovement(Vec3.ZERO);
                dashing = false;
                return;
            }
        }

        // Block collision-aware movement: if we can't fit, stop the dash early
        AABB movedBox = mob.getBoundingBox().move(step);
        if (mob.level().noCollision(mob, movedBox)) {
            mob.setPos(mob.getX() + step.x, mob.getY(), mob.getZ() + step.z);
        } else {
            dashing = false;
        }

        mob.setDeltaMovement(Vec3.ZERO);
    }

    private Player findHitPlayerSwept(Vec3 step) {
        AABB now = mob.getBoundingBox().inflate(HIT_INFLATE);
        AABB next = now.move(step).inflate(HIT_INFLATE);
        AABB swept = now.minmax(next);

        List<Player> players = mob.level().getEntitiesOfClass(
                Player.class,
                swept,
                p -> p.isAlive()
                        && !p.isSpectator()
                        && !p.getAbilities().instabuild
        );

        return players.isEmpty() ? null : players.get(0);
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
    }
}
