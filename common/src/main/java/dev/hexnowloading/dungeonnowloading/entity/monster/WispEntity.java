package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.WispAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.WispFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.WispAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WispEntity extends FlyingMob implements Enemy, TraceableEntity {

    private static final EntityDataAccessor<WispAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(WispEntity.class, EntityStates.WISP_ANIMATION_STATE);

    private AnimationChainer<WispAnimationState> animationChainer = new AnimationChainer<>();

    public final AnimationState flareUpAnimationState = new AnimationState();
    public final AnimationState tackleStartAnimationState = new AnimationState();
    public final AnimationState tackleAnimationState = new AnimationState();

    @Nullable
    protected UUID ownerUUID;
    @Nullable
    protected Entity cachedOwner;
    protected boolean leftOwner;
    protected boolean hasBeenShot;

    Entity hitEntity = null;

    public WispEntity(EntityType<? extends WispEntity> type, Level level) {
        super(type, level);
        this.moveControl = new WispFlyingMoveControl(this);
        //this.moveControl = new RampFlyingMoveControl(this, 90, true);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.75D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WispAttackGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }

        if (this.leftOwner) {
            compoundTag.putBoolean("LeftOwner", true);
        }

        compoundTag.putBoolean("HasBeenShot", this.hasBeenShot);
        super.readAdditionalSaveData(compoundTag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
            this.cachedOwner = null;
        }

        this.leftOwner = compoundTag.getBoolean("LeftOwner");
        this.hasBeenShot = compoundTag.getBoolean("HasBeenShot");
        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_STATE, WispAnimationState.IDLE);
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }
        Entity owner = this.getOwner();
        if (this.level().isClientSide || (owner == null || !owner.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {

            super.tick();

            this.tickProjectile();

        } else {
            discard();
        }


    }

    public void playFlareUpAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(WispAnimationState.FLARE_UP, WispAnimationDuration.FLARE_UP));
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(WispAnimationState.TACKLE_START, WispAnimationDuration.TACKLE_START));
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.looping(WispAnimationState.TACKLE, 0));
    }

    public void playTackleAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(WispAnimationState.TACKLE_START, WispAnimationDuration.TACKLE_START));
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.looping(WispAnimationState.TACKLE, 0));
    }

    @Nullable
    @Override
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel) this.level()).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }
    }

    protected boolean checkLeftOwner() {
        Entity entity = this.getOwner();
        if (entity != null) {
            for (Entity entity1 : this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (owner) -> {
                return !owner.isSpectator() && owner.isPickable();
            })) {
                if (entity1.getRootVehicle() == entity.getRootVehicle()) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type hitresult$type = hitResult.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)hitResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, (BlockState)null));
        } else if (hitresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult)hitResult;
            this.onHitBlock(blockhitresult);
            BlockPos blockpos = blockhitresult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
        }
    }

    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof WispLanternEntity) {
            return false;
        }
        if (!entity.canBeHitByProjectile()) {
            return false;
        } else {
            Entity ownerEntity = this.getOwner();
            return (ownerEntity == null || this.leftOwner || !ownerEntity.isPassengerOfSameVehicle(entity)) && !entity.noPhysics;
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) return;

        this.hitEntity = entityHitResult.getEntity();
        discardWithBurst();
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (this.level().isClientSide) return;

        this.hitEntity = null;
        discardWithBurst();
    }

    protected void tickProjectile() {
        if (this.level().isClientSide) {
            return;
        }

        this.animationChainer.tick(this::transitionTo);

        double spread = 0.5D;

        double cx = this.getX() + (this.random.nextDouble() - 0.5D) * spread;
        double cy = this.getY() + this.getBbHeight() * 0.5D + (this.random.nextDouble() - 0.5D) * spread;
        double cz = this.getZ() + (this.random.nextDouble() - 0.5D) * spread;

        // Velocity of the particles (0 = just hover)
        double vx = 0.0D;
        double vy = 0.0D;
        double vz = 0.0D;

        this.level().addParticle(ParticleTypes.FLAME, cx, cy, cz, vx, vy, vz);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 *= 64.0D;
        return distance < d0 * d0;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket recreateFromPacket) {
        super.recreateFromPacket(recreateFromPacket);
    }

    private float getContactDamage() {
        return (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    private boolean isSolidForWisp(net.minecraft.world.level.block.state.BlockState state) {
        return state.canOcclude() || state.isSolid();
    }

    public void discardWithBurst() {
        if (this.level().isClientSide) return;
        if (hitEntity != null) {
            boolean hasFireRes = false;
            if (hitEntity instanceof LivingEntity living) {
                if (living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    hasFireRes = true;

                    var effect = living.getEffect(MobEffects.FIRE_RESISTANCE);
                    int newDuration = Math.max(0, effect.getDuration() - 1200);
                    living.removeEffect(MobEffects.FIRE_RESISTANCE);
                    if (newDuration > 0) {
                        living.addEffect(new MobEffectInstance(
                                MobEffects.FIRE_RESISTANCE,
                                newDuration,
                                effect.getAmplifier(),
                                effect.isAmbient(),
                                effect.isVisible(),
                                effect.showIcon()
                        ));
                    }
                }
            }

            if (!hasFireRes) {
                hitEntity.push(this);
                hitEntity.hurt(hitEntity.level().damageSources().mobAttack(this), this.getContactDamage());
            }
        }

        ServerLevel server = (ServerLevel) this.level();
        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5;
        double cz = this.getZ();

        server.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 1, 0.0, 0.0, 0.0, 0.0);
        server.sendParticles(ParticleTypes.FLAME, cx, cy, cz, 24, 0.35, 0.35, 0.35, 0.08);
        server.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 12, 0.30, 0.30, 0.30, 0.03);
        this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.7f, 1.6f);
        this.gameEvent(GameEvent.ENTITY_DIE);

        // 🔥 Place fire on surfaces in a 3x3x3 around impact
        BlockPos center = this.blockPosition();
        RandomSource rand = server.getRandom();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {

                    BlockPos firePos = center.offset(dx, dy, dz);
                    BlockPos below = firePos.below();

                    // Guarantee fire only at the center
                    boolean isCenter = (dx == 0 && dy == 0 && dz == 0);

                    // 50% chance for all others
                    if (!isCenter && rand.nextFloat() > 0.5f) {
                        continue;
                    }

                    // Must be air above valid solid top face
                    if (server.isEmptyBlock(firePos)) {
                        BlockState belowState = server.getBlockState(below);
                        if (belowState.isFaceSturdy(server, below, Direction.UP)) {
                            server.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
                        }
                    }
                }
            }
        }

        this.discard();
    }


    @Override
    public void die(DamageSource damageSource) {
        if (!this.isRemoved() && !this.dead) {
            this.discardWithBurst();
        }
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {}

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }


    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            WispAnimationState animationState = this.getAnimationState();
            this.resetAnimation();
            switch (animationState) {
                case FLARE_UP -> this.flareUpAnimationState.startIfStopped(this.tickCount);
                case TACKLE_START -> this.tackleStartAnimationState.startIfStopped(this.tickCount);
                case TACKLE -> this.tackleAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);

    }

    public WispEntity transitionTo(WispAnimationState animationState) {
        switch (animationState) {
            case IDLE:
                this.entityData.set(ANIMATION_STATE, WispAnimationState.IDLE);
                break;
            case FLARE_UP:
                this.entityData.set(ANIMATION_STATE, WispAnimationState.FLARE_UP);
                break;
            case TACKLE_START:
                this.entityData.set(ANIMATION_STATE, WispAnimationState.TACKLE_START);
                break;
            case TACKLE:
                this.entityData.set(ANIMATION_STATE, WispAnimationState.TACKLE);
                break;
        }
        return this;
    }

    private void resetAnimation() {
        this.flareUpAnimationState.stop();
        this.tackleStartAnimationState.stop();
        this.tackleAnimationState.stop();
    }

    public WispAnimationState getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public enum WispAnimationState {
        IDLE,
        FLARE_UP,
        TACKLE_START,
        TACKLE
    }
}
