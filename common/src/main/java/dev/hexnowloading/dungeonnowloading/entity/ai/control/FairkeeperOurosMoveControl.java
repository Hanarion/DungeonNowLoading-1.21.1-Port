package dev.hexnowloading.dungeonnowloading.entity.ai.control;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FairkeeperOurosMoveControl extends MoveControl {

    public FairkeeperOurosMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double deltaX = wantedX - this.mob.getX();
            double deltaY = wantedY - this.mob.getBoundingBox().maxY;
            double deltaZ = wantedZ - this.mob.getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (distance < 2.5000003E-7F) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5));
                return;
            }

            double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);


            System.out.println(deltaY + " : " + wantedY + " : " + this.mob.getBoundingBox().maxY);

            Vec3 movement = new Vec3(deltaX / distance, 0.0F, deltaZ / distance)
                    .scale(speed);

            this.mob.setDeltaMovement(movement);
            lookTowardTarget();
        }
    }

    private void lookTowardTarget() {
        double directionX = this.wantedX - this.mob.getX();
        double directionZ = this.wantedZ - this.mob.getZ();
        double yaw = Math.toDegrees(Math.atan2(directionZ, directionX)) - 90.0;

        this.mob.setYRot((float) yaw);
        this.mob.yBodyRot = (float) yaw;
    }
}
