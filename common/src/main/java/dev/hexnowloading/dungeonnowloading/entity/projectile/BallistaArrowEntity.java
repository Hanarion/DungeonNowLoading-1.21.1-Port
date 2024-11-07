package dev.hexnowloading.dungeonnowloading.entity.projectile;

import com.google.common.collect.Lists;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class BallistaArrowEntity extends AbstractArrow {

    private final int DAMAGE = 19;
    private final float KNOCKBACK = 3.0F;
    private final float EXPLOSION_STRENGTH = 4.0F;

    public BallistaArrowEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public BallistaArrowEntity(LivingEntity owner, Level level) {
        super(DNLEntityTypes.BALLISTA_ARROW.get(), owner, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) {
            return;
        }
        Entity target = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        DamageSource damageSource;
        int damage = DAMAGE;

        if (owner == null) {
            damageSource = this.damageSources().arrow(this, this);
        } else {
            damageSource = this.damageSources().arrow(this, owner);
            if (owner instanceof LivingEntity) {
                ((LivingEntity) owner).setLastHurtByMob((LivingEntity) target);
            }
        }

        boolean isEnderman = target.getType() == EntityType.ENDERMAN;
        int fireTick = target.getRemainingFireTicks();
        if (this.isOnFire() && !isEnderman) {
            target.setSecondsOnFire(15);
        }

        if (target.hurt(damageSource, (float) damage)) {
            if (isEnderman) {
                return;
            }

            if (target instanceof LivingEntity livingEntity) {
                double knockback = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double) KNOCKBACK * 0.6 * knockback);
                if (vec3.lengthSqr() > 0.0) {
                    livingEntity.push(vec3.x, 0.1, vec3.z);
                }

                if (livingEntity instanceof Player player && player.isBlocking()) {
                    player.disableShield(true);
                }

                if (!this.level().isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingEntity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)owner, livingEntity);
                }

            }

            this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        } else {
            target.setRemainingFireTicks(fireTick);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        this.explode();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    private void explode() {
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_STRENGTH, Level.ExplosionInteraction.NONE);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

    @Override
    public void shootFromRotation(Entity entity, float pitch, float yaw, float roll, float velocity, float inaccuracy) {
        super.shootFromRotation(entity, pitch, yaw, roll, velocity, inaccuracy);
    }
}
