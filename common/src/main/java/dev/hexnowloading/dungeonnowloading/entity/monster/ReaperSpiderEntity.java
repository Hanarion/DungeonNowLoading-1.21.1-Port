package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.ReaperSpiderAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.ReaperSpiderRecoveryGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.ReaperSpiderStalkGoal;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.ReaperSpiderAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.LocomotionLockable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ReaperSpiderEntity extends Spider implements LocomotionLockable {
    private static final double STALK_SPEED_MULTIPLIER = 0.7D;
    private static final double REVEAL_RANGE = 5.0D;
    private static final double TACKLE_MIN_RANGE = 4.0D;
    private static final double CAVE_SPIDER_SEARCH_RANGE = 16.0D;
    private static final double BLOCK_BREAK_SLASH_CENTER_OFFSET = 1.0D;
    private static final double BLOCK_BREAK_SLASH_HORIZONTAL_RADIUS = 1.9D;
    private static final double BLOCK_BREAK_SLASH_VERTICAL_RADIUS = 1.6D;
    private static final int PLAYER_STARE_REVEAL_TICKS = 5;
    private static final int POISON_DURATION_TICKS = 15 * 20;
    private static final int POISON_AMPLIFIER = 2;
    private static final int BLOCK_BREAK_COOLDOWN_TICKS = 10;
    private static final float REVEAL_FADE_STEP = 0.1F;

    private static final EntityDataAccessor<ReaperSpiderAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(ReaperSpiderEntity.class, EntityStates.REAPER_SPIDER_ANIMATION_STATE);
    private static final EntityDataAccessor<Boolean> REVEALED_STATE = SynchedEntityData.defineId(ReaperSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BACKING_UP = SynchedEntityData.defineId(ReaperSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RETREATING_STATE = SynchedEntityData.defineId(ReaperSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE = SynchedEntityData.defineId(ReaperSpiderEntity.class, EntityDataSerializers.INT);

    private AnimationChainer<ReaperSpiderAnimationState> animationChainer = new AnimationChainer<>();

    public final AnimationState windUpAnimationState = new AnimationState();
    public final AnimationState tacklingAnimationState = new AnimationState();
    public final AnimationState doubleSlashAnimationState = new AnimationState();
    public final AnimationState recoveryAnimationState = new AnimationState();
    public final AnimationState singleSlashAnimationState = new AnimationState();

    private boolean fastClimb;
    private boolean revealed = false;
    private int staredAtTicks;
    private int stalkingTargetId = -1;
    private int blockBreakCooldown;
    private boolean pendingRevealTackle;
    private boolean locomotionLocked;
    public float clientRevealAlpha = 1.0F;
    public float clientRevealAlphaO = 1.0F;
    private boolean clientLastRevealed = false;
    public float clientWalkBackBlend = 0.0F;
    public float clientWalkBackBlendO = 0.0F;
    private float walkBackTarget = 0.0F;

    public ReaperSpiderEntity(EntityType<? extends ReaperSpiderEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 19.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_STATE,  ReaperSpiderAnimationState.IDLE);
        this.entityData.define(REVEALED_STATE, false);
        this.entityData.define(BACKING_UP, false);
        this.entityData.define(RETREATING_STATE, false);
        this.entityData.define(BEHAVIOR_STATE, BehaviorState.WANDER.ordinal());
    }

    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ReaperSpiderRecoveryGoal(this));
        this.goalSelector.addGoal(2, new ReaperSpiderAttackGoal(this));
        this.goalSelector.addGoal(3, new ReaperSpiderStalkGoal(this));

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.tickClientRevealAlpha();
            this.walkBackAnimation();
            return;
        }
        this.updateEmergencyFeedingTarget();
        this.updateStealthState();
        this.tryBreakBlocksTowardTarget();
        this.animationChainer.tick(this::transitionTo);
    }

    private void tickClientRevealAlpha() {
        boolean revealed = this.isRevealed();
        if (revealed != this.clientLastRevealed) {
            this.clientLastRevealed = revealed;
            if (!revealed) {
                this.clientRevealAlpha = Math.min(this.clientRevealAlpha, 0.2F);
                this.clientRevealAlphaO = this.clientRevealAlpha;
            }
        }

        this.clientRevealAlphaO = this.clientRevealAlpha;
        float targetAlpha = revealed ? 1.0F : 0.0F;
        if (this.clientRevealAlpha < targetAlpha) {
            this.clientRevealAlpha = Math.min(targetAlpha, this.clientRevealAlpha + REVEAL_FADE_STEP);
        } else if (this.clientRevealAlpha > targetAlpha) {
            this.clientRevealAlpha = Math.max(targetAlpha, this.clientRevealAlpha - REVEAL_FADE_STEP);
        }
    }

    private void updateEmergencyFeedingTarget() {
        if (!this.shouldHuntCaveSpiders()) {
            if (this.getTarget() instanceof CaveSpider) {
                this.setTarget(null);
            }
            return;
        }

        if (this.getTarget() instanceof CaveSpider caveSpider && caveSpider.isAlive()) {
            return;
        }

        if (this.tickCount % 20 != 0) {
            return;
        }

        CaveSpider nearest = null;
        double bestDistance = Double.MAX_VALUE;

        for (CaveSpider caveSpider : this.level().getEntitiesOfClass(CaveSpider.class, this.getBoundingBox().inflate(CAVE_SPIDER_SEARCH_RANGE))) {
            if (!caveSpider.isAlive()) {
                continue;
            }

            double distance = this.distanceToSqr(caveSpider);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = caveSpider;
            }
        }

        if (nearest != null) {
            this.setTarget(nearest);
        }
    }

    private void updateStealthState() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            this.stalkingTargetId = -1;
            this.staredAtTicks = 0;
            this.setRevealedState(true);
            this.setBehaviorState(BehaviorState.WANDER);
            this.pendingRevealTackle = false;
            this.setInvisible(false);
            return;
        }

        if (!(target instanceof Player player) || !player.isAlive() || player.getAbilities().instabuild || this.shouldHuntCaveSpiders()) {
            this.stalkingTargetId = -1;
            this.staredAtTicks = 0;
            this.setRevealedState(true);
            this.setBehaviorState(BehaviorState.WANDER);
            this.pendingRevealTackle = false;
            this.setInvisible(false);
            return;
        }

        if (this.getBehaviorState() == BehaviorState.RECOVERY) {
            this.setRevealedState(true);
            this.setInvisible(false);
            return;
        }

        if (this.stalkingTargetId != player.getId()) {
            this.stalkingTargetId = player.getId();
            this.setBehaviorState(BehaviorState.STALKING);
            this.setRevealedState(false);
            this.staredAtTicks = 0;
            this.pendingRevealTackle = false;
        }

        double distanceToPlayerSqr = this.distanceToSqr(player);
        if (this.getBehaviorState() == BehaviorState.CHASING) {
            this.setRevealedState(true);
            this.setInvisible(false);
            return;
        }

        this.setBehaviorState(BehaviorState.STALKING);
        this.setRevealedState(false);

        if (distanceToPlayerSqr <= REVEAL_RANGE * REVEAL_RANGE || this.isBeingLookedAtBy(player)) {
            this.setBehaviorState(BehaviorState.CHASING);
            this.setRevealedState(true);
            this.pendingRevealTackle = true;
            this.staredAtTicks = 0;
        } else {
            this.staredAtTicks = 0;
        }

        this.setInvisible(!this.isRevealed());
    }

    private boolean isBeingLookedAtBy(Player player) {
        Vec3 lookVector = player.getViewVector(1.0F).normalize();
        Vec3 toSpider = this.getEyePosition().subtract(player.getEyePosition());
        double distance = toSpider.length();
        if (distance < 0.0001D) {
            return true;
        }

        Vec3 directionToSpider = toSpider.normalize();
        double dot = lookVector.dot(directionToSpider);
        return dot > 1.0D - 0.025D / distance && player.hasLineOfSight(this);
    }

    private void tryBreakBlocksTowardTarget() {
        LivingEntity target = this.getTarget();
        if (!(target instanceof Player) || !target.isAlive()) {
            return;
        }

        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        if (this.blockBreakCooldown > 0) {
            this.blockBreakCooldown--;
            return;
        }

        if (this.isAttackAnimationRunning()) {
            return;
        }

        if (this.getNavigation().createPath(target, 0) != null) {
            return;
        }

        if (this.hasBreakableBlocksInSlashArea(BLOCK_BREAK_SLASH_CENTER_OFFSET, BLOCK_BREAK_SLASH_HORIZONTAL_RADIUS, BLOCK_BREAK_SLASH_VERTICAL_RADIUS)) {
            this.blockBreakCooldown = BLOCK_BREAK_COOLDOWN_TICKS;
            this.playSingleSlashAnimation(() -> {
                this.spawnSlashRangeParticles(BLOCK_BREAK_SLASH_CENTER_OFFSET, BLOCK_BREAK_SLASH_HORIZONTAL_RADIUS, BLOCK_BREAK_SLASH_VERTICAL_RADIUS);
                this.destroyBlocksInSlashArea(BLOCK_BREAK_SLASH_CENTER_OFFSET, BLOCK_BREAK_SLASH_HORIZONTAL_RADIUS, BLOCK_BREAK_SLASH_VERTICAL_RADIUS);
            });
        }
    }

    public boolean canBreakBlock(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        if (state.isAir() || !state.blocksMotion()) {
            return false;
        }

        if (state.getDestroySpeed(this.level(), pos) < 0.0F) {
            return false;
        }

        return !state.is(Blocks.BEDROCK)
                && !state.is(Blocks.REINFORCED_DEEPSLATE)
                && !state.is(Blocks.END_PORTAL_FRAME)
                && !state.is(Blocks.COMMAND_BLOCK)
                && !state.is(Blocks.CHAIN_COMMAND_BLOCK)
                && !state.is(Blocks.REPEATING_COMMAND_BLOCK)
                && !state.is(Blocks.BARRIER);
    }

    public boolean hasBreakableBlocksInSlashArea(double centerOffset, double horizontalRadius, double verticalRadius) {
        for (BlockPos pos : this.getBreakableBlocksInSlashArea(centerOffset, horizontalRadius, verticalRadius)) {
            if (this.canBreakBlock(pos)) {
                return true;
            }
        }
        return false;
    }

    public void destroyBlocksInSlashArea(double centerOffset, double horizontalRadius, double verticalRadius) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        for (BlockPos pos : this.getBreakableBlocksInSlashArea(centerOffset, horizontalRadius, verticalRadius)) {
            if (this.canBreakBlock(pos)) {
                this.level().destroyBlock(pos, true, this);
            }
        }
    }

    private Iterable<BlockPos> getBreakableBlocksInSlashArea(double centerOffset, double horizontalRadius, double verticalRadius) {
        Vec3 forward = this.getForward().normalize();
        Vec3 center = this.position()
                .add(0.0D, this.getBbHeight() * 0.5D, 0.0D)
                .add(forward.scale(centerOffset));
        AABB box = new AABB(
                center.x - horizontalRadius, center.y - verticalRadius, center.z - horizontalRadius,
                center.x + horizontalRadius, center.y + verticalRadius, center.z + horizontalRadius
        );

        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            Vec3 blockCenter = Vec3.atCenterOf(pos);
            double dx = blockCenter.x - center.x;
            double dy = blockCenter.y - center.y;
            double dz = blockCenter.z - center.z;
            double horizontal = (dx * dx + dz * dz) / (horizontalRadius * horizontalRadius);
            double vertical = (dy * dy) / (verticalRadius * verticalRadius);
            if (horizontal + vertical <= 1.0D) {
                positions.add(pos.immutable());
            }
        }

        return positions;
    }

    public void spawnSlashRangeParticles(double centerOffset, double horizontalRadius, double verticalRadius) {
        this.spawnSlashRangeParticles(centerOffset, horizontalRadius, verticalRadius, ParticleTypes.CRIT);
    }

    public void spawnDoubleSlashRangeParticles(double centerOffset, double horizontalRadius, double verticalRadius) {
        this.spawnSlashRangeParticles(centerOffset, horizontalRadius, verticalRadius, ParticleTypes.SWEEP_ATTACK);
    }

    private void spawnSlashRangeParticles(double centerOffset, double horizontalRadius, double verticalRadius, net.minecraft.core.particles.ParticleOptions particleType) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 forward = this.getForward().normalize();
        Vec3 center = this.position()
                .add(0.0D, this.getBbHeight() * 0.5D, 0.0D)
                .add(forward.scale(centerOffset));

        for (int ring = -1; ring <= 1; ring++) {
            double y = center.y + ring * (verticalRadius * 0.5D);
            double horizontalScale = Math.sqrt(Math.max(0.0D, 1.0D - (ring * 0.5D) * (ring * 0.5D)));
            double ringRadius = horizontalRadius * horizontalScale;

            for (int i = 0; i < 20; i++) {
                double angle = (Math.PI * 2.0D * i) / 20.0D;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;
                serverLevel.sendParticles(particleType, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public void applyTackleKnockback(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        dx /= len;
        dz /= len;

        Vec3 current = target.getDeltaMovement();
        target.setDeltaMovement(
                current.x + dx * 1.1D,
                Math.max(current.y, 0.35D),
                current.z + dz * 1.1D
        );
        target.hurtMarked = true;
        target.hasImpulse = true;
        target.setOnGround(false);
    }

    public void applySingleSlashKnockback(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        dx /= len;
        dz /= len;

        Vec3 current = target.getDeltaMovement();
        target.setDeltaMovement(
                current.x + dx * 0.7D,
                Math.max(current.y, 0.2D),
                current.z + dz * 0.7D
        );
        target.hurtMarked = true;
        target.hasImpulse = true;
        target.setOnGround(false);
    }

    public void walkBackAnimation() {
        this.clientWalkBackBlendO = this.clientWalkBackBlend;
        this.walkBackTarget = this.isBackingUp() ? 1.0F : 0.0F;
        float rate = (this.walkBackTarget > this.clientWalkBackBlend) ? 0.18F : 0.12F;
        this.clientWalkBackBlend += (this.walkBackTarget - this.clientWalkBackBlend) * rate;
        this.clientWalkBackBlend = Mth.clamp(this.clientWalkBackBlend, 0.0F, 1.0F);
    }

    public boolean isRevealed() {
        return this.entityData.get(REVEALED_STATE);
    }

    public void setRevealed(boolean revealed) {
        this.setRevealedState(revealed);
    }

    private void setRevealedState(boolean revealed) {
        this.revealed = revealed;
        this.entityData.set(REVEALED_STATE, revealed);
    }

    public double getChaseSpeedMultiplier() {
        return switch (this.getBehaviorState()) {
            case STALKING -> STALK_SPEED_MULTIPLIER;
            case RECOVERY -> 0.0D;
            default -> 1.0D;
        };
    }

    public double getTackleMinRange() {
        return TACKLE_MIN_RANGE;
    }

    public boolean isBackingUp() {
        return this.entityData.get(BACKING_UP);
    }

    public void setBackingUp(boolean backingUp) {
        this.entityData.set(BACKING_UP, backingUp);
    }

    public boolean isRetreatingPhase() {
        return this.entityData.get(RETREATING_STATE);
    }

    public void setRetreatingPhase(boolean retreating) {
        this.entityData.set(RETREATING_STATE, retreating);
        if (!retreating) {
            this.setBackingUp(false);
        }
    }

    public BehaviorState getBehaviorState() {
        int index = Mth.clamp(this.entityData.get(BEHAVIOR_STATE), 0, BehaviorState.values().length - 1);
        return BehaviorState.values()[index];
    }

    public void setBehaviorState(BehaviorState behaviorState) {
        if (behaviorState == BehaviorState.WANDER) {
            this.getNavigation().stop();
        }
        this.entityData.set(BEHAVIOR_STATE, behaviorState.ordinal());
    }

    public float getRevealAlpha(float partialTicks) {
        return this.clientRevealAlphaO + (this.clientRevealAlpha - this.clientRevealAlphaO) * partialTicks;
    }

    public float getEyesAlpha(float partialTicks) {
        return 0.2F + 0.8F * this.getRevealAlpha(partialTicks);
    }

    public boolean areEyesEmissive(float partialTicks) {
        return this.getRevealAlpha(partialTicks) >= 0.85F;
    }

    public boolean consumeRevealTackle() {
        boolean pending = this.pendingRevealTackle;
        this.pendingRevealTackle = false;
        return pending;
    }

    public void queueRevealTackle() {
        this.pendingRevealTackle = true;
    }

    public boolean needsEmergencyFeeding() {
        return this.getHealth() <= this.getMaxHealth() * 0.2F;
    }

    public boolean shouldHuntCaveSpiders() {
        if (this.getHealth() >= this.getMaxHealth()) {
            return false;
        }

        LivingEntity target = this.getTarget();
        return target == null || target instanceof CaveSpider;
    }

    public void applyAttackEffects(LivingEntity victim, int poisonDurationTicks) {
        victim.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDurationTicks, POISON_AMPLIFIER), this);
        if (victim instanceof CaveSpider && !victim.isAlive()) {
            float healAmount = (float) (this.getMaxHealth() * 0.3F);
            this.heal(healAmount);
            this.spawnHealParticles(healAmount);
        }
    }

    private void spawnHealParticles(float healAmount) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int particleCount = Math.max(1, Math.round(healAmount));
        serverLevel.sendParticles(
                ParticleTypes.HEART,
                this.getX(),
                this.getY() + this.getBbHeight() * 0.8D,
                this.getZ(),
                particleCount,
                this.getBbWidth() * 0.35D,
                this.getBbHeight() * 0.25D,
                this.getBbWidth() * 0.35D,
                0.0D
        );
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!hurt) {
            return false;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof Player player && !player.getAbilities().instabuild) {
            if (this.getBehaviorState() == BehaviorState.STALKING) {
                this.setBehaviorState(BehaviorState.CHASING);
                this.setRevealed(true);
                this.setInvisible(false);
                this.queueRevealTackle();
            } else if (this.getBehaviorState() == BehaviorState.RECOVERY && this.random.nextFloat() < 0.3F) {
                this.setBehaviorState(BehaviorState.CHASING);
                this.setRevealed(true);
                this.setInvisible(false);
                this.queueRevealTackle();
            }
        }

        return true;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (hit && entity instanceof LivingEntity livingEntity) {
            this.applyAttackEffects(livingEntity, POISON_DURATION_TICKS);
        }
        return hit;
    }

    public void playDoubleSlashAnimation(Runnable damageWindowCallback) {
        this.animationChainer.reset();

        // Tracks if we've already applied damage in this animation's window.
        final boolean[] hasHitInWindow = new boolean[] { false };

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                ReaperSpiderAnimationState.DOUBLE_SLASH,
                ReaperSpiderAnimationDuration.DOUBLE_SLASH,
                null, // onStart
                () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE), // onComplete
                (anim, progress) -> {
                    // damage window [0.4, 0.5]
                    if (!hasHitInWindow[0]
                            && progress >= 0.4f
                            && progress <= 0.5f) {

                        hasHitInWindow[0] = true; // ensure it only fires once

                        if (damageWindowCallback != null && this.isAlive()) {
                            damageWindowCallback.run();
                        }
                    }
                }
        ));
    }

    public void playSingleSlashAnimation(Runnable damageWindowCallback) {
        this.animationChainer.reset();

        // Tracks if we've already applied damage in this animation's window.
        final boolean[] hasHitInWindow = new boolean[] { false };

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                ReaperSpiderAnimationState.SINGLE_SLASH,
                ReaperSpiderAnimationDuration.SINGLE_SLASH,
                null, // onStart
                () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE), // onComplete
                (anim, progress) -> {
                    // damage window [0.4, 0.5]
                    if (!hasHitInWindow[0]
                            && progress >= 0.4f
                            && progress <= 0.5f) {

                        hasHitInWindow[0] = true; // ensure it only fires once

                        if (damageWindowCallback != null && this.isAlive()) {
                            damageWindowCallback.run();
                        }
                    }
                }
        ));
    }
   /* public void playSingleSlashAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(ReaperSpiderAnimationState.SINGLE_SLASH, ReaperSpiderAnimationDuration.SINGLE_SLASH, null, () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE)));
    }*/

    public void playWindUpAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(ReaperSpiderAnimationState.WIND_UP, ReaperSpiderAnimationDuration.WIND_UP, null, () -> this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE)));
    }

    public void playTacklingAnimation() {
        this.transitionTo(ReaperSpiderAnimationState.TACKLING);
    }

    @Override
    public boolean onClimbable() {
        if (this.isLocomotionLocked()) {
            return false;
        }
        return super.onClimbable() || this.horizontalCollision;
    }

    public void setFastClimb(boolean fastClimb) {
        this.fastClimb = fastClimb;
    }

    public boolean isFastClimb() {
        return fastClimb;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.tickLocomotionLock();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isLocomotionLocked()) {
            super.travel(Vec3.ZERO);
            return;
        }

        super.travel(travelVector);

        if (this.fastClimb && this.onClimbable() && !this.isFallFlying()) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.y > 0.0D) {
                this.setDeltaMovement(motion.x, motion.y * 2.0D, motion.z);
            }
        }
    }

    public boolean isLocomotionLocked() {
        return this.locomotionLocked;
    }

    public void setLocomotionLocked(boolean locomotionLocked) {
        this.locomotionLocked = locomotionLocked;
        if (locomotionLocked) {
            this.applyLocomotionLock();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (REVEALED_STATE.equals(entityDataAccessor) && this.level().isClientSide) {
            this.clientLastRevealed = this.entityData.get(REVEALED_STATE);
            if (!this.clientLastRevealed) {
                this.clientRevealAlpha = Math.min(this.clientRevealAlpha, 0.2F);
                this.clientRevealAlphaO = this.clientRevealAlpha;
            }
        }
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            ReaperSpiderAnimationState animationState = this.getAnimationState();
            this.resetAnimation();
            switch (animationState) {
                case WIND_UP -> this.windUpAnimationState.startIfStopped(this.tickCount);
                case TACKLING -> this.tacklingAnimationState.startIfStopped(this.tickCount);
                case DOUBLE_SLASH -> this.doubleSlashAnimationState.startIfStopped(this.tickCount);
                case RECOVERY -> this.recoveryAnimationState.startIfStopped(this.tickCount);
                case SINGLE_SLASH -> this.singleSlashAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public ReaperSpiderEntity transitionTo(ReaperSpiderAnimationState animationState) {
        switch (animationState) {
            case IDLE:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.IDLE);
                break;
            case WIND_UP:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.WIND_UP);
                break;
            case TACKLING:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.TACKLING);
                break;
            case DOUBLE_SLASH:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.DOUBLE_SLASH);
                break;
            case RECOVERY:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.RECOVERY);
                break;
            case SINGLE_SLASH:
                this.entityData.set(ANIMATION_STATE, ReaperSpiderAnimationState.SINGLE_SLASH);
                break;
        }
        return this;
    }

    private void resetAnimation() {
        this.tacklingAnimationState.stop();
        this.doubleSlashAnimationState.stop();
        this.singleSlashAnimationState.stop();
        this.recoveryAnimationState.stop();
        this.windUpAnimationState.stop();
    }

    public boolean isAttackAnimationRunning() {
        return getAnimationState() != ReaperSpiderAnimationState.IDLE;
    }

    public ReaperSpiderAnimationState getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public enum ReaperSpiderAnimationState {
        IDLE,
        WIND_UP,
        TACKLING,
        DOUBLE_SLASH,
        RECOVERY,
        SINGLE_SLASH
    }

    public enum BehaviorState {
        WANDER,
        STALKING,
        CHASING,
        RECOVERY
    }
}
