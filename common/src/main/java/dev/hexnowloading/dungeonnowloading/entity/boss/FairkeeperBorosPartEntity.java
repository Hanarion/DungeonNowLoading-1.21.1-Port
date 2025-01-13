package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.entity.util.Boss;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class FairkeeperBorosPartEntity extends Monster implements Boss, Enemy, SlumberingEntity {

    private static final EntityDataAccessor<Optional<UUID>> PARENT_UUID = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> HEAD_UUID = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> BODY_INDEX = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ARMOR = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TAIL = SynchedEntityData.defineId(FairkeeperBorosPartEntity.class, EntityDataSerializers.BOOLEAN);

    public FairkeeperBorosPartEntity(EntityType<? extends Monster> entityType, LivingEntity parent, LivingEntity head, int bodyIndex) {
        super(entityType, parent.level());
        this.setParent(parent);
        this.setHead(head);
        this.setBodyIndex(bodyIndex);
        this.setArmor(isArmoredSegment());
    }

    public FairkeeperBorosPartEntity(EntityType<? extends Monster> entityType, Level level) {
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
        this.entityData.define(HEAD_UUID, Optional.empty());
        this.entityData.define(BODY_INDEX, 0);
        this.entityData.define(TAIL, false);
        this.entityData.define(ARMOR, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.getParentId() != null) {
            compoundTag.putUUID("ParentUUID", this.getParentId());
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
        if (compoundTag.hasUUID("HeadUUID")) {
            this.setHeadId(compoundTag.getUUID("HeadUUID"));
        }
        this.setTail(compoundTag.getBoolean("TailPart"));
        this.setBodyIndex(compoundTag.getInt("BodyIndex"));
        this.setArmor(compoundTag.getBoolean("Armor"));
    }

    @Override
    public void tick() {
        if (this.tickCount > 10) {
            Entity parent = getParent();
            if (parent != null && !this.level().isClientSide) {
                this.setNoGravity(true);
                if (this.getHead() instanceof FairkeeperBorosEntity headEntity) {

                        int historyIndex = (this.getBodyIndex() + 1) * FairkeeperBorosEntity.SEGMENT_DELAY_STEP;
                        if (headEntity.getPositionHistory().size() > historyIndex) {
                            Vec3 targetPos = headEntity.getPositionHistory().stream().skip(historyIndex).findFirst().orElse(this.getPosition(1.0F));

                            // Move towards the target position
                            this.setPos(
                                    lerp(this.getX(), targetPos.x, 0.5),
                                    lerp(this.getY(), targetPos.y, 0.5),
                                    lerp(this.getZ(), targetPos.z, 0.5)
                            );

                            // Align rotation with the head's historical rotation
                            Vec3 nextPos = headEntity.getPositionHistory().stream().skip(historyIndex - 1).findFirst().orElse(targetPos);
                            alignRotation(targetPos, nextPos);
                        }

                }

            } else if (tickCount > 20 && !this.level().isClientSide) {
                remove(RemovalReason.DISCARDED);
            }
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
    public boolean hurt(DamageSource damageSource, float damageAmount) {

        if (!this.hasArmor() || damageSource.isCreativePlayer()) {
            return super.hurt(damageSource, damageAmount);
        }

        if (damageSource.is(DamageTypes.EXPLOSION) || (damageSource.getDirectEntity() instanceof LivingEntity livingEntity && livingEntity.canDisableShield())) {
            this.setArmor(false);
        }

        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        entity.push(this);

        if (entity instanceof LivingEntity && !(entity instanceof FairkeeperBorosPartEntity || entity instanceof FairkeeperBorosEntity)) {
            LivingEntity parent = (LivingEntity) this.getParent();
            entity.hurt(entity.level().damageSources().mobAttack(parent), (float) parent.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5f);
        }
        return super.canCollideWith(entity);
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
    public void targetRandomPlayer() {

    }

    @Override
    public boolean playerTargetingCondition() {
        return false;
    }

    @Override
    public void postPlayerTargeting() {

    }

    @Override
    public boolean isSlumbering() {
        return false;
    }

    @Override
    public boolean isStationary() {
        return false;
    }

    private Entity getHead() {
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

    public boolean isArmoredSegment() { return this.getBodyIndex() % 2 != 0; }

    public boolean hasArmor() { return this.entityData.get(ARMOR); }

    public void setArmor(boolean armor) { this.entityData.set(ARMOR, armor); }

    public boolean isTail() { return this.entityData.get(TAIL); }

    public void setTail(boolean tail) { this.entityData.set(TAIL, tail); }

}
