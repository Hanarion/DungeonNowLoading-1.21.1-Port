package dev.hexnowloading.dungeonnowloading.entity.ai.control.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class BallistaGolemPathNavigation extends GroundPathNavigation {
    public BallistaGolemPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public boolean canCutCorner(PathType blockPathTypes) {
        return false;
    }

    protected void followThePath() {
        Vec3 currentPos = this.getTempMobPos();

        // Dynamic max distance tolerance
        float tolerance = (this.mob.getBbWidth() > 0.75f) ? this.mob.getBbWidth() / 1.5f : 0.75f - this.mob.getBbWidth() / 2.0f;
        this.maxDistanceToWaypoint = tolerance;

        BlockPos nextPos = this.path.getNextNodePos();
        double dx = this.mob.getX() - (nextPos.getX() + 0.5);
        double dy = this.mob.getY() - nextPos.getY();
        double dz = this.mob.getZ() - (nextPos.getZ() + 0.5);

        boolean withinTolerance = Math.abs(dx) < tolerance && Math.abs(dz) < tolerance && Math.abs(dy) < 1.0;

        // Instead of a strict raycast/collision, check if the node is "steppable" or already in path range
        if (withinTolerance || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(currentPos)) {
            this.path.advance();
        }

        // Do stuck detection logic (vanilla-compatible)
        this.doStuckDetection(currentPos);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 vec3) {
        boolean bl2;
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        }
        Vec3 vec32 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
        if (!vec3.closerThan(vec32, 2.0)) {
            return false;
        }
        if (this.canMoveDirectly(vec3, this.path.getNextEntityPos(this.mob))) {
            return true;
        }
        Vec3 vec33 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
        Vec3 vec34 = vec32.subtract(vec3);
        Vec3 vec35 = vec33.subtract(vec3);
        double d = vec34.lengthSqr();
        double e = vec35.lengthSqr();
        boolean bl = e < d;
        boolean bl3 = bl2 = d < 0.5;
        if (bl || bl2) {
            Vec3 vec36 = vec34.normalize();
            Vec3 vec37 = vec35.normalize();
            return vec37.dot(vec36) < 0.0;
        }
        return false;
    }

    /*@Override
    protected void followThePath() {
        Vec3 mobPos = this.getTempMobPos();

        Node nextNode = this.path.getNextNode();
        if (nextNode == null) return;

        double nodeCenterX = nextNode.x; // Center of 3x3 corridor
        double nodeCenterZ = nextNode.z;

        AABB mobBox = mob.getBoundingBox().inflate(0.1);

        if (this.level.noCollision(mob, mobBox)) {
            nodeCenterX = nodeCenterX + mob.getBoundingBox().getXsize() / 2f;
            nodeCenterZ = nodeCenterZ + mob.getBoundingBox().getZsize() / 2f;
        }

        double dx = Math.abs(mob.getX() - nodeCenterX);
        double dy = Math.abs(mob.getY() - nextNode.y);
        double dz = Math.abs(mob.getZ() - nodeCenterZ);

        this.maxDistanceToWaypoint = mob.getBbWidth() / 2.0F;

        boolean closeEnough =
                dx < maxDistanceToWaypoint &&
                        dz < maxDistanceToWaypoint &&
                        dy < 1.0;

        if (closeEnough || (this.canCutCorner(nextNode.type) && this.shouldTargetNextNodeInDirection(mobPos))) {
            this.path.advance();
        }

        this.doStuckDetection(mobPos);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 vec3) {
        boolean bl2;
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        }
        Vec3 vec32 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
        if (!vec3.closerThan(vec32, 2.0)) {
            return false;
        }
        if (this.canMoveDirectly(vec3, this.path.getNextEntityPos(this.mob))) {
            return true;
        }
        Vec3 vec33 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
        Vec3 vec34 = vec32.subtract(vec3);
        Vec3 vec35 = vec33.subtract(vec3);
        double d = vec34.lengthSqr();
        double e = vec35.lengthSqr();
        boolean bl = e < d;
        boolean bl3 = bl2 = d < 0.5;
        if (bl || bl2) {
            Vec3 vec36 = vec34.normalize();
            Vec3 vec37 = vec35.normalize();
            return vec37.dot(vec36) < 0.0;
        }
        return false;
    }*/
}
