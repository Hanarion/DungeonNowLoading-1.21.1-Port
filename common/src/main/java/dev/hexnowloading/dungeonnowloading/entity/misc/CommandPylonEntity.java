package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.entity.client.animation.CopperCreepAnimation;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CommandPylonAnimation;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CommandPylonEntity extends Entity {
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

    private int aiTick;

    private static final EntityDataAccessor<Boolean> DATA_PLAYED_SETUP_ANIMATION = SynchedEntityData.defineId(CommandPylonEntity.class, EntityDataSerializers.BOOLEAN);
    private State currentState;

    public CommandPylonEntity(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_PLAYED_SETUP_ANIMATION, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.entityData.set(DATA_PLAYED_SETUP_ANIMATION, compoundTag.getBoolean("playedSetupAnimation"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("playedSetupAnimation", this.playedSetupAnimation());
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
//            this.setState(CopperCreepEntity.State.SUMMONING);
//            this.triggerSummonAnimation();

            this.entityData.set(DATA_PLAYED_SETUP_ANIMATION, true);
        }
        if (this.aiTick == (int) CommandPylonAnimation.SETUP.lengthInSeconds() * 20) {
            this.currentState = State.IDLE;
            this.idleAnimState.start(this.tickCount);
//            this.setState(CopperCreepEntity.State.IDLE);
//            this.triggerIdleAnimation();
        }

        System.out.println("wtf: " + this.level().isClientSide() + " | " + aiTick);
        this.aiTick++;
        super.tick();

    }

    private boolean playedSetupAnimation() {
        return this.entityData.get(DATA_PLAYED_SETUP_ANIMATION);
    }
}
