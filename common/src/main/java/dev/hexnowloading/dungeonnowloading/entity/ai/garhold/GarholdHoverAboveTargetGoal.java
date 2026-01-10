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

    private final GarholdEntity mob;

    private final double speed;
    private final double hoverHeight; // 6 blocks
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
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        // Desired position: centered above target (XZ exact), Y aims for hoverHeight
        double desiredX = target.getX();
        double desiredZ = target.getZ();
        double desiredY = target.getY() + hoverHeight;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Distances
        double dx = desiredX - mob.getX();
        double dz = desiredZ - mob.getZ();
        double dy = desiredY - mob.getY();

        double horDist = Math.sqrt(dx * dx + dz * dz);

        final double LOCK_DIST = 1.1;
        final double UNLOCK_DIST = 2.2;

        if (!locked) {
            // PATHFIND MODE: get around terrain
            if (--repathCooldown <= 0) {
                repathCooldown = 5;
                mob.getNavigation().moveTo(desiredX, desiredY, desiredZ, speed);
            }

            if (horDist <= LOCK_DIST) {
                locked = true;
                mob.getNavigation().stop(); // prevent drift
            }
        } else {
            // LOCK MODE: no pathfinding drift, keep XZ centered, steer Y toward hover
            mob.getNavigation().stop();

            Vec3 vel = mob.getDeltaMovement();

            // ---- XZ "perfect" lock steering ----
            double xzAccel = 0.28;   // stronger = tighter lock
            double xzMaxVel = 0.45;  // prevents overshoot wobble
            double xzDamping = 0.55; // settle

            double addX = Mth.clamp(dx * xzAccel, -xzMaxVel, xzMaxVel);
            double addZ = Mth.clamp(dz * xzAccel, -xzMaxVel, xzMaxVel);

            // ---- Y hover steering (looser is fine) ----
            double yAccel = 0.10;
            double yMaxVel = 0.25;

            double addY = Mth.clamp(dy * yAccel, -yMaxVel, yMaxVel);

            mob.setDeltaMovement(
                    vel.x * xzDamping + addX,
                    vel.y * 0.70 + addY,
                    vel.z * xzDamping + addZ
            );

            // If we get knocked away, return to navigation
            if (horDist >= UNLOCK_DIST) {
                locked = false;
                repathCooldown = 0;
            }
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
