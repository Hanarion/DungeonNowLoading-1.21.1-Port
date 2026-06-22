package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.Boss;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class FairkeeperBorosPartEntity extends Monster implements Boss, Enemy, SlumberingEntity, FairkeeperSerpentEntity {

    private static final EntityDataAccessor<Optional<UUID>> PARENT_UUID = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> HEAD_UUID = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> BODY_INDEX = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ARMOR = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TAIL = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAD_MOVING = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MODEL_VISIBLE = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROTATABLE = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.BOOLEAN);

    private float previousTilt = 0.0F;
    private boolean enableRotation = true;

    public FairkeeperBorosPartEntity(EntityType<? extends Monster> entityType, LivingEntity parent, LivingEntity head, int bodyIndex) {
        super(entityType, parent.level());
        this.setParent(parent);
        this.setHead(head);
        this.setBodyIndex(bodyIndex);
        this.setArmor(isArmoredSegment());
        this.setModelVisible(false);
        this.setPersistenceRequired();
    }

    public FairkeeperBorosPartEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PARENT_UUID, Optional.empty());
        builder.define(HEAD_UUID, Optional.empty());
        builder.define(CHILD_UUID, Optional.empty());
        builder.define(BODY_INDEX, 0);
        builder.define(TAIL, false);
        builder.define(ARMOR, false);
        builder.define(HEAD_MOVING, false);
        builder.define(MODEL_VISIBLE, true);
        builder.define(ROTATABLE, true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.getParentId() != null) {
            compoundTag.putUUID("ParentUUID", this.getParentId());
        }
        if (this.getChildId() != null) {
            compoundTag.putUUID("ChildUUID", this.getChildId());
        }
        if (this.getHeadId() != null) {
            compoundTag.putUUID("HeadUUID", this.getHeadId());
        }
        compoundTag.putBoolean("TailPart", isTail());
        compoundTag.putInt("BodyIndex", getBodyIndex());
        compoundTag.putBoolean("Armor", hasArmor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.hasUUID("ParentUUID")) {
            this.setParentId(compoundTag.getUUID("ParentUUID"));
        }
        if (compoundTag.hasUUID("ChildUUID")) {
            this.setChildId(compoundTag.getUUID("ChildUUID"));
        }
        if (compoundTag.hasUUID("HeadUUID")) {
            this.setHeadId(compoundTag.getUUID("HeadUUID"));
        }
        this.setTail(compoundTag.getBoolean("TailPart"));
        this.setBodyIndex(compoundTag.getInt("BodyIndex"));
        this.setArmor(compoundTag.getBoolean("Armor"));
    }

    @Override
    public void tick() {
        Entity parent = getParent();
        if (parent != null && !this.level().isClientSide) {
            this.setNoGravity(true);
            if (this.getHead() instanceof FairkeeperBorosEntity headEntity) {

                int historyIndex = (this.getBodyIndex() + 1) * FairkeeperBorosEntity.SEGMENT_DELAY_STEP;
                synchronized (headEntity.getPositionHistory()) {
                    if (headEntity.getPositionHistory().size() > historyIndex) {
                        this.setModelVisible(true);

                        Vec3 targetPos = headEntity.getSegmentTargetPosition(this.getBodyIndex() + 1);

                        // Move towards the target position
                        this.setPos(
                                lerp(this.getX(), targetPos.x, 0.5),
                                lerp(this.getY(), targetPos.y, 0.5),
                                lerp(this.getZ(), targetPos.z, 0.5)
                        );

                        // Align rotation with the head's historical rotation
                        //Vec3 nextPos = headEntity.getPositionHistory().stream().skip(historyIndex - 1).findFirst().orElse(targetPos);
                        /*if (headEntity.isState(FairkeeperBorosEntity.FairkeeperBorosState.AWAKENING)) {
                            this.setRotatable(false);
                        }*/
                        if (!this.isRotatable()) {
                            Vec3 awakenEndPos = headEntity.getAwakenEndPos();
                            if (awakenEndPos == null) {
                                this.setRotatable(true);
                            } else {
                                double dy = awakenEndPos.y - this.getY();

                                if (dy * dy < 5.0F * 5.0F) {
                                    this.setRotatable(true);
                                }
                            }
                        }
                        //Vec3 nextPos = headEntity.getSegmentTargetPosition(this.getBodyIndex());
                        if (this.isRotatable() && this.isHeadEntityMoving()) {
                            alignRotation(this.position(), targetPos);
                        }
                    }
                }

                this.setHeadEntityMoving(headEntity.getDeltaMovement().lengthSqr() > 0.01);

            }

        } else if (!this.level().isClientSide) {
            remove(RemovalReason.DISCARDED);
        }
        super.tick();
    }

    private double lerp(double start, double end, double factor) {
        return start + (end - start) * factor;
    }

    private void alignRotation(Vec3 currentPos, Vec3 targetPos) {
        double deltaX = targetPos.x - currentPos.x;
        double deltaZ = targetPos.z - currentPos.z;
        float yaw = (float) (Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;
        this.setYRot(yaw);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isTail()) {
            //this.setPathOnFire();
        }

        if (this.isHeadEntityMoving()) {
            this.performContactDamage();
        }

        super.customServerAiStep();
    }

    private void vertexTransmissionEffectImmunity() {
        this.removeEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
    }

    private void setPathOnFire() {
        BlockPos blockPos = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 1), Mth.floor(this.getZ()));
        if (this.level().getBlockState(blockPos.above()).is(Blocks.AIR)) {
            this.level().setBlock(blockPos.above(), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void performContactDamage() {
        this.level().getEntities(this, this.getBoundingBox(), this::canPerformContactDamageTo)
                .forEach(entity -> {
                    entity.push(this);
                    LivingEntity head = (LivingEntity) this.getHead();
                    if (head != null) {
                        entity.hurt(entity.level().damageSources().mobAttack(head), (float) (head.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F));
                    }
                });
    }

    private boolean canPerformContactDamageTo(Entity entity) {
        if (entity instanceof FairkeeperBorosEntity head) {
            return !this.getHeadId().equals(head.getUUID());
        }
        if (entity instanceof FairkeeperBorosPartEntity part) {
            return !this.getHeadId().equals(part.getHeadId());
        }
        return !(entity instanceof VertexOrbProjectileEntity) && !(entity instanceof VertexDomainProjectileEntity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damageAmount) {

        if (damageSource.getEntity() instanceof FairkeeperSerpentEntity) {
            if (damageSource.getDirectEntity() instanceof AbstractArrow arrow) {
                arrow.remove(RemovalReason.DISCARDED);
            }
            return false;
        }

        if (!this.hasArmor() || damageSource.isCreativePlayer() || damageSource.is(DNLTags.FAIRKEEPER_BOROS_BYPASS_ARMOR)) {

            float damage = damageAmount;

            if (this.isTail()) {
                damage = damageAmount * 2;
            }

            FairkeeperBorosEntity head = (FairkeeperBorosEntity) this.getHead();
            if (head != null) {
                head.setDamageFromOtherSegment(true);
                head.hurt(damageSource, damage);
            }
            return super.hurt(damageSource, 0);
        }

        if (damageSource.is(DNLTags.FAIRKEEPER_BOROS_ARMOR_HURTABLE) || (damageSource.getDirectEntity() instanceof LivingEntity livingEntity && livingEntity.getMainHandItem().getItem() instanceof PickaxeItem)) {
            boolean doesKill = this.getHealth() - damageAmount <= 0;
            float nonKillableDamage = doesKill ? 0 : damageAmount;
            if (doesKill) {
                this.setArmor(false);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
                return super.hurt(damageSource, 0);
            } else {
                //this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);
                this.level().playSound(null, this.blockPosition(), DNLSounds.FAIRKEEPER_BOROS_ARMOR_BREAK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                return super.hurt(damageSource, nonKillableDamage);
            }
        }

        if (damageSource.is(DamageTypes.IN_FIRE) || damageSource.is(DamageTypes.ON_FIRE)) return false;

        //this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 1.0F);
        this.level().playSound(null, this.blockPosition(), DNLSounds.FAIRKEEPER_BOROS_ARMOR_HIT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

        return false;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        MobEffect effect = mobEffectInstance.getEffect();
        if (effect == MobEffects.POISON || effect == DNLMobEffects.VERTEX_TRANSMISSION.get()) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
    }

    @Override
    protected int calculateFallDamage(float $$0, float $$1) {
        return 0;
    }

    @Override
    public boolean causeFallDamage(float v, float v1, DamageSource damageSource) {
        return false;
    }

    @Override
    public void resetBoss() {

    }

    @Override
    public boolean resetCondition() {
        return false;
    }

    @Override
    public BlockPos resetRegionCenter() {
        return null;
    }

    @Override
    public boolean isSlumbering() {
        return false;
    }

    @Override
    public boolean isStationary() {
        return false;
    }

    public Entity getHead() {
        UUID id = getHeadId();
        if (id != null && !this.level().isClientSide) {
            return ((ServerLevel) this.level()).getEntity(id);
        }
        return null;
    }

    public void setHead(Entity entity) {
        this.setHeadId(entity.getUUID());
    }

    public UUID getHeadId() { return this.entityData.get(HEAD_UUID).orElse(null); }

    public void setHeadId(@Nullable UUID uniqueId) {
        this.entityData.set(HEAD_UUID, Optional.ofNullable(uniqueId));
    }

    public int getBodyIndex() {
        return this.entityData.get(BODY_INDEX);
    }

    public void setBodyIndex(int index) { this.entityData.set(BODY_INDEX, index); }

    @Nullable
    public UUID getParentId() { return this.entityData.get(PARENT_UUID).orElse(null); }

    public void setParentId(@Nullable UUID uniqueId) {
        this.entityData.set(PARENT_UUID, Optional.ofNullable(uniqueId));
    }

    public void setParent(Entity entity) {
        this.setParentId(entity.getUUID());
    }

    public Entity getParent() {
        UUID id = getParentId();
        if (id != null && !this.level().isClientSide) {
            return ((ServerLevel) this.level()).getEntity(id);
        }
        return null;
    }

    @Nullable
    public UUID getChildId() { return this.entityData.get(CHILD_UUID).orElse(null); }

    public void setChildId(@Nullable UUID uniqueId) {
        this.entityData.set(CHILD_UUID, Optional.ofNullable(uniqueId));
    }

    public void setChild(Entity entity) {
        this.setChildId(entity.getUUID());
    }

    public Entity getChild() {
        UUID id = getChildId();
        if (id != null && !this.level().isClientSide) {
            return ((ServerLevel) this.level()).getEntity(id);
        }
        return null;
    }

    public float getPreviousTilt() { return this.previousTilt; }

    public void setPreviousTilt(float tilt) { this.previousTilt = tilt; }

    public boolean isHeadEntityMoving() {
        return this.entityData.get(HEAD_MOVING);
    }

    public void setHeadEntityMoving(boolean moving) {
        this.entityData.set(HEAD_MOVING, moving);
    }

    public boolean isModelVisible() {
        return this.entityData.get(MODEL_VISIBLE);
    }

    public void setModelVisible(boolean moving) {
        this.entityData.set(MODEL_VISIBLE, moving);
    }

    public boolean isArmoredSegment() { return true; }

    public boolean hasArmor() { return this.entityData.get(ARMOR); }

    public void setArmor(boolean armor) { this.entityData.set(ARMOR, armor); }

    public boolean isTail() { return this.entityData.get(TAIL); }

    public void setTail(boolean tail) { this.entityData.set(TAIL, tail); }

    public void setRotatable(boolean enableRotation) { this.entityData.set(ROTATABLE, enableRotation); }

    public boolean isRotatable() { return this.entityData.get(ROTATABLE); }
}
