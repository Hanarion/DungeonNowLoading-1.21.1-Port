package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.entity.ai.EntityBodyRotationControl;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.RepulsorAnimation;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepulsorEntity extends Mob {
    public enum State {
        SETUP,
        IDLE,
    }

    public static final float SHIELD_MAX_HEALTH = 100.0f;
    public static final float SHIELD_ALERT_THRESHOLD = SHIELD_MAX_HEALTH * 0.5F;
    private static final double BEAM_INITIAL_PARTICLE_SPACING = 0.5d;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MIN = 0.2f;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MAX = 0.4f;
    private static final int SHIELD_HEAL_AMOUNT = 5;
    private static final EntityDataAccessor<Boolean> DATA_CAN_RENDER = SynchedEntityData.defineId(RepulsorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(RepulsorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SHIELD_HEALTH = SynchedEntityData.defineId(RepulsorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(RepulsorEntity.class, EntityStates.COMMAND_PYLON_STATE);

    public AnimationState setupAnimState = new AnimationState();
    public AnimationState idleAnimState = new AnimationState();
    private Set<ThrownTrident> processedTridents = new HashSet<>();

    public RepulsorEntity(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CAN_RENDER, false);
        this.entityData.define(DATA_AGE, 0);
        this.entityData.define(DATA_SHIELD_HEALTH, SHIELD_MAX_HEALTH);
        this.entityData.define(DATA_STATE, State.SETUP);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_CAN_RENDER, compoundTag.getBoolean("canRender"));
        this.entityData.set(DATA_AGE, compoundTag.getInt("age"));
        this.entityData.set(DATA_SHIELD_HEALTH, compoundTag.getFloat("shieldHealth"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("canRender", this.canRender());
        compoundTag.putInt("age", this.getAge());
        compoundTag.putFloat("shieldHealth", this.entityData.get(DATA_SHIELD_HEALTH));
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        this.dropItem((Entity) null);
        this.discard();
        return PushReaction.NORMAL;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (this.level().isClientSide()) {
            return InteractionResult.PASS;
        }
        if (itemStack.isEmpty() || interactionHand.equals(InteractionHand.OFF_HAND)) {
            return InteractionResult.FAIL;
        }

        if (this.getShieldHealth() < SHIELD_MAX_HEALTH && itemStack.is(Items.REDSTONE)) {
            this.setShieldHealth(this.getShieldHealth() + SHIELD_HEAL_AMOUNT);
            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return InteractionResult.FAIL;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new EntityBodyRotationControl(this);
    }

    @Override
    public boolean alwaysAccepts() {
        return super.alwaysAccepts();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        } else {
            if (!this.isRemoved() && !this.level().isClientSide) {
                this.discard();
                this.markHurt();
                this.dropItem(damageSource.getEntity());
            }

            return true;
        }
    }

    public void push(double d, double e, double f) {
        if (!this.level().isClientSide && !this.isRemoved() && d * d + e * e + f * f > 0.0) {
            this.discard();
            this.dropItem((Entity) null);
        }
    }

    public void dropItem(@Nullable Entity entity) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(DNLSounds.REPULSOR_BREAK.get());
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player.getAbilities().instabuild) {
                    return;
                }
            }

            ItemStack itemStack = new ItemStack(DNLItems.REPULSOR.get());

            int damage = (int) (SHIELD_MAX_HEALTH - this.entityData.get(DATA_SHIELD_HEALTH));
            itemStack.setDamageValue(damage);

            this.spawnAtLocation(itemStack);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getAge() == 0) {
            this.setupAnimState.start(this.tickCount);
            this.entityData.set(DATA_STATE, State.SETUP);
            this.entityData.set(DATA_CAN_RENDER, true);
            this.playSound(DNLSounds.REPULSOR_PLACE.get());
        } else if (this.getAge() == 35) {
            this.playSound(DNLSounds.REPULSOR_BARRIER_BUILD.get());
        } else if (this.getAge() == (int) (RepulsorAnimation.SETUP.lengthInSeconds() * 20)) {
            this.setupAnimState.stop();
            this.entityData.set(DATA_STATE, State.IDLE);
        }

        if (this.getAge() >= RepulsorAnimation.SETUP.lengthInSeconds() * 20) {
            this.idleAnimState.startIfStopped(this.tickCount);
        }

        if (!this.level().isClientSide) {
            if (this.getAge() >= 35) {
                for (Entity entity : this.getNearbyProjectiles()) {
                    boolean discardEntity = false;
                    boolean aboveHalfHealth = this.entityData.get(DATA_SHIELD_HEALTH) > SHIELD_ALERT_THRESHOLD;

                    if (entity instanceof ThrownTrident thrownTrident) {
                        if (!processedTridents.contains(thrownTrident)) {
                            processedTridents.add(thrownTrident);

                            Vec3 motion = thrownTrident.getDeltaMovement();
                            thrownTrident.setDeltaMovement(new Vec3(
                                    -motion.x * 0.1f,
                                    -motion.y * 0.1f,
                                    -motion.z * 0.1f
                            ));
                        } else {
                            continue;
                        }
                    } else if (entity instanceof ThrownPotion thrownPotion) {
                        ItemStack itemStack = thrownPotion.getItem();
                        Potion potion = PotionUtils.getPotion(itemStack);
                        List<MobEffectInstance> list = PotionUtils.getMobEffects(itemStack);
                        boolean flag = potion == Potions.WATER && list.isEmpty();
                        if (flag) {
                            thrownPotion.applyWater();
                        } else if (!list.isEmpty()) {
                            if (itemStack.is(Items.LINGERING_POTION)) {
                                thrownPotion.makeAreaOfEffectCloud(itemStack, potion);
                            } else {
                                thrownPotion.applySplash(list, null);
                            }
                        }
                        int i = potion.hasInstantEffects() ? 2007 : 2002;
                        this.level().levelEvent(i, thrownPotion.blockPosition(), PotionUtils.getColor(itemStack));
                        discardEntity = true;
                    } else {
                        discardEntity = true;
                    }

                    spawnRedstoneBeamParticle((ServerLevel) this.level(), entity);
                    playSound(DNLSounds.REPULSOR_FIZZLE.get());

                    float shieldDamage = 1;

                    if (entity.getType().is(DNLTags.REPULSOR_HIGH_DAMAGE_PROJECTILES)) {
                        shieldDamage = 20;
                    } else if (entity.getType().is(DNLTags.REPULSOR_LOW_DAMAGE_PROJECTILES)) {
                        shieldDamage = 5;
                    }

                    float updatedShieldHealth = this.entityData.get(DATA_SHIELD_HEALTH) - shieldDamage;
                    this.entityData.set(DATA_SHIELD_HEALTH, updatedShieldHealth);

                    System.out.println("Above Half Health: " + aboveHalfHealth + " | Updated Half: " + updatedShieldHealth);

                    if (aboveHalfHealth && this.getShieldHealth() <= SHIELD_MAX_HEALTH * 0.5F) {
                        System.out.println("Alert");
                        this.playSound(DNLSounds.REPULSOR_ALERT.get());
                    }

                    if (updatedShieldHealth <= 0.0f) {
                        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                                DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                                1.0F
                        );
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 1.0F, 2.0F);
                        ((ServerLevel) this.level()).sendParticles(particleData, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.0f);
                        this.discard();
                    }

                    if (discardEntity) {
                        entity.discard();
                    }
                }
                if (this.getShieldHealth() <= RepulsorEntity.SHIELD_MAX_HEALTH * 0.5F) {
                    float healthRatio = this.getShieldHealth() / RepulsorEntity.SHIELD_ALERT_THRESHOLD;
                    float blinkCycle = 40F - 35F * (1F - healthRatio);

                    if ((this.getAge() % (int) blinkCycle) == 0) {
                        float normalizedSpeed = 1F - ((blinkCycle - 5F) / 35F);
                        float pitch = 1.0F + normalizedSpeed;

                        this.level().playSound(
                                null,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                DNLSounds.REPULSOR_BLINK.get(),
                                SoundSource.BLOCKS,
                                1.0F, // volume
                                pitch
                        );
                    }
                }
            }


            this.entityData.set(DATA_AGE, this.getAge() + 1);
        }

    }

    private void spawnRedstoneBeamParticle(ServerLevel level, Entity target) {
        Vec3 start = this.position().add(0.0F, 1.0F, 0.0F);
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

    private List<Entity> getNearbyProjectiles() {
        double centerX = this.getX();
        double centerY = this.getY();
        double centerZ = this.getZ();

        AABB detectionBox = new AABB(
                centerX - 4.5, centerY - 4.5, centerZ - 4.5,
                centerX + 4.5, centerY + 4.5, centerZ + 4.5
        );

        return this.level().getEntities(
                (Entity) null, detectionBox, entity ->
                        (entity instanceof Projectile || entity.getType().is(DNLTags.PROJECTILES) || entity instanceof ThrownPotion) && !entity.getType().is(DNLTags.REPULSOR_OMITTED_PROJECTILES)
        );
    }

    public boolean canRender() {
        return this.entityData.get(DATA_CAN_RENDER);
    }

    public int getAge() {
        return this.entityData.get(DATA_AGE);
    }

    public State getState() {
        return this.entityData.get(DATA_STATE);
    }

    public float getShieldHealth() {
        return this.entityData.get(DATA_SHIELD_HEALTH);
    }

    public void setShieldHealth(float health) {
        this.entityData.set(DATA_SHIELD_HEALTH, health);
    }

}
