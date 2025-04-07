package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.*;
import dev.hexnowloading.dungeonnowloading.entity.misc.RepulsorEntity;
import dev.hexnowloading.dungeonnowloading.entity.misc.SpecialItemEntity;
import dev.hexnowloading.dungeonnowloading.entity.misc.GreatExperienceBottleEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DNLEntityTypes {

    //public static final Supplier<EntityType<Entity>> WINDSTONE = register("windstone", () -> EntityType.Builder.of(WindstoneEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).build(new ResourceLocation(Skyisland.MOD_ID, "windstone").toString()));
    // Bosses
    public static final Supplier<EntityType<ChaosSpawnerEntity>> CHAOS_SPAWNER = register("chaos_spawner", () -> EntityType.Builder.<ChaosSpawnerEntity>of(ChaosSpawnerEntity::new, MobCategory.MONSTER).fireImmune().sized(3.0F, 3.0F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "chaos_spawner").toString()));
    public static final Supplier<EntityType<FairkeeperBorosEntity>> FAIRKEEPER_BOROS = register("fairkeeper_boros", () -> EntityType.Builder.<FairkeeperBorosEntity>of(FairkeeperBorosEntity::new, MobCategory.MONSTER).sized(3.0F, 3.0F).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper").toString()));
    public static final Supplier<EntityType<FairkeeperBorosPartEntity>> FAIRKEEPER_BOROS_PART = register("fairkeeper_boros_part", () -> EntityType.Builder.<FairkeeperBorosPartEntity>of(FairkeeperBorosPartEntity::new, MobCategory.MONSTER).sized(3.0F, 3.0F).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_borus_part").toString()));
    public static final Supplier<EntityType<FairkeeperOurosEntity>> FAIRKEEPER_OUROS = register("fairkeeper_ouros", () -> EntityType.Builder.<FairkeeperOurosEntity>of(FairkeeperOurosEntity::new, MobCategory.MONSTER).sized(3.0F, 3.0F).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_ouros").toString()));
    public static final Supplier<EntityType<FairkeeperOurosPartEntity>> FAIRKEEPER_OUROS_PART = register("fairkeeper_ouros_part", () -> EntityType.Builder.<FairkeeperOurosPartEntity>of(FairkeeperOurosPartEntity::new, MobCategory.MONSTER).sized(3.0F, 3.0F).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_ouros_part").toString()));
    public static final Supplier<EntityType<FairkeeperSerpentCallerEntity>> FAIRKEEPER_SERPENT_CALLER = register("fairkeeper_serpent_caller", () -> EntityType.Builder.of(FairkeeperSerpentCallerEntity::new, MobCategory.MONSTER).sized(1.0F, 1.0F).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_serpent_caller").toString()));

    // Monsters
    public static final Supplier<EntityType<HollowEntity>> HOLLOW = register("hollow", () -> EntityType.Builder.of(HollowEntity::new, MobCategory.MONSTER).sized(0.95F, 0.95F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "hollow").toString()));
    public static final Supplier<EntityType<SpawnerCarrierEntity>> SPAWNER_CARRIER = register("spawner_carrier", () -> EntityType.Builder.of(SpawnerCarrierEntity::new, MobCategory.MONSTER).sized(1.95F, 1.95F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "spawner_carrier").toString()));
    public static final Supplier<EntityType<ScuttleEntity>> SCUTTLE = register("scuttle", () -> EntityType.Builder.of(ScuttleEntity::new, MobCategory.MONSTER).sized(0.97F, 1.95F).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "scuttle").toString()));
    public static final Supplier<EntityType<BallistaGolemEntity>> BALLISTA_GOLEM = register("ballista_golem", () -> EntityType.Builder.of(BallistaGolemEntity::new, MobCategory.MONSTER).sized(2.9F, 4.0F).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "ballista_golem").toString()));


    // Passive
    public static final Supplier<EntityType<SealedChaosEntity>> SEALED_CHAOS = register("sealed_chaos", () -> EntityType.Builder.of(SealedChaosEntity::new, MobCategory.CREATURE).sized(1F, 1F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "sealed_chaos").toString()));
    public static final Supplier<EntityType<WhimperEntity>> WHIMPER = register("whimper", () -> EntityType.Builder.of(WhimperEntity::new, MobCategory.CREATURE).sized(0.75F, 0.75F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "whimper").toString()));
    public static final Supplier<EntityType<CopperCreepEntity>> COPPER_CREEP = register("copper_creep", () -> EntityType.Builder.of(CopperCreepEntity::new, MobCategory.CREATURE).sized(0.65F, 0.75F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "copper_creep").toString()));

    // Projectiles
    public static final Supplier<EntityType<ChaosSpawnerProjectileEntity>> CHAOS_SPAWNER_PROJECTILE = register("chaos_spawner_projectile", () -> EntityType.Builder.<ChaosSpawnerProjectileEntity>of(ChaosSpawnerProjectileEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "chaos_spawner_projectile").toString()));
    public static final Supplier<EntityType<FlameProjectileEntity>> FLAME_PROJECTILE = register("flame_projectile", () -> EntityType.Builder.<FlameProjectileEntity>of(FlameProjectileEntity::new, MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(4).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "flame_projectile").toString()));
    public static final Supplier<EntityType<VertexPillarProjectileEntity>> VERTEX_PILLAR_PROJECTILE = register("vertex_pillar_projectile", () -> EntityType.Builder.<VertexPillarProjectileEntity>of(VertexPillarProjectileEntity::new, MobCategory.MISC).sized(0.75F, 2.0F).clientTrackingRange(4).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "vertex_pillar_projectile").toString()));
    public static final Supplier<EntityType<BallistaArrowEntity>> BALLISTA_ARROW = register("ballista_arrow", () -> EntityType.Builder.<BallistaArrowEntity>of(BallistaArrowEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(20).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "ballista_arrow").toString()));
    public static final Supplier<EntityType<VertexArrowProjectileEntity>> VERTEX_ARROW_PROJECTILE = register("vertex_arrow_projectile", () -> EntityType.Builder.<VertexArrowProjectileEntity>of(VertexArrowProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "vertex_arrow_projectile").toString()));
    public static final Supplier<EntityType<VertexOrbProjectileEntity>> VERTEX_ORB_PROJECTILE = register("vertex_orb_projectile", () -> EntityType.Builder.<VertexOrbProjectileEntity>of(VertexOrbProjectileEntity::new, MobCategory.MISC).sized(0.75F, 0.75F).clientTrackingRange(4).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "vertex_orb_projectile").toString()));
    public static final Supplier<EntityType<VertexDomainProjectileEntity>> VERTEX_DOMAIN_PROJECTILE = register("vertex_domain_projectile", () -> EntityType.Builder.<VertexDomainProjectileEntity>of(VertexDomainProjectileEntity::new, MobCategory.MISC).sized(2.0F, 2.0F).clientTrackingRange(4).fireImmune().build(new ResourceLocation(DungeonNowLoading.MOD_ID, "vertex_domain_projectile").toString()));
    public static final Supplier<EntityType<BorusArrowEntity>> BORUS_ARROW = register("borus_arrow", () -> EntityType.Builder.<BorusArrowEntity>of(BorusArrowEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(20).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "borus_arrow").toString()));

    // Misc
    public static final Supplier<EntityType<SpecialItemEntity>> SPECIAL_ITEM_ENTITY = register("special_item_entity", () -> EntityType.Builder.<SpecialItemEntity>of(SpecialItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "special_item_entity").toString()));
    public static final Supplier<EntityType<GreatExperienceBottleEntity>> GREAT_EXPERIENCE_BOTTLE = register("great_experience_bottle", () -> EntityType.Builder.<GreatExperienceBottleEntity>of(GreatExperienceBottleEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "great_experience_bottle").toString()));
    public static final Supplier<EntityType<RepulsorEntity>> REPULSOR = register("repulsor", () -> EntityType.Builder.<RepulsorEntity>of(RepulsorEntity::new, MobCategory.MISC).sized(0.99F, 0.3F).build(new ResourceLocation(DungeonNowLoading.MOD_ID, "command_pylon").toString()));

    private static <T extends EntityType<?>> Supplier<T> register(String name, Supplier<T> entityTypeSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.ENTITY_TYPE, name, entityTypeSupplier);
    }
    
    public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAllAttributes() {
        Map<EntityType<? extends LivingEntity>, AttributeSupplier> map = new HashMap<>();

        // Boss
        map.put(CHAOS_SPAWNER.get(), ChaosSpawnerEntity.createAttributes().build());
        map.put(FAIRKEEPER_BOROS.get(), FairkeeperBorosEntity.createAttributes().build());
        map.put(FAIRKEEPER_BOROS_PART.get(), FairkeeperBorosPartEntity.createAttributes().build());
        map.put(FAIRKEEPER_OUROS.get(), FairkeeperOurosEntity.createAttributes().build());
        map.put(FAIRKEEPER_OUROS_PART.get(), FairkeeperBorosPartEntity.createAttributes().build());



        // Monster
        map.put(HOLLOW.get(), HollowEntity.createAttributes().build());
        map.put(SPAWNER_CARRIER.get(), SpawnerCarrierEntity.createAttributes().build());
        map.put(SCUTTLE.get(), ScuttleEntity.createAttributes().build());
        map.put(BALLISTA_GOLEM.get(), BallistaGolemEntity.createAttributes().build());

        // Passive
        map.put(SEALED_CHAOS.get(), SealedChaosEntity.createAttributes().build());
        map.put(WHIMPER.get(), WhimperEntity.createAttributes().build());
        map.put(COPPER_CREEP.get(), CopperCreepEntity.createAttributes().build());

        // Misc
        map.put(REPULSOR.get(), RepulsorEntity.createAttributes().build());

        return map;
    }

    public static void init() {}
}
