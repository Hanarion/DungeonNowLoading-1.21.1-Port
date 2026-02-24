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

    private static final double DROP_PER_TICK = 16.0 / (1.5 * 5.0);

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

        mob.getNavigation().stop();

        if (diveDropping && !diveLanded) {
            // keep falling no matter what
            mob.setDeltaMovement(0.0, -DROP_PER_TICK, 0.0);

            // try capture once (but DON'T stop falling)
            if (!mob.hasCapturedPlayer()) {
                LivingEntity hit = findHitTargetSwept();
                if (hit != null) {
                    boolean captured = mob.beginCapture(hit); // ideally returns boolean
                    // if captured == false, just keep falling
                }
            }

            if (hasBlockSupportBelow(0.05)) {
                diveLanded = true;
                mob.doGroundSmashHitExcludeCaptured();
                mob.playLandDiveAnimation();
            }
            return;
        }

        // Charge phase (before 25%)
        mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
    }

    private boolean hasBlockSupportBelow(double epsilon) {
        AABB box = mob.getBoundingBox();

        // shift the hitbox slightly downward
        AABB below = box.move(0.0, -epsilon, 0.0);

        // if this moved box collides, then something is directly under us
        return !mob.level().noCollision(mob, below);
    }

    private LivingEntity findHitTargetSwept() {
        Vec3 v = mob.getDeltaMovement();

        AABB base = mob.getBoundingBox();
        AABB now  = base.inflate(HIT_INFLATE);
        AABB next = base.move(v).inflate(HIT_INFLATE);
        AABB swept = now.minmax(next);

        List<LivingEntity> targets = mob.level().getEntitiesOfClass(
                LivingEntity.class,
                swept,
                this::isValidCaptureTarget
        );

        return targets.isEmpty() ? null : targets.get(0);
    }

    private boolean isValidCaptureTarget(LivingEntity e) {
        if (e instanceof GarholdEntity) return false;
        if (e == mob) return false;          // ✅ critical
        if (!e.isAlive()) return false;
        if (e.isSpectator()) return false;   // ✅ don’t grab spectator players

        if (!isSmallEnoughToCapture(e)) return false;

        if (e instanceof Player p) {
            return !p.isCreative();          // survival/adventure only
        }

        return true;
    }

    private boolean isSmallEnoughToCapture(LivingEntity target) {
        var selfDims = mob.getDimensions(mob.getPose());
        var targetDims = target.getDimensions(target.getPose());

        float maxW = selfDims.width * 1.5f;
        float maxH = selfDims.height * 1.5f;

        return targetDims.width <= maxW && targetDims.height <= maxH;
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
