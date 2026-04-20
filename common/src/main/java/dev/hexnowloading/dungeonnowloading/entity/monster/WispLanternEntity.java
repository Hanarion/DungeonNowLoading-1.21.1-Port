package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.WispLanternAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.HoveringFlyingMoveControl;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WispLanternEntity extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> SUMMONING_WISP = SynchedEntityData.defineId(WispLanternEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LOOK_AROUND_TRIGGER = SynchedEntityData.defineId(WispLanternEntity.class, EntityDataSerializers.INT);
    private static final int MIN_LOOK_AROUND_DELAY = 80;
    private static final int MAX_LOOK_AROUND_DELAY = 180;

    public final AnimationState summonWispAnimationState = new AnimationState();
    public final AnimationState lookAroundAnimationState = new AnimationState();

    private int lookAroundCooldown;

    public WispLanternEntity(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
        this.moveControl = new HoveringFlyingMoveControl(this);
        this.setNoGravity(true);
        //this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FLYING_SPEED, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WispLanternAttackGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, this::hasClearTargetLine));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SUMMONING_WISP, false);
        this.entityData.define(LOOK_AROUND_TRIGGER, 0);
    }

    public boolean isSummoningWisp() {
        return this.entityData.get(SUMMONING_WISP);
    }

    public void setSummoningWisp(boolean summoningWisp) {
        this.entityData.set(SUMMONING_WISP, summoningWisp);
        if (!this.level().isClientSide) {
            if (summoningWisp) {
                this.lookAroundAnimationState.stop();
                this.summonWispAnimationState.start(this.tickCount);
            } else {
                this.summonWispAnimationState.stop();
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (SUMMONING_WISP.equals(entityDataAccessor)) {
            if (this.isSummoningWisp()) {
                this.lookAroundAnimationState.stop();
                this.summonWispAnimationState.start(this.tickCount);
            } else {
                this.summonWispAnimationState.stop();
            }
        } else if (LOOK_AROUND_TRIGGER.equals(entityDataAccessor) && this.entityData.get(LOOK_AROUND_TRIGGER) > 0) {
            this.lookAroundAnimationState.start(this.tickCount);
        }

        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            return;
        }

        if (this.isInWaterOrBubble()) {
            this.hurt(this.damageSources().drown(), 1.0F);
        }

        if (this.getTarget() != null || this.isSummoningWisp()) {
            this.lookAroundCooldown = this.nextLookAroundDelay();
            return;
        }

        if (this.lookAroundCooldown <= 0) {
            this.entityData.set(LOOK_AROUND_TRIGGER, this.entityData.get(LOOK_AROUND_TRIGGER) + 1);
            this.lookAroundCooldown = this.nextLookAroundDelay();
            return;
        }

        this.lookAroundCooldown--;
    }

    private int nextLookAroundDelay() {
        return MIN_LOOK_AROUND_DELAY + this.random.nextInt(MAX_LOOK_AROUND_DELAY - MIN_LOOK_AROUND_DELAY + 1);
    }

    public boolean hasClearTargetLine(LivingEntity target) {
        Vec3 from = this.getEyePosition();
        Vec3 to = target.getEyePosition();
        HitResult hitResult = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, this));
        return hitResult.getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {}

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }
}
