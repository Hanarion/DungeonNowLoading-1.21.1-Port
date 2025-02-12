package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.util.ModelledProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class VertexDomainProjectileEntity extends ModelledProjectileEntity {
    public VertexDomainProjectileEntity(EntityType<? extends VertexDomainProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {

    }
}
