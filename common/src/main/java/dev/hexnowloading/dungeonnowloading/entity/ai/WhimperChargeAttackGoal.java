package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WhimperChargeAttackGoal extends Goal {
    private final WhimperEntity whimper;
    private boolean hasHitThisCharge;

    // Prevent rare cases where we end up hovering above the target and never intersect.
    private double lastDistToTargetSqr = Double.NaN;
    private int noProgressTicks = 0;
    private int verticalStuckTicks = 0;

    public WhimperChargeAttackGoal(WhimperEntity whimper) {
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.whimper = whimper;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.whimper.getTarget();
        if (target == null || !target.isAlive() || this.whimper.getMoveControl().hasWanted()) {
            return false;
        }

        int baseDelay = 5;
        int level = this.whimper.getOverworkedLevel();
        if (level > 0) {
            float factor = 1.0F - 0.2F * level; // 20% reduction per level
            if (factor < 0.2F) factor = 0.2F;   // cap at 80% reduction
            baseDelay = Math.max(1, (int)(baseDelay * factor));
        }

        if (this.whimper.getRandom().nextInt(reducedTickDelay(baseDelay)) != 0) {
            return false;
        }

        return this.whimper.distanceToSqr(target) > 3.0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.whimper.getMoveControl().hasWanted()
                && this.whimper.getTarget() != null
                && this.whimper.getTarget().isAlive();
    }

    @Override
    public void start() {
        LivingEntity target = this.whimper.getTarget();
        if (target != null) {
            Vec3 strikePos = getStrikePosition(target);
            // Charge quickly toward the target
            this.whimper.getMoveControl().setWantedPosition(strikePos.x, strikePos.y, strikePos.z, 1.6);
            this.lastDistToTargetSqr = this.whimper.distanceToSqr(target);
            this.noProgressTicks = 0;
        }

        this.hasHitThisCharge = false;
        this.whimper.setCharging(true);
    }

    @Override
    public void stop() {
        this.whimper.setCharging(false);
        this.hasHitThisCharge = false;
        this.lastDistToTargetSqr = Double.NaN;
        this.noProgressTicks = 0;
        this.verticalStuckTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.whimper.getTarget();
        if (target == null || !target.isAlive()) {
            this.whimper.setCharging(false);
            return;
        }

        // Hit check: AABB intersection is too strict (especially for flying mobs + gigantic hitboxes).
        // Use a reach-based sphere/box check around the target instead.
        if (!this.hasHitThisCharge && isInStrikeRange(target)) {
            this.whimper.doHurtTarget(target);
            this.hasHitThisCharge = true;
            this.whimper.setCharging(false);
            this.whimper.getMoveControl().setWantedPosition(this.whimper.getX(), this.whimper.getY(), this.whimper.getZ(), 0.0);
            return;
        }

        double distSqr = this.whimper.distanceToSqr(target);

        // If we aren't getting closer for a bit, re-aim at body height.
        if (!Double.isNaN(this.lastDistToTargetSqr)) {
            if (distSqr >= this.lastDistToTargetSqr - 0.01D) {
                this.noProgressTicks++;
            } else {
                this.noProgressTicks = 0;
            }
        }
        this.lastDistToTargetSqr = distSqr;

        // Extra guard: if we're basically on top of the target in XZ but still too high/low, nudge toward feet.
        double dx = target.getX() - this.whimper.getX();
        double dz = target.getZ() - this.whimper.getZ();
        double distXZ = dx * dx + dz * dz;
        double targetY = target.getBoundingBox().minY + (target.getBbHeight() * 0.35D);
        double yErr = Math.abs(targetY - this.whimper.getY());
        if (distXZ < 0.7D * 0.7D && yErr > 0.9D) {
            this.verticalStuckTicks++;
        } else {
            this.verticalStuckTicks = 0;
        }

        // Re-issue a new wanted position periodically when close-ish, or when we appear stuck.
        if (distSqr < 16.0 || this.noProgressTicks >= 15 || this.verticalStuckTicks >= 10) {
            Vec3 strikePos = getStrikePosition(target);

            // If we're stuck vertically, bias even lower for the re-aim.
            if (this.verticalStuckTicks >= 10) {
                double lowY = target.getBoundingBox().minY + 0.15D;
                strikePos = new Vec3(strikePos.x, lowY, strikePos.z);
            }

            this.whimper.getMoveControl().setWantedPosition(strikePos.x, strikePos.y, strikePos.z, 1.6);

            // Hard reset if we got stuck for too long.
            if (this.noProgressTicks >= 40) {
                this.whimper.setCharging(false);
                this.noProgressTicks = 0;
                this.verticalStuckTicks = 0;
            }
        }
    }

    private boolean isInStrikeRange(LivingEntity target) {
        // Base strike range, scaled a bit for gigantic whimpers.
        double range = 1.35D;
        if (this.whimper.isGigantic()) {
            range = 2.15D;
        }

        // Use the target's center, but allow vertical forgiveness so we don't get stuck hovering above.
        Vec3 t = target.getBoundingBox().getCenter();
        double dx = this.whimper.getX() - t.x;
        double dy = (this.whimper.getY() + this.whimper.getBbHeight() * 0.5D) - t.y;
        double dz = this.whimper.getZ() - t.z;

        // Vertical forgiveness: treat Y error as less important than XZ error.
        double horiz = dx * dx + dz * dz;
        double vert = dy * dy;

        double maxHoriz = range * range;
        double maxVert = (range * 1.75D) * (range * 1.75D);

        return horiz <= maxHoriz && vert <= maxVert;
    }

    private Vec3 getStrikePosition(LivingEntity target) {
        // Aim at a lower point inside the target's AABB (reduces hover-lock over small mobs).
        double minY = target.getBoundingBox().minY + 0.15D;
        double maxY = target.getBoundingBox().maxY - 0.15D;
        double y = target.getBoundingBox().minY + target.getBbHeight() * 0.35D;
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;

        // Add a tiny lateral offset so repeated re-aims don't converge to a point directly above the target.
        double ox = (this.whimper.getRandom().nextDouble() - 0.5D) * 0.4D;
        double oz = (this.whimper.getRandom().nextDouble() - 0.5D) * 0.4D;

        return new Vec3(target.getX() + ox, y, target.getZ() + oz);
    }
}
