package dev.hexnowloading.dungeonnowloading.entity.ai;

import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FairkeeperOurosDropVertexPillarGoal extends StoppableGoal {

    private final FairkeeperOurosEntity ouros;
    private FairkeeperOurosEntity.FairkeeperOurosState state;
    ImmutablePair<Float, List<Vec3>> pattern;

    private FairkeeperOurosPartEntity currentPart;
    private int arenaSize;
    private BlockPos arenaCenter;
    private int targetIndex = 0;
    private int dropIndex = 0;
    private List<Vec3> patternPos = new ArrayList<>();
    private final double speed;

    private static final float FULL_ARENA_SIZE = 36F;

    public FairkeeperOurosDropVertexPillarGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros, double speed, ImmutablePair<Float, List<Vec3>> pattern) {
        this.ouros = ouros;
        this.state = state;
        this.pattern = pattern;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.ouros.getTarget() != null && this.ouros.getTarget().isAlive() && this.ouros.isState(state);
    }

    @Override
    public void start() {
        super.start();
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        if (caller != null) {
            this.arenaSize = caller.getArenaSize();
            BlockPos callerPos = caller.blockPosition();
            this.arenaCenter = new BlockPos(callerPos.getX(), (int) this.ouros.getBoundingBox().maxY, callerPos.getZ());
        }
        this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        targetIndex = 0;
        dropIndex = 0;
        int degree = new int[]{0, 90, 180, 270}[new Random().nextInt(4)];
        patternPos = rotateVec3ListY(pattern.getRight().stream().map(vec -> new Vec3(vec.x, 0, vec.z)).collect(Collectors.toList()), degree);
    }

    @Override
    public void stop() {
        this.ouros.stopAttacking(20);
    }

    @Override
    public void tick() {

        if (this.currentPart == null || this.currentPart.isTail()) {
            this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        }

        if (dropIndex >= patternPos.size()) {
            this.stopGoal();
            return;
        }

        if (targetIndex >= patternPos.size()) {
            targetIndex = 0;
        }

        float ratio = pattern.getLeft();

        Vec3 relativeTarget = patternPos.get(targetIndex);

        double targetX = arenaCenter.getX() + 0.5F + (relativeTarget.x * arenaSize * ratio);
        double targetZ = arenaCenter.getZ() + 0.5F + (relativeTarget.z * arenaSize * ratio);

        BlockPos targetBlockPos = BlockPos.containing(targetX, this.ouros.getY(), targetZ);

        double distanceSq = this.ouros.position().distanceToSqr(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());

        if (distanceSq < 1.5) {
            targetIndex++;
            if (targetIndex >= patternPos.size()) {
                targetIndex = 0;
            }
        }

        Vec3 dropTarget = patternPos.get(dropIndex);

        double dropTargetX = arenaCenter.getX() + 0.5F + (dropTarget.x * arenaSize * ratio);
        double dropTargetZ = arenaCenter.getZ() + 0.5F + (dropTarget.z * arenaSize * ratio);

        BlockPos dropTargetBlockPos = BlockPos.containing(dropTargetX, this.currentPart.getY(), dropTargetZ);

        if (this.currentPart.isState(FairkeeperOurosPartEntity.FairkeeperOurosPartState.IDLE)) {
            this.currentPart.dropVertexPillar(dropTargetBlockPos);
        }

        double distanceSqChild = this.currentPart.position().distanceToSqr(dropTargetBlockPos.getX(), dropTargetBlockPos.getY(), dropTargetBlockPos.getZ());

        if (distanceSqChild < 1.5) {
            dropIndex++;

            this.currentPart = (FairkeeperOurosPartEntity) this.currentPart.getChild();

            if (dropIndex >= patternPos.size()) {
                this.stopGoal();
                return;
            }
        }

        this.ouros.getMoveControl().setWantedPosition(targetBlockPos.getX(), this.ouros.getBoundingBox().maxY, targetBlockPos.getZ(), speed);
    }

    public static List<Vec3> rotateVec3ListY(List<Vec3> original, int degrees) {
        return original.stream().map(vec -> rotateY(vec, degrees)).collect(Collectors.toList());
    }

    private static Vec3 rotateY(Vec3 vec, int degrees) {
        switch (degrees % 360) {
            case 0:
                return vec;
            case 90:
                return new Vec3(vec.z, vec.y, -vec.x);
            case 180:
                return new Vec3(-vec.x, vec.y, -vec.z);
            case 270:
                return new Vec3(-vec.z, vec.y, vec.x);
            default:
                throw new IllegalArgumentException("Rotation must be 0, 90, 180, or 270 degrees");
        }
    }

    public static ImmutablePair<Float, List<Vec3>> PATTERN_LINE_CENTER = ImmutablePair.of(30F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(0, 0, 1),
            new Vec3(0, 0, 1/2F),
            new Vec3(0, 0, 0),
            new Vec3(0, 0, -1/2F),
            new Vec3(0, 0, -1)
    ));

    public static ImmutablePair<Float, List<Vec3>> PATTERN_LINE_OUTER = ImmutablePair.of(30F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(1, 0, 1),
            new Vec3(1, 0, 1/2F),
            new Vec3(1, 0, 0),
            new Vec3(1, 0, -1/2F),
            new Vec3(1, 0, -1)
    ));

    public static ImmutablePair<Float, List<Vec3>> PATTERN_LINE_INNER = ImmutablePair.of(30F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(1/2F, 0, 1/2F),
            new Vec3(1/2F, 0, 0),
            new Vec3(1/2F, 0, -1/2F)
    ));
}
