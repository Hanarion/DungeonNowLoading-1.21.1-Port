package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.util.FairkeeperSerpentEntity;
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
import org.joml.Vector3f;

import java.util.List;

public class VertexOrbProjectileEntity extends ModelledProjectileEntity {

    private static final EntityDataAccessor<Integer> HURT_TIME = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HURT_TIME_DIRECT = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> VELOCITY = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.INT);

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
    private boolean hasAppliedMovement;
    private boolean hasLanded;
    private boolean delayTick;

    //private final ParticleOptions TRAIL_PARTICLE = (ParticleOptions) DNLParticleTypes.VERTEX_SPARK_PARTICLE.get();

    public VertexOrbProjectileEntity(EntityType<? extends VertexOrbProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }


    public VertexOrbProjectileEntity(Level level, LivingEntity owner, int radius) {
        this(DNLEntityTypes.VERTEX_ORB_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.setRadius(radius);
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
        if (compoundTag.contains("radius", Tag.TAG_INT)) {
            this.setRadius(compoundTag.getInt("radius"));
        }
        this.hasLanded = compoundTag.getBoolean("hasLanded");

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("power", this.newDoubleList(this.xPower, this.yPower, this.zPower));
        compoundTag.putInt("life", this.life);
        compoundTag.putInt("radius", this.getRadius());
        compoundTag.putBoolean("hasLanded", this.hasLanded);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HURT_TIME, 0);
        this.entityData.define(HURT_TIME_DIRECT, 1);
        this.entityData.define(DAMAGE, 0.0f);
        this.entityData.define(VELOCITY, Vec3.ZERO.toVector3f());
        this.entityData.define(RADIUS, 0);
    }

    @Override
    protected void tickProjectile() {

        if (!this.hasAppliedMovement) {
            if (!this.level().isClientSide) {
                this.setDeltaMovement(xPower, yPower, zPower);
                this.setVelocity(new Vec3(xPower, yPower, zPower));
            } else {
                double d = this.getVelocity().x * this.getVelocity().x + this.getVelocity().y * this.getVelocity().y + this.getVelocity().z * this.getVelocity().z;
                if (!delayTick && d < 1.0E-7) {
                    delayTick = true;
                    return;
                }
                this.setDeltaMovement(new Vec3(this.getVelocity().x, this.getVelocity().y, this.getVelocity().z));
            }
            this.hasAppliedMovement = true;
        }

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        this.checkInsideBlocks();

        this.move(MoverType.SELF, this.getDeltaMovement());

        if ((this.verticalCollision || this.horizontalCollision) || this.getDeltaMovement().lengthSqr() < 1.0E-7 && this.life <= 0) {
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
        int r = this.getRadius();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -r; y <= r; y++) {
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

        int r = this.getRadius();

        AABB effectBox = new AABB(
                this.getX() - r, this.getY() - r, this.getZ() - r,
                this.getX() + r, this.getY() + r, this.getZ() + r
        );
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, effectBox);
        for (LivingEntity entity : entities) {
            if (entity instanceof FairkeeperSerpentEntity) {
                continue;
            }
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

        spawnParticleOnAllPlanes(particleData, this.position(), this.getRadius());
    }

    private void spawnRedstoneParticle() {
        float scaleMultiplier = (float) Math.random() * MAX_RANDOM_PARTICLE_SCALE_MULTIPLIER;
        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(),
                BEAM_PARTICLE_SCALE * scaleMultiplier
        );

        spawnParticleOnAllPlanes(particleData, this.position(), this.getRadius());
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
    public void push(Entity $$0) {
    }

    @Override
    public void push(double $$0, double $$1, double $$2) {
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
        shoot(x, y, z, target.getX(), target.getY(), target.getZ(), speed, inaccuracy);
    }

    public void shoot(double x, double y, double z, double targetX, double targetY, double targetZ, float speed, float inaccuracy) {
        this.moveTo(x, y, z, this.getYRot(), this.getXRot());
        this.reapplyPosition();
        Vec3 direction = new Vec3(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ()).normalize();
        double randX = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        double randY = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        double randZ = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        Vec3 inaccurateDirection = new Vec3(direction.x + randX, direction.y + randY, direction.z + randZ).normalize();
        this.xPower = inaccurateDirection.x * speed;
        this.yPower = inaccurateDirection.y * speed;
        this.zPower = inaccurateDirection.z * speed;
        this.setDeltaMovement(xPower, yPower, zPower);
        this.setVelocity(new Vec3(xPower, yPower, zPower));
        this.hasAppliedMovement = true;
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

    public void setVelocity(Vec3 vec3) {
        this.entityData.set(VELOCITY, vec3.toVector3f());
    }

    public Vector3f getVelocity() {
        return this.entityData.get(VELOCITY);
    }

    public void setRadius (int radius) {
        this.entityData.set(RADIUS, radius);
    }

    public int getRadius() {
        return this.entityData.get(RADIUS);
    }
}
