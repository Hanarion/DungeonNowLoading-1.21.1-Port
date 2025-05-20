package dev.hexnowloading.dungeonnowloading.entity.passive;

import dev.hexnowloading.dungeonnowloading.entity.client.animation.copper_creep.CopperCreepAnimation;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.PlayerSupporterEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.util.SummonFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CopperCreepEntity extends PathfinderMob implements PlayerSupporterEntity, PowerableMob {

    public enum State {
        SUMMONING,
        IDLE,
        RUNNING_TOWARDS_PLAYER,
        WALKING_TOWARDS_PLAYER,
        FOLLOWING,
        WANDERING,
        DETONATION,
        SITTING_DETONATION,
        SITTING,
        SIT,
        STAND,
        WRONG_OWNER
    }

    public enum Skin {

        DEFAULT("default"),
        BUTLER("butler");

        public final String name;

        Skin(String name) {
            this.name = name;
        }

        public String getId() {
            return this.name;
        }

        public static Skin fromId(String id) {
            for (Skin skin : values()) {
                if (skin.name.equalsIgnoreCase(id)) {
                    return skin;
                }
            }
            return DEFAULT;
        }
    }

    private static final String DEFUSED_CUSTOM_NAME = "Defused";
    private static final float EXPLOSION_RADIUS = 3.0f;
    private static final float POWERED_EXPLOSION_RADIUS = 5.0f;

    private final AttributeModifier SPEED_MODIFIER = new AttributeModifier(UUID.randomUUID(), "Slowdown Speed", -1.0, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final EntityDataAccessor<Optional<UUID>> SUMMONER_UUID = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_IS_POWERED = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_ALREADY_SUMMONED = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<State> STATE = SynchedEntityData.defineId(CopperCreepEntity.class, EntityStates.COPPER_CREEP_STATE);
    private static final EntityDataAccessor<Skin> SKIN = SynchedEntityData.defineId(CopperCreepEntity.class, EntityStates.COPPER_CREEP_SKIN);
    private static final EntityDataAccessor<Boolean> SKIN_VALIDATION = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final byte TRIGGER_IDLE_ANIMATION_STATE = 70;
    private static final byte TRIGGER_WALKING_ANIMATION_STATE = 71;
    private static final byte TRIGGER_RUNNING_ANIMATION_STATE = 72;
    private static final byte TRIGGER_SUMMON_ANIMATION_STATE = 73;
    private static final byte TRIGGER_DETONATION_ANIMATION_STATE = 74;
    private static final byte TRIGGER_SIT_ANIMATION_STATE = 75;
    private static final byte TRIGGER_STAND_ANIMATION_STATE = 76;
    private static final byte TRIGGER_WRONG_OWNER_ANIMATION_STATE = 77;
    private static final byte TRIGGER_SITTING_ANIMATION_STATE = 78;
    private static final byte TRIGGER_SITTING_DETONATION_STATE = 79;

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState walkingAnimationState = new AnimationState();
    public AnimationState runningAnimationState = new AnimationState();
    public AnimationState summonAnimationState = new AnimationState();
    public AnimationState detonationAnimationState = new AnimationState();
    public AnimationState sitAnimationState = new AnimationState();
    public AnimationState standAnimationState = new AnimationState();
    public AnimationState wrongOwnerAnimationState = new AnimationState();
    public AnimationState sittingAnimationState = new AnimationState();
    public AnimationState sittingDetonationAnimationState = new AnimationState();
    private int lightningAttractTimer = 0;
    private int swell = 0;
    private final int MAX_SWELL = Mth.ceil(CopperCreepAnimation.DETONATION.lengthInSeconds() * 20);
    private Player summoner;
    private int aiTick;
    private int sitAnimationTick;
    private int standAnimationTick;

    public State currentState;

    public CopperCreepEntity(EntityType<? extends CopperCreepEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;

        this.summoner = null;
        this.lightningAttractTimer = 60 * (3 + this.random.nextInt(28));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.175F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        //this.goalSelector.addGoal(1, new CopperCreepInWaterGoal(this));
        this.goalSelector.addGoal(2, new CopperCreepSittingGoal(this));
        this.goalSelector.addGoal(3, new CopperCreepFollowSummoner(this));
        this.goalSelector.addGoal(4, new CustomMeleeAttackGoal(this, 2.0f, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Monster.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, livingEntity -> livingEntity instanceof Player player && isPlayerOnDifferentTeam(player)));
    }

    public void ignite() {
        this.entityData.set(DATA_IS_IGNITED, true);
    }

    public float getSwelling(float f) {
        if (this.MAX_SWELL == 0) {
            return 0;
        }
        return Mth.clamp((float) this.swell / (float) this.MAX_SWELL, 0.0F, 1.0F);
    }

    // Get the Summoner's UUID, returning an Optional
    public Optional<UUID> getSummonerUUID() {
        return this.entityData.get(SUMMONER_UUID);
    }

    // Set the Summoner's UUID
    public void setSummonerUUID(UUID summonerUUID) {
        this.entityData.set(SUMMONER_UUID, Optional.of(summonerUUID));
    }

    // Define Synched Data (This is the part where we define what data to sync)
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(SUMMONER_UUID, Optional.empty()); // Initially, no UUID is set
        this.entityData.define(DATA_IS_POWERED, false);
        this.entityData.define(DATA_IS_IGNITED, false);
        this.entityData.define(DATA_IS_ALREADY_SUMMONED, false);
        this.entityData.define(STATE, State.SUMMONING);
        this.entityData.define(SKIN, Skin.DEFAULT);
        this.entityData.define(SKIN_VALIDATION, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.getSummonerUUID().ifPresent(uuid -> compoundTag.putUUID("summonerUUID", uuid));

        if ((Boolean) this.entityData.get(DATA_IS_POWERED)) {
            compoundTag.putBoolean("powered", true);
        }

        compoundTag.putBoolean("ignited", this.isIgnited());
        compoundTag.putBoolean("isAlreadySummoned", this.isAlreadySummoned());
        compoundTag.putBoolean("isSitting", this.isState(State.SITTING));
        compoundTag.putBoolean("isWandering", this.isState(State.WANDERING));
        compoundTag.putString("skin", getSkin().getId());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if (compoundTag.contains("summonerUUID")) {
            UUID summonerUUID = compoundTag.getUUID("summonerUUID");
            this.setSummonerUUID(summonerUUID);
        }

        this.entityData.set(DATA_IS_POWERED, compoundTag.getBoolean("powered"));

        if (compoundTag.getBoolean("ignited") && !this.isDefused()) {
            this.ignite();
        }

        this.entityData.set(DATA_IS_ALREADY_SUMMONED, compoundTag.getBoolean("isAlreadySummoned"));
        if (compoundTag.getBoolean("isSitting")) {
            this.setState(State.SITTING);
        }
        if (compoundTag.getBoolean("isWandering")) {
            this.setState(State.WANDERING);
        }
        if (SummonFlag.isSummoning()) {
            this.setSkinValidation(true);
        }
        if (compoundTag.contains("skin") && !this.entityData.get(SKIN_VALIDATION)) {
            this.entityData.set(SKIN, Skin.fromId(compoundTag.getString("skin")));
        }
        this.setSkinValidation(true);
    }

    //    @Override
//    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, MobSpawnType $$2, @Nullable SpawnGroupData $$3, @Nullable CompoundTag $$4) {
////        triggerIdleAnimation();
////        triggerSummonAnimation();
//        return super.finalizeSpawn($$0, $$1, $$2, $$3, $$4);
//    }

    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(ItemTags.CREEPER_IGNITERS)) {
            SoundEvent soundEvent = itemStack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
            this.level().playSound(player, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);

            if (!this.isDefused()) {
                this.ignite();
            }

            if (!this.level().isClientSide) {
                if (!itemStack.isDamageableItem()) {
                    itemStack.shrink(1);
                } else {
                    itemStack.hurtAndBreak(1, player, (playerx) -> {
                        playerx.broadcastBreakEvent(interactionHand);
                    });
                }
            }

        } else {
            if (!this.level().isClientSide) {
                Optional<UUID> summonerUUID = this.getSummonerUUID();
                if (summonerUUID.isPresent() && summonerUUID.get().equals(player.getUUID())) {
                    if (this.getState() == State.IDLE || this.getState() == State.FOLLOWING) {
                        player.displayClientMessage(Component.translatable("entity.dungeonnowloading.copper_creep.state_wander"), true);
                        this.setState(State.WANDERING);
                    } else if (this.getState() == State.WANDERING && this.canSit() && this.sitAnimationTick <= 0) {
                        player.displayClientMessage(Component.translatable("entity.dungeonnowloading.copper_creep.state_sit"), true);
                        this.triggerSitAnimation();
                        this.setState(State.SIT);
                        this.playSound(DNLSounds.COPPER_CREEP_SIT_DOWN.get());
                        this.getNavigation().stop();
                        AttributeInstance moveSpeedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                        if (moveSpeedAttr != null && !moveSpeedAttr.hasModifier(SPEED_MODIFIER)) {
                            moveSpeedAttr.addTransientModifier(SPEED_MODIFIER);
                        }
                        this.sitAnimationTick = Mth.ceil(CopperCreepAnimation.SIT.lengthInSeconds() * 20);
                    } else if (this.getState() == State.SITTING && this.sitAnimationTick <= 0) {
                        player.displayClientMessage(Component.translatable("entity.dungeonnowloading.copper_creep.state_follow"), true);
                        this.standUp();
                    }
                } else {
                    this.triggerWrongOwnerAnimation();
                }
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private boolean canSit() {
        return !this.isInWaterOrBubble() && this.onGround();
    }

    // Example method to get the summoner as a Player (returns null if no summoner)
    public Player getSummoner() {
        if (this.summoner == null) {
            Optional<UUID> summonerUUID = getSummonerUUID();
            // Use UUID to fetch player instance
            summonerUUID.ifPresent(value -> this.summoner = this.level().getPlayerByUUID(value));
        }
        return this.summoner;
    }

    public boolean isPowered() {
        return (Boolean) this.entityData.get(DATA_IS_POWERED);
    }

    public boolean isIgnited() {
        return (Boolean) this.entityData.get(DATA_IS_IGNITED);
    }

    @Override
    public void customServerAiStep() {

        if (this.aiTick == 0 && !this.isAlreadySummoned()) {
            this.setState(State.SUMMONING);
            this.triggerSummonAnimation();
            this.playSound(DNLSounds.COPPER_CREEP_SPAWN.get());
            this.entityData.set(DATA_IS_ALREADY_SUMMONED, true);
        }

        if (this.isState(State.SUMMONING) && this.aiTick == (int) (CopperCreepAnimation.SUMMON.lengthInSeconds() * 20)) {
            this.setState(State.IDLE);
        }

        if (this.getState() != State.SIT && this.getState() != State.SITTING && this.getState() != State.STAND && this.getState() != State.WANDERING) {
            if (this.getTarget() == null) {
                this.setState(State.IDLE);
            } else {
                this.setState(State.FOLLOWING);
            }
        }

        if (this.sitAnimationTick-- > 0 && this.sitAnimationTick <= 0) {
            this.getNavigation().stop();
            this.setState(State.SITTING);
        }

        if (this.standAnimationTick-- > 0 && this.standAnimationTick <= 0) {
            this.getNavigation().stop();
            this.setState(State.IDLE);
        }

        this.aiTick++;
        super.customServerAiStep();
    }

    public boolean isAlreadySummoned() {
        return this.entityData.get(DATA_IS_ALREADY_SUMMONED);
    }

    @Override
    public void tick() {
        if (this.lightningAttractTimer > 0) {
            this.lightningAttractTimer--;
        }

        if (lightningAttractTimer == 0 && !this.isPowered()) {
            attemptToAttractLightning();
        }

        if (this.isIgnited()) {
            if (this.swell == 0) {

                if (this.getState() == State.SITTING) {
                    triggerSittingDetonationAnimation();
                    this.setState(State.SITTING_DETONATION);
                } else {
                    AttributeInstance moveSpeedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (moveSpeedAttr != null && !moveSpeedAttr.hasModifier(SPEED_MODIFIER)) {
                        moveSpeedAttr.addTransientModifier(SPEED_MODIFIER);
                    }
                    triggerDetonationAnimation();
                    this.setState(State.DETONATION);
                }

                this.playSound(DNLSounds.COPPER_CREEP_PRIME.get());
            }

            if (this.swell < this.MAX_SWELL) {
                this.swell++;
            } else if (!this.level().isClientSide) {
                this.dead = true;
                float finalExplosionRadius = EXPLOSION_RADIUS;
                if (this.isPowered()) {
                    finalExplosionRadius = POWERED_EXPLOSION_RADIUS;
                }

                DamageSource source = level().damageSources().explosion(this, this.getSummoner());
                this.level().explode(this, source, null, this.getX(), this.getY(), this.getZ(), finalExplosionRadius, false, Level.ExplosionInteraction.NONE);
                this.discard();
                this.spawnLingeringCloud();
            }
        }

        super.tick();

        if (!this.level().isClientSide) return;

        if (this.getState() == State.IDLE || this.getState() == State.WANDERING) {
            this.standAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        if (this.getState() == State.SITTING) {
            this.sitAnimationState.stop();
            this.sittingAnimationState.startIfStopped(this.tickCount);
        }
    }

    private void spawnLingeringCloud() {
        Collection<MobEffectInstance> mobEffectInstances = this.getActiveEffects();
        if (!mobEffectInstances.isEmpty()) {
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            areaEffectCloud.setRadius(2.5F);
            areaEffectCloud.setRadiusOnUse(-0.5F);
            areaEffectCloud.setWaitTime(10);
            areaEffectCloud.setDuration(areaEffectCloud.getDuration() / 2);
            areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float)areaEffectCloud.getDuration());
            Iterator var3 = mobEffectInstances.iterator();

            while(var3.hasNext()) {
                MobEffectInstance effectInstance = (MobEffectInstance)var3.next();
                areaEffectCloud.addEffect(new MobEffectInstance(effectInstance));
            }

            this.level().addFreshEntity(areaEffectCloud);
        }
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        this.setRemainingFireTicks(this.getRemainingFireTicks() + 1);
        if (this.getRemainingFireTicks() == 0) {
            this.setSecondsOnFire(8);
        }

//        this.hurt(this.damageSources().lightningBolt(), 5.0F);

        this.entityData.set(DATA_IS_POWERED, true);
    }

    @Override
    public void handleEntityEvent(byte event) {
        switch (event) {
            case TRIGGER_IDLE_ANIMATION_STATE:
                this.sittingAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
                break;
            case TRIGGER_WALKING_ANIMATION_STATE:
                this.resetAnimations();
                this.walkingAnimationState.start(this.tickCount);
                break;
            case TRIGGER_RUNNING_ANIMATION_STATE:
                this.resetAnimations();
                this.runningAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SUMMON_ANIMATION_STATE:
                this.resetAnimations();
                this.summonAnimationState.start(this.tickCount);
                break;
            case TRIGGER_DETONATION_ANIMATION_STATE:
                this.resetAnimations();
                this.detonationAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SIT_ANIMATION_STATE:
                this.resetAnimations();
                this.sitAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SITTING_ANIMATION_STATE:
                this.idleAnimationState.stop();
                this.sittingAnimationState.startIfStopped(this.tickCount);
                break;
            case TRIGGER_STAND_ANIMATION_STATE:
                this.resetAnimations();
                this.standAnimationState.start(this.tickCount);
                break;
            case TRIGGER_WRONG_OWNER_ANIMATION_STATE:
                this.wrongOwnerAnimationState.stop();
                this.wrongOwnerAnimationState.startIfStopped(this.tickCount);
                break;
            case TRIGGER_SITTING_DETONATION_STATE:
                this.resetAnimations();
                this.sittingDetonationAnimationState.start(this.tickCount);
                break;
        }

        super.handleEntityEvent(event);
    }

    private void resetAnimations() {
        this.idleAnimationState.stop();
        this.walkingAnimationState.stop();
        this.runningAnimationState.stop();
        this.summonAnimationState.stop();
        this.detonationAnimationState.stop();
        this.sitAnimationState.stop();
        this.sittingAnimationState.stop();
        this.standAnimationState.stop();
        this.wrongOwnerAnimationState.stop();
        this.sittingDetonationAnimationState.stop();
    }

    private boolean isPlayerOnDifferentTeam(Player player) {
        Optional<UUID> summonerUUID = this.getSummonerUUID();
        UUID playerUUID = player.getUUID();

        if (summonerUUID.isPresent() && summonerUUID.get().equals(playerUUID)) {
            return false;
        }

        if (player.isCreative() || player.isSpectator()) {
            return false;
        }

        // If we don't have a summoner, no one is an enemy
        if (!summonerUUID.isPresent()) {
            return false;
        }

        // Get the actual summoner player
        Player summoner = this.getSummoner();
        if (summoner == null) {
            return false;
        }

        // Check if players are allied
        boolean isAllied = player.isAlliedTo(summoner);
        return !isAllied;
    }

    public boolean isDefused() {
        return this.hasCustomName();
    }

    private void attemptToAttractLightning() {
        if (this.level().isThundering() && hasClearSkyAbove()) {
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this.level());
            if (lightningBolt != null) {
                lightningBolt.moveTo(this.getX(), this.getY(), this.getZ());
                this.level().addFreshEntity(lightningBolt);

                this.entityData.set(DATA_IS_POWERED, true);
            }
        }
    }

    private boolean hasClearSkyAbove() {
        BlockPos posAbove = this.blockPosition().above();
        while (posAbove.getY() < this.level().getMaxBuildHeight()) {
            if (!this.level().isEmptyBlock(posAbove)) {
                return false;
            }
            posAbove = posAbove.above();
        }
        return true;
    }

    private void standUp() {
        this.triggerStandAnimation();
        this.setState(State.STAND);
        this.playSound(DNLSounds.COPPER_CREEP_STAND_UP.get());
        this.getNavigation().stop();
        AttributeInstance moveSpeedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null) {
            moveSpeedAttr.removeModifier(SPEED_MODIFIER);
        }
        this.standAnimationTick = Mth.ceil(CopperCreepAnimation.STAND.lengthInSeconds() * 20);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt && !this.level().isClientSide) {
            if (this.getState() == State.SITTING && this.sitAnimationTick <= 0) {
                this.standUp();
            }
        }
        return hurt;
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.4F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        if (this.getState() == State.SITTING || this.getState() == State.SITTING_DETONATION) return;
        this.playSound(DNLSounds.COPPER_CREEP_STEP.get(), 0.5F, 1.0F);
    }

    @Override
    public Fallsounds getFallSounds() {
        return new Fallsounds(DNLSounds.COPPER_CREEP_LAND.get(),DNLSounds.COPPER_CREEP_LAND.get());
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return DNLSounds.COPPER_CREEP_HIT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.COPPER_CREEP_DEATH.get();
    }

    public State getState() {
        return this.entityData.get(STATE);
    }

    public boolean isState(CopperCreepEntity.State state) {
        return this.getState().equals(state);
    }

    private void triggerIdleAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_IDLE_ANIMATION_STATE);
    }

    private void triggerWalkingAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_WALKING_ANIMATION_STATE);
    }

    private void triggerRunningAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_RUNNING_ANIMATION_STATE);
    }

    private void triggerSummonAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_SUMMON_ANIMATION_STATE);
    }

    private void triggerDetonationAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_DETONATION_ANIMATION_STATE);
    }

    private void triggerSitAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_SIT_ANIMATION_STATE);
    }

    private void triggerSittingAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_SITTING_ANIMATION_STATE);
    }

    private void triggerStandAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_STAND_ANIMATION_STATE);
    }

    private void triggerWrongOwnerAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_WRONG_OWNER_ANIMATION_STATE);
    }

    private void triggerSittingDetonationAnimation() {
        this.level().broadcastEntityEvent(this, TRIGGER_SITTING_DETONATION_STATE);
    }

    private void setState(State state) {
        this.currentState = state;
        this.entityData.set(STATE, state);
    }

    public Skin getSkin() {
        return this.entityData.get(SKIN);
    }

    public void setSkin(Skin skin) {
        this.entityData.set(SKIN, skin);
    }

    public void setCosmeticMode(String id) {
        setSkin(Skin.fromId(id));
    }

    public void setSkinValidation(boolean skinValidation) {
        this.entityData.set(SKIN_VALIDATION, skinValidation);
    }

    private class CopperCreepInWaterGoal extends Goal {

        private final CopperCreepEntity copperCreep;

        public CopperCreepInWaterGoal(CopperCreepEntity copperCreep) {
            this.copperCreep = copperCreep;
        }

        @Override
        public boolean canUse() {
            return this.copperCreep.getState() == State.SITTING && !this.copperCreep.canSit();
        }

        @Override
        public void start() {
            this.copperCreep.standUp();
        }
    }

    private class CopperCreepFollowSummoner extends Goal {

        private final CopperCreepEntity copperCreep;
        private int updatePathTick;
        private float speed;

        public CopperCreepFollowSummoner(CopperCreepEntity copperCreep) {
            this.copperCreep = copperCreep;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return copperCreep.getState() == State.IDLE && copperCreep.getTarget() == null && copperCreep.getSummoner() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return copperCreep.getState() != State.WANDERING && copperCreep.getTarget() == null && this.copperCreep.getSummoner() != null;
        }

        @Override
        public void stop() {
            this.copperCreep.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.updatePathTick-- > 0) {
                Player summoner = this.copperCreep.getSummoner();
                float distance = this.copperCreep.distanceTo(summoner);
                if (distance < 5) {
                    this.speed = 0.8f;
                    this.copperCreep.setState(State.WALKING_TOWARDS_PLAYER);
                } else {
                    this.speed = 2.0f;
                    this.copperCreep.setState(State.RUNNING_TOWARDS_PLAYER);
                }
            } else {
                this.copperCreep.getNavigation().moveTo(copperCreep.getSummoner(), speed);
                this.updatePathTick = this.adjustedTickDelay(10);
            }
        }
    }

    private class CopperCreepSittingGoal extends Goal {

        private final CopperCreepEntity copperCreep;

        public CopperCreepSittingGoal(CopperCreepEntity copperCreep) {
            this.copperCreep = copperCreep;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return copperCreep.getState() == State.SITTING;
        }

        @Override
        public void start() {
            this.copperCreep.getNavigation().stop();
            this.copperCreep.setTarget(null);
        }

        /*@Override
        public void stop() {
            this.copperCreep.setState(State.STAND);
            this.copperCreep.standAnimationTick = Mth.ceil(CopperCreepAnimation.STAND.lengthInSeconds() * 20);
        }*/
    }

    private class CustomMeleeAttackGoal extends MeleeAttackGoal {

        private final CopperCreepEntity copperCreep;

        public CustomMeleeAttackGoal(CopperCreepEntity mob, double v, boolean b) {
            super(mob, v, b);
            copperCreep = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return copperCreep.getState() != State.WANDERING && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {

            if (copperCreep.getState() != State.FOLLOWING) return false;

            return super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity $$0, double $$1) {
//            double $$2 = this.getAttackReachSqr($$0);
//            if ($$1 <= $$2 && this.ticksUntilNextAttack <= 0) {
//                this.resetAttackCooldown();
//                this.mob.swing(InteractionHand.MAIN_HAND);
//                this.mob.doHurtTarget($$0);
//            }
        }
    }
}
