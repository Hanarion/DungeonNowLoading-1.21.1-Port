package dev.hexnowloading.dungeonnowloading.entity.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ProjectileUtils {

    public static void checkAndUnloadProjectile(Entity entity) {
        if (entity != null && !entity.level().isClientSide) {
            if (!((ServerLevel) entity.level()).isPositionEntityTicking(entity.blockPosition())) {
                entity.discard();
            }
        }
    }

}
