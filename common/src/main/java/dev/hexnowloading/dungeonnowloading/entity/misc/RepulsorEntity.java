package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.util.ItemNbt;
import dev.hexnowloading.dungeonnowloading.entity.ai.EntityBodyRotationControl;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.RepulsorAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.item.RepulsorItem;
import dev.hexnowloading.dungeonnowloading.item.ScrapItem;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.*;
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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

    public enum Skin {
        DEFAULT("default"),
        GOLDEN("golden");

        public final String id;
        Skin(String id) { this.id = id; }
        public String getId() { return id; }

        public static Skin fromId(String id) {
            for (Skin s : values()) if (s.id.equalsIgnoreCase(id)) return s;
            return DEFAULT;
        }
    }

    public static final int SHIELD_MAX_HEALTH = 100;
    public static final float SHIELD_ALERT_THRESHOLD = SHIELD_MAX_HEALTH * 0.5F;
    private static final double BEAM_INITIAL_PARTICLE_SPACING = 0.5d;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MIN = 0.1f;
    private static final float BEAM_INITIAL_PARTICLE_SCALE_MAX = 0.3f;
    private static final int SHIELD_HEAL_AMOUNT = 5;
    private static final EntityDataAccessor<Boolean> DATA_CAN_RENDER = SynchedEntityData.defineId(RepulsorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(RepulsorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SHIELD_HEALTH = SynchedEntityData.defineId(RepulsorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(RepulsorEntity.class, EntityStates.COMMAND_PYLON_STATE);
    private static final EntityDataAccessor<Skin> DATA_SKIN =
            SynchedEntityData.defineId(RepulsorEntity.class, EntityStates.REPULSOR_SKIN); // same serializer enum you already use
    private static final EntityDataAccessor<Boolean> DATA_SKIN_VALIDATION =
            SynchedEntityData.defineId(RepulsorEntity.class, EntityDataSerializers.BOOLEAN);


    public AnimationState setupAnimState = new AnimationState();
    public AnimationState idleAnimState = new AnimationState();
    public AnimationState rechargeAnimState = new AnimationState();
    private Set<ThrownTrident> processedTridents = new HashSet<>();

    private int rechargeAnimDuration;

    private ItemStack sourceStack = ItemStack.EMPTY;

    public RepulsorEntity(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CAN_RENDER, false);
        builder.define(DATA_AGE, 0);
        builder.define(DATA_SHIELD_HEALTH, SHIELD_MAX_HEALTH);
        builder.define(DATA_STATE, State.SETUP);
        builder.define(DATA_SKIN, Skin.DEFAULT);
        builder.define(DATA_SKIN_VALIDATION, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_CAN_RENDER, compoundTag.getBoolean("canRender"));
        this.entityData.set(DATA_AGE, compoundTag.getInt("age"));
        this.entityData.set(DATA_SHIELD_HEALTH, compoundTag.getInt("shieldHealth"));
        if (compoundTag.contains("skin") && !this.getSkinValidation()) {
            this.entityData.set(DATA_SKIN, Skin.fromId(compoundTag.getString("skin")));
        }
        if (compoundTag.contains("SourceStack", 10)) { // 10 = compound
            this.sourceStack = ItemNbt.load(compoundTag.getCompound("SourceStack"));
        } else {
            this.sourceStack = ItemStack.EMPTY;
        }
        this.setSkinValidation(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("canRender", this.canRender());
        compoundTag.putInt("age", this.getAge());
        compoundTag.putInt("shieldHealth", this.entityData.get(DATA_SHIELD_HEALTH));
        compoundTag.putString("skin", getSkin().getId());
        if (!this.sourceStack.isEmpty()) {
            CompoundTag s = ItemNbt.save(this.sourceStack);
            compoundTag.put("SourceStack", s);
        }
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

        if (itemStack.is(Items.REDSTONE) && this.getAge() >= 35 && this.getShieldHealth() < SHIELD_MAX_HEALTH && this.rechargeAnimDuration <= 0) {
            if (this.getShieldHealth() >= 95) {
                this.playSound(DNLSounds.REPULSOR_RECHARGE.get(), 0.5f, 0.5f);
            } else {
                this.playSound(DNLSounds.REPULSOR_RECHARGE.get(), 0.5f, 1f);
            }
            this.rechargeAnimDuration = (int) (RepulsorAnimationDuration.RECHARGE * 20);
            if (!this.level().isClientSide) {
                itemStack.shrink(1);
                this.setShieldHealth(this.getShieldHealth() + SHIELD_HEAL_AMOUNT);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, interactionHand);
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
            if (entity instanceof Player player && player.getAbilities().instabuild) return;

            // Start from original placed stack to preserve enchants/NBT if available
            ItemStack itemStack = this.sourceStack.isEmpty() ? new ItemStack(DNLItems.REPULSOR.get()) : this.sourceStack.copy();
            itemStack.setCount(1);

            int damage = SHIELD_MAX_HEALTH - this.entityData.get(DATA_SHIELD_HEALTH);
            itemStack.setDamageValue(damage);

            // Ensure cosmetic matches entity skin (in case original lacked tag but entity was golden)
            if (this.getSkin() == RepulsorEntity.Skin.GOLDEN) {
                RepulsorItem.setGolden(itemStack);
            }

            this.spawnAtLocation(itemStack);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getAge() == 1) {
            this.setupAnimState.start(this.tickCount);
            this.entityData.set(DATA_STATE, State.SETUP);
            this.entityData.set(DATA_CAN_RENDER, true);
            this.playSound(DNLSounds.REPULSOR_PLACE.get());
        } else if (this.getAge() == 36) {
            this.playSound(DNLSounds.REPULSOR_BARRIER_BUILD.get());
        } else if (this.getAge() == (int) (RepulsorAnimationDuration.SETUP * 20) + 1) {
            this.setupAnimState.stop();
            this.entityData.set(DATA_STATE, State.IDLE);
        }

        if (this.getAge() >= RepulsorAnimationDuration.SETUP * 20 + 1) {
            this.idleAnimState.startIfStopped(this.tickCount);
        }

        if (this.rechargeAnimDuration-- > 0) {
            this.rechargeAnimState.startIfStopped(this.tickCount);
            if (this.rechargeAnimDuration <= 0) {
                this.rechargeAnimState.stop();
            }
        }

        if (!this.level().isClientSide) {
            if (this.getAge() >= 36) {
                for (Entity entity : this.getNearbyProjectiles()) {
                    boolean discardEntity = false;
                    boolean aboveHalfHealth = this.entityData.get(DATA_SHIELD_HEALTH) > SHIELD_ALERT_THRESHOLD;
                    if (entity instanceof ThrownTrident thrownTrident) {
                        if (!processedTridents.contains(thrownTrident) && !thrownTrident.inGround) {
                            processedTridents.add(thrownTrident);

                            Vec3 motion = thrownTrident.getDeltaMovement();
                            thrownTrident.setDeltaMovement(new Vec3(
                                    -motion.x * 0.1f,
                                    -motion.y * 0.1f,
                                    -motion.z * 0.1f
                            ));
                            thrownTrident.hasImpulse = true;
                        } else {
                            continue;
                        }
                    } else if (entity instanceof ThrownPotion thrownPotion) {
                        ItemStack itemStack = thrownPotion.getItem();
                        PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                        Holder<Potion> potion = potionContents.potion().orElse(null);
                        List<MobEffectInstance> list = new java.util.ArrayList<>();
                        potionContents.forEachEffect(list::add);
                        boolean isWater = potionContents.is(Potions.WATER) && list.isEmpty();
                        if (isWater) {
                            // Splash water: extinguish fire / harm water-sensitive entities in range.
                            AABB area = thrownPotion.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
                            for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, area, ThrownPotion.WATER_SENSITIVE_OR_ON_FIRE)) {
                                if (living.isSensitiveToWater()) {
                                    living.hurt(this.damageSources().indirectMagic(this, null), 1.0F);
                                }
                                if (living.isOnFire() && living.isAlive()) {
                                    living.extinguishFire();
                                }
                            }
                        } else if (!list.isEmpty()) {
                            if (itemStack.is(Items.LINGERING_POTION)) {
                                AreaEffectCloud cloud = new AreaEffectCloud(this.level(), thrownPotion.getX(), thrownPotion.getY(), thrownPotion.getZ());
                                cloud.setRadius(3.0F);
                                cloud.setRadiusOnUse(-0.5F);
                                cloud.setWaitTime(10);
                                cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
                                cloud.setPotionContents(potionContents);
                                this.level().addFreshEntity(cloud);
                            } else {
                                AABB area = thrownPotion.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
                                for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
                                    double distSqr = thrownPotion.distanceToSqr(living);
                                    if (distSqr < 16.0D) {
                                        double factor = living == entity ? 1.0D : 1.0D - Math.sqrt(distSqr) / 4.0D;
                                        for (MobEffectInstance effect : list) {
                                            Holder<net.minecraft.world.effect.MobEffect> mobEffect = effect.getEffect();
                                            if (mobEffect.value().isInstantenous()) {
                                                mobEffect.value().applyInstantenousEffect(this, this, living, effect.getAmplifier(), factor);
                                            } else {
                                                int duration = effect.mapDuration(d -> (int) (factor * d + 0.5D));
                                                MobEffectInstance scaled = new MobEffectInstance(mobEffect, duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible());
                                                if (!scaled.endsWithin(20)) {
                                                    living.addEffect(scaled, this);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        int i = (potion != null && potion.value().hasInstantEffects()) ? 2007 : 2002;
                        this.level().levelEvent(i, thrownPotion.blockPosition(), potionContents.getColor());
                        discardEntity = true;
                    } else if (entity instanceof AbstractArrow arrow && arrow.inGround) {
                        continue;
                    } else {
                        discardEntity = true;
                    }

                    spawnRedstoneBeamParticle((ServerLevel) this.level(), entity);
                    playSound(DNLSounds.REPULSOR_FIZZLE.get());

                    int shieldDamage = 1;

                    if (entity.getType().is(DNLTags.REPULSOR_HIGH_DAMAGE_PROJECTILES)) {
                        shieldDamage = 20;
                    } else if (entity.getType().is(DNLTags.REPULSOR_LOW_DAMAGE_PROJECTILES)) {
                        shieldDamage = 5;
                    }

                    int updatedShieldHealth = this.entityData.get(DATA_SHIELD_HEALTH) - shieldDamage;
                    this.entityData.set(DATA_SHIELD_HEALTH, updatedShieldHealth);

                    if (aboveHalfHealth && this.getShieldHealth() <= SHIELD_MAX_HEALTH * 0.5F) {
                        this.playSound(DNLSounds.REPULSOR_ALERT.get());
                    }

                    if (discardEntity) {
                        entity.discard();
                    }

                    if (updatedShieldHealth <= 0.0f) {
                        ScalableParticleType.ScalableParticleData particleData = new ScalableParticleType.ScalableParticleData(
                                DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                                1.0F
                        );
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 1.0F, 2.0F);
                        ((ServerLevel) this.level()).sendParticles(particleData, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.0f);

                        // Uses itself up: if original had Break Protection, drop Item Scraps keeping enchants
                        maybeDropScrapOnUsedUp();

                        this.discard();
                        return;
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
                                0.5F,
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

            if (i == particleCount) {
                particleData = new ScalableParticleType.ScalableParticleData(
                        DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(),
                        BEAM_INITIAL_PARTICLE_SCALE_MAX + 0.2F
                );
            }
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
                (Entity) null, detectionBox, entity -> (
                                entity instanceof Projectile
                                || entity.getType().is(DNLTags.PROJECTILES)
                                || entity instanceof ThrownPotion)
                                && !entity.getType().is(DNLTags.REPULSOR_OMITTED_PROJECTILES)
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

    public int getShieldHealth() {
        return this.entityData.get(DATA_SHIELD_HEALTH);
    }

    public void setShieldHealth(int health) {
        this.entityData.set(DATA_SHIELD_HEALTH, health);
    }

    public Skin getSkin() { return this.entityData.get(DATA_SKIN); }
    public void setSkin(Skin skin) { this.entityData.set(DATA_SKIN, skin); }
    public void setCosmeticMode(String id) { setSkin(Skin.fromId(id)); } // parity helper
    public boolean getSkinValidation() { return this.entityData.get(DATA_SKIN_VALIDATION); }
    public void setSkinValidation(boolean v) { this.entityData.set(DATA_SKIN_VALIDATION, v); }

    public void setSourceStack(ItemStack stack) {
        this.sourceStack = stack.copy();
    }
    public ItemStack getSourceStack() { return sourceStack; }

    private void maybeDropScrapOnUsedUp() {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) return;
        if (this.level().isClientSide) return;
        if (this.sourceStack.isEmpty()) return;
        // Only convert to scrap if the original item had Break Protection
        if (EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(this.level(), DNLEnchantments.BREAK_PROTECTION), this.sourceStack) <= 0) return;

        ItemStack original = this.sourceStack.copy();
        // Mark as fully broken so reconstruction math is straightforward; NBT (incl. enchants) is preserved
        original.setDamageValue(original.getMaxDamage());

        ItemStack scrap = ScrapItem.ofOriginal(original);
        this.spawnAtLocation(scrap);
    }

}
