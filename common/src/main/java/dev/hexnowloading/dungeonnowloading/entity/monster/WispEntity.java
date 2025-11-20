package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.WispAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.WispFlyingMoveControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
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

    @Nullable
    protected UUID ownerUUID;
    @Nullable
    protected Entity cachedOwner;
    protected boolean leftOwner;
    protected boolean hasBeenShot;

    int impactTick;
    boolean delayImpact = false;
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

            if (delayImpact) {
                if (impactTick > 0) {
                    impactTick--;
                } else {
                    discardWithBurst();
                    return;
                }
            }

            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onHit(hitResult);
            }

            super.tick();

            this.tickProjectile();

        } else {
            discard();
        }
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
        if (!entity.canBeHitByProjectile()) {
            return false;
        } else {
            Entity ownerEntity = this.getOwner();
            return (ownerEntity == null || this.leftOwner || !ownerEntity.isPassengerOfSameVehicle(entity)) && !entity.noPhysics;
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) return;

        if (delayImpact) return;

        delayImpact = true;
        impactTick = 1;
        hitEntity = entityHitResult.getEntity();
        //discardWithBurst();
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (this.level().isClientSide) return;

        if (delayImpact) return;

        delayImpact = true;
        //discardWithBurst();
    }

    protected void tickProjectile() {
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
}
