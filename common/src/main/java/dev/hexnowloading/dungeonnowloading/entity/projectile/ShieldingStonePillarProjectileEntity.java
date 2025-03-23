package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.block.ShieldingStonePillarBlock;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.ModelledProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.ProjectileUtils;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ShieldingStonePillarProjectileEntity extends ModelledProjectileEntity {

    private static final float IMPACT_RANGE = 3.0F;
    private static final float DAMAGE = 5.0F;
    private static final float SHILED_DAMAGE_REDUCTION = 0.55F;
    private static final boolean SHIELD_PENETRATION = true;
    private static final int LIFETIME = 1000;
    private static final int LIFETIME_AFTER_LANDING = 3;

    private int tickCount;
    private boolean hasLanded;
    private boolean canSummonVertexOrb;
    private float damagePercentage;

    public ShieldingStonePillarProjectileEntity(EntityType<? extends ShieldingStonePillarProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.tickCount = LIFETIME;
        this.hasLanded = false;
    }

    public ShieldingStonePillarProjectileEntity(Level level, LivingEntity livingEntity, float damagePercentage) {
        this(DNLEntityTypes.SHIELDING_STONE_PILLAR_PROJECTILE.get(), level);
        this.setOwner(livingEntity);
        this.damagePercentage = damagePercentage;
        this.canSummonVertexOrb = false;
    }

    public ShieldingStonePillarProjectileEntity(Level level, LivingEntity livingEntity, float damagePercentage, boolean canSummonVertexOrb) {
        this(DNLEntityTypes.SHIELDING_STONE_PILLAR_PROJECTILE.get(), level);
        this.setOwner(livingEntity);
        this.damagePercentage = damagePercentage;
        this.canSummonVertexOrb = canSummonVertexOrb;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void tickProjectile() {
        ProjectileUtils.checkAndUnloadProjectile(this);
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.onGround()) {
            if (this.level().isClientSide) {
                return;
            }

            if (!this.hasLanded && this.level().destroyBlock(this.blockPosition().below(), false, this)) {
                return;
            }

            if (!this.hasLanded) {
                AABB aabb = this.getBoundingBox().inflate(IMPACT_RANGE);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
                for (LivingEntity mob : targets) {
                    this.pushNearbyMobs(mob);
                }
                if (this.level().getBlockState(this.blockPosition().below()).is(this.getPillarBlock())) {
                    this.breakLogic();
                    this.discard();
                } else {
                    this.placePillarBlock();
                }

                this.tickCount = LIFETIME_AFTER_LANDING;
                this.hasLanded = true;
            }

            if (this.tickCount <= 0) {
                this.discard();
            }
            this.tickCount--;
        } else {
            if (this.tickCount <= 0) {
                this.discard();
            }
            this.tickCount--;
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    private void breakLogic() {

        this.level().destroyBlock(this.blockPosition().below(), false);

        if (this.canSummonVertexOrb) {
            VertexOrbProjectileEntity vertexOrbProjectileEntity = new VertexOrbProjectileEntity(this.level(), (LivingEntity) this.getOwner(), 2);
            vertexOrbProjectileEntity.shoot(this.getX(), this.getY() - 2, this.getZ(), this.getX(), vertexOrbProjectileEntity.getY() - 1, this.getZ(), 1.0F, 0.02F);
            this.level().addFreshEntity(vertexOrbProjectileEntity);
        }
    }

    private void pushNearbyMobs(LivingEntity mob) {
        float actualDamage = DAMAGE;
        if (mob instanceof FairkeeperSerpentEntity) {
            return;
        }
        if (this.getOwner() instanceof LivingEntity owner) {
            actualDamage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * damagePercentage;
        }
        if (mob instanceof Player player && player.isBlocking()) {
            player.disableShield(true);
            actualDamage *= 1.0F - SHILED_DAMAGE_REDUCTION;
        }
        double x = mob.getX() - this.getX();
        double z = mob.getZ() - this.getZ();
        double a = x * x + z * z;

        mob.push(x / a * 6.0F, 0.2F, z / a * 6.0F);
        mob.hurt(this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), actualDamage);
    }

    protected Block getPillarBlock() {
        return DNLBlocks.SHIELDING_STONE_PILLAR.get();
    }

    protected void placePillarBlock() {
        this.level().setBlock(this.blockPosition(), DNLBlocks.SHIELDING_STONE_PILLAR.get().defaultBlockState(), Block.UPDATE_ALL);
        this.level().setBlock(this.blockPosition().above(), DNLBlocks.SHIELDING_STONE_PILLAR.get().defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF , DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        ShieldingStonePillarBlock.linkOnPlaced(this.level(), this.blockPosition());
        ((ServerLevel) this.level()).sendParticles(new ScalableAxisParticleType.ScalableAxisParticleData(DNLParticleTypes.WHITE_SHOCKWAVE_PARTICLE.get(), 0, 90, 5.0F), this.blockPosition().getX() + 0.5F, this.blockPosition().getY() + 0.01F, this.blockPosition().getZ() + 0.5F, 1, 0, 0, 0, 0);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_LAND, this.getSoundSource(), 3.0F, 1.0F);
    }
}
