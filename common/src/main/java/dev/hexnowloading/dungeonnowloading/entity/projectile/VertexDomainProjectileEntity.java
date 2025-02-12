package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.util.ModelledProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.AxisParticleType;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;

public class VertexDomainProjectileEntity extends ModelledProjectileEntity {

    private static final EntityDataAccessor<Integer> HURT_TIME = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HURT_TIME_DIRECT = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(VertexDomainProjectileEntity.class, EntityDataSerializers.FLOAT);

    private final DynamicGameEventListener<BlockPlaceBreakListener> dynamicBlockPlaceBreakListener;

    private static final int BASE_DAMAGE = 20;
    private static final int SLOWNESS_AMPLIFIER = 4;
    private static final int SLOWNESS_DURATION = 100;
    private static final int DURATION_ON_GROUND = 1200;
    private static final float HEALTH = 100F;
    private static final int RANGE = 7;

    private static final double BEAM_INITIAL_PARTICLE_SPACING = 0.5d;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MIN = 0.2f;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MAX = 0.4f;

    private double xPower;
    private double yPower;
    private double zPower;
    private int life;

    public VertexDomainProjectileEntity(EntityType<? extends VertexDomainProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.dynamicBlockPlaceBreakListener = new DynamicGameEventListener<>(new BlockPlaceBreakListener(new EntityPositionSource(this, 1.0F), RANGE));
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("power", this.newDoubleList(this.xPower, this.yPower, this.zPower));
        compoundTag.putInt("life", this.life);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HURT_TIME, 0);
        this.entityData.define(HURT_TIME_DIRECT, 1);
        this.entityData.define(DAMAGE, 0.0f);
    }

    @Override
    protected void tickProjectile() {
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        this.checkInsideBlocks();

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.tickCount == 1) {
            this.setDeltaMovement(xPower, yPower, zPower);
        }

        if (this.verticalCollision || this.horizontalCollision) {
            this.life = DURATION_ON_GROUND;
            this.setDeltaMovement(Vec3.ZERO);
        }

        if (this.life > 0) {
            this.life--;
            spawnBoundaryParticles(RANGE);
            if (this.life <= 0) {
                this.discard();
            }
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

            level.sendParticles(particleData, particlePos.x, particlePos.y, particlePos.z, 1,
                    0.0F, 0.0F, 0.0F, 0.0F);
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
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
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
        bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).getAbilities().instabuild;
        if (bl || this.getDamage() > HEALTH) {
            this.discard();
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
    public void animateHurt(float f) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() * 11.0f);
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
            BlockPos eventBlockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));

            Vec3 centerPos = this.getListenerSource().getPosition(serverLevel).orElseThrow();
            BlockPos centerBlockPos = new BlockPos((int) Math.floor(centerPos.x), (int) Math.floor(centerPos.y) - 1, (int) Math.floor(centerPos.z));
            if (Math.abs(eventBlockPos.getX() - centerBlockPos.getX()) <= this.listenerRadius && Math.abs(eventBlockPos.getY() - centerBlockPos.getY()) <= this.listenerRadius && Math.abs(eventBlockPos.getZ() - centerBlockPos.getZ()) <= this.listenerRadius) {
                if (gameEvent == GameEvent.BLOCK_PLACE || gameEvent == GameEvent.BLOCK_DESTROY) {
                    if (context.sourceEntity() instanceof Player player && !player.getAbilities().instabuild && applyDamage(player, BASE_DAMAGE, 1.0F)) {
                        spawnRedstoneBeamParticle(serverLevel, player);
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
