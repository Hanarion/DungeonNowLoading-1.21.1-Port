package dev.hexnowloading.dungeonnowloading.entity.ai;

import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.ShieldingStonePillarProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.EnumSet;
import java.util.List;

public class FairkeeperOurosVertexPillarDropGoal extends Goal {

    private final FairkeeperOurosEntity ouros;
    private FairkeeperOurosEntity.FairkeeperOurosState state;
    ImmutablePair<Float, List<Vec3>> pattern;

    private FairkeeperOurosPartEntity currentPart;
    private int arenaSize;
    private BlockPos arenaCenter;
    private int targetIndex = 0;
    private int dropIndex = 0;
    private final double speed;

    private static final float FULL_ARENA_SIZE = 49F;

    public FairkeeperOurosVertexPillarDropGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros, double speed, ImmutablePair<Float, List<Vec3>> pattern) {
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
        FairkeeperSerpentCallerEntity caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        if (caller != null) {
            this.arenaSize = caller.getArenaSize();
            BlockPos callerPos = caller.blockPosition();
            this.arenaCenter = new BlockPos(callerPos.getX(), (int) this.ouros.getBoundingBox().maxY, callerPos.getZ());
        }
        this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        targetIndex = 0;
        dropIndex = 0;
    }

    @Override
    public void tick() {

        if (this.currentPart == null || this.currentPart.isTail()) {
            this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        }

        if (dropIndex >= pattern.getRight().size()) {
            this.ouros.stopAttacking(10 * this.pattern.getRight().size());
            return;
        }

        if (targetIndex >= pattern.getRight().size()) {
            targetIndex = 0;
        }

        float ratio = pattern.getLeft();

        Vec3 relativeTarget = pattern.getRight().get(targetIndex);

        double targetX = arenaCenter.getX() + 0.5F + (relativeTarget.x * arenaSize * ratio);
        double targetZ = arenaCenter.getZ() + 0.5F + (relativeTarget.z * arenaSize * ratio);

        BlockPos targetBlockPos = BlockPos.containing(targetX, this.ouros.getY(), targetZ);

        double distanceSq = this.ouros.position().distanceToSqr(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());

        if (distanceSq < 1.5) {
            targetIndex++;
            if (targetIndex >= pattern.getRight().size()) {
                targetIndex = 0;
            }
        }

        Vec3 dropTarget = pattern.getRight().get(dropIndex);

        double dropTargetX = arenaCenter.getX() + 0.5F + (dropTarget.x * arenaSize * ratio);
        double dropTargetZ = arenaCenter.getZ() + 0.5F + (dropTarget.z * arenaSize * ratio);

        BlockPos dropTargetBlockPos = BlockPos.containing(dropTargetX, this.currentPart.getY(), dropTargetZ);

        double distanceSqChild = this.currentPart.position().distanceToSqr(dropTargetBlockPos.getX(), dropTargetBlockPos.getY(), dropTargetBlockPos.getZ());

        if (distanceSqChild < 1.5) {
            dropIndex++;
            Level level = this.ouros.level();
            ShieldingStonePillarProjectileEntity stonePillar = new ShieldingStonePillarProjectileEntity(this.ouros.level(), this.ouros, 0.8F);
            stonePillar.setPos(dropTargetBlockPos.getX() + 0.5, this.currentPart.getBoundingBox().minY, dropTargetBlockPos.getZ() + 0.5);
            level.addFreshEntity(stonePillar);

            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(dropTargetBlockPos.getX(), this.currentPart.getY(), dropTargetBlockPos.getZ());

            while (mutableBlockPos.getY() > level.getMinBuildHeight() && !level.getBlockState(mutableBlockPos).blocksMotion()) {
                mutableBlockPos.move(Direction.DOWN);
            }

            BlockState blockState = level.getBlockState(mutableBlockPos);
            if (blockState.blocksMotion()) {
                ((ServerLevel) level).sendParticles(new ScalableAxisParticleType.ScalableAxisParticleData(DNLParticleTypes.REDSTONE_HAZARD_INDICATOR_PARTICLE.get(), 0, 90, 1.0F), mutableBlockPos.getX() + 0.5F, mutableBlockPos.getY() + 1.05F, mutableBlockPos.getZ() + 0.5F, 1, 0, 0, 0, 0);
            }

            this.currentPart = (FairkeeperOurosPartEntity) this.currentPart.getChild();

            if (dropIndex >= pattern.getRight().size()) {
                this.ouros.stopAttacking(10 * this.pattern.getRight().size());
                return;
            }
        }

        this.ouros.getMoveControl().setWantedPosition(targetBlockPos.getX(), this.ouros.getBoundingBox().maxY, targetBlockPos.getZ(), speed);
    }

    public static ImmutablePair<Float, List<Vec3>> PATTERN_SMALL_SQUARE = ImmutablePair.of(15F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(1, 0, 1),
            new Vec3(1, 0, -1),
            new Vec3(-1, 0, -1),
            new Vec3(-1, 0, 1)
    ));

    public static ImmutablePair<Float, List<Vec3>> PATTERN_SINGLE_LINE = ImmutablePair.of(39F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(0, 0, 1),
            new Vec3(0, 0, 1/3F),
            new Vec3(0, 0, -1/3F),
            new Vec3(0, 0, -1)
    ));

    public static ImmutablePair<Float, List<Vec3>> PATTERN_LARGE_SQUARE = ImmutablePair.of(29F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(1, 0, 1),
            new Vec3(1, 0, 0),
            new Vec3(1, 0, -1),
            new Vec3(0, 0, -1),
            new Vec3(-1, 0, -1),
            new Vec3(-1, 0, 0),
            new Vec3(-1, 0, 1),
            new Vec3(0, 0, 1)
    ));

    public static ImmutablePair<Float, List<Vec3>> PATTERN_CROSS = ImmutablePair.of(39F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(0, 0, 1),
            new Vec3(0, 0, 1/3F),
            new Vec3(0, 0, -1/3F),
            new Vec3(0, 0, -1),
            new Vec3(1, 0, 0),
            new Vec3(1/3F, 0, 0),
            new Vec3(-1/3F, 0, 0),
            new Vec3(-1, 0, 0)
    ));

    public static ImmutablePair<Float, List<Vec3>> PATTERN_DOUBLE_LINE = ImmutablePair.of(39F/FULL_ARENA_SIZE, ImmutableList.of(
            new Vec3(1/3F, 0, 1),
            new Vec3(1/3F, 0, 1/3F),
            new Vec3(1/3F, 0, -1/3F),
            new Vec3(1/3F, 0, -1),
            new Vec3(-1/3F, 0, -1),
            new Vec3(-1/3F, 0, -1/3F),
            new Vec3(-1/3F, 0, 1/3F),
            new Vec3(-1/3F, 0, 1)
    ));
}
