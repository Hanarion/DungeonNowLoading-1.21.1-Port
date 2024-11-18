package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.entity.client.animation.CopperCreepAnimation;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CommandPylonAnimation;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

public class CommandPylonEntity extends Mob{
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
    private static final EntityDataAccessor<Boolean> DATA_PLAYED_SETUP_ANIMATION = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.BOOLEAN);
    private int aiTick;
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

    //    @Override
//    public boolean ) {
//        return false;
//    }

//    @Override
//    public Vec3 getDeltaMovement() {
//        return Vec3.ZERO;
//    }
//
//    @Override
//    public void setDeltaMovement(Vec3 vec3) {
//
//    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PLAYED_SETUP_ANIMATION, false);
        this.entityData.define(DATA_CAN_RENDER, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.entityData.set(DATA_PLAYED_SETUP_ANIMATION, compoundTag.getBoolean("playedSetupAnimation"));
        this.entityData.set(DATA_CAN_RENDER, compoundTag.getBoolean("canRender"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("playedSetupAnimation", this.playedSetupAnimation());
        compoundTag.putBoolean("canRender", this.canRender());
    }

    @Override
    public boolean alwaysAccepts() {
        return super.alwaysAccepts();
    }

    @Override
    public void tick() {
        if (this.aiTick == 0 && !this.playedSetupAnimation()) {
            this.currentState = State.SETUP;
            this.setupAnimState.start(this.tickCount);
            this.entityData.set(DATA_CAN_RENDER, true);
            this.entityData.set(DATA_PLAYED_SETUP_ANIMATION, true);
        }
        if (this.aiTick == (int) CommandPylonAnimation.SETUP.lengthInSeconds() * 20) {
            this.currentState = State.IDLE;
            this.setupAnimState.stop();
            this.idleAnimState.start(this.tickCount);
        }

        this.aiTick++;
        super.tick();
    }

    public boolean canRender() {
        return this.entityData.get(DATA_CAN_RENDER);
    }

    private boolean playedSetupAnimation() {
        return this.entityData.get(DATA_PLAYED_SETUP_ANIMATION);
    }
}
