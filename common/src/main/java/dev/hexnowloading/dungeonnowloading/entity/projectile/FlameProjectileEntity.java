package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.util.ProjectileUtils;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class FlameProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<ItemStack> FIRE_ITEM_STACK = SynchedEntityData.defineId(FlameProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FlameProjectileEntity.class, EntityDataSerializers.FLOAT);

    public FlameProjectileEntity(EntityType entityType, Level level) {
        super(entityType, level);
    }

    public FlameProjectileEntity(LivingEntity owner, Level level) {
        super(DNLEntityTypes.FLAME_PROJECTILE.get(), owner, level);
        this.setDamage(0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 0.0f);
    }

    @Override
    public void tick() {
        ProjectileUtils.checkAndUnloadProjectile(this);
        super.tick();

        float progress = (float) this.tickCount / 60.0F; // ✅ Normalize progress (0 to 1)
        float scale = 0.4F + (1.6F * progress); // ✅ Scale from 0.5 → 1.0

        ScalableParticleType.ScalableParticleData particleData;

        if (this.getOwner() instanceof Player) {
            particleData = new ScalableParticleType.ScalableParticleData(DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), scale);
        } else {
            particleData = new ScalableParticleType.ScalableParticleData(DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), 1.0F);
        }

        this.level().addAlwaysVisibleParticle(particleData, true, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);

        if (this.tickCount > 60) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == EntityEvent.DEATH) {
            for (int i = 0; i < 8; ++i) {
                ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), 1.0F);
                this.level().addAlwaysVisibleParticle(particleData, true, this.getX(), this.getY(), this.getZ(), ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level().isClientSide) {
            return;
        }
        Entity target = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        if (target instanceof Arrow arrow) {
            arrow.discard();
            return;
        }
        if (owner instanceof LivingEntity livingEntity) {
            double damageAmount = livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
            target.setSecondsOnFire(5);
            if (!(target instanceof LivingEntity targetLivingEntity)) {
                return;
            }
            if (targetLivingEntity.fireImmune()) {
                return;
            }
            if (targetLivingEntity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                return;
            }
            if (this.getDamage() > 0 && target.hurt(this.damageSources().mobProjectile(this, livingEntity), this.getDamage()) && target.isAlive()) {
                this.doEnchantDamageEffects(livingEntity, target);
                return;
            }
            if (target.hurt(this.damageSources().mobProjectile(this, livingEntity), (float) damageAmount) && target.isAlive()) {
                this.doEnchantDamageEffects(livingEntity, target);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level().isClientSide) {
            return;
        }
        Entity owner = this.getOwner();
        if (!(owner instanceof LivingEntity) || this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
            if (this.level().isEmptyBlock(blockPos)) {
                this.level().setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level(), blockPos));
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public void setDamage(float f) {
        this.entityData.set(DAMAGE, Float.valueOf(f));
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE).floatValue();
    }
}
