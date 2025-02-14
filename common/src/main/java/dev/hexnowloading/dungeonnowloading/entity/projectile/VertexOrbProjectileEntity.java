package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.util.ModelledProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VertexOrbProjectileEntity extends ModelledProjectileEntity {

    private static final EntityDataAccessor<Integer> HURT_TIME = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HURT_TIME_DIRECT = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.FLOAT);

    private static final int BASE_DAMAGE = 6;
    private static final int SLOWNESS_DURATION = 20;
    private static final int SLOWNESS_AMPLIFIER = 4;
    private static final float HEALTH = 10;

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

    public VertexOrbProjectileEntity(Level level, LivingEntity owner) {
        super(DNLEntityTypes.VERTEX_ORB_PROJECTILE.get(), level);
        this.setOwner(owner);
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
        this.entityData.define(HURT_TIME, 0);
        this.entityData.define(HURT_TIME_DIRECT, 1);
        this.entityData.define(DAMAGE, 0.0f);
    }

    @Override
    protected void tickProjectile() {

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        this.checkInsideBlocks();

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.tickCount == 1) {
            this.setDeltaMovement(xPower, yPower, zPower);
        }

        if (this.verticalCollision || this.horizontalCollision) {
            this.life = DURATION_ON_GROUND;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEE_HURT, this.getSoundSource(), 3.0F, 2.0F);
            this.setDeltaMovement(Vec3.ZERO);
            spawnInitialRedstoneParticles();
            blockDestruction();
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

    private void blockDestruction() {
        BlockPos pos = this.blockPosition();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos targetPos = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (!this.level().getBlockState(targetPos).is(BlockTags.WITHER_IMMUNE)) {
                        this.level().destroyBlock(targetPos, true);
                    }
                }
            }
        }
    }


    private void applyEffect() {
        Level level = this.level();
        if (level.isClientSide) {
            return;
        }

        AABB effectBox = new AABB(
                this.getX() - 2, this.getY() - 2, this.getZ() - 2,
                this.getX() + 2, this.getY() + 2, this.getZ() + 2
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
        Vec3 start = this.position().add(0.0F, this.getBoundingBox().getYsize() / 2, 0.0F);
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
        float particleScale = BEAM_INITIAL_PARTICLE_SCALE_MIN + (float) Math.random() * (BEAM_INITIAL_PARTICLE_SCALE_MAX - BEAM_INITIAL_PARTICLE_SCALE_MIN);
        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                particleScale
        );

        spawnParticleOnAllPlanes(particleData, this.position(), 2);
    }

    private void spawnRedstoneParticle() {
        float scaleMultiplier = (float) Math.random() * MAX_RANDOM_PARTICLE_SCALE_MULTIPLIER;
        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(),
                BEAM_PARTICLE_SCALE * scaleMultiplier
        );

        spawnParticleOnAllPlanes(particleData, this.position(), 2);
    }

    private void spawnParticleOnAllPlanes(ParticleOptions particleData, Vec3 center, int radius) {
        double centerX = center.x;
        double centerY = center.y;
        double centerZ = center.z;

        for (double x = centerX - radius - 1; x <= centerX + radius; x += 1) {
            for (double z = centerZ - radius - 1; z <= centerZ + radius; z += 1) {
                if (x == centerX - radius - 1 || x == centerX + radius || z == centerZ - radius - 1 || z == centerZ + radius) {
                    this.level().addParticle(particleData, x + 0.5, centerY, z + 0.5, 0.0, 0.0, 0.0);
                }
            }
        }

        // **2. XY Plane (Vertical Front)**
        for (double x = centerX - radius - 1; x <= centerX + radius; x += 1) {
            for (double y = centerY - radius - 1; y <= centerY + radius; y += 1) {
                if (x == centerX - radius - 1 || x == centerX + radius || y == centerY - radius - 1|| y == centerY + radius) {
                    this.level().addParticle(particleData, x + 0.5, y + 0.5, centerZ, 0.0, 0.0, 0.0);
                }
            }
        }

        // **3. ZY Plane (Vertical Side)**
        for (double z = centerZ - radius - 1; z <= centerZ + radius; z += 1) {
            for (double y = centerY - radius - 1; y <= centerY + radius; y += 1) {
                if (z == centerZ - radius - 1|| z == centerZ + radius || y == centerY - radius - 1 || y == centerY + radius) {
                    this.level().addParticle(particleData, centerX, y + 0.5, z + 0.5, 0.0, 0.0, 0.0);
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

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        boolean bl;
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        }
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + f);
        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        boolean bl2 = bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).getAbilities().instabuild;
        if (bl || this.getDamage() > HEALTH) {
            this.discard();
        }
        return true;
    }

    @Override
    public void animateHurt(float f) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() * 11.0f);
    }

    public void shootTowardsTarget(double x, double y, double z, LivingEntity target, float speed, float inaccuracy) {
        this.moveTo(x, y, z, this.getYRot(), this.getXRot());
        this.reapplyPosition();

        // Calculate base direction
        Vec3 direction = new Vec3(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ()).normalize();

        // Add random inaccuracy
        double randX = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        double randY = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        double randZ = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;

        // Apply inaccuracy to direction vector
        Vec3 inaccurateDirection = new Vec3(direction.x + randX, direction.y + randY, direction.z + randZ).normalize();

        // Apply velocity
        this.xPower = inaccurateDirection.x * speed;
        this.yPower = inaccurateDirection.y * speed;
        this.zPower = inaccurateDirection.z * speed;
    }


    public void setDamage(float f) {
        this.entityData.set(DAMAGE, Float.valueOf(f));
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE).floatValue();
    }

    public void setHurtTime(int i) {
        this.entityData.set(HURT_TIME, i);
    }

    public int getHurtTime() {
        return this.entityData.get(HURT_TIME);
    }

    public void setHurtDir(int i) {
        this.entityData.set(HURT_TIME_DIRECT, i);
    }

    public int getHurtDir() {
        return this.entityData.get(HURT_TIME_DIRECT);
    }
}
