package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.entity.ai.EntityBodyRotationControl;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CopperCreepAnimation;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CommandPylonAnimation;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CommandPylonEntity extends Mob {
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

    private static final EntityDataAccessor<Boolean> DATA_CAN_RENDER = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.INT);
    private State currentState;

    public CommandPylonEntity(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D);
//                .add(Attributes.FOLLOW_RANGE, 16.0F),
//                .add(Attributes.MOVEMENT_SPEED, 0.175F);
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_CAN_RENDER, compoundTag.getBoolean("canRender"));
        this.entityData.set(DATA_AGE, compoundTag.getInt("age"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("canRender", this.canRender());
        compoundTag.putInt("age", this.getAge());
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (this.level().isClientSide()) {
            return InteractionResult.PASS;
        }

        if (!itemStack.isEmpty()) {
            return InteractionResult.FAIL;
        }

        ItemStack itemStackToGivePlayer = new ItemStack(DNLItems.COMMAND_PYLON.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackToGivePlayer);
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

//    @Override
//    public void die(DamageSource $$0) {
//        System.out.println("dieded lolxd");
//        this.discard();
//    }
//
//    @Override
//    public void handleDamageEvent(DamageSource $$0) {
//        super.handleDamageEvent($$0);
//    }

    @Override
    public void tick() {
        if (this.getAge() == 0) {
            this.currentState = State.SETUP;
            this.setupAnimState.start(this.tickCount);
            this.entityData.set(DATA_CAN_RENDER, true);
        } else if (this.getAge() == (int) (CommandPylonAnimation.SETUP.lengthInSeconds() * 20)) {
            this.currentState = State.IDLE;
            this.setupAnimState.stop();
            this.idleAnimState.start(this.tickCount);
        }

        super.tick();
    }

    public boolean canRender() {
        return this.entityData.get(DATA_CAN_RENDER);
    }

    public int getAge() {
        return this.entityData.get(DATA_AGE);
    }
}
