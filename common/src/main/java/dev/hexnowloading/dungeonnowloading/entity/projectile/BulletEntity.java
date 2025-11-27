package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class BulletEntity extends ThrowableItemProjectile {

    private BulletType bulletType = BulletType.IRON;
    private int life;

    public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public BulletEntity(Level level, LivingEntity owner, BulletType type) {
        super(DNLEntityTypes.BULLET.get(), owner, level);
        this.bulletType = type;
    }

    public void setBulletType(BulletType type) {
        this.bulletType = type;
    }

    public BulletType getBulletType() {
        return bulletType;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE; // purely visual; swap if you like
    }

    // --- physics / lifetime ---

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            life++;
            if (life > bulletType.getMaxLifeTicks()) {
                this.discard();
            }
        }
    }

    @Override
    protected float getGravity() {
        return bulletType.getGravity();
    }

    // --- damage ---

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        if (!this.level().isClientSide) {
            if (hitResult.getEntity() instanceof LivingEntity target) {
                DamageSource source = this.level().damageSources().thrown(this, this.getOwner());
                target.hurt(source, bulletType.getDamage());
            }

            this.discard();
        }
    }

    // --- save / load type ---

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("BulletType", bulletType.name());
        tag.putInt("Life", life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BulletType")) {
            this.bulletType = BulletType.byName(tag.getString("BulletType"));
        }
        this.life = tag.getInt("Life");
    }
}
