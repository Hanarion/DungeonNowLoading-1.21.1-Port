package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.ModelledProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.AxisParticleType;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.*;
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
    private static final EntityDataAccessor<Integer> DYING_TICK = SynchedEntityData.defineId(VertexOrbProjectileEntity.class, EntityDataSerializers.INT);

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
    public static final int DURATION_ON_GROUND = 300;

    private double xPower;
    private double yPower;
    private double zPower;
    private int life;
    private int expansionTick;
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
        this.setDyingTick(0);
        this.life = 0;
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
        this.entityData.define(DYING_TICK, 0);
    }

    @Override
    protected void tickProjectile() {

        if (this.getDyingTick() > 0) {
            this.setDyingTick(this.getDyingTick() - 1);
            if (this.getDyingTick() <= 0) {
                discard();
            }
            return;
        }

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

        if (this.life <= 0) {
            this.level().addParticle(DustParticleOptions.REDSTONE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }

        if ((this.verticalCollision || this.horizontalCollision) || this.getDeltaMovement().lengthSqr() < 1.0E-7 && this.life <= 0) {
            this.life = DURATION_ON_GROUND;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 3.0F, 1.0F);
            this.setDeltaMovement(Vec3.ZERO);
            spawnInitialRedstoneParticles();
            blockDestruction();
            this.expansionTick = 40;
        }

        if (this.life > 0) {
            this.life--;
            if (this.expansionTick > 0) {
                this.expansionTick--;
            }
            applyEffect();
            if (this.life > 50) {
                spawnRedstoneParticle();
            }
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
            if (applyDamage(entity, BASE_DAMAGE, 0.6F)) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER)); // Slowness V (4 = level 5)
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
        for (int i = 0; i < 10; i++) {
            this.level().addParticle(particleData, this.getX() + 2 * (this.level().getRandom().nextFloat() - this.level().getRandom().nextFloat()), this.getY()  + 2 * (this.level().getRandom().nextFloat() - this.level().getRandom().nextFloat()),this.getZ()  + 2 * (this.level().getRandom().nextFloat() - this.level().getRandom().nextFloat()), 0, 0, 0);
        }
        this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() ,this.getY() ,this.getZ(), 0, 0, 0);



        //spawnParticleOnAllPlanes(particleData, this.position(), this.getRadius());
    }

    private void spawnRedstoneParticle() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (this.level().getRandom().nextFloat() > 0.3F) return; // 10% spawn chance

        int radius = this.getRadius();
        Vec3 center = this.position();
        double centerX = center.x;
        double centerY = center.y - 0.5;
        double centerZ = center.z;

        int side = this.level().getRandom().nextInt(4);
        double x = centerX;
        double z = centerZ;
        float angleToSide = 0F;

        switch (side) {
            case 0: // +X Side
                x = centerX + radius + 0.49;
                z = centerZ - radius + this.level().getRandom().nextDouble() * (radius * 2);
                angleToSide = 90F + 180F; // Flipped
                break;
            case 1: // -X Side
                x = centerX - radius - 0.49;
                z = centerZ - radius + this.level().getRandom().nextDouble() * (radius * 2);
                angleToSide = -90F + 180F; // Flipped
                break;
            case 2: // +Z Side
                z = centerZ + radius + 0.49;
                x = centerX - radius + this.level().getRandom().nextDouble() * (radius * 2);
                angleToSide = 0F + 180F; // Flipped
                break;
            case 3: // -Z Side
                z = centerZ - radius - 0.49;
                x = centerX - radius + this.level().getRandom().nextDouble() * (radius * 2);
                angleToSide = 180F + 180F; // Flipped
                break;
        }

        // Ensure the angle remains within 0-360°
        angleToSide = (angleToSide + 360F) % 360F;

        // Spawn the particle moving upwards, aligned with the side of the boundary
        serverLevel.sendParticles(
                new AxisParticleType.AxisParticleData(DNLParticleTypes.VERTEX_BOUNDARY_PARTICLE.get(), 1, angleToSide),
                x, centerY, z, 1, // Position at ground level
                0.0, 0.02, 0.0, // Motion: Only moving up
                0.0
        );
    }




    private void spawnParticleOnAllPlanes(ParticleOptions particleData, Vec3 center, int radius) {
        double centerX = center.x;
        double centerY = center.y;
        double centerZ = center.z;



        /*for (double x = centerX - radius - 1; x <= centerX + radius; x += 1) {
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
        }*/
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
        if (this.getDyingTick() > 0) {
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
        bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).getAbilities().instabuild;
        if (bl || this.getDamage() > HEALTH) {
            if (this.getLife() > 0) {
                this.setDyingTick(20);
                this.refreshDimensions();
            } else {
                this.discard();
            }
            ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                    DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                    1.0F
            );
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 1.0F, 2.0F);
            ((ServerLevel)this.level()).sendParticles(particleData, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.0f);
        }
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (this.getDyingTick() > 0) {
            return EntityDimensions.fixed(0.0F, 0.0F);
        }
        return EntityDimensions.scalable(1.0F, 1.0F);
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

    public int getLife() { return this.life; }

    public int getMaxLife() { return DURATION_ON_GROUND; }

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

    public void setDyingTick(int tick) { this.entityData.set(DYING_TICK, tick); }

    public int getDyingTick() { return this.entityData.get(DYING_TICK); }

    public int getExpansionTick() {return expansionTick;}
}
