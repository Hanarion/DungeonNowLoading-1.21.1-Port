package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.util.DNLMath;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VertexPillarBlockEntity extends BlockEntity {

    private final List<BlockPos> linkedPositions = new ArrayList<>();
    private int age;


    private static final float DAMAGE = 6.0F;
    private static final double BEAM_HITBOX_RADIUS = 0.5d;
    private static final int SLOWNESS_DURATION = 20;
    private static final int SLOWNESS_AMPLIFIER = 4;

    private static final double BEAM_PARTICLE_SPACING = 0.4d;
    private static final float BEAM_PARTICLE_SCALE = 0.05f;
    private static final float MAX_RANDOM_PARTICLE_SCALE_MULTIPLIER = 3;

    private static final double BEAM_INITIAL_PARTICLE_SPACING = 0.8d;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MIN = 0.2f;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MAX = 0.4f;

    public VertexPillarBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.VERTEX_PILLAR.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        ListTag posList = new ListTag();

        for (BlockPos pos : linkedPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            posList.add(posTag);
        }

        compoundTag.put("LinkedPositions", posList);
        compoundTag.putInt("Age", this.age);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.linkedPositions.clear();

        ListTag posList = compoundTag.getList("LinkedPositions", CompoundTag.TAG_COMPOUND);

        for (Tag tag : posList) {
            CompoundTag posTag = (CompoundTag) tag;
            BlockPos loadedPos = new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
            linkedPositions.add(loadedPos);
        }

        this.age = compoundTag.getInt("Age");
    }

    public List<BlockPos> getLinkedPositions() {
        return linkedPositions;
    }

    public boolean addLink(BlockPos pos) {
        if (!linkedPositions.contains(pos)) {
            linkedPositions.add(pos);
            setChanged();
            return true;
        }
        return false;
    }

    public void removeLink(BlockPos pos) {
        linkedPositions.remove(pos);
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VertexPillarBlockEntity blockEntity) {
        if (!level.isClientSide) {
            for (BlockPos linkedPos : blockEntity.getLinkedPositions()) {
                BlockEntity targetBE = level.getBlockEntity(linkedPos);
                if (targetBE instanceof VertexPillarBlockEntity pillarBlockEntity && pillarBlockEntity.age > blockEntity.age) {
                    if (blockEntity.age > 0) {
                        spawnRedstoneLaser((ServerLevel) level, pos, linkedPos);
                    } else {
                        spawnInitialParticleBeamVFX(level, pos.getCenter().add(0.0f, 1.0f, 0.0f), linkedPos.getCenter().add(0.0f, 1.0f, 0.0f));
                        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), DNLSounds.VERTEX_NODE_CONNECT.get(), SoundSource.NEUTRAL, 0.5F, 1.2F / (DNLMath.randomRange(0.0f, 1.0f) * 0.2F + 0.9F)
                        );
                    }
                }
            }
            blockEntity.age++;
        }
    }

    private static void spawnRedstoneLaser(ServerLevel level, BlockPos start, BlockPos end) {
        Vec3 startVec = Vec3.atCenterOf(start).add(0.0f, 1.0f, 0.0f);
        Vec3 endVec = Vec3.atCenterOf(end).add(0.0f, 1.0f, 0.0f);

        List<LivingEntity> entities = getLivingEntitiesTouchingBeam(
                level,
                startVec,
                endVec,
                BEAM_HITBOX_RADIUS
        );

        for (LivingEntity entity : entities) {
            if (entity instanceof FairkeeperSerpentEntity) {
                continue;
            }
            entity.hurt(entity.level().damageSources().magic(), DAMAGE);
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER));
        }

        spawnParticleBeamVFX(level, startVec, endVec);

    }

    private static List<LivingEntity> getLivingEntitiesTouchingBeam(Level level, Vec3 beamStart, Vec3 beamEnd, double radius) {
        List<LivingEntity> entitiesInCapsule = new ArrayList<>();

        List<LivingEntity> allEntities = level.getEntitiesOfClass(LivingEntity.class, new AABB(
                Math.min(beamStart.x, beamEnd.x) - radius,
                Math.min(beamStart.y, beamEnd.y) - radius,
                Math.min(beamStart.z, beamEnd.z) - radius,
                Math.max(beamStart.x, beamEnd.x) + radius,
                Math.max(beamStart.y, beamEnd.y) + radius,
                Math.max(beamStart.z, beamEnd.z) + radius
        ));

        for (LivingEntity entity : allEntities) {

            if (isBoundingBoxIntersectingWithCapsule(entity.getBoundingBox(), beamStart, beamEnd, radius)) {
                entitiesInCapsule.add(entity);
            }
        }

        return entitiesInCapsule;
    }

    private static Vec3 getClosestPointOnBeam(AABB boundingBox, Vec3 start, Vec3 end) {
        Vec3 beamDirection = end.subtract(start).normalize();
        Vec3 boxCenter = boundingBox.getCenter();
        double t = beamDirection.dot(boxCenter.subtract(start));
        t = Math.max(0, Math.min(t, start.distanceTo(end)));
        return start.add(beamDirection.scale(t));
    }

    private static boolean isBoundingBoxIntersectingWithCapsule(AABB boundingBox, Vec3 beamStart, Vec3 beamEnd, double radius) {
        Vec3 closestPointOnBeam = getClosestPointOnBeam(boundingBox, beamStart, beamEnd);

        return closestPointOnBeam.distanceToSqr(boundingBox.getCenter()) <= radius * radius || boundingBox.contains(closestPointOnBeam);
    }

    private static void spawnParticleBeamVFX(Level level, Vec3 startPos, Vec3 endPos) {
        Vec3 line = endPos.subtract(startPos);
        double distance = line.length();

        int numberOfParticles = (int) Math.ceil(distance / BEAM_PARTICLE_SPACING);

        for (int i = 0; i <= numberOfParticles; i++) {
            int randomNumber = ThreadLocalRandom.current().nextInt(3);
            if (randomNumber != 0) {
                continue;
            }

            double t = (double) i / numberOfParticles;
            Vec3 particlePos = startPos.add(line.scale(t));

            if (level instanceof ServerLevel _level) {
                float scaleMultiplier = (float) Math.random() * MAX_RANDOM_PARTICLE_SCALE_MULTIPLIER;

                ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                        DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(),
                        BEAM_PARTICLE_SCALE * scaleMultiplier
                );

                _level.sendParticles(particleData, particlePos.x, particlePos.y, particlePos.z, 1, 0.0d, 0.0d, 0.0d, 0);
            }
        }
    }

    private static void spawnInitialParticleBeamVFX(Level level, Vec3 startPos, Vec3 endPos) {
        Vec3 line = endPos.subtract(startPos);
        double distance = line.length();

        int numberOfParticles = (int) Math.ceil(distance / BEAM_INITIAL_PARTICLE_SPACING);

        for (int i = 0; i <= numberOfParticles; i++) {

            double t = (double) i / numberOfParticles;
            Vec3 particlePos = startPos.add(line.scale(t));

            if (level instanceof ServerLevel _level) {
                float particleScale = BEAM_INITIAL_PARTICLE_SCALE_MIN + (float) Math.random() * (BEAM_INITIAL_PARTICLE_SCALE_MAX - BEAM_INITIAL_PARTICLE_SCALE_MIN);

                ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                        DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                        particleScale
                );
                _level.sendParticles(particleData, particlePos.x, particlePos.y, particlePos.z, 1, 0.0d, 0.0d, 0.0d, 0);
            }
        }
    }
}
