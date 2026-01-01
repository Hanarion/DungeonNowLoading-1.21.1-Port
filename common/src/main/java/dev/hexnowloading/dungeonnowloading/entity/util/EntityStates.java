package dev.hexnowloading.dungeonnowloading.entity.util;

import dev.hexnowloading.dungeonnowloading.entity.boss.*;
import dev.hexnowloading.dungeonnowloading.entity.misc.RepulsorEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.*;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public class EntityStates {
    public static final EntityDataSerializer<ChaosSpawnerEntity.State> CHAOS_SPAWNER_STATE;
    public static final EntityDataSerializer<FairkeeperSerpentCallerEntity.FairkeeperSerpentCallerAnimationState> FAIRKEEPER_SERPENT_CALLER_ANIMATION_STATE;
    public static final EntityDataSerializer<FairkeeperBorosEntity.FairkeeperBorosState> FAIRKEEPER_BOROS_STATE;
    public static final EntityDataSerializer<FairkeeperBorosEntity.FairkeeperBorosAnimationState> FAIRKEEPER_BOROS_ANIMATION_STATE;
    public static final EntityDataSerializer<FairkeeperOurosEntity.FairkeeperOurosState> FAIRKEEPER_OUROS_STATE;
    public static final EntityDataSerializer<FairkeeperOurosEntity.FairkeeperOurosAnimationState> FAIRKEEPER_OUROS_ANIMATION_STATE;
    public static final EntityDataSerializer<FairkeeperOurosPartEntity.FairkeeperOurosPartAnimationState> FAIRKEEPER_OUROS_PART_ANIMATION_STATE;
    public static final EntityDataSerializer<FairkeeperOurosPartEntity.FairkeeperOurosPartState> FAIRKEEPER_OUROS_PART_STATE;
    public static final EntityDataSerializer<ScuttleEntity.ScuttleState> SCUTTLE_STATE;
    public static final EntityDataSerializer<BallistaGolemEntity.BallistaGolemState> BALLISTA_GOLEM_STATE;
    public static final EntityDataSerializer<CopperCreepEntity.State> COPPER_CREEP_STATE;
    public static final EntityDataSerializer<CopperCreepEntity.Skin> COPPER_CREEP_SKIN;
    public static final EntityDataSerializer<CopperCreepEntity.CopperCreepAnimationState> COPPER_CREEP_ANIMATION_STATE;
    public static final EntityDataSerializer<VertexDomainProjectileEntity.VertexDomainAnimationState> VERTEX_DOMAIN_ANIMATION_STATE;
    public static final EntityDataSerializer<RepulsorEntity.State> COMMAND_PYLON_STATE;
    public static final EntityDataSerializer<RepulsorEntity.Skin> REPULSOR_SKIN;
    public static final EntityDataSerializer<MimicartEntity.MimicartAnimationState> MIMICART_ANIMATION_STATE;
    public static final EntityDataSerializer<WebSpitterEntity.WebSpitterAnimationState> WEB_SPITTER_ANIMATION_STATE;
    public static final EntityDataSerializer<ReaperSpiderEntity.ReaperSpiderAnimationState> REAPER_SPIDER_ANIMATION_STATE;
    public static final EntityDataSerializer<WispEntity.WispAnimationState> WISP_ANIMATION_STATE;

    static {
        CHAOS_SPAWNER_STATE = EntityDataSerializer.simpleEnum(ChaosSpawnerEntity.State.class);
        FAIRKEEPER_SERPENT_CALLER_ANIMATION_STATE = EntityDataSerializer.simpleEnum(FairkeeperSerpentCallerEntity.FairkeeperSerpentCallerAnimationState.class);
        FAIRKEEPER_BOROS_STATE = EntityDataSerializer.simpleEnum(FairkeeperBorosEntity.FairkeeperBorosState.class);
        FAIRKEEPER_BOROS_ANIMATION_STATE = EntityDataSerializer.simpleEnum(FairkeeperBorosEntity.FairkeeperBorosAnimationState.class);
        FAIRKEEPER_OUROS_STATE = EntityDataSerializer.simpleEnum(FairkeeperOurosEntity.FairkeeperOurosState.class);
        FAIRKEEPER_OUROS_ANIMATION_STATE = EntityDataSerializer.simpleEnum(FairkeeperOurosEntity.FairkeeperOurosAnimationState.class);
        FAIRKEEPER_OUROS_PART_STATE = EntityDataSerializer.simpleEnum(FairkeeperOurosPartEntity.FairkeeperOurosPartState.class);
        FAIRKEEPER_OUROS_PART_ANIMATION_STATE = EntityDataSerializer.simpleEnum(FairkeeperOurosPartEntity.FairkeeperOurosPartAnimationState.class);
        SCUTTLE_STATE = EntityDataSerializer.simpleEnum(ScuttleEntity.ScuttleState.class);
        BALLISTA_GOLEM_STATE = EntityDataSerializer.simpleEnum(BallistaGolemEntity.BallistaGolemState.class);
        COPPER_CREEP_STATE = EntityDataSerializer.simpleEnum(CopperCreepEntity.State.class);
        COPPER_CREEP_SKIN = EntityDataSerializer.simpleEnum(CopperCreepEntity.Skin.class);
        COPPER_CREEP_ANIMATION_STATE = EntityDataSerializer.simpleEnum(CopperCreepEntity.CopperCreepAnimationState.class);
        VERTEX_DOMAIN_ANIMATION_STATE = EntityDataSerializer.simpleEnum(VertexDomainProjectileEntity.VertexDomainAnimationState.class);
        COMMAND_PYLON_STATE = EntityDataSerializer.simpleEnum(RepulsorEntity.State.class);
        REPULSOR_SKIN = EntityDataSerializer.simpleEnum(RepulsorEntity.Skin.class);
        MIMICART_ANIMATION_STATE = EntityDataSerializer.simpleEnum(MimicartEntity.MimicartAnimationState.class);
        WEB_SPITTER_ANIMATION_STATE = EntityDataSerializer.simpleEnum(WebSpitterEntity.WebSpitterAnimationState.class);
        REAPER_SPIDER_ANIMATION_STATE = EntityDataSerializer.simpleEnum(ReaperSpiderEntity.ReaperSpiderAnimationState.class);
        WISP_ANIMATION_STATE = EntityDataSerializer.simpleEnum(WispEntity.WispAnimationState.class);

        EntityDataSerializers.registerSerializer(CHAOS_SPAWNER_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_SERPENT_CALLER_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_BOROS_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_BOROS_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_OUROS_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_OUROS_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_OUROS_PART_STATE);
        EntityDataSerializers.registerSerializer(FAIRKEEPER_OUROS_PART_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(SCUTTLE_STATE);
        EntityDataSerializers.registerSerializer(BALLISTA_GOLEM_STATE);
        EntityDataSerializers.registerSerializer(COPPER_CREEP_STATE);
        EntityDataSerializers.registerSerializer(COPPER_CREEP_SKIN);
        EntityDataSerializers.registerSerializer(COPPER_CREEP_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(VERTEX_DOMAIN_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(COMMAND_PYLON_STATE);
        EntityDataSerializers.registerSerializer(REPULSOR_SKIN);
        EntityDataSerializers.registerSerializer(MIMICART_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(WEB_SPITTER_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(REAPER_SPIDER_ANIMATION_STATE);
        EntityDataSerializers.registerSerializer(WISP_ANIMATION_STATE);
    }
}
