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
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FairkeeperBorosShootArrowGoal extends StoppableGoal {
    private final FairkeeperBorosEntity boros;
    private FairkeeperBorosEntity.FairkeeperBorosState state;
    ShootingPattern pattern;

    private FairkeeperBorosPartEntity currentPart;
    private int arenaSize;
    private BlockPos arenaCenter;
    private int targetIndex = 0;
    private final double speed;
    private List<FairkeeperBorosPartEntity> partList = new ArrayList<>();
    private int stoppingTick;
    private boolean forceStop;

    private static final float FULL_ARENA_SIZE = 49F;
    private static final int STOP_DURATION = 32;
    private static final int SHOOT_ARROW_TICK = 15;
    private static final int BEAM_TICK = 30;

    public FairkeeperBorosShootArrowGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, double speed, ShootingPattern pattern) {
        this.boros = boros;
        this.state = state;
        this.pattern = pattern;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.boros.getTarget() != null && this.boros.getTarget().isAlive() && this.boros.isState(state);
    }

    @Override
    public void start() {
        super.start();
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.boros.getCaller();
        if (caller != null) {
            this.arenaSize = caller.getArenaSize();
            BlockPos callerPos = caller.blockPosition();
            this.arenaCenter = new BlockPos(callerPos.getX(), (int) this.boros.getY(), callerPos.getZ());
        }
        this.currentPart = (FairkeeperBorosPartEntity) this.boros.getChild();
        if (this.currentPart == null) {
            this.stopGoal();
            return;
        }
        this.partList.add(this.currentPart);
        for (int i = 0; i < 12; i++) {
            this.currentPart = (FairkeeperBorosPartEntity) this.currentPart.getChild();
            if (this.currentPart == null) {
                this.stopGoal();
                return;
            }
            this.partList.add(this.currentPart);
        }
        targetIndex = 0;
        stoppingTick = 0;
        if (this.pattern.shiftStartingPoint) {
            rotatePatternToClosest();
        }
    }

    @Override
    public void stop() {
        this.boros.stopAttacking(20);
    }

    @Override
    public void tick() {

        if (this.stoppingTick > 0) {
            this.stoppingTick--;

            if (stoppingTick == reducedTickDelay(BEAM_TICK)) {
                pattern.positionList().get(targetIndex).getMiddle().stream().forEach(partIndex ->{
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
                    this.boros.playBeamSound(part.getX(), part.getY(), part.getZ());
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
                    this.boros.playArrowSound(part.getX(), part.getY(), part.getZ());
                });
            }

            if (stoppingTick <= 0) {
                targetIndex++;

                if (targetIndex >= pattern.positionList().size()) {
                    this.stopGoal();
                    return;
                }

                double difference = pattern.positionList().get(targetIndex).getRight().lengthSqr() - pattern.positionList.get(targetIndex - 1).getRight().lengthSqr();

                if (difference * difference < 1.0E-7) {
                    stoppingTick = reducedTickDelay(STOP_DURATION);
                    return;
                }
            } else {
                return;
            }
        }

        float ratio = pattern.ratio();

        Vec3 relativeTarget = pattern.positionList().get(targetIndex).getRight();

        double targetX = arenaCenter.getX() + 0.5F + (relativeTarget.x * arenaSize * ratio);
        double targetZ = arenaCenter.getZ() + 0.5F + (relativeTarget.z * arenaSize * ratio);

        BlockPos targetBlockPos = BlockPos.containing(targetX, this.boros.getY(), targetZ);

        double distanceSq = this.boros.position().distanceToSqr(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());

        if (distanceSq < 1.5) {
            if (pattern.positionList.get(targetIndex).getMiddle().isEmpty()) {
                this.targetIndex++;
            } else {
                this.stoppingTick = reducedTickDelay(STOP_DURATION);
                return;
            }
        }

        this.boros.getMoveControl().setWantedPosition(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ(), speed);
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



    private void rotatePatternToClosest() {
        List<Triple<ShootingType, List<Integer>, Vec3>> originalList = pattern.positionList();

        // Try rotating the first vector by 0, 90, 180, and 270 degrees
        Vec3 firstVector = originalList.get(0).getRight();
        double minDistance = Double.MAX_VALUE;
        int bestRotation = 0;

        Vec3 borosPos = this.boros.position();

        // Test all 4 rotation angles
        for (int angle : List.of(0, 90, 180, 270)) {
            Vec3 rotatedVector = rotateVector(firstVector, angle);
            Vec3 absolutePos = new Vec3(
                    arenaCenter.getX() + rotatedVector.x * arenaSize * pattern.ratio(),
                    this.boros.getY(),
                    arenaCenter.getZ() + rotatedVector.z * arenaSize * pattern.ratio()
            );
            double distance = borosPos.distanceToSqr(absolutePos);

            if (distance < minDistance) {
                minDistance = distance;
                bestRotation = angle;
            }
        }

        // Apply the best rotation to all vectors
        List<Triple<ShootingType, List<Integer>, Vec3>> rotatedList = new ArrayList<>();
        for (Triple<ShootingType, List<Integer>, Vec3> entry : originalList) {
            Vec3 rotatedVec = rotateVector(entry.getRight(), bestRotation);
            rotatedList.add(Triple.of(entry.getLeft(), entry.getMiddle(), rotatedVec));
        }

        // Update the pattern with the rotated vectors
        this.pattern = new ShootingPattern(pattern.shiftStartingPoint(), pattern.ratio(), ImmutableList.copyOf(rotatedList));
    }

    private Vec3 rotateVector(Vec3 vector, int degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double newX = vector.x * cos - vector.z * sin;
        double newZ = vector.x * sin + vector.z * cos;

        return new Vec3(newX, vector.y, newZ);
    }

    private record ShootingPattern(boolean shiftStartingPoint, float ratio, ImmutableList<Triple<ShootingType, List<Integer>, Vec3>> positionList) {}

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

    public static ShootingPattern PATTERN_LINE = new ShootingPattern(true, 39F/FULL_ARENA_SIZE, ImmutableList.of(
            Triple.of(ShootingType.NONE, List.of(), new Vec3(0, 0, 1)),
            Triple.of(ShootingType.BOTH, ODD_PARTS, new Vec3(0, 0, -1)),
            Triple.of(ShootingType.BOTH, EVEN_PARTS, new Vec3(0, 0, -1)),
            Triple.of(ShootingType.BOTH, ODD_PARTS, new Vec3(0, 0, -1))
    ));

    public static ShootingPattern PATTERN_SLITHER = new ShootingPattern(true, 39F/FULL_ARENA_SIZE, ImmutableList.of(
            Triple.of(ShootingType.NONE, List.of(), new Vec3(1, 0, 1)),
            Triple.of(ShootingType.RIGHT, ALL_PARTS, new Vec3(-1, 0, 1))
            /*Triple.of(ShootingType.NONE, List.of(), new Vec3(-1, 0, 5/6F)),
            Triple.of(ShootingType.LEFT, ALL_PARTS, new Vec3(1, 0, 5/6F)),
            Triple.of(ShootingType.NONE, List.of(), new Vec3(1, 0, 4/6F)),
            Triple.of(ShootingType.RIGHT, ALL_PARTS, new Vec3(-1, 0, 4/6F))*/
    ));
}
