package dev.hexnowloading.dungeonnowloading.entity.ai.control.pathfinding;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WispPathNavigation extends FlyingPathNavigation {
    public WispPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected Vec3 getTempMobPos() {
        if (this.mob instanceof WispEntity wisp) {
            return wisp.getNavigationAnchorPos();
        }
        return this.mob.getBoundingBox().getCenter();
    }

    @Override
    protected void followThePath() {
        Vec3 currentCenter = this.getTempMobPos();
        this.maxDistanceToWaypoint = Math.max(0.18F, this.mob.getBbWidth() * 0.3F);

        BlockPos nextPos = this.path.getNextNodePos();
        Vec3 nextCenter = Vec3.atCenterOf(nextPos);
        boolean withinTolerance =
                Math.abs(currentCenter.x - nextCenter.x) < this.maxDistanceToWaypoint
                        && Math.abs(currentCenter.z - nextCenter.z) < this.maxDistanceToWaypoint
                        && Math.abs(currentCenter.y - nextCenter.y) < 0.6D;

        if (withinTolerance) {
            this.path.advance();
        }

        this.doStuckDetection(currentCenter);
    }
}
