package dev.hexnowloading.dungeonnowloading.entity.passive;

import dev.hexnowloading.dungeonnowloading.config.PvpConfig;
import dev.hexnowloading.dungeonnowloading.entity.ai.WhimperChargeAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.WhimperMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.ai.WhimperRandomMoveGoal;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.util.OverworkedPenaltyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class WhimperEntity extends PathfinderMob implements OwnableEntity {

    private static final EntityDataAccessor<Integer> DESPAWN_TICK = SynchedEntityData.defineId(WhimperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(WhimperEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(WhimperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GIGANTIC = SynchedEntityData.defineId(WhimperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> OVERWORKED_LEVEL = SynchedEntityData.defineId(WhimperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<WhimperAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(WhimperEntity.class, EntityStates.WHIMPER_ANIMATION_STATE);
    private static final EntityDataAccessor<Skin> SKIN = SynchedEntityData.defineId(WhimperEntity.class, EntityStates.WHIMPER_SKIN);
    private static final EntityDataAccessor<Boolean> SKIN_VALIDATION = SynchedEntityData.defineId(WhimperEntity.class, EntityDataSerializers.BOOLEAN);

    private static final java.util.UUID GIGANTISM_MAX_HEALTH_MODIFIER_ID = java.util.UUID.nameUUIDFromBytes("dnl_gigantism_max_health".getBytes());

    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState blessingAnimationState = new AnimationState();
    public AnimationState idleBreakAnimationState = new AnimationState();
    public AnimationState idleBreakLanternAnimationState = new AnimationState();

    private AnimationChainer<WhimperAnimationState> animationChainer = new AnimationChainer<>();

    public WhimperEntity(EntityType<? extends WhimperEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
        this.moveControl = new WhimperMoveControl(this);
        this.lookControl = new LookControl(this);
        this.noPhysics = true;
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.FOLLOW_RANGE, 48.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WhimperChargeAttackGoal(this));
        this.goalSelector.addGoal(6, new WhimperRandomMoveGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Mob.class, 5, false, false,
                mob -> PvpConfig.TOGGLE_PVP_MODE.get()
                        && mob instanceof OwnableEntity
                        && !isAlliedTo(mob)
        ));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, Player.class, 5, true, false,
                player -> PvpConfig.TOGGLE_PVP_MODE.get()
                        && !isOwner((Player) player)
        ));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, Mob.class, 5, false, false,
                mob -> mob instanceof Enemy && !isAlliedTo(mob)
        ));
    }

    private boolean isOwner(Player player) {
        UUID owner = this.getOwnerUUID();
        return owner != null && owner.equals(player.getUUID());
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        UUID myOwner = this.getOwnerUUID();
        if (myOwner == null) return false; // or true to be ultra-conservative during first ticks

        if (other instanceof Player p) {
            return myOwner.equals(p.getUUID());
        }
        if (other instanceof OwnableEntity ownable) {
            UUID theirOwner = ownable.getOwnerUUID();
            return theirOwner != null && myOwner.equals(theirOwner);
        }
        return super.isAlliedTo(other);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DESPAWN_TICK, 600);
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(CHARGING, false);
        this.entityData.define(GIGANTIC, false);
        this.entityData.define(OVERWORKED_LEVEL, 0);
        this.entityData.define(ANIMATION_STATE, WhimperAnimationState.IDLE);
        this.entityData.define(SKIN, Skin.DEFAULT);
        this.entityData.define(SKIN_VALIDATION, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DespawnTicks", this.entityData.get(DESPAWN_TICK));
        if (this.getOwnerUUID() != null) {
            compoundTag.putUUID("Owner", this.getOwnerUUID());
        }
        compoundTag.putBoolean("Gigantic", this.isGigantic());
        compoundTag.putInt("OverworkedLevel", this.getOverworkedLevel());
        compoundTag.putString("Skin", getSkin().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DESPAWN_TICK, compoundTag.getInt("DespawnTicks"));
        UUID uuid;
        if (compoundTag.hasUUID("Owner")) {
            uuid = compoundTag.getUUID("Owner");
        } else {
            String string = compoundTag.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), string);
        }
        if (uuid != null) {
            this.setOwnerUUID(uuid);
        }
        if (compoundTag.contains("Gigantic")) {
            this.setGigantic(compoundTag.getBoolean("Gigantic"));
        }
        if (compoundTag.contains("OverworkedLevel")) {
            this.setOverworkedLevel(compoundTag.getInt("OverworkedLevel"));
        }
        if (compoundTag.contains("skin") && !this.entityData.get(SKIN_VALIDATION)) {
            this.entityData.set(SKIN, WhimperEntity.Skin.fromId(compoundTag.getString("skin")));
        }

        // Ensure attributes are consistent after loading.
        this.applyGigantismHealthBonus();
        this.applyOverworkedAttackSpeedBonus();
    }

    @Override
    public void tick() {
        super.tick();

        animationChainer.tick(this::transitionTo);
    }

    @Override
    protected void customServerAiStep() {
        if (this.getDespawnTick() > 0) {
            int despawnTick = this.getDespawnTick() - 1;
            if (despawnTick <= 0) {
                this.discardWithParticle();
            }
            setDespawnTick(despawnTick);
        }
        super.customServerAiStep();
    }

    private void discardWithParticle() {
        if (!this.level().isClientSide) {
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.0D);
        }
        this.discard();
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions base = super.getDimensions(pose);
        return this.isGigantic() ? base.scale(2.0F) : base;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        // Scale eye height with current bounding box height for consistent visuals
        return size.height * 0.8F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return DNLSounds.WHIMPER_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return DNLSounds.WHIMPER_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.WHIMPER_DEATH.get();
    }

    public int getDespawnTick() { return this.entityData.get(DESPAWN_TICK); }

    public void setDespawnTick(int tick) { this.entityData.set(DESPAWN_TICK, tick); }

    public UUID getOwnerUUID() { return (UUID) ((Optional) this.entityData.get(OWNER_UUID)).orElse((Object) null); }

    public void setOwnerUUID(UUID uuid) { this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid)); }

    public boolean IsCharging() { return this.entityData.get(CHARGING); }

    public void setCharging(boolean b) { this.entityData.set(CHARGING, b); }

    public boolean isGigantic() {
        return this.entityData.get(GIGANTIC);
    }

    public void setGigantic(boolean gigantic) {
        this.entityData.set(GIGANTIC, gigantic);
        this.refreshDimensions();
        this.applyGigantismHealthBonus();
    }

    public int getOverworkedLevel() {
        return this.entityData.get(OVERWORKED_LEVEL);
    }

    public void setOverworkedLevel(int level) {
        int clamped = Math.max(0, Math.min(5, level));
        this.entityData.set(OVERWORKED_LEVEL, clamped);
        this.applyOverworkedAttackSpeedBonus();
    }

    public boolean isOverworked() {
        return this.getOverworkedLevel() > 0;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (ANIMATION_STATE.equals(key)) {
            this.resetAnimations();
            WhimperAnimationState state = this.entityData.get(ANIMATION_STATE);
            switch (state) {
                case ATTACK -> this.attackAnimationState.start(this.tickCount);
                case BLESSING -> this.blessingAnimationState.start(this.tickCount);
                case IDLE_BREAK -> this.idleBreakAnimationState.start(this.tickCount);
                case IDLE_BREAK_LANTERN -> this.idleBreakLanternAnimationState.start(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(key);
        if (GIGANTIC.equals(key)) {
            this.refreshDimensions();
            // Server owns attributes, but this is harmless if called client-side.
            this.applyGigantismHealthBonus();
        }
    }

    private void applyGigantismHealthBonus() {
        // Attributes are authoritative server-side.
        if (this.level() != null && this.level().isClientSide) {
            return;
        }

        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;

        double beforeMax = this.getMaxHealth();

        AttributeModifier existing = maxHealth.getModifier(GIGANTISM_MAX_HEALTH_MODIFIER_ID);
        if (existing != null) {
            maxHealth.removeModifier(existing);
        }

        if (this.isGigantic()) {
            // +50% max health. Use a transient modifier so it takes effect immediately after spawn and doesn't depend on NBT.
            maxHealth.addTransientModifier(new AttributeModifier(
                    GIGANTISM_MAX_HEALTH_MODIFIER_ID,
                    "dnl_gigantism_max_health",
                    0.5D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        double afterMax = this.getMaxHealth();
        float gain = (float) (afterMax - beforeMax);

        // If gigantism increased max HP, heal by that gained amount (so it actually becomes harder to kill).
        if (gain > 0.0F) {
            this.setHealth(Math.min((float) afterMax, this.getHealth() + gain));
        } else {
            // Otherwise clamp to max.
            if (this.getHealth() > afterMax) {
                this.setHealth((float) afterMax);
            }
        }
    }

    // DNL_OVERWORKED_PATCH_BEGIN
    public void applyOverworkedAttackSpeedBonus() {
        int level = this.getOverworkedLevel();
        AttributeInstance attackSpeed = this.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            UUID modifierId = UUID.nameUUIDFromBytes("dnl_overworked_attack_speed".getBytes());
            if (attackSpeed.getModifier(modifierId) != null) {
                attackSpeed.removeModifier(modifierId);
            }
            if (level > 0) {
                double bonus = 0.2D * level;
                attackSpeed.addPermanentModifier(new AttributeModifier(
                        modifierId,
                        "dnl_overworked_attack_speed",
                        bonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        UUID owner = this.getOwnerUUID();
        int overworkedLevel = this.getOverworkedLevel();
        super.remove(reason);

        if (!this.level().isClientSide && owner != null && overworkedLevel > 0) {
            OverworkedPenaltyUtil.refreshOwnerPenaltyIfPossible(this.level(), owner);
        }
    }
    // DNL_OVERWORKED_PATCH_END

    public void playAnimation(WhimperAnimationState state) {
        this.animationChainer.reset();

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                state,
                state.duration(),
                null,
                () -> this.transitionTo(WhimperAnimationState.IDLE)
        ));
    }

    public WhimperEntity transitionTo(WhimperAnimationState state) {
        this.entityData.set(ANIMATION_STATE, state);
        return this;
    }

    private void resetAnimations() {
        this.attackAnimationState.stop();
        this.blessingAnimationState.stop();
        this.idleBreakAnimationState.stop();
        this.idleBreakLanternAnimationState.stop();
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

    public enum WhimperAnimationState {
        IDLE(0.0F),
        ATTACK(0.875F),
        BLESSING(1.625F),
        IDLE_BREAK(2.25F),
        IDLE_BREAK_LANTERN(2.25F);

        private final float duration;

        WhimperAnimationState(float duration) {
            this.duration = duration;
        }

        public float duration() {
            return this.duration;
        }
    }

    public enum Skin {

        DEFAULT("default"),
        LANTERN("lantern");

        public final String name;

        Skin(String name) {
            this.name = name;
        }

        public String getId() {
            return this.name;
        }

        public static WhimperEntity.Skin fromId(String id) {
            for (WhimperEntity.Skin skin : values()) {
                if (skin.name.equalsIgnoreCase(id)) {
                    return skin;
                }
            }
            return DEFAULT;
        }
    }

}