package dev.hexnowloading.dungeonnowloading.entity.ai.control.pathfinding;

import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class BallistaGolemNodeEvaluator extends WalkNodeEvaluator {

    public BallistaGolemNodeEvaluator() {
        super();
    }



    /*@Nullable
    @Override
    protected Node findAcceptedNode(int x, int y, int z, int maxJumpHeight, double floorY, Direction direction, BlockPathTypes previousType) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        double nodeFloorY = this.getFloorLevel(mutablePos.set(x, y, z));

        if (nodeFloorY - floorY > this.getMobJumpHeight()) {
            return null;
        }

        BlockPathTypes pathType = this.getCachedBlockType(this.mob, x, y, z);
        float malus = this.mob.getPathfindingMalus(pathType);

        Node node = null;
        if (malus >= 0.0F) {
            // No centering here
            node = this.getNode(x, y, z);
            node.type = pathType;
            node.costMalus = Math.max(node.costMalus, malus);
        }

        if (doesBlockHavePartialCollision(previousType) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
            node = null;
        }

        return node;
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, (double)this.mob.maxUpStep());
    }

    private static boolean doesBlockHavePartialCollision(BlockPathTypes $$0) {
        return $$0 == BlockPathTypes.FENCE || $$0 == BlockPathTypes.DOOR_WOOD_CLOSED || $$0 == BlockPathTypes.DOOR_IRON_CLOSED;
    }

    private boolean canReachWithoutCollision(Node $$0) {
        AABB $$1 = this.mob.getBoundingBox();
        Vec3 $$2 = new Vec3((double)$$0.x - this.mob.getX() + $$1.getXsize() / 2.0, (double)$$0.y - this.mob.getY() + $$1.getYsize() / 2.0, (double)$$0.z - this.mob.getZ() + $$1.getZsize() / 2.0);
        int $$3 = Mth.ceil($$2.length() / $$1.getSize());
        $$2 = $$2.scale((double)(1.0F / (float)$$3));

        for(int $$4 = 1; $$4 <= $$3; ++$$4) {
            $$1 = $$1.move($$2);
            if (this.hasCollisions($$1)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasCollisions(AABB $$0) {
        return this.collisionCache.computeIfAbsent($$0, ($$1) -> {
            return !this.level.noCollision(this.mob, $$0);
        });
    }*/

    /*@Override
    protected boolean isDiagonalValid(Node current, @Nullable Node dir1, @Nullable Node dir2, @Nullable Node diagonal) {
        // Only allow diagonals if BOTH straight directions are valid
        return dir1 != null && dir2 != null && !dir1.closed && !dir2.closed &&
                dir1.costMalus >= 0.0F && dir2.costMalus >= 0.0F &&
                diagonal != null && !diagonal.closed && diagonal.costMalus >= 0.0F;
    }*/
}
