package dev.hexnowloading.dungeonnowloading.entity.ai.control;

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
            Vec3 targetPos = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 mobPos = this.mob.position();

            double deltaX = targetPos.x - mobPos.x;
            double deltaY = targetPos.y - mobPos.y;
            double deltaZ = targetPos.z - mobPos.z;

            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (distance < this.mob.getBoundingBox().getSize()) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5));
                return;
            }

            double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);

            Vec3 movement = new Vec3(deltaX / distance, deltaY / distance, deltaZ / distance)
                    .scale(speed);

            this.mob.setDeltaMovement(movement);
            this.mob.getLookControl().setLookAt(targetPos.x, targetPos.y, targetPos.z);
        }
    }
}
