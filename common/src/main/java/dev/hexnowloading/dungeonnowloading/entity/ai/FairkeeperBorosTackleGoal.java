package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperBorosTackleGoal extends StoppableGoal {

    private final FairkeeperBorosEntity boros;
    private final FairkeeperBorosEntity.FairkeeperBorosState state;
    private final double speed;
    private LivingEntity target;
    private Vec3 targetPosition;
    private final double tackleRange;
    private final double addSpeedBy;
    private int tackleDuration;
    private int tackleCooldown;
    private int arenaSize;
    private int loopCount;
    private int totalDuration;
    private int crashDuration;
    private BlockPos arenaCenter;

    private final int TACKLE_DURATION = reducedTickDelay(40);
    private final int SLOWDOWN_DURATION = reducedTickDelay(10);
    private final int TACKLE_COOLDOWN = reducedTickDelay(40);
    private final int EXPIRY_DURATION = reducedTickDelay(300);
    private final int CRASH_DURATION = reducedTickDelay(40);
    private final int TOTAL_LOOP = 3;
    private final double SLOWDOWN_SPEED_BY = 0.5;
    private static final float OFFSET_FROM_WALL = 1.5f;
/*    private final double ACCELERATE_RANGE = 6.0;
    private final double ACCELERATE_SPEED_BY = 0.3;*/

    public FairkeeperBorosTackleGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, double speed, double tackleRange, double addSpeedBy) {
        this.state = state;
        this.boros = boros;
        this.speed = speed;
        this.tackleRange = tackleRange;
        this.addSpeedBy = addSpeedBy;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = this.boros.getTarget();
        return this.target != null && this.target.isAlive() && this.boros.isState(state);
    }

    @Override
    public void start() {
        this.targetPosition = this.target.getPosition(1.0F);
        FairkeeperSerpentCallerEntity caller = ((FairkeeperSerpentCallerEntity) boros.getCaller());
        this.arenaSize = caller != null ? caller.getArenaSize() : 0;
        this.arenaCenter = caller != null ? caller.blockPosition() : BlockPos.ZERO;
        this.loopCount = 0;
        this.tackleCooldown = 0;
        this.totalDuration = EXPIRY_DURATION;
        super.start();
    }

    @Override
    public void stop() {
        this.boros.stopAttacking(20);
        this.boros.transitionTo(FairkeeperBorosEntity.FairkeeperBorosAnimationState.MOUTH_CLOSE);
    }

    @Override
    public void tick() {

        double updatedSpeed = this.speed;
        double distanceSqr = this.boros.distanceToSqr(this.target);
        targetPosition = this.target.getPosition(1.0f);

        if (this.totalDuration > 0) {
            this.totalDuration--;
        } else if (this.tackleDuration <= 0) {
            //this.boros.setAnimationState(FairkeeperBorosEntity.FairkeeperBorosAnimationState.MOUTH_CLOSE);
            this.stopGoal();
            return;
        }

        if (this.tackleCooldown > 0) {
            this.tackleCooldown--;
        }

        if (this.tackleCooldown <= 0 && this.tackleDuration <= 0 && distanceSqr < this.tackleRange * this.tackleRange) {
            this.tackleDuration = TACKLE_DURATION + SLOWDOWN_DURATION;
            this.boros.playMouthOpenAndClose();
            //this.boros.setAnimationState(FairkeeperBorosEntity.FairkeeperBorosAnimationState.MOUTH_OPEN);
        }

        if (this.tackleDuration > 0) {
            this.tackleDuration--;
            if (this.tackleDuration > TACKLE_DURATION) {
                updatedSpeed = this.speed - SLOWDOWN_SPEED_BY;
            } else {
                updatedSpeed = this.speed + this.addSpeedBy;
            }
            double viewDistance = 1.5F;
            Vec3 offset = this.boros.getViewVector(1.0F).scale(viewDistance);
            Vec3 potentialTargetPosition = this.boros.position().add(offset);
            double minX = this.arenaCenter.getX() + 0.5F - this.arenaSize + OFFSET_FROM_WALL;
            double maxX = this.arenaCenter.getX() + 0.5F + this.arenaSize - OFFSET_FROM_WALL;
            double minZ = this.arenaCenter.getZ() + 0.5F - this.arenaSize + OFFSET_FROM_WALL;
            double maxZ = this.arenaCenter.getZ() + 0.5F + this.arenaSize - OFFSET_FROM_WALL;
            boolean isInsideArena = potentialTargetPosition.x >= minX && potentialTargetPosition.x <= maxX && potentialTargetPosition.z >= minZ && potentialTargetPosition.z <= maxZ;
            if (isInsideArena) {
                this.targetPosition = potentialTargetPosition;
            } else {
                this.crashDuration = CRASH_DURATION;
                this.tackleDuration = 0;
            }
            if (this.tackleDuration <= 0) {
                if (loopCount == TOTAL_LOOP) {
                    this.stopGoal();
                    return;
                } else {
                    loopCount++;
                    this.tackleCooldown = TACKLE_COOLDOWN;
                }
            }
        }

        if (this.crashDuration > 0) {
            this.crashDuration--;
            updatedSpeed = this.speed - SLOWDOWN_SPEED_BY;
        }

        this.boros.getMoveControl().setWantedPosition(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, updatedSpeed);
    }
}
