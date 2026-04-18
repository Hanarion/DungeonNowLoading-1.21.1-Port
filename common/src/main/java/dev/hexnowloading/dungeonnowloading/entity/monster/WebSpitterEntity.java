package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.WebSpitterRangedAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.WebSpitterRetreatGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.WebSpitterMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.WebSpitterAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.projectile.WebWebProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class WebSpitterEntity extends Spider implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> ANCHORED = SynchedEntityData.defineId(WebSpitterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BACKING_UP = SynchedEntityData.defineId(WebSpitterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<WebSpitterAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(WebSpitterEntity.class, EntityStates.WEB_SPITTER_ANIMATION_STATE);

    private AnimationChainer<WebSpitterAnimationState> animationChainer = new AnimationChainer<>();

    public final AnimationState shootAnimationState = new AnimationState();

    private WebSpitterRangedAttackGoal rangedGoal;
    private WebSpitterRetreatGoal retreatGoal;

    private static final double ANCHORED_BREAK_RANGE = 10.0D; // blocks

    public float clientWalkBackBlend = 0.0F;
    public float clientWalkBackBlendO = 0.0F;
    private float walkBackTarget = 0.0F;

    private static final float WALK_BACK_IN = 0.18F;
    private static final float WALK_BACK_OUT = 0.12F;

    public WebSpitterEntity(EntityType<? extends WebSpitterEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new WebSpitterMoveControl(this, 0.6D, 0.7d);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896)
                .add(Attributes.ATTACK_DAMAGE, 6.0F);
    }

    // --- Anchored state sync ---

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANCHORED, Boolean.FALSE);
        this.entityData.define(BACKING_UP, Boolean.FALSE);
        this.entityData.define(ANIMATION_STATE, WebSpitterAnimationState.IDLE);
    }

    public boolean isAnchored() {
        return this.entityData.get(ANCHORED);
    }

    public void setAnchored(boolean anchored) {
        this.entityData.set(ANCHORED, anchored);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Anchored", this.isAnchored());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAnchored(tag.getBoolean("Anchored"));
    }

    // Optional: randomly decide anchored state on spawn
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        // e.g. 40% chance to spawn as anchored turret
        return data;
    }

    // --- Goals ---

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        float minDistance = 7.0F;
        float maxDistance = 16.0F;
        double moveSpeed = 1.0D;

        this.retreatGoal = new WebSpitterRetreatGoal(this, moveSpeed, minDistance);
        this.rangedGoal = new WebSpitterRangedAttackGoal(this, moveSpeed, 40, minDistance, maxDistance);

        this.goalSelector.addGoal(2, this.retreatGoal);
        this.goalSelector.addGoal(3, this.rangedGoal);

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Fixes spider climbing when backing up.
     *
     * Vanilla spiders only become climbable *one tick after* hitting a wall,
     * because `isClimbing` updates after movement. When backing up, that delay
     * prevents climbing from ever triggering.
     *
     * By including `horizontalCollision` here, we allow the spider to climb
     * immediately when pushing into a wall — including while reversing.
     */

    @Override
    public boolean onClimbable() {
        boolean vanilla = super.onClimbable();
        return vanilla || this.horizontalCollision;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.walkBackAnimation();
            return;
        }
        this.animationChainer.tick(this::transitionTo);
    }

    public void walkBackAnimation() {
        this.clientWalkBackBlendO = this.clientWalkBackBlend;

        this.walkBackTarget = this.isBackingUp() ? 1.0F : 0.0F;

        float rate = (this.walkBackTarget > this.clientWalkBackBlend) ? WALK_BACK_IN : WALK_BACK_OUT;
        this.clientWalkBackBlend += (this.walkBackTarget - this.clientWalkBackBlend) * rate;
        this.clientWalkBackBlend = Mth.clamp(this.clientWalkBackBlend, 0.0F, 1.0F);
    }

    public void playShootAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(WebSpitterAnimationState.SHOOT, WebSpitterAnimationDuration.SHOOT, null, () -> this.entityData.set(ANIMATION_STATE, WebSpitterAnimationState.IDLE)));
    }


    // --- Ranged attack implementation ---

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.level().isClientSide) return;

        this.playSound(DNLSounds.WEB_SPITTER_SHOOT.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);

        WebWebProjectileEntity projectile = new WebWebProjectileEntity(this.level(), this);
        projectile.setPos(this.getX(), this.getEyeY() - 0.2F, this.getZ());
        projectile.setOwner(this);

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dHorizontal = Math.sqrt(dx * dx + dz * dz);

        // --- HERE: aim 1 block higher than before ---
        // was: target.getY(0.333D)
        double targetY = target.getY(0.333D); // tweak +1.0D to +0.8D or +1.2D if needed
        double dy = targetY - projectile.getY();

        if (dHorizontal < 0.001D) {
            projectile.shoot(dx, dy, dz, 0.8F, 0.0F);
            this.level().addFreshEntity(projectile);
            return;
        }

        float speed = 1.1F;
        double v = speed;
        double g = WebWebProjectileEntity.GRAVITY;

        double v2 = v * v;
        double d = dHorizontal;
        double y = dy;

        double underSqrt = v2 * v2 - g * (g * d * d + 2.0D * y * v2);

        if (underSqrt < 0.0D) {
            // fallback: straight-ish shot
            projectile.shoot(dx, dy, dz, speed, 0.0F);
        } else {
            double sqrt = Math.sqrt(underSqrt);

            // high arc
            double tanTheta = (v2 - sqrt) / (g * d);
            double cosTheta = 1.0D / Math.sqrt(1.0D + tanTheta * tanTheta);
            double sinTheta = tanTheta * cosTheta;

            double dirX = dx / d;
            double dirZ = dz / d;

            double nx = cosTheta * dirX;
            double ny = sinTheta;
            double nz = cosTheta * dirZ;

            projectile.shoot(nx, ny, nz, speed, 0.0F);
        }

        this.level().addFreshEntity(projectile);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (!result) return false;

        Entity rawAttacker = source.getEntity();
        if (rawAttacker instanceof LivingEntity attacker) {

            // Only flee if the hit was strong enough
            if (amount > 5.0F) {
                double distSq = this.distanceToSqr(attacker);

                // Only flee if attacker is too close
                if (distSq < rangedGoal.getMinAttackDistanceSq()) {
                    retreatGoal.requestRetreat();
                }
            }
        }
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DNLSounds.WEB_SPITTER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DNLSounds.WEB_SPITTER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.WEB_SPITTER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(DNLSounds.WEB_SPITTER_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            WebSpitterAnimationState animationState = this.getAnimationState();
            this.resetAnimation();
            switch (animationState) {
                case SHOOT -> this.shootAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public WebSpitterEntity transitionTo(WebSpitterAnimationState animationState) {
        switch (animationState) {
            case IDLE:
                this.entityData.set(ANIMATION_STATE, WebSpitterAnimationState.IDLE);
                break;
            case SHOOT:
                this.entityData.set(ANIMATION_STATE, WebSpitterAnimationState.SHOOT);
                break;
        }
        return this;
    }

    private void resetAnimation() {
        this.shootAnimationState.stop();
    }

    public boolean shouldBreakAnchorTo(LivingEntity target) {
        return this.distanceToSqr(target) <= ANCHORED_BREAK_RANGE * ANCHORED_BREAK_RANGE;
    }

    public boolean isBackingUp() {
        return this.entityData.get(BACKING_UP);
    }

    public void setBackingUp(boolean backingUp) {
        this.entityData.set(BACKING_UP, backingUp);
    }

    public WebSpitterAnimationState getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public enum WebSpitterAnimationState {
        IDLE,
        SHOOT
    }
}
