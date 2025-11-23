package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import dev.hexnowloading.dungeonnowloading.entity.monster.WebSpitterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;

public class WebSpitterMoveControl extends MoveControl {

    private final WebSpitterEntity webSpitter;
    private final double sideStrafeSpeed;
    private final double backStrafeSpeed;

    public WebSpitterMoveControl(WebSpitterEntity mob,
                                 double sideStrafeSpeed,
                                 double backStrafeSpeed) {
        super(mob);
        this.webSpitter = mob;
        this.sideStrafeSpeed = sideStrafeSpeed;
        this.backStrafeSpeed = backStrafeSpeed;
    }

    @Override
    public void strafe(float forward, float right) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = forward;
        this.strafeRight = right;

        // Back-only → use backStrafeSpeed, otherwise sideStrafeSpeed
        if (forward < 0.0F && right == 0.0F) {
            this.speedModifier = this.backStrafeSpeed;
        } else {
            this.speedModifier = this.sideStrafeSpeed;
        }
    }

    @Override
    public void tick() {
        if (this.operation == Operation.STRAFE) {
            // --- custom STRAFE handling with "safe" drop / hazard check ---

            float baseSpeed = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float speed = (float) this.speedModifier * baseSpeed;

            float f = this.strafeForwards;
            float f1 = this.strafeRight;
            float len = Mth.sqrt(f * f + f1 * f1);
            if (len < 1.0F) {
                len = 1.0F;
            }

            len = speed / len;
            f *= len;
            f1 *= len;

            float sin = Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180F));
            float cos = Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180F));

            float dx = f * cos - f1 * sin;
            float dz = f1 * cos + f * sin;

            // First attempt: requested strafe direction
            if (!this.isSafeStrafe(dx, dz)) {
                // If we have sideways movement, try the opposite sideways direction
                if (this.strafeRight != 0.0F) {
                    // flip the horizontal strafe direction
                    this.strafeRight = -this.strafeRight;

                    // recompute with flipped right component
                    f = this.strafeForwards;
                    f1 = this.strafeRight;
                    len = Mth.sqrt(f * f + f1 * f1);
                    if (len < 1.0F) {
                        len = 1.0F;
                    }

                    len = speed / len;
                    f *= len;
                    f1 *= len;

                    sin = Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180F));
                    cos = Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180F));

                    dx = f * cos - f1 * sin;
                    dz = f1 * cos + f * sin;

                    // Second attempt: mirrored strafe
                    if (!this.isSafeStrafe(dx, dz)) {
                        // both directions are unsafe → cancel strafe this tick
                        this.mob.setZza(0.0F);
                        this.mob.setXxa(0.0F);
                        this.operation = Operation.WAIT;
                        return;
                    }
                } else {
                    // no sideways component (pure forward/back) and it's unsafe → cancel
                    this.mob.setZza(0.0F);
                    this.mob.setXxa(0.0F);
                    this.operation = Operation.WAIT;
                    return;
                }
            }

            // If we got here, we have a safe direction (original or flipped)
            this.mob.setSpeed(speed);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;

        } else {
            // MOVE_TO / JUMPING / WAIT → vanilla behavior
            super.tick();
        }
    }


    /**
     * Custom "isWalkable" for strafing:
     *  - allows flat or 1-block-down steps
     *  - blocks lava/water
     *  - blocks drops bigger than 1 block
     */
    /**
     * Custom "isWalkable" for strafing:
     *  - allows flat, 1-block-up, or up to 3-block-down steps
     *  - allows pushing into solid walls so spiders can climb them
     *  - blocks lava/water
     *  - blocks drops bigger than 3 blocks
     */
    /**
     * Custom "isWalkable" for strafing:
     *  - allows flat, 1-block-up, or up to 3-block-down steps
     *  - allows pushing into solid walls so spiders can climb them
     *  - blocks lava/water
     *  - blocks drops bigger than 3 blocks
     */
    /**
     * Custom "isWalkable" for strafing:
     *  - allows flat, 1-block-up, or up to 3-block-down steps
     *  - allows pushing into solid walls so spiders can climb them
     *  - blocks lava/water
     *  - blocks drops bigger than 3 blocks
     */
    private boolean isSafeStrafe(float worldOffsetX, float worldOffsetZ) {
        PathNavigation nav = this.mob.getNavigation();
        if (nav == null) {
            return true;
        }

        NodeEvaluator eval = nav.getNodeEvaluator();
        if (eval == null) {
            return true;
        }

        int x = Mth.floor(this.mob.getX() + (double)worldOffsetX);
        int z = Mth.floor(this.mob.getZ() + (double)worldOffsetZ);
        int y = this.mob.getBlockY();

        BlockPos pos = new BlockPos(x, y, z);

        // --- 0) Special case: pure BACK_UP, prefer allowing push into a wall behind ---
        if (this.strafeForwards < 0.0F && this.strafeRight == 0.0F) {
            // Compute a "backward" direction based on facing, then look a bit farther in that direction.
            float yawRad = this.mob.getYRot() * ((float)Math.PI / 180F);

            // Forward vector
            double forwardX = -Mth.sin(yawRad);
            double forwardZ =  Mth.cos(yawRad);

            // Backward direction (where we're trying to go)
            double backX = -forwardX;
            double backZ = -forwardZ;

            // Look about half a block behind
            BlockPos behindPos = BlockPos.containing(
                    this.mob.getX() + backX * 0.5D,
                    this.mob.getY(),
                    this.mob.getZ() + backZ * 0.5D
            );

            // If there's a solid block right behind (non-fluid, non-dangerous), allow pressing into it.
            if (!this.mob.level().getFluidState(behindPos).is(FluidTags.LAVA)
                    && !this.mob.level().getFluidState(behindPos).is(FluidTags.WATER)) {

                BlockPathTypes behindType = eval.getBlockPathType(
                        this.mob.level(),
                        behindPos.getX(), behindPos.getY(), behindPos.getZ()
                );
                if (!isDangerous(behindType)) {
                    var behindState = this.mob.level().getBlockState(behindPos);
                    if (!behindState.getCollisionShape(this.mob.level(), behindPos).isEmpty()) {
                        // solid-ish wall behind us → ok to back into it and climb
                        return true;
                    }
                }
            }
        }

        // 1) fluids at destination
        if (this.mob.level().getFluidState(pos).is(FluidTags.LAVA)
                || this.mob.level().getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }

        // 2) Path type at feet height
        BlockPathTypes feetType = eval.getBlockPathType(this.mob.level(), x, y, z);
        if (isDangerous(feetType)) {
            return false;
        }
        if (feetType == BlockPathTypes.WALKABLE) {
            return true;
        }

        // 3) Allow stepping up exactly 1 block if above is walkable and not hazardous
        int upY = y + 1;
        BlockPos upPos = new BlockPos(x, upY, z);
        if (!this.mob.level().getFluidState(upPos).is(FluidTags.LAVA)
                && !this.mob.level().getFluidState(upPos).is(FluidTags.WATER)) {

            BlockPathTypes upType = eval.getBlockPathType(this.mob.level(), x, upY, z);
            if (!isDangerous(upType) && upType == BlockPathTypes.WALKABLE) {
                return true;
            }
        }

        // 4) Climbable-wall exception at feet: solid collision directly at dest
        var stateAtFeet = this.mob.level().getBlockState(pos);
        if (!stateAtFeet.getCollisionShape(this.mob.level(), pos).isEmpty()) {
            return true;
        }

        // 5) Allow stepping down up to 3 blocks if a safe WALKABLE surface is found
        for (int depth = 1; depth <= 3; depth++) {
            int by = y - depth;
            BlockPos belowPos = new BlockPos(x, by, z);

            if (this.mob.level().getFluidState(belowPos).is(FluidTags.LAVA)
                    || this.mob.level().getFluidState(belowPos).is(FluidTags.WATER)) {
                return false;
            }

            BlockPathTypes belowType = eval.getBlockPathType(this.mob.level(), x, by, z);
            if (isDangerous(belowType)) {
                return false;
            }

            if (belowType == BlockPathTypes.WALKABLE) {
                return true;
            }
        }

        return false;
    }




    private boolean isDangerous(BlockPathTypes type) {
        if (type == null) return false;
        return switch (type) {
            case LAVA,
                 DAMAGE_FIRE,
                 DAMAGE_OTHER,
                 DANGER_FIRE,
                 DANGER_OTHER,
                 WATER -> true;
            default -> false;
        };
    }
}
