package dev.hexnowloading.dungeonnowloading.entity.util;

import dev.hexnowloading.dungeonnowloading.entity.boss.*;
import dev.hexnowloading.dungeonnowloading.entity.misc.RepulsorEntity;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.*;
import dev.hexnowloading.dungeonnowloading.entity.monster.*;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.util.ByIdMap;

public class EntityStates {

    /**
     * 1.21 removed simpleEnum(Class); rebuild the equivalent
     * ordinal-based serializer from a StreamCodec.
     */
    private static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        StreamCodec<io.netty.buffer.ByteBuf, T> codec = ByteBufCodecs.idMapper(
                ByIdMap.continuous(Enum::ordinal, values, ByIdMap.OutOfBoundsStrategy.ZERO),
                Enum::ordinal
        );
        return EntityDataSerializer.forValueType(codec.cast());
    }

    public static final EntityDataSerializer<ChaosSpawnerEntity.State> CHAOS_SPAWNER_STATE;
    public static final EntityDataSerializer<SpawnerCarrierEntity.SpawnerCarrierAnimationState> SPAWNER_CARRIER_ANIMATION_STATE;
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
    public static final EntityDataSerializer<SeepingSoulEntity.SeepingSoulAnimationState> SEEPING_SOUL_ANIMATION_STATE;
    public static final EntityDataSerializer<GarholdEntity.GarholdState> GARHOLD_STATE;
    public static final EntityDataSerializer<GarholdEntity.GarholdAnimationState> GARHOLD_ANIMATION_STATE;
    //public static final EntityDataSerializer<GarholdEntity.GarholdBottomGateAnimationState> GARHOLD_GATE_ANIMATION_STATE;
    public static final EntityDataSerializer<GarholdEntity.GarholdSideGateAnimationState> GARHOLD_SIDE_GATE_ANIMATION_STATE;
    public static final EntityDataSerializer<BrokenGarholdEntity.BrokenGarholdState> BROKEN_GARHOLD_STATE;
    public static final EntityDataSerializer<WhimperEntity.WhimperAnimationState> WHIMPER_ANIMATION_STATE;
    public static final EntityDataSerializer<WhimperEntity.Skin> WHIMPER_SKIN;
    public static final EntityDataSerializer<MimicartEntity.MimicartAnimationState> MIMICART_ANIMATION_STATE;
    public static final EntityDataSerializer<SilkSpiderEntity.SilkSpiderAnimationState> SILK_SPIDER_ANIMATION_STATE;
    public static final EntityDataSerializer<ReaperSpiderEntity.ReaperSpiderAnimationState> REAPER_SPIDER_ANIMATION_STATE;
    public static final EntityDataSerializer<WispEntity.WispAnimationState> WISP_ANIMATION_STATE;

    static {
        CHAOS_SPAWNER_STATE = simpleEnum(ChaosSpawnerEntity.State.class);
        SPAWNER_CARRIER_ANIMATION_STATE = simpleEnum(SpawnerCarrierEntity.SpawnerCarrierAnimationState.class);
        FAIRKEEPER_SERPENT_CALLER_ANIMATION_STATE = simpleEnum(FairkeeperSerpentCallerEntity.FairkeeperSerpentCallerAnimationState.class);
        FAIRKEEPER_BOROS_STATE = simpleEnum(FairkeeperBorosEntity.FairkeeperBorosState.class);
        FAIRKEEPER_BOROS_ANIMATION_STATE = simpleEnum(FairkeeperBorosEntity.FairkeeperBorosAnimationState.class);
        FAIRKEEPER_OUROS_STATE = simpleEnum(FairkeeperOurosEntity.FairkeeperOurosState.class);
        FAIRKEEPER_OUROS_ANIMATION_STATE = simpleEnum(FairkeeperOurosEntity.FairkeeperOurosAnimationState.class);
        FAIRKEEPER_OUROS_PART_STATE = simpleEnum(FairkeeperOurosPartEntity.FairkeeperOurosPartState.class);
        FAIRKEEPER_OUROS_PART_ANIMATION_STATE = simpleEnum(FairkeeperOurosPartEntity.FairkeeperOurosPartAnimationState.class);
        SCUTTLE_STATE = simpleEnum(ScuttleEntity.ScuttleState.class);
        BALLISTA_GOLEM_STATE = simpleEnum(BallistaGolemEntity.BallistaGolemState.class);
        COPPER_CREEP_STATE = simpleEnum(CopperCreepEntity.State.class);
        COPPER_CREEP_SKIN = simpleEnum(CopperCreepEntity.Skin.class);
        COPPER_CREEP_ANIMATION_STATE = simpleEnum(CopperCreepEntity.CopperCreepAnimationState.class);
        VERTEX_DOMAIN_ANIMATION_STATE = simpleEnum(VertexDomainProjectileEntity.VertexDomainAnimationState.class);
        COMMAND_PYLON_STATE = simpleEnum(RepulsorEntity.State.class);
        REPULSOR_SKIN = simpleEnum(RepulsorEntity.Skin.class);
        MIMICART_ANIMATION_STATE = simpleEnum(MimicartEntity.MimicartAnimationState.class);
        SILK_SPIDER_ANIMATION_STATE = simpleEnum(SilkSpiderEntity.SilkSpiderAnimationState.class);
        REAPER_SPIDER_ANIMATION_STATE = simpleEnum(ReaperSpiderEntity.ReaperSpiderAnimationState.class);
        WISP_ANIMATION_STATE = simpleEnum(WispEntity.WispAnimationState.class);
        SEEPING_SOUL_ANIMATION_STATE = simpleEnum(SeepingSoulEntity.SeepingSoulAnimationState.class);
        GARHOLD_STATE = simpleEnum(GarholdEntity.GarholdState.class);
        GARHOLD_ANIMATION_STATE = simpleEnum(GarholdEntity.GarholdAnimationState.class);
        //GARHOLD_GATE_ANIMATION_STATE = simpleEnum(GarholdEntity.GarholdBottomGateAnimationState.class);
        GARHOLD_SIDE_GATE_ANIMATION_STATE = simpleEnum(GarholdEntity.GarholdSideGateAnimationState.class);
        BROKEN_GARHOLD_STATE = simpleEnum(BrokenGarholdEntity.BrokenGarholdState.class);
        WHIMPER_ANIMATION_STATE = simpleEnum(WhimperEntity.WhimperAnimationState.class);
        WHIMPER_SKIN = simpleEnum(WhimperEntity.Skin.class);

    }

    private static boolean registered = false;

    /**
     * 1.21 NeoForge forbids vanilla {@code EntityDataSerializers.registerSerializer} for modded
     * serializers; route them through the platform registry (deferred onto the mod bus). Called
     * from mod init so registration runs while the registry is still open.
     */
    public static void init() {
        if (registered) {
            return;
        }
        registered = true;

        Services.REGISTRY.registerEntityDataSerializer("chaos_spawner_state", CHAOS_SPAWNER_STATE);
        Services.REGISTRY.registerEntityDataSerializer("spawner_carrier_animation_state", SPAWNER_CARRIER_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("fairkeeper_serpent_caller_animation_state", FAIRKEEPER_SERPENT_CALLER_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("fairkeeper_boros_state", FAIRKEEPER_BOROS_STATE);
        Services.REGISTRY.registerEntityDataSerializer("fairkeeper_boros_animation_state", FAIRKEEPER_BOROS_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("fairkeeper_ouros_state", FAIRKEEPER_OUROS_STATE);
        Services.REGISTRY.registerEntityDataSerializer("fairkeeper_ouros_animation_state", FAIRKEEPER_OUROS_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("fairkeeper_ouros_part_state", FAIRKEEPER_OUROS_PART_STATE);
        Services.REGISTRY.registerEntityDataSerializer("fairkeeper_ouros_part_animation_state", FAIRKEEPER_OUROS_PART_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("scuttle_state", SCUTTLE_STATE);
        Services.REGISTRY.registerEntityDataSerializer("ballista_golem_state", BALLISTA_GOLEM_STATE);
        Services.REGISTRY.registerEntityDataSerializer("copper_creep_state", COPPER_CREEP_STATE);
        Services.REGISTRY.registerEntityDataSerializer("copper_creep_skin", COPPER_CREEP_SKIN);
        Services.REGISTRY.registerEntityDataSerializer("copper_creep_animation_state", COPPER_CREEP_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("vertex_domain_animation_state", VERTEX_DOMAIN_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("command_pylon_state", COMMAND_PYLON_STATE);
        Services.REGISTRY.registerEntityDataSerializer("repulsor_skin", REPULSOR_SKIN);
        Services.REGISTRY.registerEntityDataSerializer("mimicart_animation_state", MIMICART_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("silk_spider_animation_state", SILK_SPIDER_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("reaper_spider_animation_state", REAPER_SPIDER_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("wisp_animation_state", WISP_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("seeping_soul_animation_state", SEEPING_SOUL_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("garhold_state", GARHOLD_STATE);
        Services.REGISTRY.registerEntityDataSerializer("garhold_animation_state", GARHOLD_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("garhold_side_gate_animation_state", GARHOLD_SIDE_GATE_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("broken_garhold_state", BROKEN_GARHOLD_STATE);
        Services.REGISTRY.registerEntityDataSerializer("whimper_animation_state", WHIMPER_ANIMATION_STATE);
        Services.REGISTRY.registerEntityDataSerializer("whimper_skin", WHIMPER_SKIN);
    }
}
