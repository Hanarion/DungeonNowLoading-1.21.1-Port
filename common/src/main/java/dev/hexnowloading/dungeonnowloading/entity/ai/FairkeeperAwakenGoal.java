package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.FairkeeperFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperAwakenGoal extends Goal {
    private final FairkeeperBorosEntity fairkeeperEntity;

    public FairkeeperAwakenGoal(FairkeeperBorosEntity fairkeeperEntity) {
        this.fairkeeperEntity = fairkeeperEntity;
    }

    @Override
    public boolean canUse() {
        return this.fairkeeperEntity.isState(FairkeeperBorosEntity.FairkeeperState.AWAKENING);
    }

    @Override
    public void start() {
    }

    @Override
    public void tick() {
        if (this.fairkeeperEntity.getDeltaMovement().length() < 0.001f) {
            int x = Mth.floor(this.fairkeeperEntity.getX());
            int y = Mth.floor(this.fairkeeperEntity.getY());
            int z = Mth.floor(this.fairkeeperEntity.getZ());
            boolean collidedWithBlock = false;

            if (this.fairkeeperEntity.distanceToSqr(this.fairkeeperEntity.getMoveControl().getWantedX(), this.fairkeeperEntity.getMoveControl().getWantedY(), this.fairkeeperEntity.getMoveControl().getWantedZ()) > 0.1) {
                y += 1;
                collidedWithBlock = true;
            }

            for (int ix = -2; ix <= 2; ix++) {
                for (int iz = -2; iz <= 2; iz++) {
                    for (int iy = 0; iy <= 4; iy++) {
                        int dx = x + ix;
                        int dy = y + iy;
                        int dz = z + iz;
                        BlockPos blockPos = new BlockPos(dx, dy, dz);
                        BlockState blockState = this.fairkeeperEntity.level().getBlockState(blockPos);
                        if (!blockState.isAir()) {
                            if (!blockState.is(BlockTags.WITHER_IMMUNE)) {
                                this.fairkeeperEntity.level().destroyBlock(blockPos, true, this.fairkeeperEntity);
                            } else {
                                collidedWithBlock = false;
                            }
                        }
                    }
                }
            }
            if (collidedWithBlock) {
                return;
            }

            this.fairkeeperEntity.setDeltaMovement(Vec3.ZERO);
            ((FairkeeperFlyingMoveControl) this.fairkeeperEntity.getMoveControl()).setWaitOperation();
        }
        if (!this.fairkeeperEntity.getMoveControl().hasWanted()) {
            this.fairkeeperEntity.stopAttacking(20);
        }
    }
}
