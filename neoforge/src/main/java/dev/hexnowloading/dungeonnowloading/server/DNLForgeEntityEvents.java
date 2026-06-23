package dev.hexnowloading.dungeonnowloading.server;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.DNLEntityEvents;
import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = DungeonNowLoading.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class DNLForgeEntityEvents {

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        for (EntityType<? extends LivingEntity> type : DNLEntityTypes.getAllAttributes().keySet()) {
            event.put(type, DNLEntityTypes.getAllAttributes().get(type));
        }
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(DNLEntityTypes.HOLLOW.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HollowEntity::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(DNLEntityTypes.SPAWNER_CARRIER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnerCarrierEntity::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(DNLEntityTypes.SCUTTLE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScuttleEntity::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    // 1.21 NeoForge: LivingDamageEvent -> LivingDamageEvent.Pre (post-armor, getNewDamage/setNewDamage).
    public static void onLivingDamageEvent(LivingDamageEvent.Pre event) {
        Entity attackingEntity = event.getSource().getEntity();
        LivingEntity hurtedEntity = event.getEntity();
        float damage = event.getNewDamage();
        if (attackingEntity instanceof LivingEntity livingEntity) {
            event.setNewDamage(DNLEntityEvents.onLivingDamageEvent(livingEntity, hurtedEntity, damage));
        }
    }

    // 1.21 NeoForge: LivingHurtEvent -> LivingIncomingDamageEvent (pre-armor, getAmount/setAmount).
    public static void onLivingHurtEvent(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        float damage = event.getAmount();
        if (attacker instanceof LivingEntity attackerEntity) {
            event.setAmount(DNLEntityEvents.onLivingHurtEvent(attackerEntity, target, damage));
        }
    }

}
