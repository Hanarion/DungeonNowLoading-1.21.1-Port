package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;

public class ReaperSpiderMoveControl extends MoveControl {

    private final ReaperSpiderEntity reaperSpider;
    private final double sideStrafeSpeed;
    private final double backStrafeSpeed;

    private float strafeForward;
    private float strafeRight;
    private boolean hasStrafeCommand = false;

    public ReaperSpiderMoveControl(ReaperSpiderEntity mob,
                                   double sideStrafeSpeed,
                                   double backStrafeSpeed) {
        super(mob);
        this.reaperSpider = mob;
        this.sideStrafeSpeed = sideStrafeSpeed;
        this.backStrafeSpeed = backStrafeSpeed;
    }

    /**
     * Called from the attack goal to request a strafe move.
     */
    public void setStrafe(float forward, float right, double speedModifier) {
        this.operation = Operation.STRAFE;
        this.strafeForward = forward;
        this.strafeRight = right;
        this.hasStrafeCommand = true;

        if (forward < 0.0F && right == 0.0F) {
            this.speedModifier = this.backStrafeSpeed;
        } else {
            this.speedModifier = speedModifier <= 0 ? this.sideStrafeSpeed : speedModifier;
        }
    }

    @Override
    public void strafe(float forward, float right) {
        // vanilla-compatible fallback if something else calls it
        setStrafe(forward, right, this.sideStrafeSpeed);
    }

    @Override
    public void tick() {
        if (this.operation != Operation.STRAFE || !this.hasStrafeCommand) {
            this.hasStrafeCommand = false;
            super.tick();
            return;
        }

        float forward = this.strafeForward;
        float right = this.strafeRight;

        float len = Mth.sqrt(forward * forward + right * right);
        if (len < 1.0E-4F) {
            this.mob.setZza(0.0F);
            this.mob.setXxa(0.0F);
            this.operation = Operation.WAIT;
            this.hasStrafeCommand = false;
            return;
        }

        forward /= len;
        right /= len;

        if (!isStrafeDirectionSafe(forward, right)) {
            // can't move safely in this direction; stand still this tick
            this.mob.setZza(0.0F);
            this.mob.setXxa(0.0F);
            this.operation = Operation.WAIT;
            this.hasStrafeCommand = false;
            return;
        }

        float baseSpeed = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.mob.setSpeed((float) this.speedModifier * baseSpeed);
        this.mob.setZza(forward);
        this.mob.setXxa(right);

        // consume this command; goal must resend next tick
        this.hasStrafeCommand = false;
    }

    public boolean isStrafeDirectionSafe(float forward, float right) {
        float yawRad = this.mob.getYRot() * (Mth.PI / 180.0F);
        float sin = Mth.sin(yawRad);
        float cos = Mth.cos(yawRad);

        float dx = right * cos - forward * sin;
        float dz = forward * cos + right * sin;

        return isSafeStrafe(dx, dz);
    }

    private boolean isSafeStrafe(float worldOffsetX, float worldOffsetZ) {
        double nx = this.mob.getX() + worldOffsetX;
        double nz = this.mob.getZ() + worldOffsetZ;
        int x = Mth.floor(nx);
        int z = Mth.floor(nz);
        int y = this.mob.getBlockY();
        BlockPos pos = new BlockPos(x, y, z);

        // fluids
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
            BlockPathTypes feetType = eval.getBlockPathType(this.mob.level(), x, y, z);
            if (isDangerous(feetType)) return false;
            if (feetType == BlockPathTypes.WALKABLE) return true;

            var stateFeet = this.mob.level().getBlockState(pos);
            if (!stateFeet.getCollisionShape(this.mob.level(), pos).isEmpty()) {
                return true;
            }

            for (int depth = 1; depth <= 3; depth++) {
                BlockPathTypes typeBelow = eval.getBlockPathType(this.mob.level(), x, y - depth, z);
                if (isDangerous(typeBelow)) return false;
                if (typeBelow == BlockPathTypes.WALKABLE) return true;
            }

            return false;
        }

        // fallback: any solid floor within 3 blocks below
        for (int i = 0; i <= 3; i++) {
            BlockPos fp = pos.below(i);
            if (!this.mob.level().getBlockState(fp).getCollisionShape(this.mob.level(), fp).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean isDangerous(BlockPathTypes type) {
        if (type == null) return false;
        return switch (type) {
            case LAVA, DAMAGE_FIRE, DAMAGE_OTHER,
                 DANGER_FIRE, DANGER_OTHER,
                 WATER -> true;
            default -> false;
        };
    }
}
