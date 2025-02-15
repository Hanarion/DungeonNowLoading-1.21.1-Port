package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BorusArrowEntity extends AbstractArrow {

    private final int DAMAGE = 10;
    private final int MAX_BLOCK_PENETRATION = 3;
    private final float KNOCKBACK = 3.0F;
    private final int DECELERATION_TICK = 2;

    private int arrowPhase;
    private int blockBreakCount;
    private Vec3 constantDeltaMovement;

    public BorusArrowEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.constantDeltaMovement = Vec3.ZERO;
        this.blockBreakCount = 0;
        this.arrowPhase = 0;
    }

    public BorusArrowEntity(LivingEntity owner, Level level) {
        super(DNLEntityTypes.BORUS_ARROW.get(), owner, level);
        this.setOwner(owner);
        this.constantDeltaMovement = Vec3.ZERO;
        this.blockBreakCount = 0;
        this.arrowPhase = 0;
    }

    @Override
    public void tick() {
        if (this.tickCount > 100) {
            this.discard();
        }

        if (this.arrowPhase == 0) {
            this.setNoGravity(true);
            moving(1.0F);
        } else if (this.arrowPhase >= 1 && this.arrowPhase < DECELERATION_TICK + 1){
            moving(0.5F);
            this.arrowPhase++;
        } else {
            this.setNoGravity(false);
        }
        super.tick();
    }

    private void moving(float scale) {
        if (this.tickCount <= 1) {
            this.constantDeltaMovement = this.getDeltaMovement();
        } else {
            this.setDeltaMovement(this.constantDeltaMovement.scale(scale));
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (blockBreakCount >= MAX_BLOCK_PENETRATION) {
            return;
        }
        BlockState blockState = this.level().getBlockState(blockHitResult.getBlockPos());
        if (!blockState.is(BlockTags.WITHER_IMMUNE)) {
            this.level().destroyBlock(blockHitResult.getBlockPos(), true);
            this.setDeltaMovement(this.constantDeltaMovement);
            this.blockBreakCount++;
            if (blockBreakCount >= MAX_BLOCK_PENETRATION) {
                this.arrowPhase++;
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) {
            return;
        }
        Entity target = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        DamageSource damageSource;
        int damage = owner instanceof LivingEntity livingEntity ? (int) livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE) : DAMAGE;

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
}
