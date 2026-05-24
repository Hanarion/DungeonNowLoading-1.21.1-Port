package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
    private static final int TACKLE_SOUND_INTERVAL = 10;
    private static final float MINECART_WIGGLE_DAMAGE = 0.5F;
    private static final int FURNACE_MINECART_FUEL_TICKS = 200;
    private static final int FURNACE_BLOCK_FUEL_TICKS = 25;

    protected float damage = 10.0F;
    protected Entity hitEntity;
    protected LivingEntity homingTarget;
    protected double preservedSpeed;
    protected BlockPos suppressFireAtBlockPos;
    protected boolean discardWhenTargetMissing = true;

    public WispProjectileEntity(EntityType<? extends WispProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public WispProjectileEntity(Level level, LivingEntity owner) {
        super(DNLEntityTypes.WISP_PROJECTILE.get(), owner, level);
    }

    protected WispProjectileEntity(EntityType<? extends WispProjectileEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, owner, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HOMING_TARGET_ID, 0);
    }

    @Override
    public void tick() {
        this.updateHoming();
        this.tickWithCenteredCollision();
        this.preserveSpeed();

        if (!this.level().isClientSide) {
            this.breakThroughPowderSnowAtCurrentPosition();
        }

        if (!this.level().isClientSide && this.isInWaterOrBubble()) {
            this.discardWithWaterExtinguish();
            return;
        }

        if (this.level().isClientSide) {
            return;
        }

        if (this.getHomingTarget() == null && this.discardWhenTargetMissing) {
            this.hitEntity = null;
            this.discardWithBurst();
            return;
        }

        this.playTackleSound();
        if (this.tickCount > 1) {
            this.spawnTrailParticles();
        }

        if (this.tickCount > MAX_LIFE) {
            this.hitEntity = null;
            this.discardWithBurst();
        }
    }

    protected void tickWithCenteredCollision() {
        double centerOffset = this.getBbHeight() * 0.5D;
        this.setPos(this.getX(), this.getY() + centerOffset, this.getZ());
        super.tick();
        if (!this.isRemoved()) {
            this.setPos(this.getX(), this.getY() - centerOffset, this.getZ());
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

    protected void seedRotationFromMovement() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() <= 1.0E-7D) {
            return;
        }

        double horizontalDistance = motion.horizontalDistance();
        this.setXRot((float)(-(Mth.atan2(motion.y, horizontalDistance) * (double)(180F / (float)Math.PI))));
        this.setYRot((float)(Mth.atan2(motion.z, motion.x) * (double)(180F / (float)Math.PI)) - 90.0F);
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
            this.handleEntityHit((EntityHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            this.handleBlockHit((BlockHitResult) hitResult);
        }

        if (this.shouldDiscardAfterHit(hitResult)) {
            this.discardWithBurst();
        }
    }

    protected boolean shouldDiscardAfterHit(HitResult hitResult) {
        return true;
    }

    protected void handleEntityHit(EntityHitResult hitResult) {
        this.hitEntity = hitResult.getEntity();
        this.suppressFireAtBlockPos = null;
        if (this.hitEntity instanceof AbstractMinecart minecart) {
            this.handleMinecartHit(minecart);
            this.hitEntity = null;
        }
        this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, (BlockState) null));
    }

    protected void handleBlockHit(BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        this.hitEntity = null;
        this.suppressFireAtBlockPos = null;
        if (this.breakThroughPowderSnow(blockPos)) {
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, Blocks.POWDER_SNOW.defaultBlockState()));
            return;
        }
        this.heatBlock(blockPos);
        this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof WispProjectileEntity || entity instanceof WispEntity) {
            return false;
        }

        if (entity instanceof WispLanternEntity && !(this.getOwner() instanceof Player)) {
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

    public void setAttackDamage(float damage) {
        this.setDamage(damage);
    }

    public void setDiscardWhenTargetMissing(boolean discardWhenTargetMissing) {
        this.discardWhenTargetMissing = discardWhenTargetMissing;
    }

    public void setHomingTarget(LivingEntity homingTarget) {
        this.homingTarget = homingTarget;
        this.entityData.set(HOMING_TARGET_ID, homingTarget == null ? 0 : homingTarget.getId());
    }

    protected void updateHoming() {
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

    protected double getHomingStrength(double distance) {
        double progress = Mth.clamp((distance - MIN_HOMING_DISTANCE) / (MAX_HOMING_DISTANCE - MIN_HOMING_DISTANCE), 0.0D, 1.0D);
        return Mth.lerp(progress, MIN_HOMING_STRENGTH, MAX_HOMING_STRENGTH);
    }

    protected void setPreservedSpeedFromMovement() {
        double speed = this.getDeltaMovement().length();
        if (speed > 1.0E-7D) {
            this.preservedSpeed = speed;
        }
    }

    protected void preserveSpeed() {
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

    protected LivingEntity getHomingTarget() {
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

    protected void spawnTrailParticles() {
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

    protected void discardWithBurst() {
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }

        if (this.hitEntity != null) {
            this.applyEntityImpact(this.hitEntity);
        }

        ServerLevel server = (ServerLevel) this.level();
        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5D;
        double cz = this.getZ();

        server.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLAME, cx, cy, cz, 24, 0.35D, 0.35D, 0.35D, 0.08D);
        server.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 12, 0.30D, 0.30D, 0.30D, 0.03D);
        this.level().playSound(null, cx, cy, cz, DNLSounds.WISP_DEATH.get(), SoundSource.HOSTILE, 0.9F, 0.95F + this.random.nextFloat() * 0.1F);
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

                    if (firePos.equals(this.suppressFireAtBlockPos)) {
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

    protected void applyEntityImpact(Entity target) {
        boolean hasFireRes = false;
        if (target instanceof LivingEntity living && living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
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

        if (hasFireRes) {
            return;
        }

        Entity owner = this.getOwner();
        target.push(this);
        if (target instanceof LivingEntity living) {
            living.setSecondsOnFire(4);
        }
        if (owner instanceof LivingEntity livingOwner) {
            target.hurt(this.damageSources().mobProjectile(this, livingOwner), this.damage);
        } else {
            target.hurt(this.damageSources().generic(), this.damage);
        }
    }

    protected void playTackleSound() {
        if (this.tickCount % TACKLE_SOUND_INTERVAL != 0) {
            return;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), DNLSounds.WISP_TACKLE.get(), SoundSource.HOSTILE, 0.65F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    protected void discardWithWaterExtinguish() {
        if (!(this.level() instanceof ServerLevel server) || this.isRemoved()) {
            return;
        }

        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5D;
        double cz = this.getZ();

        server.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 18, 0.25D, 0.25D, 0.25D, 0.03D);
        this.level().playSound(null, cx, cy, cz, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.7F, 1.4F + this.random.nextFloat() * 0.3F);
        this.level().playSound(null, cx, cy, cz, DNLSounds.WISP_DEATH.get(), SoundSource.HOSTILE, 0.75F, 0.95F + this.random.nextFloat() * 0.1F);
        this.discard();
    }

    protected void handleMinecartHit(AbstractMinecart minecart) {
        if (this.level().isClientSide) {
            return;
        }

        DamageSource source = this.getOwner() instanceof LivingEntity livingOwner
                ? this.damageSources().mobProjectile(this, livingOwner)
                : this.damageSources().generic();
        minecart.hurt(source, MINECART_WIGGLE_DAMAGE);

        if (minecart instanceof MinecartTNT tntMinecart) {
            this.explodeTntMinecart(tntMinecart);
        } else if (minecart instanceof MinecartFurnace furnaceMinecart) {
            this.fuelFurnaceMinecart(furnaceMinecart);
        }
    }

    protected void fuelFurnaceMinecart(MinecartFurnace furnaceMinecart) {
        CompoundTag tag = new CompoundTag();
        furnaceMinecart.saveWithoutId(tag);
        int fuel = Math.min(32000, tag.getShort("Fuel") + FURNACE_MINECART_FUEL_TICKS);
        tag.putShort("Fuel", (short) fuel);
        furnaceMinecart.load(tag);

        Vec3 motion = this.getDeltaMovement();
        Vec3 horizontalMotion = new Vec3(motion.x, 0.0D, motion.z);
        if (horizontalMotion.lengthSqr() <= 1.0E-7D) {
            horizontalMotion = new Vec3(furnaceMinecart.getX() - this.getX(), 0.0D, furnaceMinecart.getZ() - this.getZ());
        }

        if (horizontalMotion.lengthSqr() > 1.0E-7D) {
            Vec3 push = horizontalMotion.normalize();
            furnaceMinecart.xPush = push.x;
            furnaceMinecart.zPush = push.z;
        }
    }

    protected void explodeTntMinecart(MinecartTNT tntMinecart) {
        if (!(this.level() instanceof ServerLevel server) || tntMinecart.isRemoved()) {
            return;
        }

        Entity owner = this.getOwner();
        server.explode(
                tntMinecart,
                this.damageSources().explosion(this, owner),
                null,
                tntMinecart.getX(),
                tntMinecart.getY(),
                tntMinecart.getZ(),
                4.0F,
                false,
                Level.ExplosionInteraction.TNT
        );
        tntMinecart.discard();
    }

    protected boolean fuelFurnaceBlock(ServerLevel server, BlockPos blockPos) {
        BlockEntity blockEntity = server.getBlockEntity(blockPos);
        if (!(blockEntity instanceof AbstractFurnaceBlockEntity furnace)) {
            return false;
        }

        CompoundTag tag = furnace.saveWithoutMetadata();
        int burnTime = Math.min(Short.MAX_VALUE, tag.getShort("BurnTime") + FURNACE_BLOCK_FUEL_TICKS);
        tag.putShort("BurnTime", (short) burnTime);
        furnace.load(tag);
        furnace.setChanged();

        BlockState state = server.getBlockState(blockPos);
        if (state.hasProperty(AbstractFurnaceBlock.LIT) && !state.getValue(AbstractFurnaceBlock.LIT)) {
            server.setBlock(blockPos, state.setValue(AbstractFurnaceBlock.LIT, true), 3);
        }

        return true;
    }

    protected boolean tryApplyHeatFlavor(ServerLevel server, BlockPos blockPos, BlockState blockState) {
        if (blockState.is(Blocks.WATER_CAULDRON) && blockState.hasProperty(LayeredCauldronBlock.LEVEL)) {
            LayeredCauldronBlock.lowerFillLevel(blockState, server, blockPos);
            return true;
        }

        if (CampfireBlock.canLight(blockState)) {
            server.setBlock(blockPos, blockState.setValue(CampfireBlock.LIT, true), 11);
            return true;
        }

        if (CandleBlock.canLight(blockState) || CandleCakeBlock.canLight(blockState)) {
            server.setBlock(blockPos, blockState.setValue(BlockStateProperties.LIT, true), 11);
            return true;
        }

        if (blockState.is(Blocks.TNT)) {
            TntBlock.explode(server, blockPos);
            server.removeBlock(blockPos, false);
            return true;
        }

        if (blockState.is(Blocks.CARVED_PUMPKIN)) {
            BlockState jackOLantern = Blocks.JACK_O_LANTERN.defaultBlockState();
            if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && jackOLantern.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                jackOLantern = jackOLantern.setValue(BlockStateProperties.HORIZONTAL_FACING, blockState.getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
            server.setBlockAndUpdate(blockPos, jackOLantern);
            return true;
        }

        if (blockState.is(Blocks.SNOW)) {
            if (blockState.hasProperty(BlockStateProperties.LAYERS) && blockState.getValue(BlockStateProperties.LAYERS) > 1) {
                server.setBlock(blockPos, blockState.setValue(BlockStateProperties.LAYERS, blockState.getValue(BlockStateProperties.LAYERS) - 1), 3);
            } else {
                server.removeBlock(blockPos, false);
            }
            return true;
        }

        if (blockState.is(Blocks.SNOW_BLOCK)) {
            server.removeBlock(blockPos, false);
            return true;
        }

        if (blockState.is(Blocks.ICE) || blockState.is(Blocks.FROSTED_ICE)) {
            server.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
            return true;
        }

        return false;
    }

    protected void breakThroughPowderSnowAtCurrentPosition() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }

        BlockPos basePos = this.blockPosition();
        BlockPos centerPos = BlockPos.containing(this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ());
        this.breakThroughPowderSnow(basePos);
        if (!centerPos.equals(basePos)) {
            this.breakThroughPowderSnow(centerPos);
        }
    }

    protected boolean breakThroughPowderSnow(BlockPos blockPos) {
        if (!(this.level() instanceof ServerLevel server)) {
            return false;
        }

        BlockState blockState = server.getBlockState(blockPos);
        if (!blockState.is(Blocks.POWDER_SNOW)) {
            return false;
        }

        this.setSharedFlagOnFire(false);
        return server.destroyBlock(blockPos, false, this);
    }

    protected void heatBlock(BlockPos blockPos) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }

        BlockState blockState = server.getBlockState(blockPos);
        if (blockState.isAir() || blockState.getDestroySpeed(server, blockPos) < 0.0F) {
            return;
        }

        if (this.fuelFurnaceBlock(server, blockPos)) {
            return;
        }

        if (this.tryApplyHeatFlavor(server, blockPos, blockState)) {
            return;
        }

        if (this.tryTransformSmeltedBlock(server, blockPos, blockState)) {
            return;
        }

        if (this.trySmeltBlockDrops(server, blockPos, blockState)) {
            return;
        }

        if (blockState.ignitedByLava()) {
            server.destroyBlock(blockPos, false, this);
        }
    }

    protected boolean tryTransformSmeltedBlock(ServerLevel server, BlockPos blockPos, BlockState blockState) {
        ItemStack input = new ItemStack(blockState.getBlock());
        if (input.isEmpty()) {
            return false;
        }

        SimpleContainer container = new SimpleContainer(input);
        return server.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, container, server)
                .map(recipe -> this.applyBlockSmeltingTransform(server, blockPos, container, recipe))
                .orElse(false);
    }

    protected boolean applyBlockSmeltingTransform(ServerLevel server, BlockPos blockPos, SimpleContainer container, SmeltingRecipe recipe) {
        ItemStack result = recipe.assemble(container, server.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        if (result.getItem() instanceof BlockItem blockItem) {
            Block resultBlock = blockItem.getBlock();
            if (resultBlock != Blocks.AIR) {
                server.setBlockAndUpdate(blockPos, resultBlock.defaultBlockState());
                return true;
            }
        }

        return false;
    }

    protected boolean trySmeltBlockDrops(ServerLevel server, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity = server.getBlockEntity(blockPos);
        java.util.List<ItemStack> drops = Block.getDrops(blockState, server, blockPos, blockEntity, this, ItemStack.EMPTY);
        if (drops.isEmpty()) {
            return false;
        }

        java.util.List<ItemStack> heatedDrops = new java.util.ArrayList<>();
        boolean smeltedAny = false;
        for (ItemStack drop : drops) {
            ItemStack heatedDrop = this.getSmeltedStack(server, drop);
            if (!heatedDrop.isEmpty()) {
                heatedDrops.add(heatedDrop);
                smeltedAny = true;
            } else {
                heatedDrops.add(drop.copy());
            }
        }

        if (!smeltedAny) {
            return false;
        }

        if (!server.destroyBlock(blockPos, false, this)) {
            return true;
        }

        this.suppressFireAtBlockPos = blockPos.immutable();
        for (ItemStack heatedDrop : heatedDrops) {
            this.dropItem(server, blockPos, heatedDrop);
        }

        return true;
    }

    protected ItemStack getSmeltedStack(ServerLevel server, ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack singleInput = input.copy();
        singleInput.setCount(1);
        SimpleContainer container = new SimpleContainer(singleInput);
        return server.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, container, server)
                .map(recipe -> {
                    ItemStack result = recipe.assemble(container, server.registryAccess());
                    if (result.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    ItemStack smelted = result.copy();
                    smelted.setCount(result.getCount() * input.getCount());
                    return smelted;
                })
                .orElse(ItemStack.EMPTY);
    }

    protected void dropItem(ServerLevel server, BlockPos blockPos, ItemStack drop) {
        if (drop.isEmpty()) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(server, blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, drop);
        itemEntity.setDefaultPickUpDelay();
        server.addFreshEntity(itemEntity);
    }
}
