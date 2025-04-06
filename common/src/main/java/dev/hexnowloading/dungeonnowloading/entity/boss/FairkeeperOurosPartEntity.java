package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.Boss;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class FairkeeperOurosPartEntity extends Monster implements Boss, Enemy, SlumberingEntity, FairkeeperSerpentEntity {

    private static final EntityDataAccessor<Optional<UUID>> PARENT_UUID = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Optional<UUID>> HEAD_UUID = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> CHILD_UUID = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> BODY_INDEX = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DROPPER = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TAIL = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAD_MOVING = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MODEL_VISIBLE = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROTATABLE = SynchedEntityData.defineId(FairkeeperOurosPartEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState dropScuttleAnimationState = new AnimationState();
    public final AnimationState setupCannonAnimationState = new AnimationState();

    private static final byte TRIGGER_IDLE_ANIMATION_BYTE = 70;
    private static final byte TRIGGER_DROP_SCUTTLE_ANIMATION_BYTE = 71;
    private static final byte TRIGGER_SETUP_CANNON_ANIMATION_BYTE = 72;

    private float previousTilt = 0.0F;

    public FairkeeperOurosPartEntity(EntityType<? extends Monster> entityType, LivingEntity parent, LivingEntity head, int bodyIndex) {
        super(entityType, parent.level());
        this.setParent(parent);
        this.setHead(head);
        this.setBodyIndex(bodyIndex);
        this.setArmor(isArmoredSegment());
        this.setModelVisible(false);
        this.setPersistenceRequired();
    }

    public FairkeeperOurosPartEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PARENT_UUID, Optional.empty());
        this.entityData.define(CHILD_UUID, Optional.empty());
        this.entityData.define(HEAD_UUID, Optional.empty());
        this.entityData.define(BODY_INDEX, 0);
        this.entityData.define(TAIL, false);
        this.entityData.define(DROPPER, false);
        this.entityData.define(HEAD_MOVING, false);
        this.entityData.define(MODEL_VISIBLE, true);
        this.entityData.define(ROTATABLE, true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.getParentId() != null) {
            compoundTag.putUUID("ParentUUID", this.getParentId());
        }
        if (this.getChild() != null) {
            compoundTag.putUUID("ChildUUID", this.getChildId());
        }
        if (this.getHead() != null) {
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
            if (this.getHead() instanceof FairkeeperOurosEntity headEntity) {

                int historyIndex = (this.getBodyIndex() + 1) * FairkeeperOurosEntity.SEGMENT_DELAY_STEP;
                synchronized (headEntity.getPositionHistory()) {
                    if (headEntity.getPositionHistory().size() > historyIndex) {
                        this.setModelVisible(true);

                        Vec3 targetPos = headEntity.getPositionHistory().stream().skip(historyIndex).findFirst().orElse(this.getPosition(1.0F));

                        // Move towards the target position
                        this.setPos(
                                lerp(this.getX(), targetPos.x, 0.5),
                                lerp(this.getY(), targetPos.y, 0.5),
                                lerp(this.getZ(), targetPos.z, 0.5)
                        );

                        // Align rotation with the head's historical rotation
                        Vec3 nextPos = headEntity.getPositionHistory().stream().skip(historyIndex - 1).findFirst().orElse(targetPos);
                        /*if (headEntity.isState(FairkeeperOurosEntity.FairkeeperOurosState.AWAKENING)) {
                            this.enableRotation = false;
                        }*/
                        if (!this.isRotatable()) {
                            Vec3 awakenEndPos = headEntity.getAwakenEndPos();
                            if (awakenEndPos == null) {
                                awakenEndPos = Vec3.ZERO;
                            }
                            double dy = awakenEndPos.y - (this.getY() + this.getBbHeight());

                            if (dy * dy < 5.0F * 5.0F) {
                                this.setRotatable(true);
                            }
                        }
                        if (this.isRotatable()) {
                            alignRotation(targetPos, nextPos);
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

        if (this.isHeadEntityMoving()) {
            this.performContactDamage();
        }

        super.customServerAiStep();
    }

    private void vertexTransmissionEffectImmunity() {
        this.removeEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
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
        if (entity instanceof FairkeeperOurosEntity head) {
            return !this.getHeadId().equals(head.getUUID());
        }
        if (entity instanceof FairkeeperOurosPartEntity part) {
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

        if (!this.hasArmor() || damageSource.isCreativePlayer()) {
            FairkeeperOurosEntity head = (FairkeeperOurosEntity) this.getHead();
            if (head != null) {
                head.hurt(damageSource, damageAmount);
            }
            return super.hurt(damageSource, 0);
        }

        if (damageSource.is(DamageTypes.EXPLOSION) || (damageSource.getDirectEntity() instanceof LivingEntity livingEntity && livingEntity.canDisableShield())) {
            this.setArmor(false);
        }

        return false;
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
    public void handleEntityEvent(byte b) {
        switch (b) {
            case TRIGGER_IDLE_ANIMATION_BYTE:
                this.idleAnimationState.start(this.tickCount);
                break;
            case TRIGGER_DROP_SCUTTLE_ANIMATION_BYTE:
                this.dropScuttleAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SETUP_CANNON_ANIMATION_BYTE:
                this.setupCannonAnimationState.start(this.tickCount);
                break;
        }
        super.handleEntityEvent(b);
    }

    private void stopAllAnimation() {
        this.idleAnimationState.stop();
        this.dropScuttleAnimationState.stop();
        this.setupCannonAnimationState.stop();
    }

    public void triggerIdleAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_IDLE_ANIMATION_BYTE); }
    public void triggerDropScuttleAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_DROP_SCUTTLE_ANIMATION_BYTE); }
    public void triggerSetupCannonAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_SETUP_CANNON_ANIMATION_BYTE); }

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

    public boolean isArmoredSegment() { return false; }

    public boolean hasArmor() { return this.entityData.get(DROPPER); }

    public void setArmor(boolean armor) { this.entityData.set(DROPPER, armor); }

    public boolean isTail() { return this.entityData.get(TAIL); }

    public void setTail(boolean tail) { this.entityData.set(TAIL, tail); }

    public void setRotatable(boolean enableRotation) { this.entityData.set(ROTATABLE, enableRotation); }

    public boolean isRotatable() { return this.entityData.get(ROTATABLE); }

}