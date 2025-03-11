package dev.hexnowloading.dungeonnowloading.entity.util;

import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public class EntityStates {
    public static final EntityDataSerializer<ChaosSpawnerEntity.State> CHAOS_SPAWNER_STATE;
    public static final EntityDataSerializer<FairkeeperSerpentCallerEntity.FairkeeperSerpentCallerState> FAIRKEEPER_SERPENT_CALLER_STATE;
    public static final EntityDataSerializer<FairkeeperBorosEntity.FairkeeperBorosState> FAIRKEEPER_BOROS_STATE;
    public static final EntityDataSerializer<FairkeeperBorosEntity.FairkeeperBorosAnimationState> FAIRKEEPER_BOROS_ANIMATION_STATE;
    public static final EntityDataSerializer<FairkeeperOurosEntity.FairkeeperOurosState> FAIRKEEPER_OUROS_STATE;
    public static final EntityDataSerializer<ScuttleEntity.ScuttleState> SCUTTLE_STATE;
    public static final EntityDataSerializer<BallistaGolemEntity.BallistaGolemState> BALLISTA_GOLEM_STATE;
    public static final EntityDataSerializer<CopperCreepEntity.State> COPPER_CREEP_STATE;
    public static final EntityDataSerializer<VertexDomainProjectileEntity.VertexDomainAnimationState> VERTEX_DOMAIN_ANIMATION_STATE;

    static {
        CHAOS_SPAWNER_STATE = EntityDataSerializer.simpleEnum(ChaosSpawnerEntity.State.class);
        FAIRKEEPER_SERPENT_CALLER_STATE = EntityDataSerializer.simpleEnum(FairkeeperSerpentCallerEntity.FairkeeperSerpentCallerState.class);
        FAIRKEEPER_BOROS_STATE = EntityDataSerializer.simpleEnum(FairkeeperBorosEntity.FairkeeperBorosState.class);
        FAIRKEEPER_BOROS_ANIMATION_STATE = EntityDataSerializer.simpleEnum(FairkeeperBorosEntity.FairkeeperBorosAnimationState.class);
        FAIRKEEPER_OUROS_STATE = EntityDataSerializer.simpleEnum(FairkeeperOurosEntity.FairkeeperOurosState.class);
        SCUTTLE_STATE = EntityDataSerializer.simpleEnum(ScuttleEntity.ScuttleState.class);
        BALLISTA_GOLEM_STATE = EntityDataSerializer.simpleEnum(BallistaGolemEntity.BallistaGolemState.class);
        COPPER_CREEP_STATE = EntityDataSerializer.simpleEnum(CopperCreepEntity.State.class);
        VERTEX_DOMAIN_ANIMATION_STATE = EntityDataSerializer.simpleEnum(VertexDomainProjectileEntity.VertexDomainAnimationState.class);

        EntityDataSerializers.registerSerializer(CHAOS_SPAWNER_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_SERPENT_CALLER_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_BOROS_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_BOROS_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_OUROS_STATE);
        EntityDataSerializers.registerSerializer(SCUTTLE_STATE);
        EntityDataSerializers.registerSerializer(BALLISTA_GOLEM_STATE);
        EntityDataSerializers.registerSerializer(COPPER_CREEP_STATE);
        EntityDataSerializers.registerSerializer(VERTEX_DOMAIN_ANIMATION_STATE);
    }
}
