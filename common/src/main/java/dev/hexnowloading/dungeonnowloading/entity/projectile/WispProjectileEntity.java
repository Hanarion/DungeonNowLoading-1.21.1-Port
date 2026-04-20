package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WispProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> HOMING_TARGET_ID = SynchedEntityData.defineId(WispProjectileEntity.class, EntityDataSerializers.INT);

    public static final float GRAVITY = 0.0F;
    private static final int MAX_LIFE = 200;
    private static final double MIN_HOMING_STRENGTH = 0.035D;
    private static final double MAX_HOMING_STRENGTH = 0.2D;
    private static final double MIN_HOMING_DISTANCE = 2.0D;
    private static final double MAX_HOMING_DISTANCE = 6.0D;

    private float damage = 10.0F;
    private Entity hitEntity;
    private LivingEntity homingTarget;
    private double preservedSpeed;

    public WispProjectileEntity(EntityType<? extends WispProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public WispProjectileEntity(Level level, LivingEntity owner) {
        super(DNLEntityTypes.WISP_PROJECTILE.get(), owner, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HOMING_TARGET_ID, 0);
    }

    @Override
    public void tick() {
        this.updateHoming();
        super.tick();
        this.preserveSpeed();

        if (this.level().isClientSide) {
            return;
        }

        if (this.getHomingTarget() == null) {
            this.hitEntity = null;
            this.discardWithBurst();
            return;
        }

        this.spawnTrailParticles();

        if (this.tickCount > MAX_LIFE) {
            this.hitEntity = null;
            this.discardWithBurst();
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        this.setPreservedSpeedFromMovement();
        this.seedRotationFromMovement();
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        super.lerpMotion(x, y, z);
        this.setPreservedSpeedFromMovement();
        this.seedRotationFromMovement();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.setXRot(packet.getXRot());
        this.setYRot(packet.getYRot());
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.setPreservedSpeedFromMovement();
        this.seedRotationFromMovement();
    }

    private void seedRotationFromMovement() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() <= 1.0E-7D) {
            return;
        }

        double horizontalDistance = motion.horizontalDistance();
        this.setXRot((float)(Mth.atan2(motion.y, horizontalDistance) * (double)(180F / (float)Math.PI)));
        this.setYRot((float)(Mth.atan2(motion.x, motion.z) * (double)(180F / (float)Math.PI)));
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level().isClientSide) {
            return;
        }

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.hitEntity = ((EntityHitResult) hitResult).getEntity();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, (BlockState) null));
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            this.hitEntity = null;
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
        }

        this.discardWithBurst();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof WispProjectileEntity || entity instanceof WispLanternEntity || entity instanceof WispEntity) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    protected float getGravity() {
        return GRAVITY;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /*@Override
    public boolean displayFireAnimation() {
        return !this.isRemoved();
    }*/

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }

        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        }

        this.hitEntity = null;
        this.discardWithBurst();
        return true;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setHomingTarget(LivingEntity homingTarget) {
        this.homingTarget = homingTarget;
        this.entityData.set(HOMING_TARGET_ID, homingTarget == null ? 0 : homingTarget.getId());
    }

    private void updateHoming() {
        LivingEntity target = this.getHomingTarget();
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        if (speed <= 1.0E-7D) {
            return;
        }

        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 desired = targetPos.subtract(this.position());
        if (desired.lengthSqr() <= 1.0E-7D) {
            return;
        }

        double homingStrength = this.getHomingStrength(desired.length());
        Vec3 steered = motion.normalize().lerp(desired.normalize(), homingStrength).normalize().scale(speed);
        this.setDeltaMovement(steered);
    }

    private double getHomingStrength(double distance) {
        double progress = Mth.clamp((distance - MIN_HOMING_DISTANCE) / (MAX_HOMING_DISTANCE - MIN_HOMING_DISTANCE), 0.0D, 1.0D);
        return Mth.lerp(progress, MIN_HOMING_STRENGTH, MAX_HOMING_STRENGTH);
    }

    private void setPreservedSpeedFromMovement() {
        double speed = this.getDeltaMovement().length();
        if (speed > 1.0E-7D) {
            this.preservedSpeed = speed;
        }
    }

    private void preserveSpeed() {
        if (this.preservedSpeed <= 1.0E-7D) {
            this.setPreservedSpeedFromMovement();
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() <= 1.0E-7D) {
            return;
        }

        this.setDeltaMovement(motion.normalize().scale(this.preservedSpeed));
    }

    private LivingEntity getHomingTarget() {
        if (this.homingTarget != null && this.homingTarget.isAlive() && !this.homingTarget.isRemoved()) {
            return this.homingTarget;
        }

        int targetId = this.entityData.get(HOMING_TARGET_ID);
        Entity target = targetId == 0 ? null : this.level().getEntity(targetId);
        if (target instanceof LivingEntity living) {
            this.homingTarget = living;
            return living;
        }

        return null;
    }

    private void spawnTrailParticles() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        Vec3 trailDirection = motion.lengthSqr() > 1.0E-7D ? motion.normalize() : Vec3.ZERO;
        double centerY = this.getY() + this.getBbHeight() * 0.5D;

        for (int i = 0; i < 3; i++) {
            double distanceBack = 0.12D + i * 0.16D;
            double x = this.getX() - trailDirection.x * distanceBack + (this.random.nextDouble() - 0.5D) * 0.3D;
            double y = centerY - trailDirection.y * distanceBack + (this.random.nextDouble() - 0.5D) * 0.3D;
            double z = this.getZ() - trailDirection.z * distanceBack + (this.random.nextDouble() - 0.5D) * 0.3D;

            server.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.01D);
        }

        if (this.tickCount % 2 == 0) {
            double x = this.getX() - trailDirection.x * 0.35D;
            double y = centerY - trailDirection.y * 0.35D;
            double z = this.getZ() - trailDirection.z * 0.35D;
            server.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.03D, 0.03D, 0.03D, 0.005D);
        }
    }

    private void discardWithBurst() {
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }

        if (this.hitEntity != null) {
            boolean hasFireRes = false;
            if (this.hitEntity instanceof LivingEntity living) {
                if (living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    hasFireRes = true;

                    MobEffectInstance effect = living.getEffect(MobEffects.FIRE_RESISTANCE);
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
                Entity owner = this.getOwner();
                this.hitEntity.push(this);
                if (owner instanceof LivingEntity livingOwner) {
                    this.hitEntity.hurt(this.damageSources().mobProjectile(this, livingOwner), this.damage);
                } else {
                    this.hitEntity.hurt(this.damageSources().generic(), this.damage);
                }
            }
        }

        ServerLevel server = (ServerLevel) this.level();
        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5D;
        double cz = this.getZ();

        server.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLAME, cx, cy, cz, 24, 0.35D, 0.35D, 0.35D, 0.08D);
        server.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 12, 0.30D, 0.30D, 0.30D, 0.03D);
        this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.7F, 1.6F);
        this.gameEvent(GameEvent.ENTITY_DIE);

        BlockPos center = this.blockPosition();
        RandomSource rand = server.getRandom();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos firePos = center.offset(dx, dy, dz);
                    BlockPos below = firePos.below();

                    boolean isCenter = dx == 0 && dy == 0 && dz == 0;
                    if (!isCenter && rand.nextFloat() > 0.5F) {
                        continue;
                    }

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
}
