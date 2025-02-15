package dev.hexnowloading.dungeonnowloading.entity.ai;

import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BorusArrowEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FairkeeperBorosCircleAndShootArrowGoal extends Goal {
    private final FairkeeperBorosEntity boros;
    private Entity circlingTarget;
    private final double radius;
    private final double speed;
    private final boolean clockwise;
    private final boolean circlePlayer;
    private double angle;
    private double travelledAngle;  // Stores the starting angle for tracking revolutions
    private double targetX, targetY, targetZ;
    private int arenaSize;
    private BlockPos arenaCenter;
    private FairkeeperBorosEntity.FairkeeperBorosState state;

    private ShootingPattern pattern;
    private List<FairkeeperBorosPartEntity> partList = new ArrayList<>();
    private int stoppingTick;
    private int targetIndex = 0;

    private static final double THRESHOLD = 2.0;
    private static final int STOP_DURATION = 32;
    private static final int SHOOT_ARROW_TICK = 15;
    private static final int BEAM_TICK = 30;
    private static final float FULL_ARENA_SIZE = 49F;

    public FairkeeperBorosCircleAndShootArrowGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, double speed, ShootingPattern pattern) {
        this.state = state;
        this.boros = boros;
        this.radius = pattern.ratio * FULL_ARENA_SIZE / 2;
        this.speed = speed;
        this.clockwise = pattern.rotateClockWise;
        this.circlePlayer = pattern.circlePlayer;
        this.pattern = pattern;
        this.angle = 0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        boolean b;
        if (this.circlePlayer) {
            this.circlingTarget = this.boros.getTarget();
            b = this.circlingTarget != null && this.circlingTarget.isAlive();
        } else {
            this.circlingTarget = this.boros.getCaller();
            b = this.circlingTarget != null;
        }
        return b && this.boros.isState(state);
    }

    @Override
    public void start() {
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.boros.getCaller();
        if (caller != null) {
            this.arenaSize = caller.getArenaSize();
            BlockPos callerPos = caller.blockPosition();
            this.arenaCenter = new BlockPos(callerPos.getX(), (int) this.boros.getY(), callerPos.getZ());
        }

        FairkeeperBorosPartEntity currentPart = (FairkeeperBorosPartEntity) this.boros.getChild();
        if (currentPart == null) {
            this.boros.stopAttacking(20);
            return;
        }
        this.partList.add(currentPart);
        for (int i = 0; i < 12; i++) {
            currentPart = (FairkeeperBorosPartEntity) currentPart.getChild();
            if (currentPart == null) {
                this.boros.stopAttacking(20);
                return;
            }
            this.partList.add(currentPart);
        }

        if (this.circlingTarget != null) {
            double centerX = circlePlayer ? this.circlingTarget.getX() : this.arenaCenter.getX();
            double centerZ = circlePlayer ? this.circlingTarget.getZ() : this.arenaCenter.getZ();

            double deltaX = this.boros.getX() - centerX;
            double deltaZ = this.boros.getZ() - centerZ;
            this.angle = Math.toDegrees(Math.atan2(deltaZ, deltaX));
            this.angle = (this.angle + 360) % 360;
        } else {
            this.boros.stopAttacking(20);
            return;
        }

        this.travelledAngle = 0;
        targetIndex = 0;
        this.stoppingTick = 0;
        updateTargetPosition();
    }

    @Override
    public void tick() {

        if (this.stoppingTick > 0) {
            this.stoppingTick--;

            if (stoppingTick == reducedTickDelay(BEAM_TICK)) {
                pattern.positionList().get(targetIndex).getMiddle().stream().forEach(partIndex -> {
                    ShootingType shootingType = pattern.positionList().get(targetIndex).getLeft();
                    FairkeeperBorosPartEntity part = this.partList.get(partIndex);
                    if (shootingType == ShootingType.BOTH || shootingType == ShootingType.LEFT) {
                        this.spawnRedstoneTrail(part, 90, 0, 0);
                    }
                    if (shootingType == ShootingType.BOTH || shootingType == ShootingType.RIGHT) {
                        this.spawnRedstoneTrail(part, -90, 0, 0);
                    }
                    if (shootingType == ShootingType.TRIPLE_BOTH || shootingType == ShootingType.TRIPLE_LEFT) {
                        this.spawnRedstoneTrail(part, 90, -1.5, 0);
                        this.spawnRedstoneTrail(part, 90, 0, 0);
                        this.spawnRedstoneTrail(part, 90, 1.5, 0);
                    }
                    if (shootingType == ShootingType.TRIPLE_BOTH || shootingType == ShootingType.TRIPLE_RIGHT) {
                        this.spawnRedstoneTrail(part, -90, -1.5, 0);
                        this.spawnRedstoneTrail(part, -90, 0, 0);
                        this.spawnRedstoneTrail(part, -90, 1.5, 0);
                    }
                });
            }

            if (stoppingTick == reducedTickDelay(SHOOT_ARROW_TICK)) {
                pattern.positionList().get(targetIndex).getMiddle().stream().forEach(partIndex -> {
                    ShootingType shootingType = pattern.positionList().get(targetIndex).getLeft();
                    FairkeeperBorosPartEntity part = this.partList.get(partIndex);
                    if (shootingType == ShootingType.BOTH || shootingType == ShootingType.LEFT) {
                        this.shootArrow(part, 90, 0, 0);
                    }
                    if (shootingType == ShootingType.BOTH || shootingType == ShootingType.RIGHT) {
                        this.shootArrow(part, -90, 0, 0);
                    }
                    if (shootingType == ShootingType.TRIPLE_BOTH || shootingType == ShootingType.TRIPLE_LEFT) {
                        this.shootArrow(part, 90, -1.5, 0);
                        this.shootArrow(part, 90, 0, 0);
                        this.shootArrow(part, 90, 1.5, 0);
                    }
                    if (shootingType == ShootingType.TRIPLE_BOTH || shootingType == ShootingType.TRIPLE_RIGHT) {
                        this.shootArrow(part, -90, -1.5, 0);
                        this.shootArrow(part, -90, 0, 0);
                        this.shootArrow(part, -90, 1.5, 0);
                    }
                });
            }

            if (stoppingTick <= 0) {
                targetIndex++;

                if (targetIndex >= pattern.positionList().size()) {
                    this.boros.stopAttacking(20);
                    return;
                }

                double difference = pattern.positionList.get(targetIndex).getRight() - pattern.positionList.get(targetIndex - 1).getRight();

                if (difference * difference < 1.0E-7) {
                    stoppingTick = reducedTickDelay(STOP_DURATION);
                    return;
                }
            } else {
                return;
            }
        }

        if (this.circlingTarget != null) {
            double deltaX = this.boros.getX() - this.targetX;
            double deltaZ = this.boros.getZ() - this.targetZ;
            if ((deltaX * deltaX + deltaZ * deltaZ) < THRESHOLD * THRESHOLD) {
                this.angle += this.clockwise ? -10 : 10;
                this.angle = (this.angle + 360) % 360;
                this.travelledAngle += this.clockwise ? -10 : 10;

                if (this.hasCompletedRevolution()) {
                    this.stoppingTick = reducedTickDelay(STOP_DURATION);
                    return;
                }

                updateTargetPosition();
            }

            this.boros.getMoveControl().setWantedPosition(this.targetX, this.targetY, this.targetZ, this.speed);
        }
    }

    private void updateTargetPosition() {
        double angleRad = Math.toRadians(this.angle);

        // Determine the center of the circle based on `circlePlayer`
        double centerX = circlePlayer ? this.circlingTarget.getX() : this.arenaCenter.getX();
        double centerZ = circlePlayer ? this.circlingTarget.getZ() : this.arenaCenter.getZ();

        // Calculate potential target position on the circle
        double potentialX = centerX + this.radius * Math.cos(angleRad);
        double potentialZ = centerZ + this.radius * Math.sin(angleRad);

        // Define arena boundaries
        double minX = this.arenaCenter.getX() - this.arenaSize;
        double maxX = this.arenaCenter.getX() + this.arenaSize;
        double minZ = this.arenaCenter.getZ() - this.arenaSize;
        double maxZ = this.arenaCenter.getZ() + this.arenaSize;

        // Clamp position inside arena boundaries
        this.targetX = Math.max(minX, Math.min(maxX, potentialX));
        this.targetZ = Math.max(minZ, Math.min(maxZ, potentialZ));

        // Maintain the Y-coordinate of the circling target
        this.targetY = this.circlingTarget.getY();
    }

    private boolean hasCompletedRevolution() {
        System.out.println(this.angle + " : " + this.travelledAngle);
        return Math.abs(this.travelledAngle) >= this.pattern.positionList().get(targetIndex).getRight();
    }

    private void shootArrow(FairkeeperBorosPartEntity partEntity, float angleOffset, double rxOffset, double rzOffset) {
        double viewDistance = 2.0F;

        float borosFacingYaw = partEntity.getYRot();

        double finalAngle = Math.toRadians(borosFacingYaw - angleOffset);
        double cosFinal = Math.cos(finalAngle);
        double sinFinal = Math.sin(finalAngle);

        Vec3 shootDirection = new Vec3(-sinFinal, 0, cosFinal).normalize();

        double rotatedRxOffset = rxOffset * cosFinal - rzOffset * sinFinal;
        double rotatedRzOffset = rxOffset * sinFinal + rzOffset * cosFinal;

        Vec3 startPos = new Vec3(
                partEntity.getX() + rotatedRxOffset,
                partEntity.getY() + partEntity.getBoundingBox().getYsize() / 2,
                partEntity.getZ() + rotatedRzOffset
        );

        Vec3 arrowPos = startPos.add(shootDirection.scale(viewDistance));

        BorusArrowEntity arrow = new BorusArrowEntity(this.boros, this.boros.level());
        arrow.setPos(arrowPos);
        arrow.shootFromRotation(partEntity, partEntity.getXRot(), borosFacingYaw - angleOffset, 0.0F, 2.0F, 1.0F);
        this.boros.level().addFreshEntity(arrow);
    }



    private void spawnRedstoneTrail(FairkeeperBorosPartEntity partEntity, float angleOffset, double rxOffset, double rzOffset) {
        double viewDistance = 2.0F;

        float borosFacingYaw = partEntity.getYRot();

        double finalAngle = Math.toRadians(borosFacingYaw - angleOffset);
        double cosFinal = Math.cos(finalAngle);
        double sinFinal = Math.sin(finalAngle);

        Vec3 beamDirection = new Vec3(-sinFinal, 0, cosFinal).normalize();

        double rotatedRxOffset = rxOffset * cosFinal - rzOffset * sinFinal;
        double rotatedRzOffset = rxOffset * sinFinal + rzOffset * cosFinal;

        Vec3 startPos = new Vec3(
                partEntity.getX() + rotatedRxOffset,
                partEntity.getY() + partEntity.getBoundingBox().getYsize() / 2,
                partEntity.getZ() + rotatedRzOffset
        ).add(beamDirection.scale(viewDistance));

        for (int i = 0; i <= 30; i++) {
            Vec3 particlePos = startPos.add(beamDirection.scale(i));
            ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(DNLParticleTypes.ARROW_HAZARD_INDICATOR.get(), 0.5F);
            ((ServerLevel) partEntity.level()).sendParticles(particleData, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
        }
    }

    private record ShootingPattern(boolean circlePlayer, boolean rotateClockWise, float ratio, ImmutableList<Triple<ShootingType, List<Integer>, Float>> positionList) {}

    private enum ShootingType {
        NONE,
        LEFT,
        RIGHT,
        BOTH,
        TRIPLE_LEFT,
        TRIPLE_RIGHT,
        TRIPLE_BOTH
    }

    private static List<Integer> ODD_PARTS = List.of(1, 3, 5, 7, 9, 11);
    private static List<Integer> EVEN_PARTS = List.of(0, 2, 4, 6, 8, 10, 12);
    private static List<Integer> ALL_PARTS = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

    public static ShootingPattern PATTERN_SMALL_CIRLCE = new ShootingPattern(false, true, 14F/FULL_ARENA_SIZE, ImmutableList.of(
            Triple.of(ShootingType.RIGHT, ODD_PARTS, 360F),
            Triple.of(ShootingType.RIGHT, EVEN_PARTS, 360F),
            Triple.of(ShootingType.RIGHT, ODD_PARTS, 360F)
    ));

    public static ShootingPattern PATTERN_LARGE_CIRCLE = new ShootingPattern(false, true, 29F/FULL_ARENA_SIZE, ImmutableList.of(
            Triple.of(ShootingType.LEFT, ODD_PARTS, 120F),
            Triple.of(ShootingType.LEFT, EVEN_PARTS, 240F),
            Triple.of(ShootingType.LEFT, ODD_PARTS, 360F)
    ));

    public static ShootingPattern PATTERN_PLAYER_LARGE_CIRCLE = new ShootingPattern(false, true, 29F/FULL_ARENA_SIZE, ImmutableList.of(
            Triple.of(ShootingType.LEFT, ODD_PARTS, 120F),
            Triple.of(ShootingType.LEFT, EVEN_PARTS, 240F),
            Triple.of(ShootingType.LEFT, ODD_PARTS, 360F)
    ));
}
