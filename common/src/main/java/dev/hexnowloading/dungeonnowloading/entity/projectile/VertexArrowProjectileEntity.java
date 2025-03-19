package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.components.VertexNode;
import dev.hexnowloading.dungeonnowloading.potion.VertexTransmissionEffect;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.util.DNLMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class VertexArrowProjectileEntity extends AbstractArrow {
    private int powerLevel = 0;
    private VertexNode vertexNode = new VertexNode(this);
    private int powerIncrementTimer = 0;
    private int life;

    private static final EntityDataAccessor<Integer> POWER_LEVEL = SynchedEntityData.defineId(VertexArrowProjectileEntity.class, EntityDataSerializers.INT);
    private static final int ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS = 120 ;
    private static final int ADVANCE_POWER_LEVEL_THRESHOLD_TICKS = 8;
    private static final int MAX_POWER_LEVEL = 3;
    private static final int DESPAWN_TIME_TICKS = 400;

    public VertexArrowProjectileEntity(EntityType entityType, Level level) {
        super(entityType, level);
    }

    public VertexArrowProjectileEntity(Level level, LivingEntity shooter) {
        super(DNLEntityTypes.VERTEX_ARROW_PROJECTILE.get(), shooter, level);
    }

    public int getPowerLevel() { return this.entityData.get(POWER_LEVEL); }

    public boolean isFullyPowered() { return this.powerLevel == MAX_POWER_LEVEL; }

    public VertexNode getVertexNode() {
        return this.vertexNode;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(POWER_LEVEL, 0);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("powerLevel", this.entityData.get(POWER_LEVEL));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(POWER_LEVEL, compoundTag.getInt("powerLevel"));
    }

    @Override
    protected void tickDespawn() {
        this.life++;
        if (this.life >= DESPAWN_TIME_TICKS) {
            this.vertexNode.disconnect_all();
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);

        if (entityHitResult.getEntity() instanceof LivingEntity entity) {
            int slownessDurationTicks = ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS;
            int slownessAmplifier = this.vertexNode.getConnectionCount();
            int vertexTransDurationTicks = ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS;
            int vertexTransAmplifier = 0;

            // Slowness application
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDurationTicks, slownessAmplifier));

            // Vertex Transmission application
            boolean entityHasEffect = entity.hasEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
            if (!entityHasEffect) {
                entity.addEffect(new MobEffectInstance(DNLMobEffects.VERTEX_TRANSMISSION.get(), vertexTransDurationTicks, vertexTransAmplifier));
            } else {
                entity.addEffect(new MobEffectInstance(DNLMobEffects.VERTEX_TRANSMISSION.get(), vertexTransDurationTicks, vertexTransAmplifier));
                VertexTransmissionEffect vertexTransmissionEffect = (VertexTransmissionEffect) entity.getEffect(DNLMobEffects.VERTEX_TRANSMISSION.get()).getEffect();
                vertexTransmissionEffect.markAsReconnectionCase(entity.getUUID());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.inGround) {
            this.vertexNode.disconnect_all();
            this.powerIncrementTimer = 0;
            this.powerLevel = 0;
            this.entityData.set(POWER_LEVEL, 0);
        } else {
            if (this.powerLevel == 0 && this.powerIncrementTimer == 0) {
                this.level().playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        DNLSounds.VERTEX_ARROW_BOOTUP.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.2F / (DNLMath.randomRange(0.0f, 1.0f) * 0.2F + 0.9F)
                );
            }

            if (this.powerLevel < MAX_POWER_LEVEL) {
                this.powerIncrementTimer++;
                if (this.powerIncrementTimer >= ADVANCE_POWER_LEVEL_THRESHOLD_TICKS) {
                    this.powerLevel++;
                    this.entityData.set(POWER_LEVEL, this.powerLevel);
                    this.powerIncrementTimer = 0;
                }
            } else if (!this.level().isClientSide && !this.vertexNode.attemptedConnection() && this.life != DESPAWN_TIME_TICKS) {
                this.vertexNode.connectToNearbyNodes(this);
            }
        }

        vertexNode.tick(this);
    }
}
