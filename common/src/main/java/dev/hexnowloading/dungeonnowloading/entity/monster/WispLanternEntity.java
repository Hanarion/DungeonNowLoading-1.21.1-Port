package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.WispLanternAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.WispLanternLightGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.WispLanternWanderGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.WispLightBlocksGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.HoveringFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WispLanternEntity extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> SUMMONING_WISP = SynchedEntityData.defineId(WispLanternEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LOOK_AROUND_TRIGGER = SynchedEntityData.defineId(WispLanternEntity.class, EntityDataSerializers.INT);
    private static final int MIN_LOOK_AROUND_DELAY = 80;
    private static final int MAX_LOOK_AROUND_DELAY = 180;
    private static final int MIN_SUMMON_HORIZONTAL_RADIUS = 3;
    private static final int MAX_SUMMON_HORIZONTAL_RADIUS = 6;
    private static final int SUMMON_VERTICAL_RANGE = 2;
    private static final int MAX_SUMMON_ATTEMPTS = 20;
    private static final int SUMMON_EXTINGUISH_START_TICKS = 10;
    private static final int SUMMON_EXTINGUISH_END_TICKS = 40;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState summonWispAnimationState = new AnimationState();
    public final AnimationState lookAroundAnimationState = new AnimationState();

    private int lookAroundCooldown;
    private int summonAnimationStartTick = Integer.MIN_VALUE;

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
        this.goalSelector.addGoal(2, new WispLanternLightGoal(this));
        this.goalSelector.addGoal(3, new WispLanternWanderGoal(this));
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
        this.summonAnimationStartTick = summoningWisp ? this.tickCount : Integer.MIN_VALUE;
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
                this.summonAnimationStartTick = this.tickCount;
                this.lookAroundAnimationState.stop();
                this.summonWispAnimationState.start(this.tickCount);
            } else {
                this.summonAnimationStartTick = Integer.MIN_VALUE;
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
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.canBeExtinguishedDuringSummon() && this.isInWaterOrBubble()) {
            this.extinguishDuringSummon();
            return;
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

    public boolean canBeExtinguishedDuringSummon() {
        if (!this.isSummoningWisp() || this.summonAnimationStartTick == Integer.MIN_VALUE) {
            return false;
        }

        int elapsedTicks = this.tickCount - this.summonAnimationStartTick;
        return elapsedTicks >= SUMMON_EXTINGUISH_START_TICKS && elapsedTicks <= SUMMON_EXTINGUISH_END_TICKS;
    }

    @Nullable
    public BlockPos findNearestVisibleLightTarget() {
        BlockPos origin = this.blockPosition();
        BlockPos best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (int dx = -WispLightBlocksGoal.HORIZONTAL_SEARCH_RANGE; dx <= WispLightBlocksGoal.HORIZONTAL_SEARCH_RANGE; dx++) {
            for (int dy = -WispLightBlocksGoal.VERTICAL_SEARCH_RANGE; dy <= WispLightBlocksGoal.VERTICAL_SEARCH_RANGE; dy++) {
                for (int dz = -WispLightBlocksGoal.HORIZONTAL_SEARCH_RANGE; dz <= WispLightBlocksGoal.HORIZONTAL_SEARCH_RANGE; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (!WispLightBlocksGoal.isValidLightTarget(this.level(), candidate)) {
                        continue;
                    }
                    if (!this.hasLineOfSightToLightTarget(candidate)) {
                        continue;
                    }

                    double distanceSqr = candidate.distToCenterSqr(this.position());
                    if (distanceSqr < bestDistanceSqr) {
                        bestDistanceSqr = distanceSqr;
                        best = candidate.immutable();
                    }
                }
            }
        }

        return best;
    }

    @Nullable
    public BlockPos findSummonPos() {
        BlockPos base = this.blockPosition();
        for (int attempt = 0; attempt < MAX_SUMMON_ATTEMPTS; attempt++) {
            int dx = this.random.nextInt(-MAX_SUMMON_HORIZONTAL_RADIUS, MAX_SUMMON_HORIZONTAL_RADIUS + 1);
            int dz = this.random.nextInt(-MAX_SUMMON_HORIZONTAL_RADIUS, MAX_SUMMON_HORIZONTAL_RADIUS + 1);
            int horizontalDistanceSqr = dx * dx + dz * dz;
            if (horizontalDistanceSqr < MIN_SUMMON_HORIZONTAL_RADIUS * MIN_SUMMON_HORIZONTAL_RADIUS
                    || horizontalDistanceSqr > MAX_SUMMON_HORIZONTAL_RADIUS * MAX_SUMMON_HORIZONTAL_RADIUS) {
                continue;
            }

            int dy = this.random.nextInt(-SUMMON_VERTICAL_RANGE, SUMMON_VERTICAL_RANGE + 1);
            BlockPos pos = base.offset(dx, dy, dz);
            if (this.isAirLikeAndSafe(pos)) {
                return pos;
            }
        }
        return null;
    }

    public void spawnSummonedWisp(BlockPos spawnPos, @Nullable LivingEntity target) {
        WispEntity wisp = DNLEntityTypes.WISP.get().create(this.level());
        if (wisp == null) {
            return;
        }

        wisp.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
        wisp.setOwner(this);
        if (target != null) {
            wisp.setTarget(target);

            double dx = target.getX() - wisp.getX();
            double dz = target.getZ() - wisp.getZ();
            float yaw = (float) (net.minecraft.util.Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
            wisp.setYRot(yaw);
            wisp.setYHeadRot(yaw);
            wisp.setYBodyRot(yaw);
        }

        if (!this.level().noCollision(wisp)) {
            return;
        }

        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF, wisp.getX(), wisp.getY(), wisp.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.01D);
        }
        this.level().addFreshEntity(wisp);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (this.canBeExtinguishedDuringSummon()) {
            if (!this.level().isClientSide) {
                this.extinguishDuringSummon();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, interactionHand);
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

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.getDirectEntity() instanceof ThrownPotion) {
            if (this.canBeExtinguishedDuringSummon()) {
                this.extinguishDuringSummon();
                return true;
            }
            return false;
        }

        if (this.isFatalWaterDamage(damageSource, amount)) {
            this.dropCoalBlockOnWaterDeath();
            return true;
        }

        return super.hurt(damageSource, amount);
    }

    private boolean hasLineOfSightToLightTarget(BlockPos pos) {
        Vec3 from = this.getEyePosition();
        Vec3 to = WispLightBlocksGoal.getApproachPos(this.level(), pos);
        if (this.hasWaterBetween(from, to, pos)) {
            return false;
        }

        Vec3 start = from;
        for (int i = 0; i < 8; i++) {
            HitResult hitResult = this.level().clip(new ClipContext(start, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hitResult.getType() == HitResult.Type.MISS) {
                return true;
            }
            if (hitResult.getType() != HitResult.Type.BLOCK) {
                return false;
            }

            net.minecraft.world.phys.BlockHitResult blockHitResult = (net.minecraft.world.phys.BlockHitResult) hitResult;
            BlockPos hitPos = blockHitResult.getBlockPos();
            if (hitPos.equals(pos)) {
                return true;
            }
            if (!this.level().getBlockState(hitPos).is(Blocks.FIRE)) {
                return false;
            }

            start = blockHitResult.getLocation().add(to.subtract(from).normalize().scale(0.05D));
        }

        return false;
    }

    private boolean hasWaterBetween(Vec3 from, Vec3 to, BlockPos targetPos) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-6D) {
            return false;
        }

        Vec3 step = delta.scale(0.25D / length);
        Vec3 cursor = from;
        int steps = Math.max(1, (int) Math.ceil(length / 0.25D));
        for (int i = 0; i <= steps; i++) {
            BlockPos samplePos = BlockPos.containing(cursor);
            if (!samplePos.equals(targetPos) && !this.level().getBlockState(samplePos).getFluidState().isEmpty()) {
                return true;
            }
            cursor = cursor.add(step);
        }
        return false;
    }

    private boolean isAirLikeAndSafe(BlockPos pos) {
        if (!this.level().isInWorldBounds(pos) || !this.level().getWorldBorder().isWithinBounds(pos)) {
            return false;
        }

        var state = this.level().getBlockState(pos);
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (!state.getCollisionShape(this.level(), pos).isEmpty()) {
            return false;
        }
        return this.level().isEmptyBlock(pos);
    }

    private boolean isFatalWaterDamage(DamageSource damageSource, float amount) {
        return damageSource.is(DamageTypeTags.IS_DROWNING) && amount >= this.getHealth();
    }

    private void dropCoalBlockOnWaterDeath() {
        if (this.isRemoved() || this.level().isClientSide) {
            return;
        }

        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6D, this.getZ(), 18, 0.25D, 0.25D, 0.25D, 0.03D);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.8F, 1.0F);
        this.spawnAtLocation(new ItemStack(Blocks.COAL_BLOCK));
        this.discard();
    }

    private void extinguishDuringSummon() {
        if (this.isRemoved() || this.level().isClientSide) {
            return;
        }

        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6D, this.getZ(), 18, 0.25D, 0.25D, 0.25D, 0.03D);
            server.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.6D, this.getZ(), 8, 0.18D, 0.18D, 0.18D, 0.01D);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.9F, 1.1F + this.random.nextFloat() * 0.1F);
        this.spawnAtLocation(new ItemStack(Blocks.COAL_BLOCK));
        this.discard();
    }
}
