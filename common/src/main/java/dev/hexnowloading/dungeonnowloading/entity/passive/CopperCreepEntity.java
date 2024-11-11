package dev.hexnowloading.dungeonnowloading.entity.passive;

import dev.hexnowloading.dungeonnowloading.entity.util.PlayerSupporterEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class CopperCreepEntity extends PathfinderMob implements PlayerSupporterEntity {
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState walkingAnimationState = new AnimationState();
    public AnimationState runningAnimationState = new AnimationState();
    public AnimationState detonationAnimationState = new AnimationState();

    private static final EntityDataAccessor<Optional<UUID>> SUMMONER_UUID = SynchedEntityData.defineId(CopperCreepEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private int swell = 0;
    private int maxSwell = 30;
    private boolean isIgnited = false;
    private float explosionRadius = 2.0f;
    private Player summoner;

    private final byte TRIGGER_IDLE_ANIMATION_STATE = 70;
    private final byte TRIGGER_WALKING_ANIMATION_STATE = 71;
    private final byte TRIGGER_RUNNING_ANIMATION_STATE = 72;
    private final byte TRIGGER_DETONATION_ANIMATION_STATE = 73;

    public CopperCreepEntity(EntityType<? extends CopperCreepEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;

        this.summoner = null;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.35F);
    }

    public void ignite() {
        this.isIgnited = true;
    }

    public float getSwelling(float f) {
        if (this.maxSwell == 0) {
            return 0;
        }
        return Mth.clamp((float) this.swell / (float) this.maxSwell, 0.0F, 1.0F);
    }

    // Set the Summoner's UUID
    public void setSummonerUUID(UUID summonerUUID) {
        this.entityData.set(SUMMONER_UUID, Optional.of(summonerUUID));
    }

    // Get the Summoner's UUID, returning an Optional
    public Optional<UUID> getSummonerUUID() {
        return this.entityData.get(SUMMONER_UUID);
    }

    // Define Synched Data (This is the part where we define what data to sync)
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SUMMONER_UUID, Optional.empty()); // Initially, no UUID is set
    }

    // Add custom save data (write the UUID to NBT)
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        // Save the UUID to NBT
        this.getSummonerUUID().ifPresent(uuid -> compoundTag.putUUID("summonerUUID", uuid));
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
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, MobSpawnType $$2, @Nullable SpawnGroupData $$3, @Nullable CompoundTag $$4) {
        triggerIdleAnimation();
        return super.finalizeSpawn($$0, $$1, $$2, $$3, $$4);
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

    @Override
    public void tick() {
        if (this.isIgnited) {
            if (this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 1.0F);
            }

            if (this.swell < this.maxSwell) {
                this.swell++;
            } else if (!this.level().isClientSide) {
                this.dead = true;
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.explosionRadius, Level.ExplosionInteraction.NONE);
                this.discard();
            }
        }
        super.tick();
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
            case TRIGGER_DETONATION_ANIMATION_STATE:
                this.detonationAnimationState.start(this.tickCount);
                break;
        }

        super.handleEntityEvent(event);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MoveDirectlyTowardsTargetGoal(this, this.getTarget(), 1.0f));
        this.targetSelector.addGoal(2, new NearestNonSummonerAttackableTargetGoal<>(this, Monster.class, true));
    }

    private void triggerIdleAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_IDLE_ANIMATION_STATE); }
    private void triggerWalkingAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_WALKING_ANIMATION_STATE); }
    private void triggerRunningAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_RUNNING_ANIMATION_STATE); }
    private void triggerDetonationAnimation() { this.level().broadcastEntityEvent(this, TRIGGER_DETONATION_ANIMATION_STATE); }

    private class MoveDirectlyTowardsTargetGoal extends Goal {
        private static final float AT_TARGET_STOP_DISTANCE = 0.5f;
        private final PathfinderMob mob;
        private LivingEntity target;
        private final double speedModifier;

        public MoveDirectlyTowardsTargetGoal(PathfinderMob mob, LivingEntity target, double speedModifier) {
            this.mob = mob;
            this.target = target;
            this.speedModifier = speedModifier;
        }

        @Override
        public boolean canUse() {
            // Make sure the target is not null
            if (this.target == null) {
                this.target = this.mob.getTarget(); // Try to set the target dynamically here
            }

            // Now check if the target exists and if it's far enough to move
            return this.target != null && this.mob.distanceTo(this.target) > AT_TARGET_STOP_DISTANCE;
        }

        @Override
        public void tick() {
            // Move the mob directly towards the target
            this.mob.getNavigation().moveTo(this.target.getX(), this.target.getY(), this.target.getZ(), this.speedModifier);
        }
    }

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
