package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BallistaGolemMoveControl extends MoveControl {

    private final Mob mob;

    private static final float SLOWDOWN_SPINNING = 10.0F;

    public BallistaGolemMoveControl(Mob mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) return;
        this.operation = Operation.WAIT;

        double deltaX = wantedX - this.mob.getX();
        double deltaY = wantedY - this.mob.getY();
        double deltaZ = wantedZ - this.mob.getZ();
        double distance = deltaX * deltaX + deltaZ * deltaZ + deltaY * deltaY;
        if (distance < 2.500000277905201E-7) {
            this.mob.setZza(0.0F);
            return;
        }

        float desiredYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX))) - 90.0F;
        float currentYaw = this.mob.getYRot();
        float yawDiff = Mth.wrapDegrees(desiredYaw - currentYaw);
        float clampedYawDiff = Mth.clamp(yawDiff, -SLOWDOWN_SPINNING, SLOWDOWN_SPINNING);
        this.mob.setYRot(currentYaw + clampedYawDiff);
        this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));

        // Overshoot logic
        Path path = this.mob.getNavigation().getPath();
        int index = path.getNextNodeIndex();

        if (path != null && index > 0 && index < path.getNodeCount() - 1) {
            Node prev = path.getNode(index - 1);
            Node turn = path.getNode(index);
            Node next = path.getNode(index + 1);

            boolean isMinusXtoPlusZ =
                    (prev.x == turn.x - 1 && prev.z == turn.z) &&
                            (next.x == turn.x && next.z == turn.z + 1);

            boolean isMinusZtoPlusX =
                    (prev.x == turn.x && prev.z == turn.z - 1) &&
                            (next.x == turn.x + 1 && next.z == turn.z);

            // If we're in one of the desired L-turn shapes
            if (isMinusXtoPlusZ || isMinusZtoPlusX) {
                // Determine direction to next node from mob position
                double dx = (next.x + 0.5) - this.mob.getX();
                double dz = (next.z + 0.5) - this.mob.getZ();

                // Determine strafe direction relative to mob rotation
                float yawRad = this.mob.getYRot() * ((float)Math.PI / 180F);
                double strafeDirX = -Math.sin(yawRad); // Left = negative xxa
                double strafeDirZ = Math.cos(yawRad);

                double dot = dx * strafeDirX + dz * strafeDirZ;

                if (isMinusXtoPlusZ && dot > 0.0) {
                    this.mob.setXxa(0.2F); // Strafe right
                    System.out.println("✅ Strafe right toward +Z");
                } else if (isMinusZtoPlusX && dot > 0.0) {
                    this.mob.setXxa(-0.2F); // Strafe left
                    System.out.println("✅ Strafe left toward +X");
                } else {
                    this.mob.setXxa(0.0F);
                    System.out.println("🚫 Strafing canceled — would move away from node");
                }
            }
        }

        // Jumping logic
        BlockPos blockPos = this.mob.blockPosition();
        BlockState blockState = this.mob.level().getBlockState(blockPos);
        VoxelShape collisionShape = blockState.getCollisionShape(this.mob.level(), blockPos);
        if (deltaY > (double)this.mob.maxUpStep() && deltaZ * deltaZ + deltaX * deltaX < (double)Math.max(1.0F, this.mob.getBbWidth()) || !collisionShape.isEmpty() && this.mob.getY() < collisionShape.max(Direction.Axis.Y) + (double)blockPos.getY() && !blockState.is(BlockTags.DOORS) && !blockState.is(BlockTags.FENCES)) {
            this.mob.getJumpControl().jump();
            this.operation = Operation.JUMPING;
        }
    }
}
