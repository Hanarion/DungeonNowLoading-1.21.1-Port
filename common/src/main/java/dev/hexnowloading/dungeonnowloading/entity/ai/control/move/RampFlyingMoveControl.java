package dev.hexnowloading.dungeonnowloading.entity.ai.control.move;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

/**
 * A flying MoveControl that ramps speed from 0 to max over a given time.
 * - Uses wanted position (MOVE_TO operation).
 * - Optional attribute scaling (FLYING_SPEED) like vanilla FlyingMoveControl.
 * - Smoothstep easing for a nice acceleration curve.
 */
public class RampFlyingMoveControl extends MoveControl {

    private final int maxTurn;           // degrees per tick to yaw/pitch toward travel
    private final boolean hoversInPlace; // if false, gravity re-enables when idle

    // Target state
    private Vec3 targetPos = null;
    private double stopRadius = 0.25;    // how close counts as "arrived"

    // Speed ramp
    private double maxSpeedBlocksPerTick = 0.4; // absolute speed cap (blocks/tick) before attribute scaling
    private int rampTimeTicks = 10;             // ticks to go 0 -> max
    private int rampAge = 0;                    // ticks since MOVE_TO started
    private boolean scaleByFlyingAttribute = true;

    public RampFlyingMoveControl(Mob mob, int maxTurn, boolean hoversInPlace) {
        super(mob);
        this.maxTurn = maxTurn;
        this.hoversInPlace = hoversInPlace;
    }

    // --------------------------
    // API: set destination
    // --------------------------
    @Override
    public void setWantedPosition(double x, double y, double z, double speedModifier) {
        // Keep vanilla-style API: treat speedModifier as "max speed multiplier" on top of defaults
        // We’ll interpret this as blocks/tick when scaleByFlyingAttribute == false,
        // or as a multiplier on FLyingSpeed when true.
        this.targetPos = new Vec3(x, y, z);
        this.operation = Operation.MOVE_TO;
        this.rampAge = 0;

        if (!this.scaleByFlyingAttribute) {
            // interpret as absolute blocks/tick
            this.maxSpeedBlocksPerTick = Math.max(0.0, speedModifier);
        } else {
            // interpret as multiplier (like vanilla): store as blocks/tick derived from attribute in tick()
            // leave maxSpeedBlocksPerTick as-is; we’ll compute per-tick using the attribute * modifier
            this.maxSpeedBlocksPerTick = Math.max(0.0, speedModifier); // treated as a multiplier placeholder
        }

        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        this.speedModifier = speedModifier;
    }

    /** Convenience: full control over ramp and stop radius. */
    public void setWantedPositionRamp(double x, double y, double z,
                                      double maxSpeedBlocksPerTick,
                                      int rampTimeTicks,
                                      double stopRadius,
                                      boolean scaleByFlyingAttribute) {
        this.targetPos = new Vec3(x, y, z);
        this.operation = Operation.MOVE_TO;
        this.rampAge = 0;

        this.maxSpeedBlocksPerTick = Math.max(0.0, maxSpeedBlocksPerTick);
        this.rampTimeTicks = Math.max(1, rampTimeTicks);
        this.stopRadius = Math.max(0.0, stopRadius);
        this.scaleByFlyingAttribute = scaleByFlyingAttribute;

        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        // keep speedModifier for compatibility (unused for absolute mode)
        this.speedModifier = maxSpeedBlocksPerTick;
    }

    /** Optional: switch to WAIT explicitly. */
    public void setWaitOperation() {
        this.operation = Operation.WAIT;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO || this.targetPos == null) {
            if (!hoversInPlace) this.mob.setNoGravity(false);
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
            return;
        }

        this.mob.setNoGravity(true);

        // Compute vector to target
        Vec3 to = this.targetPos.subtract(this.mob.position());
        double dist = to.length();
        if (dist <= this.stopRadius || dist < 1.0e-6) {
            // Arrived
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.operation = Operation.WAIT;
            return;
        }

        // Orientation (yaw/pitch) toward target, clamped by maxTurn
        float targetYaw = (float)(Mth.atan2(to.z, to.x) * (180F / Math.PI)) - 90F;
        this.mob.setYRot(rotlerp(this.mob.getYRot(), targetYaw, this.maxTurn));

        double horiz = Math.sqrt(to.x * to.x + to.z * to.z);
        float targetPitch = (float)(-(Mth.atan2(to.y, horiz) * (180F / Math.PI)));
        this.mob.setXRot(rotlerp(this.mob.getXRot(), targetPitch, this.maxTurn));

        // Easing: smoothstep from 0 → 1 over rampTimeTicks
        double p = Mth.clamp((double) this.rampAge / (double) this.rampTimeTicks, 0.0, 1.0);
        double ease = p * p * (3.0 - 2.0 * p); // smoothstep

        // Final speed (blocks/tick)
        double maxSpeed;
        if (scaleByFlyingAttribute) {
            // vanilla-like: use attribute * "speedModifier" (here we treat maxSpeedBlocksPerTick as the modifier)
            double attr = this.mob.getAttributeValue(Attributes.FLYING_SPEED);
            maxSpeed = Math.max(0.0, attr * this.maxSpeedBlocksPerTick);
        } else {
            maxSpeed = this.maxSpeedBlocksPerTick;
        }

        // Current speed along the ramp
        double speedNow = ease * maxSpeed;

        // Velocity toward target at speedNow
        Vec3 dir = to.scale(1.0 / dist);
        Vec3 vel = dir.scale(speedNow);
        this.mob.setDeltaMovement(vel);

        // progress time
        if (this.rampAge < this.rampTimeTicks) this.rampAge++;
    }
}
