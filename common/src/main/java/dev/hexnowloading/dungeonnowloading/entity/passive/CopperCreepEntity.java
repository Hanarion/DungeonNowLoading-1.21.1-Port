package dev.hexnowloading.dungeonnowloading.entity.passive;

import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CopperCreepAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.PlayerSupporterEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class CopperCreepEntity extends PathfinderMob implements PlayerSupporterEntity {
    public enum State {
        SUMMONING,
        IDLE,
        FOLLOWING,
        DETONATION
    }

    private static final String DEFUSED_CUSTOM_NAME = "Defused";
    private static final float EXPLOSION_RADIUS = 2.0f;
    private static final float POWERED_EXPLOSION_RADIUS = 5.0f;
    private static final EntityDataAccessor<Optional<UUID>> SUMMONER_UUID = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_IS_POWERED = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_ALREADY_SUMMONED = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<State> STATE = SynchedEntityData.defineId(CopperCreepEntity.class, EntityStates.COPPER_CREEP_STATE);
    private final byte TRIGGER_IDLE_ANIMATION_STATE = 70;
    private final byte TRIGGER_WALKING_ANIMATION_STATE = 71;
    private final byte TRIGGER_RUNNING_ANIMATION_STATE = 72;
    private final byte TRIGGER_SUMMON_ANIMATION_STATE = 73;
    private final byte TRIGGER_DETONATION_ANIMATION_STATE = 74;

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState walkingAnimationState = new AnimationState();
    public AnimationState runningAnimationState = new AnimationState();
    public AnimationState summonAnimationState = new AnimationState();
    public AnimationState detonationAnimationState = new AnimationState();
    private int swell = 0;
    private int maxSwell = 30;
    private Player summoner;
    private int aiTick;

//    private boolean isSummonAnimInitialized;

    public State currentState;
    private State lastState;

    public CopperCreepEntity(EntityType<? extends CopperCreepEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;

        this.summoner = null;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
//                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.175F);
    }

    public void ignite() {
        this.entityData.set(DATA_IS_IGNITED, true);
    }

    public float getSwelling(float f) {
        if (this.maxSwell == 0) {
            return 0;
        }
        return Mth.clamp((float) this.swell / (float) this.maxSwell, 0.0F, 1.0F);
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
    }

    // Add custom save data (write the UUID to NBT)
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        // Save the UUID to NBT
        this.getSummonerUUID().ifPresent(uuid -> compoundTag.putUUID("summonerUUID", uuid));

        if ((Boolean) this.entityData.get(DATA_IS_POWERED)) {
            compoundTag.putBoolean("powered", true);
        }

        compoundTag.putBoolean("ignited", this.isIgnited());
        compoundTag.putBoolean("isAlreadySummoned", this.isAlreadySummoned());
    }

    // Read custom save data (read the UUID from NBT)
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if (compoundTag.contains("summonerUUID")) {
            // Read the UUID and set it
            UUID summonerUUID = compoundTag.getUUID("summonerUUID");
            this.setSummonerUUID(summonerUUID);
        }

        this.entityData.set(DATA_IS_POWERED, compoundTag.getBoolean("powered"));

        if (compoundTag.getBoolean("ignited") && !this.isDefused()) {
            this.ignite();
        }

        this.entityData.set(DATA_IS_ALREADY_SUMMONED, compoundTag.getBoolean("isAlreadySummoned"));
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

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(player, interactionHand);
        }
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
//            this.currentState = State.SUMMONING;
            this.setState(State.SUMMONING);
            this.triggerSummonAnimation();
            this.entityData.set(DATA_IS_ALREADY_SUMMONED, true);
        }
        if (this.aiTick == (int) CopperCreepAnimation.SUMMON.lengthInSeconds() * 20) {
//            this.currentState = State.IDLE;
            this.setState(State.IDLE);
            this.triggerIdleAnimation();
        }

        if (this.getTarget() == null) {
//            this.currentState = State.IDLE;
            this.setState(State.IDLE);
        } else {
//            this.currentState = State.FOLLOWING;
            this.setState(State.FOLLOWING);
        }

        this.aiTick++;
        super.customServerAiStep();
    }

    public boolean isAlreadySummoned() {
        return this.entityData.get(DATA_IS_ALREADY_SUMMONED);
    }

    @Override
    public void tick() {
        if (this.isIgnited()) {
            if (this.swell == 0) {
                this.setState(State.DETONATION);

                triggerDetonationAnimation();

                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 1.0F);
                AttributeInstance moveSpeedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                moveSpeedAttr.setBaseValue(0.0f);
            }

            if (this.swell < this.maxSwell) {
                this.swell++;
            } else if (!this.level().isClientSide) {
                this.dead = true;
                float finalExplosionRadius = EXPLOSION_RADIUS;
                if (this.isPowered()) {
                    finalExplosionRadius = POWERED_EXPLOSION_RADIUS;
                }

                this.level().explode(this, this.getX(), this.getY(), this.getZ(), finalExplosionRadius, Level.ExplosionInteraction.NONE);
                this.discard();
            }
        }
        super.tick();
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        super.thunderHit(serverLevel, lightningBolt);
        this.entityData.set(DATA_IS_POWERED, true);
    }

    @Override
    public void handleEntityEvent(byte event) {
        switch (event) {
            case TRIGGER_IDLE_ANIMATION_STATE:
                this.idleAnimationState.start(this.tickCount);
                break;
            case TRIGGER_WALKING_ANIMATION_STATE:
                this.walkingAnimationState.start(this.tickCount);
                break;
            case TRIGGER_RUNNING_ANIMATION_STATE:
                this.runningAnimationState.start(this.tickCount);
                break;
            case TRIGGER_SUMMON_ANIMATION_STATE:
                this.summonAnimationState.start(this.tickCount);
                break;
            case TRIGGER_DETONATION_ANIMATION_STATE:
                this.detonationAnimationState.start(this.tickCount);
                break;
        }

        super.handleEntityEvent(event);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
//        this.goalSelector.addGoal(1, new FollowMobGoal(this, this.getTarget(), 1.0f));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0f, 60, false));
        this.goalSelector.addGoal(1, new FollowMobGoal(this, 2.0, 0.5F, 16.0F));
        this.targetSelector.addGoal(1, new NearestNonSummonerAttackableTargetGoal<>(this, Monster.class, true));
    }

    public boolean isDefused() {
        if (this.hasCustomName()) {
            return DEFUSED_CUSTOM_NAME.equals(this.getCustomName().getString());
        }
        return false;
    }

    public State getState() {
        return this.entityData.get(STATE);
    }

    public boolean isState(CopperCreepEntity.State ballistaGolemState) {
        return this.getState().equals(ballistaGolemState);
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

//    private boolean stateChanged() {
//        boolean hasStateChanged = this.currentState != lastState;
//        if (hasStateChanged) {
//            this.lastState = this.currentState;
//        }
//
//        return hasStateChanged;
//    }

    private void setState(State state) {
        this.currentState = state;
        this.entityData.set(STATE, state);
    }

//
//    private class MoveDirectlyTowardsTargetGoal extends Goal {
//        private static final float AT_TARGET_STOP_DISTANCE = 0.5f;
//        private final PathfinderMob mob;
//        private final double speedModifier;
//        private LivingEntity target;
//
//        public MoveDirectlyTowardsTargetGoal(PathfinderMob mob, LivingEntity target, double speedModifier) {
//            this.mob = mob;
//            this.target = target;
//            this.speedModifier = speedModifier;
//        }
//
//        @Override
//        public boolean canUse() {
//            // Make sure the target is not null
//            if (this.target == null) {
//                this.target = this.mob.getTarget(); // Try to set the target dynamically here
//            }
//
//            if (this.mob.getAttribute(Attributes.FOLLOW_RANGE))
//
//            // Now check if the target exists and if it's far enough to move
//            return this.target != null && this.mob.distanceTo(this.target) > AT_TARGET_STOP_DISTANCE;
//        }
//
//        @Override
//        public void tick() {
//            // Move the mob directly towards the target
//            this.mob.getNavigation().moveTo(this.target.getX(), this.target.getY(), this.target.getZ(), this.speedModifier);
//        }
//    }

    private class NearestNonSummonerAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final CopperCreepEntity mob;

        public NearestNonSummonerAttackableTargetGoal(CopperCreepEntity mob, Class<T> targetClass, boolean mustSee) {
            super(mob, targetClass, mustSee);
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            // First, check if there is a valid target that meets the conditions of NearestAttackableTargetGoal
            boolean canUse = super.canUse();

            // Check if the mob has a summoner UUID set
            Optional<UUID> summonerUUID = this.mob.getSummonerUUID();
            if (summonerUUID.isPresent()) {
                // Get the summoner Player from the UUID
                Player summoner = this.mob.level().getPlayerByUUID(summonerUUID.get());
                if (summoner != null && this.target != null && this.target.equals(summoner)) {
                    // If the target is the summoner, return false
                    return false;
                }
            }

            return canUse;
        }
    }
}
