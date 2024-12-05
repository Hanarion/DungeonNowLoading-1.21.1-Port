package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.entity.ai.EntityBodyRotationControl;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CommandPylonAnimation;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandPylonEntity extends Mob {
    public float getAntennaRotationSpeed() {
        return entityData.get(DATA_ANTENNA_ROTATION_SPEED);
    }

    public void setAntennaRotationSpeed(float rotationSpeed) {
        entityData.set(DATA_ANTENNA_ROTATION_SPEED, rotationSpeed);
    }

    public float getGearRotationSpeed() {
        return entityData.get(DATA_GEAR_ROTATION_SPEED);
    }

    public void setGearRotationSpeed(float rotationSpeed) {
        entityData.set(DATA_GEAR_ROTATION_SPEED, rotationSpeed);
    }

    public float getShieldHealth() {
        return entityData.get(DATA_SHIELD_HEALTH);
    }

    public enum State {
        SETUP,
        IDLE,
        BASE_DOWN,
        BASE_UP
    }

    public AnimationState setupAnimState = new AnimationState();
    public AnimationState idleAnimState = new AnimationState();
    public AnimationState baseDownAnimState = new AnimationState();
    public AnimationState baseUpAnimState = new AnimationState();

    public static final float SHIELD_MAX_HEALTH = 540.0f;
    public static final float SETUP_DURATION_TICKS = (int) (CommandPylonAnimation.SETUP.lengthInSeconds() * 20);
    private static final float BASE_ANTENNA_ROTATION_SPEED = 0.05f;
    private static final float BASE_GEAR_ROTATION_SPEED = 0.05f;
    private static final float SHIELD_PROJECTILE_DAMAGE = 10.0f;
    private static final EntityDataAccessor<Boolean> DATA_CAN_RENDER = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SHIELD_HEALTH = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ANTENNA_ROTATION_SPEED = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_GEAR_ROTATION_SPEED = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(CommandPylonEntity.class, EntityStates.COMMAND_PYLON_STATE);

    public CommandPylonEntity(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D);
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CAN_RENDER, false);
        this.entityData.define(DATA_AGE, 0);
        this.entityData.define(DATA_SHIELD_HEALTH, SHIELD_MAX_HEALTH);
        this.entityData.define(DATA_STATE, State.SETUP);
        this.entityData.define(DATA_ANTENNA_ROTATION_SPEED, BASE_ANTENNA_ROTATION_SPEED);
        this.entityData.define(DATA_GEAR_ROTATION_SPEED, BASE_GEAR_ROTATION_SPEED);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_CAN_RENDER, compoundTag.getBoolean("canRender"));
        this.entityData.set(DATA_AGE, compoundTag.getInt("age"));
        this.entityData.set(DATA_SHIELD_HEALTH, compoundTag.getFloat("shieldHealth"));
        this.entityData.set(DATA_ANTENNA_ROTATION_SPEED, compoundTag.getFloat("antennaRotationSpeed"));
        this.entityData.set(DATA_GEAR_ROTATION_SPEED, compoundTag.getFloat("gearRotationSpeed"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("canRender", this.canRender());
        compoundTag.putInt("age", this.getAge());
        compoundTag.putFloat("shieldHealth", this.entityData.get(DATA_SHIELD_HEALTH));
        compoundTag.putFloat("antennaRotationSpeed", this.entityData.get(DATA_ANTENNA_ROTATION_SPEED));
        compoundTag.putFloat("gearRotationSpeed", this.entityData.get(DATA_GEAR_ROTATION_SPEED));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (this.level().isClientSide()) {
            return InteractionResult.PASS;
        }
        if (!itemStack.isEmpty() || interactionHand.equals(InteractionHand.OFF_HAND)) {
            return InteractionResult.FAIL;
        }

        ItemStack itemStackToGivePlayer = new ItemStack(DNLItems.COMMAND_PYLON.get());
        player.setItemInHand(interactionHand, itemStackToGivePlayer);
        this.discard();
        return InteractionResult.sidedSuccess(this.level().isClientSide);
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
    public void customServerAiStep() {
        this.entityData.set(DATA_AGE, this.getAge() + 1);
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
            this.dropItem((Entity)null);
        }
    }

    public void dropItem(@Nullable Entity entity) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.getAbilities().instabuild) {
                    return;
                }
            }

            this.spawnAtLocation(DNLItems.COMMAND_PYLON.get());
        }
    }

    @Override
    public void tick() {
        if (this.getAge() == 0) {
            this.setupAnimState.start(this.tickCount);
            this.entityData.set(DATA_CAN_RENDER, true);
        } else if (this.getAge() == (int) (CommandPylonAnimation.SETUP.lengthInSeconds() * 20)) {
            this.setupAnimState.stop();
            this.idleAnimState.start(this.tickCount);
        }

        if (!this.level().isClientSide) {
            for (Entity entity : this.getNearbyProjectiles()) {
                float currentShieldHealth = this.entityData.get(DATA_SHIELD_HEALTH);
                this.entityData.set(DATA_SHIELD_HEALTH, currentShieldHealth - SHIELD_PROJECTILE_DAMAGE);

                // TODO: make it fancier than just discarding the projectiles
                entity.discard();

                if (currentShieldHealth - SHIELD_PROJECTILE_DAMAGE <= 0.0f) {
                    this.dropItem(null);
                    this.discard();
                }
            }
        }

        super.tick();
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
                (Entity) null, detectionBox, entity -> entity instanceof Projectile
        );
    }

    public boolean canRender() {
        return this.entityData.get(DATA_CAN_RENDER);
    }

    public int getAge() {
        return this.entityData.get(DATA_AGE);
    }
}
