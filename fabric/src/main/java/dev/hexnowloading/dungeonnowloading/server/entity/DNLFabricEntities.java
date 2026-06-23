package dev.hexnowloading.dungeonnowloading.server.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

public class DNLFabricEntities {
    public static void registerSpawnPlacements() {
        // 1.21: SpawnPlacements.Type -> SpawnPlacementType (SpawnPlacementTypes constants);
        // SpawnPlacements.register is private in vanilla, widened via dungeonnowloading.accesswidener.
        register(DNLEntityTypes.HOLLOW.get(), SpawnPlacementTypes.NO_RESTRICTIONS);
        register(DNLEntityTypes.SPAWNER_CARRIER.get(), SpawnPlacementTypes.ON_GROUND);
    }

    private static <T extends Monster> void register(net.minecraft.world.entity.EntityType<T> type, SpawnPlacementType placement) {
        SpawnPlacements.register(type, placement, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
    }
}
