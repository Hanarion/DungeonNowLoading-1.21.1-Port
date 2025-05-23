package dev.hexnowloading.dungeonnowloading.entity.ai;

import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BorusArrowEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FairkeeperBorosPursueAndShootArrowGoal extends StoppableGoal {
    private final FairkeeperBorosEntity boros;
    private final FairkeeperBorosEntity.FairkeeperBorosState state;
    private final ShootingPattern pattern;
    private final double speed;
    private LivingEntity target;
    private Vec3 targetPosition;
    private final double shootingRange;
    private int stoppingTick;
    private int maxShootingCooldown;
    private int shootingCooldown;
    private int targetIndex;
    private int totalDuration;

    private static final int EXPIRY_DURATION = 300;
    private static final int STOP_DURATION = 42;
    private static final int BEAM_TICK = 40;
    private static final int SHOOT_ARROW_TICK = 25;
    private static final int CLOSE_MOUTH_TICK = 18;
    private static final int DEFAULT_SHOOTING_COOLDOWN = 60;
    private static final float ADDED_SPEED = 0.2F;

    public FairkeeperBorosPursueAndShootArrowGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros, double speed, double shootingRange, int shootingCooldown, ShootingPattern pattern) {
        this.state = state;
        this.boros = boros;
        this.speed = speed;
        this.shootingRange = shootingRange;
        this.maxShootingCooldown = shootingCooldown;
        this.pattern = pattern;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = this.boros.getTarget();
        return this.target != null && this.target.isAlive() && this.boros.isState(state);
    }

    @Override
    public void start() {
        super.start();
        this.targetPosition = this.target.getPosition(1.0F);
        this.targetIndex = 0;
        this.shootingCooldown = this.maxShootingCooldown;
        this.totalDuration = reducedTickDelay(EXPIRY_DURATION);
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
                pattern.arrowPattern.get(targetIndex).stream().forEach(angle -> {
                    this.spawnRedstoneTrail(this.boros, angle, 0, 0);
                });
                //this.boros.playBeamSound(this.boros.getX(), this.boros.getY(), this.boros.getZ());
                this.boros.playBeamSound(this.boros.getX(), this.boros.getY(), this.boros.getZ());
                this.boros.playMouthOpenSound(this.boros.getX(), this.boros.getY(), this.boros.getZ());
                this.boros.playMouthOpenForShootingArrow();
                this.boros.transitionTo(FairkeeperBorosEntity.FairkeeperBorosAnimationState.MOUTH_OPEN_WITHOUT_OPENED);
            }

            if (stoppingTick == reducedTickDelay(SHOOT_ARROW_TICK)) {
                pattern.arrowPattern.get(targetIndex).stream().forEach((angle -> {
                    this.shootArrow(this.boros, angle, 0, 0);
                }));
                this.boros.playArrowSound(this.boros.getX(), this.boros.getY(), this.boros.getZ());
            }

            if (stoppingTick == reducedTickDelay(CLOSE_MOUTH_TICK)) {
                this.boros.playMouthClose();
            }

            if (this.stoppingTick <= 0) {
                targetIndex++;
                if (targetIndex >= pattern.arrowPattern.size()) {
                    this.stopGoal();
                    return;
                }
                this.shootingCooldown = this.maxShootingCooldown;
            } else {
                return;
            }
        }

        if (this.totalDuration > 0) {
            this.totalDuration--;
        } else {
            this.stopGoal();
            return;
        }

        double updatedSpeed = speed;
        double distanceSqr = this.boros.distanceToSqr(this.target);
        targetPosition = this.target.getPosition(1.0f);

        if (this.shootingCooldown <= 0) {
            this.stoppingTick = reducedTickDelay(STOP_DURATION);
            return;
        } else {
            this.shootingCooldown--;
        }

        if (distanceSqr < this.shootingRange * this.shootingRange) {
            updatedSpeed = speed + ADDED_SPEED;
        }

        this.boros.getMoveControl().setWantedPosition(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, updatedSpeed);
    }

    private void shootArrow(FairkeeperBorosEntity partEntity, float angleOffset, double rxOffset, double rzOffset) {
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
                partEntity.getY() + partEntity.getBoundingBox().getYsize() * FairkeeperBorosEntity.SHOOT_ARROW_HEIGHT,
                partEntity.getZ() + rotatedRzOffset
        );

        Vec3 arrowPos = startPos.add(shootDirection.scale(viewDistance));

        BorusArrowEntity arrow = new BorusArrowEntity(this.boros, this.boros.level());
        arrow.setPos(arrowPos);
        arrow.shootFromRotation(partEntity, partEntity.getXRot(), borosFacingYaw - angleOffset, 0.0F, 2.0F, 1.0F);
        this.boros.level().addFreshEntity(arrow);
    }

    private void spawnRedstoneTrail(FairkeeperBorosEntity partEntity, float angleOffset, double rxOffset, double rzOffset) {
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
                partEntity.getY() + partEntity.getBoundingBox().getYsize() * FairkeeperBorosEntity.SHOOT_ARROW_HEIGHT,
                partEntity.getZ() + rotatedRzOffset
        ).add(beamDirection.scale(viewDistance));

        for (int i = 0; i <= 30; i++) {
            Vec3 particlePos = startPos.add(beamDirection.scale(i));
            ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(DNLParticleTypes.ARROW_HAZARD_INDICATOR.get(), 0.5F);
            ((ServerLevel) partEntity.level()).sendParticles(particleData, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
        }
    }

    public record ShootingPattern(ImmutableList<ImmutableList<Float>> arrowPattern) {}

    public static final ShootingPattern PATTERN_SINGLE = new ShootingPattern(ImmutableList.of(
            ImmutableList.of(0.0f),
            ImmutableList.of(0.0f),
            ImmutableList.of(0.0f)
    ));

    public static final ShootingPattern PATTERN_DOUBLE = new ShootingPattern(ImmutableList.of(
            ImmutableList.of(10.0f, -10.0f),
            ImmutableList.of(10.0f, -10.0f),
            ImmutableList.of(10.0f, -10.0f)
    ));

    public static final ShootingPattern PATTERN_TRIPLE = new ShootingPattern(ImmutableList.of(
            ImmutableList.of(15.0f, 0.0f, -15.0f),
            ImmutableList.of(15.0f, 0.0f, -15.0f),
            ImmutableList.of(15.0f, 0.0f, -15.0f)
    ));

    public static final ShootingPattern PATTERN_DESPERATE = new ShootingPattern(ImmutableList.of(
            ImmutableList.of(25.0f, 15F, 0.0f, -15F, -25.0f),
            ImmutableList.of(30F, 10F, -10F, -30F),
            ImmutableList.of(25.0f, 15F, 0.0f, -15F, -25.0f),
            ImmutableList.of(30F, 10F, -10F, -30F),
            ImmutableList.of(30F, 20F, 10F, 0F, -10F, -20F -30F)
    ));



}