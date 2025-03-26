package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BallistaGolemMoveControl extends MoveControl {

    private final BallistaGolemEntity ballistaGolem;

    private static final float ROTATION_RATE = 10.0F;

    public BallistaGolemMoveControl(BallistaGolemEntity ballistaGolem) {
        super(ballistaGolem);
        this.ballistaGolem = ballistaGolem;
    }

    @Override
    public void tick() {

        if (this.ballistaGolem.getState() == BallistaGolemEntity.BallistaGolemState.RELOAD || this.ballistaGolem.getState() == BallistaGolemEntity.BallistaGolemState.SHOOT || this.ballistaGolem.getState() == BallistaGolemEntity.BallistaGolemState.AWAKENING) {
            ballistaGolem.setSpeed(0.0F);
            ballistaGolem.setZza(0.0F);
            ballistaGolem.setXxa(0.0F);
            return;
        }

        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;

            double deltaX = wantedX - this.ballistaGolem.getX();
            double deltaY = wantedY - this.ballistaGolem.getY();
            double deltaZ = wantedZ - this.ballistaGolem.getZ();
            double distance = deltaX * deltaX + deltaZ * deltaZ + deltaY * deltaY;
            if (distance < 2.500000277905201E-7) {
                this.ballistaGolem.setZza(0.0F);
                return;
            }

            float desiredYaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX))) - 90.0F;
            float currentYaw = this.ballistaGolem.getYRot();
            float yawDiff = Mth.wrapDegrees(desiredYaw - currentYaw);
            float clampedYawDiff = Mth.clamp(yawDiff, -ROTATION_RATE, ROTATION_RATE);
            this.ballistaGolem.setYRot(currentYaw + clampedYawDiff);
            this.ballistaGolem.setSpeed((float) (this.speedModifier * this.ballistaGolem.getAttributeValue(Attributes.MOVEMENT_SPEED)));

            Path path = this.ballistaGolem.getNavigation().getPath();
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

                if (isMinusXtoPlusZ || isMinusZtoPlusX) {
                    double dx = (next.x + 0.5) - this.ballistaGolem.getX();
                    double dz = (next.z + 0.5) - this.ballistaGolem.getZ();

                    float yawRad = this.ballistaGolem.getYRot() * ((float) Math.PI / 180F);
                    double strafeDirX = -Math.sin(yawRad);
                    double strafeDirZ = Math.cos(yawRad);

                    double dot = dx * strafeDirX + dz * strafeDirZ;

                    if (isMinusXtoPlusZ && dot > 0.0) {
                        this.ballistaGolem.setXxa(0.2F);
                    } else if (isMinusZtoPlusX && dot > 0.0) {
                        this.ballistaGolem.setXxa(-0.2F);
                    } else {
                        this.ballistaGolem.setXxa(0.0F);
                    }
                }
            }

            BlockPos blockPos = this.ballistaGolem.blockPosition();
            BlockState blockState = this.ballistaGolem.level().getBlockState(blockPos);
            VoxelShape collisionShape = blockState.getCollisionShape(this.ballistaGolem.level(), blockPos);
            if (deltaY > (double) this.ballistaGolem.maxUpStep() && deltaZ * deltaZ + deltaX * deltaX < (double) Math.max(1.0F, this.ballistaGolem.getBbWidth()) || !collisionShape.isEmpty() && this.ballistaGolem.getY() < collisionShape.max(Direction.Axis.Y) + (double) blockPos.getY() && !blockState.is(BlockTags.DOORS) && !blockState.is(BlockTags.FENCES)) {
                this.ballistaGolem.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        }
    }
}
