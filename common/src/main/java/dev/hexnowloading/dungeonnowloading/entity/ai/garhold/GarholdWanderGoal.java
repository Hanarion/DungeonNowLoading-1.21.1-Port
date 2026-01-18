package dev.hexnowloading.dungeonnowloading.entity.ai.garhold;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity.GarholdState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GarholdWanderGoal extends Goal {

    private final GarholdEntity mob;
    private final double speed;
    private final int radiusXZ;
    private final int radiusY;

    private int cooldownTicks = 0;

    public GarholdWanderGoal(GarholdEntity mob, double speed, int radiusXZ, int radiusY) {
        this.mob = mob;
        this.speed = speed;
        this.radiusXZ = radiusXZ;
        this.radiusY = radiusY;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getGarholdState() != GarholdState.FLYING) return false;
        if (mob.getTarget() != null) return false;
        if (mob.hasCapturedPlayer()) return false;

        // don’t spam pathing every tick
        return cooldownTicks-- <= 0;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getGarholdState() == GarholdState.FLYING
                && mob.getTarget() == null
                && !mob.hasCapturedPlayer()
                && !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        cooldownTicks = 20 + mob.getRandom().nextInt(40); // 1–3 seconds between picks

        Vec3 dest = pickRandomDestination();
        if (dest != null) {
            mob.getNavigation().moveTo(dest.x, dest.y, dest.z, speed);
        }
    }

    private Vec3 pickRandomDestination() {
        BlockPos base = mob.blockPosition();
        for (int tries = 0; tries < 12; tries++) {
            int dx = Mth.nextInt(mob.getRandom(), -radiusXZ, radiusXZ);
            int dz = Mth.nextInt(mob.getRandom(), -radiusXZ, radiusXZ);
            int dy = Mth.nextInt(mob.getRandom(), -radiusY, radiusY);

            BlockPos p = base.offset(dx, dy, dz);

            // For flying mobs: we mostly just need the mob's AABB to fit at the destination.
            // Use its current hitbox size, moved to that spot.
            double x = p.getX() + 0.5;
            double y = p.getY();
            double z = p.getZ() + 0.5;

            if (mob.level().noCollision(mob, mob.getBoundingBox().move(x - mob.getX(), y - mob.getY(), z - mob.getZ()))) {
                return new Vec3(x, y, z);
            }
        }
        return null;
    }
}