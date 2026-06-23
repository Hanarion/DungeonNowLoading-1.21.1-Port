package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import dev.hexnowloading.dungeonnowloading.entity.monster.SilkSpiderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class SilkSpiderMoveControl extends MoveControl {

    private final SilkSpiderEntity silkSpider;
    private final double sideStrafeSpeed;
    private final double backStrafeSpeed;

    private float strafeSideMagnitude = 0.0F;
    private int strafeDir = 1;
    private int strafeDirTimer = 0;

    public SilkSpiderMoveControl(SilkSpiderEntity mob,
                                 double sideStrafeSpeed,
                                 double backStrafeSpeed) {
        super(mob);
        this.silkSpider = mob;
        this.sideStrafeSpeed = sideStrafeSpeed;
        this.backStrafeSpeed = backStrafeSpeed;
    }

    @Override
    public void strafe(float forward, float right) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = forward;
        this.strafeSideMagnitude = Math.abs(right);

        if (forward < 0.0F && this.strafeSideMagnitude == 0.0F) {
            this.speedModifier = this.backStrafeSpeed;
        } else {
            this.speedModifier = this.sideStrafeSpeed;
        }

        if (this.strafeSideMagnitude > 0.0F && this.strafeDirTimer <= 0) {
            this.strafeDirTimer = 20 + this.mob.getRandom().nextInt(20);
        }

        if (this.strafeDir == 0) {
            this.strafeDir = this.mob.getRandom().nextBoolean() ? 1 : -1;
        }
    }

    @Override
    public void tick() {
        if (this.operation != Operation.STRAFE) {
            super.tick();
            return;
        }

        float forward = this.strafeForwards;
        float right = 0.0F;

        // BACKPEDAL hazard redirection
        if (this.strafeSideMagnitude <= 0.0F && forward < 0.0F) {
            if (!isStrafeDirectionSafe(forward, 0.0F)) {
                float sideTestMag = 0.5F;

                boolean rightSafe = isStrafeDirectionSafe(forward, sideTestMag);
                boolean leftSafe = isStrafeDirectionSafe(forward, -sideTestMag);

                if (rightSafe || leftSafe) {
                    this.strafeSideMagnitude = sideTestMag;
                    this.speedModifier = this.sideStrafeSpeed;
                    this.strafeDir = rightSafe && !leftSafe ? 1 :
                            !rightSafe && leftSafe ? -1 :
                                    this.mob.getRandom().nextBoolean() ? 1 : -1;
                    this.strafeDirTimer = 20 + this.mob.getRandom().nextInt(20);
                }
            }
        }

        // Sideways orbit logic
        if (this.strafeSideMagnitude > 0.0F) {
            if (--this.strafeDirTimer <= 0) {
                this.strafeDirTimer = 20 + this.mob.getRandom().nextInt(20);
                this.strafeDir = -this.strafeDir;
            }

            right = this.strafeSideMagnitude * this.strafeDir;

            if (!isStrafeDirectionSafe(forward, right)) {
                int oldDir = this.strafeDir;
                this.strafeDir = -this.strafeDir;
                right = this.strafeSideMagnitude * this.strafeDir;

                if (!isStrafeDirectionSafe(forward, right)) {
                    this.strafeDir = oldDir;
                    right = 0.0F;
                }
            }
        }

        float baseSpeed = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.mob.setSpeed((float) this.speedModifier * baseSpeed);
        this.mob.setZza(forward);
        this.mob.setXxa(right);
    }


    private boolean isSafeStrafe(float worldOffsetX, float worldOffsetZ) {
        double nx = this.mob.getX() + worldOffsetX;
        double nz = this.mob.getZ() + worldOffsetZ;
        int x = Mth.floor(nx);
        int z = Mth.floor(nz);
        int y = this.mob.getBlockY();
        BlockPos pos = new BlockPos(x, y, z);

        // fluid checks
        if (this.mob.level().getFluidState(pos).is(FluidTags.WATER)
                || this.mob.level().getFluidState(pos).is(FluidTags.LAVA)) {
            return false;
        }

        for (int i = 1; i <= 3; i++) {
            BlockPos below = pos.below(i);
            if (this.mob.level().getFluidState(below).is(FluidTags.WATER)
                    || this.mob.level().getFluidState(below).is(FluidTags.LAVA)) {
                return false;
            }
        }

        PathNavigation nav = this.mob.getNavigation();
        NodeEvaluator eval = nav != null ? nav.getNodeEvaluator() : null;

        if (eval != null) {
            PathType feetType = WalkNodeEvaluator.getPathTypeStatic(this.mob, new BlockPos(x, y, z));
            if (isDangerous(feetType)) return false;
            if (feetType == PathType.WALKABLE) return true;

            var stateFeet = this.mob.level().getBlockState(pos);
            if (!stateFeet.getCollisionShape(this.mob.level(), pos).isEmpty()) {
                return true;
            }

            for (int depth = 1; depth <= 3; depth++) {
                PathType typeBelow = WalkNodeEvaluator.getPathTypeStatic(this.mob, new BlockPos(x, y - depth, z));
                if (isDangerous(typeBelow)) return false;
                if (typeBelow == PathType.WALKABLE) return true;
            }

            return false;
        }

        // fallback: solid floor detection
        for (int i = 0; i <= 3; i++) {
            BlockPos fp = pos.below(i);
            if (!this.mob.level().getBlockState(fp).getCollisionShape(this.mob.level(), fp).isEmpty()) {
                return true;
            }
        }

        return false;
    }


    public boolean isStrafeDirectionSafe(float forward, float right) {
        float len = Mth.sqrt(forward * forward + right * right);
        if (len <= 1e-4F) return true;

        forward /= len;
        right /= len;

        float yawRad = this.mob.getYRot() * (Mth.PI / 180.0F);
        float sin = Mth.sin(yawRad);
        float cos = Mth.cos(yawRad);

        float dx = right * cos - forward * sin;
        float dz = forward * cos + right * sin;

        return isSafeStrafe(dx, dz);
    }

    private boolean isDangerous(PathType type) {
        if (type == null) return false;
        return switch (type) {
            case LAVA, DAMAGE_FIRE, DAMAGE_OTHER,
                 DANGER_FIRE, DANGER_OTHER,
                 WATER -> true;
            default -> false;
        };
    }
}
