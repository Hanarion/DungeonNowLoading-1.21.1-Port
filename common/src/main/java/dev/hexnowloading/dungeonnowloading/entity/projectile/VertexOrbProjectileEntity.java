package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.util.ModelledProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VertexOrbProjectileEntity extends ModelledProjectileEntity {

    private static final int BASE_DAMAGE = 6;
    private static final int SLOWNESS_DURATION = 20;
    private static final int SLOWNESS_AMPLIFIER = 4;

    private static final double BEAM_INITIAL_PARTICLE_SPACING = 0.5d;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MIN = 0.2f;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MAX = 0.4f;
    private static final double BEAM_PARTICLE_SPACING = 0.1d;
    private static final float BEAM_PARTICLE_SCALE = 0.05f;
    private static final float MAX_RANDOM_PARTICLE_SCALE_MULTIPLIER = 3;
    private static final int DURATION_ON_GROUND = 300;

    private double xPower;
    private double yPower;
    private double zPower;
    private int life;
    //private final ParticleOptions TRAIL_PARTICLE = (ParticleOptions) DNLParticleTypes.VERTEX_SPARK_PARTICLE.get();

    public VertexOrbProjectileEntity(EntityType<? extends VertexOrbProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("power", Tag.TAG_LIST)) {
            ListTag listTag = compoundTag.getList("power", Tag.TAG_DOUBLE);
            if (listTag.size() == 3) {
                this.xPower = listTag.getDouble(0);
                this.yPower = listTag.getDouble(1);
                this.zPower = listTag.getDouble(2);
            }
        }
        if (compoundTag.contains("life", Tag.TAG_INT)) {
            this.life = compoundTag.getInt("life");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("power", this.newDoubleList(this.xPower, this.yPower, this.zPower));
        compoundTag.putInt("life", this.life);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void tickProjectile() {

        if (this.tickCount > 400 && this.life <= 0) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        this.checkInsideBlocks();

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.tickCount == 1) {
            this.setDeltaMovement(xPower, yPower, zPower);
        }

        if (this.verticalCollision || this.horizontalCollision) {
            this.life = DURATION_ON_GROUND;
            this.setDeltaMovement(Vec3.ZERO);
            spawnInitialRedstoneParticles();
        }

        if (this.life > 0) {
            this.life--;
            applyEffect();
            spawnRedstoneParticle();
            if (this.life <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    private void applyEffect() {
        Level level = this.level();
        if (level.isClientSide) {
            return;
        }

        AABB effectBox = new AABB(
                this.getX() - 2, this.getY() - 1, this.getZ() - 2,
                this.getX() + 2, this.getY() + 1, this.getZ() + 2
        );
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, effectBox);
        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER)); // Slowness V (4 = level 5)
            if (applyDamage(entity, BASE_DAMAGE, 0.6F)) {
                spawnRedstoneBeamParticle((ServerLevel) level, entity);
            }
        }
    }

    private void spawnRedstoneBeamParticle(ServerLevel level, LivingEntity target) {
        Vec3 start = this.position();
        Vec3 end = target.position().add(0, target.getBbHeight() * 0.5, 0);

        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        double stepSize = BEAM_INITIAL_PARTICLE_SPACING;
        int particleCount = (int) (distance / stepSize);
        Vec3 step = direction.normalize().scale(stepSize);

        float particleScale = BEAM_INITIAL_PARTICLE_SCALE_MIN + (float) Math.random() *
                (BEAM_INITIAL_PARTICLE_SCALE_MAX - BEAM_INITIAL_PARTICLE_SCALE_MIN);
        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                particleScale
        );

        for (int i = 0; i <= particleCount; i++) {
            Vec3 particlePos = start.add(step.scale(i));

            level.sendParticles(particleData, particlePos.x, particlePos.y, particlePos.z, 1,
                    0.0F, 0.0F, 0.0F, 0.0F);
        }
    }


    private void spawnInitialRedstoneParticles() {
        double y = this.getY(); // Keep particles at the ground level
        Level level = this.level();
        float particleScale = BEAM_INITIAL_PARTICLE_SCALE_MIN + (float) Math.random() * (BEAM_INITIAL_PARTICLE_SCALE_MAX - BEAM_INITIAL_PARTICLE_SCALE_MIN);
        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                particleScale
        );

        // Iterate over a 5x5 square outline
        for (double x = this.getX() - 3; x <= this.getX() + 2; x += 1) {
            for (double z = this.getZ() - 3; z <= this.getZ() + 2; z += 1) {
                if (x == this.getX() - 3 || x == this.getX() + 2 || z == this.getZ() - 3 || z == this.getZ() + 2) {
                    level.addParticle(particleData, x + 0.5, y, z + 0.5, 1.0, 0.0, 0.0); // Red color
                }
            }
        }
    }

    private void spawnRedstoneParticle() {
        double y = this.getY(); // Keep particles at the ground level
        Level level = this.level();
        float scaleMultiplier = (float) Math.random() * MAX_RANDOM_PARTICLE_SCALE_MULTIPLIER;
        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(),
                BEAM_PARTICLE_SCALE * scaleMultiplier
        );

        // Iterate over a 5x5 square outline
        for (double x = this.getX() - 3; x <= this.getX() + 2; x += 1) {
            for (double z = this.getZ() - 3; z <= this.getZ() + 2; z += 1) {
                if (x == this.getX() - 3 || x == this.getX() + 2 || z == this.getZ() - 3 || z == this.getZ() + 2) {
                    level.addParticle(particleData, x + 0.5, y, z + 0.5, 1.0, 0.0, 0.0); // Red color
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        applyDamage(entityHitResult.getEntity(), BASE_DAMAGE, 0.6F);
    }

    private boolean applyDamage(Entity targetEntity, int baseDamage, float ownerAttackPercentage) {
        LivingEntity owner = null;
        int damage = baseDamage;
        if (this.getOwner() != null && this.getOwner() instanceof LivingEntity livingEntity) {
            damage = (int) (livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE) * ownerAttackPercentage);
            owner = livingEntity;
        }
        if (targetEntity instanceof LivingEntity target) {
            return target.hurt(this.level().damageSources().mobProjectile(this, owner), damage);
        }
        return false;
    }
}
