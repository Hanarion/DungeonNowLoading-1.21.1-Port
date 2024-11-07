package dev.hexnowloading.dungeonnowloading.components;

import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.potion.VertexTransmissionEffect;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class VertexNode {
    private List<VertexNodeConnectionContext> connectedNodes = new ArrayList<>();
    private Entity entityRef;
    private boolean attemptedConnection = false;

    private static final float MAX_RANDOM_PARTICLE_SCALE_MULTIPLIER = 3;
    private static final double BEAM_PARTICLE_SPACING = 0.1d;
    private static final int MAX_CONNECTION_COUNT = 2;
    private static final double MAX_CONNECTION_RADIUS = 10.0d;
    private static final double BEAM_HITBOX_RADIUS = 0.5d;
    private static final int ENTITY_EFFECT_DURATION_TICKS = 60;
    private static final float BEAM_PARTICLE_SCALE = 0.05f;

    public VertexNode(Entity entityRef) {
        this.entityRef = entityRef;
    }

    public int getConnectionCount() {
        return this.connectedNodes.size();
    }

    public boolean attemptedConnection() {
        return attemptedConnection;
    }

    public boolean connectionLimitReached() {
        return this.getConnectionCount() >= MAX_CONNECTION_COUNT;
    }

    public void disconnect_all() {
        for (VertexNodeConnectionContext connectedNodeCtx : this.connectedNodes) {
            connectedNodeCtx.getVertexNode().disconnectNode(this);
        }

        this.connectedNodes.clear();
        this.attemptedConnection = false;
    }

    public void disconnectNode(VertexNode node) {
        this.connectedNodes.removeIf(nodeConnectionCtx -> nodeConnectionCtx.getVertexNode() == node);
    }

    public void connectNode(VertexNode node, boolean isBeamParent) {
        this.connectedNodes.add(new VertexNodeConnectionContext(node, isBeamParent));
    }

    public void connectToNearbyNodes(Entity sourceEntity) {
        List<VertexNode> nearbyNodes = this.getNearbyNodes(sourceEntity);

        for (VertexNode node : nearbyNodes) {
            this.connectNode(node, true);
            node.connectNode(this, false);
        }
        this.attemptedConnection = true;
    }

    public void tick(Entity entity) {
        boolean isClientSide = entity.level().isClientSide;

        for (VertexNodeConnectionContext connectedNodeContext : new ArrayList<>(this.connectedNodes)) {
            VertexNode connectedNode = connectedNodeContext.getVertexNode();
            boolean isBeamParent = connectedNodeContext.isBeamParent();

            // VFX
            if (isBeamParent) {
                this.spawnParticleBeamVFX(entity.level(), entity.getEyePosition(), connectedNode.entityRef.getEyePosition());
            }

            // Collision logic
            if (!isClientSide && isBeamParent && entity.tickCount % 2 == 0) {
                // Entity interacting logic
                List<LivingEntity> entitiesTouchingBeam = getLivingEntitiesTouchingBeam(
                        entity.level(),
                        entity.position(),
                        connectedNode.entityRef.position(),
                        BEAM_HITBOX_RADIUS
                );

                // Excludes self
                entitiesTouchingBeam.remove(this.entityRef);
                entitiesTouchingBeam.remove(connectedNode.entityRef);

                for (LivingEntity livingEntity : entitiesTouchingBeam) {
                    if (!livingEntity.hasEffect(DNLMobEffects.VERTEX_TRANSMISSION.get())) {
                        livingEntity.addEffect(new MobEffectInstance(DNLMobEffects.VERTEX_TRANSMISSION.get(), ENTITY_EFFECT_DURATION_TICKS, 0));
                    }
                }
            }

            // Auto-disconnect logic
            boolean nodeShouldDisconnect = connectedNode.entityRef.isRemoved() || !connectedNode.entityRef.isAlive();
            if (connectedNode.entityRef instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) {
                nodeShouldDisconnect = true;
            }

            if (!isClientSide && nodeShouldDisconnect) {
                connectedNode.disconnect_all();
            }

        }
    }

    private List<VertexNode> getNearbyNodes(Entity sourceEntity) {
        double radius_squared = MAX_CONNECTION_RADIUS * MAX_CONNECTION_RADIUS; // Avoid Math.pow for better performance

        return sourceEntity.level().getEntitiesOfClass(
            Entity.class,
            sourceEntity.getBoundingBox().inflate(MAX_CONNECTION_RADIUS)
        )
            .stream()
            .filter(potentialNodeEntity -> potentialNodeEntity != sourceEntity)
            .filter(potentialNodeEntity ->
                potentialNodeEntity instanceof VertexArrowProjectileEntity arrowEntity && arrowEntity.isFullyPowered() // check if it's a custom arrow entity
                || (potentialNodeEntity instanceof LivingEntity livingEntity && livingEntity.hasEffect(DNLMobEffects.VERTEX_TRANSMISSION.get())) // check for effect
            )
            .filter(nodeEntity -> nodeEntity.distanceToSqr(sourceEntity) <= radius_squared)
            .sorted(Comparator.comparingDouble(nodeEntity -> nodeEntity.distanceToSqr(sourceEntity)))
            .map(nodeEntity -> {
                // Retrieve VertexNodeComponent directly from custom arrows or from the effect
                if (nodeEntity instanceof VertexArrowProjectileEntity arrowEntity) {
                    return arrowEntity.getVertexNode(); // Direct property on the custom entity
                } else {
                    LivingEntity livingEntity = (LivingEntity) nodeEntity;
                    return getComponentFromEffect(livingEntity); // Method to extract from the status effect
                }
            })
            .filter(Objects::nonNull) // Only keep entities that have the component
            .filter(node -> node.getConnectionCount() < MAX_CONNECTION_COUNT)
            .limit(MAX_CONNECTION_COUNT)
            .collect(Collectors.toList());
    }

    // Helper method to retrieve the VertexNodeComponent from a LivingEntity's status effect
    private VertexNode getComponentFromEffect(LivingEntity entity) {
        if (entity.hasEffect(DNLMobEffects.VERTEX_TRANSMISSION.get())) {
            MobEffectInstance effectInstance = entity.getEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());

            if (effectInstance.getEffect() instanceof VertexTransmissionEffect customEffect) {
                return customEffect.getVertexNode(entity.getUUID());
            }
        }
        return null;
    }

    private void spawnParticleBeamVFX(Level level, Vec3 startPos, Vec3 endPos) {
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
//                DustParticleOptions smallRedstoneParticle = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 0.5f);
//                _level.sendParticles(smallRedstoneParticle, particlePos.x, particlePos.y, particlePos.z, 1, 0.0d, 0.0d, 0.0d, 0);
            }
        }
    }

    private List<LivingEntity> getLivingEntitiesTouchingBeam(Level level, Vec3 beamStart, Vec3 beamEnd, double radius) {
        // Create a capsule-shaped volume representing the beam
        // The AABB of the beam will be expanded to account for the radius
        List<LivingEntity> entitiesInCapsule = new ArrayList<>();

        // Fetch all living entities in the vicinity of the beam's bounding box
        List<LivingEntity> allEntities = level.getEntitiesOfClass(LivingEntity.class, new AABB(
                Math.min(beamStart.x, beamEnd.x) - radius,
                Math.min(beamStart.y, beamEnd.y) - radius,
                Math.min(beamStart.z, beamEnd.z) - radius,
                Math.max(beamStart.x, beamEnd.x) + radius,
                Math.max(beamStart.y, beamEnd.y) + radius,
                Math.max(beamStart.z, beamEnd.z) + radius
        ));

        // Iterate through each entity to check for intersection with the capsule
        for (LivingEntity entity : allEntities) {
//            if (entity == this) continue; // Exclude self

            // Check if the entity's bounding box intersects with the capsule defined by the beam
            if (isBoundingBoxIntersectingWithCapsule(entity.getBoundingBox(), beamStart, beamEnd, radius)) {
                entitiesInCapsule.add(entity);
            }
        }

        return entitiesInCapsule;
    }

    private boolean isBoundingBoxIntersectingWithCapsule(AABB boundingBox, Vec3 beamStart, Vec3 beamEnd, double radius) {
        // Find the closest point on the line segment (beam) to the bounding box
        Vec3 closestPointOnBeam = getClosestPointOnBeam(boundingBox, beamStart, beamEnd);

        // Check if the closest point is within the radius of the capsule
        return closestPointOnBeam.distanceToSqr(boundingBox.getCenter()) <= radius * radius || boundingBox.contains(closestPointOnBeam);
    }

    private Vec3 getClosestPointOnBeam(AABB boundingBox, Vec3 start, Vec3 end) {
        Vec3 beamDirection = end.subtract(start).normalize();
        Vec3 boxCenter = boundingBox.getCenter();

        // Project the box center onto the line defined by the beam
        double t = beamDirection.dot(boxCenter.subtract(start));

        // Clamp t to the length of the beam
        t = Math.max(0, Math.min(t, start.distanceTo(end)));

        // Get the closest point on the beam
        return start.add(beamDirection.scale(t));
    }

//    private boolean isValidConnectableNode(Entity entity) {
//        if (entity instanceof VertexArrowProjectileEntity) {
//            return true;
//        }
//        if (entity instanceof LivingEntity livingEntity) {
//            MobEffectInstance vertexTransmissionEffect = livingEntity.getEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
//            return vertexTransmissionEffect != null && vertexTransmissionEffect.getDuration() > 0;
//        }
//        return false;
//    }
}
