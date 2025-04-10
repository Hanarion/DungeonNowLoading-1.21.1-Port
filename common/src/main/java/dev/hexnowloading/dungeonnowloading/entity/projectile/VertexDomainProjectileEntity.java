package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.ModelledProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.AxisParticleType;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.BiConsumer;

public class VertexDomainProjectileEntity extends ModelledProjectileEntity {

    private static final EntityDataAccessor<Integer> HURT_TIME = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HURT_TIME_DIRECT = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> VELOCITY = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<VertexDomainAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityStates.VERTEX_DOMAIN_ANIMATION_STATE);
    private static final EntityDataAccessor<Integer> DYING_TICK = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> GROUND_COLLISION = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.BOOLEAN);


    private final DynamicGameEventListener<BlockPlaceBreakListener> dynamicBlockPlaceBreakListener;

    public final AnimationState spinAnimationState = new AnimationState();
    public final AnimationState impactAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();

    private static final int BASE_DAMAGE = 20;
    private static final int SLOWNESS_AMPLIFIER = 4;
    private static final int SLOWNESS_DURATION = 100;
    public static final int DURATION_ON_GROUND = 1200;
    private static final float BASE_HEALTH = 100F;
    private static final int RANGE = 7;

    private static final double BEAM_INITIAL_PARTICLE_SPACING = 0.5d;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MIN = 0.2f;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MAX = 0.4f;

    private double xPower;
    private double yPower;
    private double zPower;
    private int life;
    private float health;
    private boolean hasAppliedMovement;
    private boolean delayTick;
    private boolean explosionImmune;

    private int impactAnimationTimeOut;
    private int expansionTick;
    public static final int IMPACT_ANIMATION_DURATION = 5;
    public static final int EXPANSION_DURATION = 20;

    public VertexDomainProjectileEntity(EntityType<? extends VertexDomainProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.dynamicBlockPlaceBreakListener = new DynamicGameEventListener<>(new BlockPlaceBreakListener(new EntityPositionSource(this, 1.0F), RANGE));
        this.health = BASE_HEALTH;
    }

    public VertexDomainProjectileEntity(Level level, LivingEntity owner, float health) {
        super(DNLEntityTypes.VERTEX_DOMAIN_PROJECTILE.get(), level);
        this.dynamicBlockPlaceBreakListener = new DynamicGameEventListener<>(new BlockPlaceBreakListener(new EntityPositionSource(this, 1.0F), RANGE));
        this.setOwner(owner);
        this.health = health;
        this.setDyingTick(0);
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
        if (compoundTag.contains("health", Tag.TAG_FLOAT)) {
            this.health = compoundTag.getFloat("health");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("power", this.newDoubleList(this.xPower, this.yPower, this.zPower));
        compoundTag.putInt("life", this.life);
        compoundTag.putFloat("health", this.health);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HURT_TIME, 0);
        this.entityData.define(HURT_TIME_DIRECT, 1);
        this.entityData.define(DAMAGE, 0.0f);
        this.entityData.define(VELOCITY, Vec3.ZERO.toVector3f());
        this.entityData.define(ANIMATION_STATE, VertexDomainAnimationState.IDLE);
        this.entityData.define(DYING_TICK, 0);
        this.entityData.define(GROUND_COLLISION, false);
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
            this.transitionTo(VertexDomainAnimationState.SPIN);
            this.hasAppliedMovement = true;
        }

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        this.checkInsideBlocks();

        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.life <= 0) {
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * 2.0;
                double offsetY = (Math.random() - 0.5) * 2.0;
                double offsetZ = (Math.random() - 0.5) * 2.0;

                this.level().addParticle(DustParticleOptions.REDSTONE,
                        this.getX() + offsetX,
                        this.getY() + offsetY + this.getBbHeight() * 0.5F,
                        this.getZ() + offsetZ,
                        0.0, 0.0, 0.0);
            }
        }

        if ((this.onGround() || this.horizontalCollision) && this.life <= 0) {
            this.life = DURATION_ON_GROUND;
            this.transitionTo(VertexDomainAnimationState.IMPACT);
            this.expansionTick = EXPANSION_DURATION;
            if (!this.level().isClientSide) {
                this.entityData.set(GROUND_COLLISION, this.onGround());
            }
            this.impactDamage();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.END_GATEWAY_SPAWN, this.getSoundSource(), 3.0F, 2.0F);
        }

        if (this.life > 0) {
            this.life--;
            if (this.expansionTick > 0) {
                this.expansionTick--;
            }
            spawnBoundaryParticles(RANGE);
            if (!this.level().isClientSide && (this.life <= 0 || !this.entityData.get(GROUND_COLLISION) || this.level().getBlockState(this.blockPosition().below()).isAir())) {
                this.remove(RemovalReason.DISCARDED);
            }
        }

        this.animationControl();
        if (this.life <= 0) {
            int DESTRUCTION_RANGE = 2;
            this.blockDestructionTick(-DESTRUCTION_RANGE, DESTRUCTION_RANGE, -1, 3, -DESTRUCTION_RANGE, DESTRUCTION_RANGE);
        }
    }

    @Override
    public void setDeltaMovement(Vec3 vec31) {
        Vec3 vec3 = vec31;
        if (this.entityData.get(GROUND_COLLISION)) {
            vec3 = Vec3.ZERO;
        };
        super.setDeltaMovement(vec3);
    }

    private void impactDamage() {
        float particleScale = BEAM_INITIAL_PARTICLE_SCALE_MIN + (float) Math.random() * (BEAM_INITIAL_PARTICLE_SCALE_MAX - BEAM_INITIAL_PARTICLE_SCALE_MIN);
        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                particleScale
        );
        for (int i = 0; i < 30; i++) {
            this.level().addParticle(particleData, this.getX() + 6 * (this.level().getRandom().nextFloat() - this.level().getRandom().nextFloat()), this.getY()  + 6 * (this.level().getRandom().nextFloat() - this.level().getRandom().nextFloat()),this.getZ()  + 6 * (this.level().getRandom().nextFloat() - this.level().getRandom().nextFloat()), 0, 0, 0);
        }
        this.explosionImmune = true;
        if (!this.level().isClientSide) {
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), 6.0F, Level.ExplosionInteraction.NONE);
        }
        this.explosionImmune = false;
    }

    private void blockDestructionTick(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        if (this.level().isClientSide) {
            return;
        }
        for (int ix = minX; ix <= maxX; ix++) {
            for (int iz = minZ; iz <= maxZ; iz++) {
                for (int iy = minY; iy <= maxY; iy++) {
                    int dx = this.getBlockX() + ix;
                    int dy = this.getBlockY() + iy;
                    int dz = this.getBlockZ() + iz;
                    BlockPos blockPos = new BlockPos(dx, dy, dz);
                    BlockState blockState = this.level().getBlockState(blockPos);
                    if (!blockState.isAir()) {
                        if (!blockState.is(BlockTags.WITHER_IMMUNE)) {
                            this.level().destroyBlock(blockPos, false, this);
                        }
                    }
                }
            }
        }
    }

    private void animationControl() {
        if (!this.level().isClientSide) {
            return;
        }

        if (this.impactAnimationTimeOut-- > 0) {
            if (this.impactAnimationTimeOut <= 0) {
                this.transitionTo(VertexDomainAnimationState.IDLE);
            }
        }

        if (this.impactAnimationState.isStarted() && this.impactAnimationTimeOut <= 0) {
            this.impactAnimationTimeOut = IMPACT_ANIMATION_DURATION;
        }
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> eventListener) {
        Level level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            eventListener.accept(this.dynamicBlockPlaceBreakListener, serverLevel);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        applyDamage(entityHitResult.getEntity(), BASE_DAMAGE, 1.0F);
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

            level.sendParticles(particleData, particlePos.x, particlePos.y, particlePos.z, 1, 0.0F, 0.0F, 0.0F, 0.0F);
            level.sendParticles(DustParticleOptions.REDSTONE, particlePos.x, particlePos.y, particlePos.z, 1, 0.0D, 0.0, 0.0, 0.0);
        }
    }

    private void outlineCauseBlock(ServerLevel level, BlockPos blockPos) {
        double x = (double) blockPos.getX() + 0.5D;
        double y = (double) blockPos.getY() + 0.5D;
        double z = (double) blockPos.getZ() + 0.5D;

        level.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 270), blockPos.getX() + 1.1F, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 90), blockPos.getX() - 0.1F, y, z, 1, 0, 0, 0, 0);

        level.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 0, 90), x, blockPos.getY() + 1.1F, z, 1, 0, 0, 0, 0);
        level.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 0, 270), x, blockPos.getY() - 0.1F, z, 1, 0, 0, 0, 0);

        level.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 180), x, y, blockPos.getZ() + 1.1F, 1, 0, 0, 0, 0);
        level.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 0), x, y, blockPos.getZ() - 0.1F, 1, 0, 0, 0, 0);
    }

    public void spawnBoundaryParticles(int radius) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return; // Ensure we are on the server side

        // Define the 7x7x7 region centered around the entity
        double centerX = Mth.floor(this.getX()) + 0.5F;
        double centerY = Mth.floor(this.getY()) + 0.5F;
        double centerZ = Mth.floor(this.getZ()) + 0.5F;
        double halfSize = radius - 0.5F;

        double minX = centerX - halfSize;
        double maxX = centerX + halfSize;
        double minY = centerY - halfSize;
        double maxY = centerY + halfSize;
        double minZ = centerZ - halfSize;
        double maxZ = centerZ + halfSize;

        float xy = (float) (1.0F - ((float) (maxX - minX) * (maxY - minY)) / 1024F);
        float xz = (float) (1.0F - ((float) (maxX - minX) * (maxZ - minZ)) / 1024F);
        float yz = (float) (1.0F - ((float) (maxY - minY) * (maxZ - minZ)) / 1024F);

        double x = minX + (maxX - minX) * level().random.nextFloat();
        double y = minY + (maxY - minY) * level().random.nextFloat();
        double z = minZ + (maxZ - minZ) * level().random.nextFloat();
        float r = level().random.nextFloat();

        // YZ Plane (Front & Back)
        if (r + 0.2F > yz) {
            serverLevel.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 90),
                    minX + (level().random.nextFloat() - level().random.nextFloat()) * 0.1F, y, z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 270),
                    maxX + (level().random.nextFloat() - level().random.nextFloat()) * 0.1F, y, z, 1, 0, 0, 0, 0);
        }

        // XZ Plane (Top & Bottom)
        if (r + 0.2F > xz) {
            serverLevel.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 0, 270),
                    x, minY + (level().random.nextFloat() - level().random.nextFloat()) * 0.1F, z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 0, 90),
                    x, maxY + (level().random.nextFloat() - level().random.nextFloat()) * 0.1F, z, 1, 0, 0, 0, 0);
        }

        // XY Plane (Left & Right)
        if (r + 0.2F > xy) {
            serverLevel.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 0),
                    x, y, minZ + (level().random.nextFloat() - level().random.nextFloat()) * 0.1F, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(new AxisParticleType.AxisParticleData(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), 1, 180),
                    x, y, maxZ + (level().random.nextFloat() - level().random.nextFloat()) * 0.1F, 1, 0, 0, 0, 0);
        }
    }


    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return entity.canBeCollidedWith() || entity.isPushable();
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.getDyingTick() <= 0;
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
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (damageSource.is(DamageTypes.EXPLOSION)) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public void shootTowardsTarget(double x, double y, double z, LivingEntity target, float speed, float inaccuracy) {
        this.moveTo(x, y, z, this.getYRot(), this.getXRot());
        this.reapplyPosition();
        Vec3 direction = new Vec3(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ()).normalize();
        double randX = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        double randY = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        double randZ = (Mth.nextDouble(this.random, -1.0, 1.0)) * inaccuracy;
        Vec3 inaccurateDirection = new Vec3(direction.x + randX, direction.y + randY, direction.z + randZ).normalize();
        this.xPower = inaccurateDirection.x * speed;
        this.yPower = inaccurateDirection.y * speed;
        this.zPower = inaccurateDirection.z * speed;
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
        this.transitionTo(VertexDomainAnimationState.SPIN);
        this.hasAppliedMovement = true;
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
        if (this.explosionImmune) {
            return false;
        }
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + f);
        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).getAbilities().instabuild;
        if (bl || this.getDamage() > this.health) {
            if (this.getLife() > 0) {
                this.setDyingTick(20);
                this.refreshDimensions();
            } else {
                this.discard();
            }
            ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                    DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                    3.0F
            );
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 1.0F, 2.0F);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), DNLSounds.OVERCHARGED_REDSTONE_BLOCK_TNT_EXPLOSION.get(), this.getSoundSource(), 1.0F, 2.0F);
            ((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 5, 2, 2, 2, 0.0f);
            ((ServerLevel)this.level()).sendParticles(particleData, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.0f);
        }
        return true;
    }

    @Override
    public void remove(RemovalReason removalReason) {
        super.remove(removalReason);

        if (this.level() instanceof ServerLevel serverLevel) {
            this.dynamicBlockPlaceBreakListener.remove(serverLevel);
        }
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

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            VertexDomainAnimationState animationState = this.getAnimationState();
            this.resetAnimations();
            switch (animationState) {
                case IDLE -> this.idleAnimationState.startIfStopped(this.tickCount);
                case SPIN -> this.spinAnimationState.startIfStopped(this.tickCount);
                case IMPACT -> this.impactAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetAnimations() {
        this.idleAnimationState.stop();
        this.spinAnimationState.stop();
        this.impactAnimationState.stop();
    }

    public VertexDomainProjectileEntity transitionTo(VertexDomainAnimationState state) {
        switch (state) {
            case IDLE:
                this.setAnimationState(VertexDomainAnimationState.IDLE);
                break;
            case SPIN:
                this.setAnimationState(VertexDomainAnimationState.SPIN);
                break;
            case IMPACT:
                this.setAnimationState(VertexDomainAnimationState.IMPACT);
                break;
        }
        return this;
    }

    public Vector3f getVelocity() {
        return this.entityData.get(VELOCITY);
    }

    public void setVelocity(Vec3 vec3) {
        this.entityData.set(VELOCITY, vec3.toVector3f());
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

    public float getHealth() {
        return this.health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public void setDyingTick(int tick) { this.entityData.set(DYING_TICK, tick); }

    public int getDyingTick() { return this.entityData.get(DYING_TICK); }

    public int getLife() { return this.life; }

    public int getImpactAnimationTimeOut() { return this.impactAnimationTimeOut; }

    public int getExpansionTick() { return this.expansionTick; }

    public VertexDomainAnimationState getAnimationState() { return this.entityData.get(ANIMATION_STATE); }

    public void setAnimationState(VertexDomainAnimationState state) { this.entityData.set(ANIMATION_STATE, state); }

    public enum VertexDomainAnimationState {
        IDLE,
        SPIN,
        IMPACT
    }

    class BlockPlaceBreakListener implements GameEventListener {
        private final PositionSource listenerSource;
        private final int listenerRadius;

        public BlockPlaceBreakListener(PositionSource positionSource, int radius) {
            this.listenerSource = positionSource;
            this.listenerRadius = radius - 1;
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return (int) Math.ceil(this.listenerRadius * Math.sqrt(3));
        }

        @Override
        public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 pos) {
            if (VertexDomainProjectileEntity.this.life <= 0) {
                return false;
            }

            BlockPos eventBlockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));

            Vec3 centerPos = this.getListenerSource().getPosition(serverLevel).orElseThrow();
            BlockPos centerBlockPos = new BlockPos((int) Math.floor(centerPos.x), (int) Math.floor(centerPos.y) - 1, (int) Math.floor(centerPos.z));
            if (Math.abs(eventBlockPos.getX() - centerBlockPos.getX()) <= this.listenerRadius && Math.abs(eventBlockPos.getY() - centerBlockPos.getY()) <= this.listenerRadius && Math.abs(eventBlockPos.getZ() - centerBlockPos.getZ()) <= this.listenerRadius) {
                if (gameEvent == GameEvent.BLOCK_PLACE || gameEvent == GameEvent.BLOCK_DESTROY) {
                    if (context.sourceEntity() instanceof Player player && !player.getAbilities().instabuild && applyDamage(player, BASE_DAMAGE, 1.0F)) {
                        spawnRedstoneBeamParticle(serverLevel, player);
                        serverLevel.playSound((Entity) null, centerBlockPos, SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 3.0F, 1.0F);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER));
                        outlineCauseBlock(serverLevel, eventBlockPos);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
