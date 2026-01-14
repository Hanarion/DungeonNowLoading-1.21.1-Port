package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class GarholdDiveCaptureGoal extends Goal {

    private static final double DROP_PER_TICK = 8.0 / (1.5 * 10.0);

    // how generous the grab feels
    private static final double HIT_INFLATE = 0.35;

    private final GarholdEntity mob;

    private boolean diveDropping;
    private boolean diveLanded;
    private int diveTicks;

    private Player capturedPlayer;

    public GarholdDiveCaptureGoal(GarholdEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.getGarholdState() == GarholdState.DIVE;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getGarholdState() == GarholdState.DIVE;
    }

    @Override
    public void start() {
        diveTicks = 0;
        diveDropping = false;
        diveLanded = false;
        capturedPlayer = null;

        mob.getNavigation().stop();
        mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);

        mob.playChargeDiveWithProgress(
                (anim, progress) -> {
                    if (!diveDropping && progress >= 0.25f) {
                        diveDropping = true;
                    }
                },
                () -> {
                    if (!diveLanded && capturedPlayer == null) {
                        mob.playLandDiveAnimation();
                    }
                }
        );
    }

    @Override
    public void tick() {
        diveTicks++;

        LivingEntity target = mob.getTarget();
        if (target != null) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        mob.getNavigation().stop();

        if (diveDropping && !diveLanded) {
            mob.setDeltaMovement(0.0, -DROP_PER_TICK, 0.0);

            // --- capture check (swept AABB to avoid tunneling) ---
            if (capturedPlayer == null) {
                Player hit = findHitPlayerSwept(); // your swept AABB method
                if (hit != null && !mob.hasCapturedPlayer()) {
                    mob.beginCapture(hit);

                    // stop Garhold movement if desired
                    mob.setDeltaMovement(Vec3.ZERO);

                    // start your closing-gate animation next
                    // mob.playClosingGateWithProgress(...)

                    return;
                }
            }

            if (mob.onGround()) {
                diveLanded = true;

                // only land-dive if we didn't capture someone
                if (capturedPlayer == null) {
                    mob.playLandDiveAnimation();
                }
            }
            return;
        }

        // Charge phase (before 25%)
        mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
    }

    private Player findHitPlayerSwept() {
        Vec3 v = mob.getDeltaMovement();

        AABB now = mob.getBoundingBox().inflate(HIT_INFLATE);
        AABB next = mob.getBoundingBox().move(v).inflate(HIT_INFLATE);

        // union of the two AABBs (covers the swept volume)
        AABB swept = now.minmax(next);

        List<Player> players = mob.level().getEntitiesOfClass(
                Player.class,
                swept,
                p -> p.isAlive() && !p.isSpectator()
        );

        return players.isEmpty() ? null : players.get(0);
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();

        if (mob.getGarholdState() == GarholdState.DIVE) {
            mob.setGarholdState(GarholdState.FLYING);
        }

        capturedPlayer = null;
    }
}
