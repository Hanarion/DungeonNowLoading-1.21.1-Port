package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.garhold.*;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.GarholdAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStartTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStopTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class GarholdEntity extends Monster {

    private static final EntityDataAccessor<GarholdState> STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_STATE);
    private static final EntityDataAccessor<GarholdAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_ANIMATION_STATE);
    //private static final EntityDataAccessor<GarholdBottomGateAnimationState> BOTTOM_GATE_ANIMATION_STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_GATE_ANIMATION_STATE);
    private static final EntityDataAccessor<GarholdSideGateAnimationState> SIDE_GATE_ANIMATION_STATE = SynchedEntityData.defineId(GarholdEntity.class, EntityStates.GARHOLD_SIDE_GATE_ANIMATION_STATE);
    private static final EntityDataAccessor<Boolean> BOTTOM_OPENESS = SynchedEntityData.defineId(GarholdEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SIDE_OPENESS = SynchedEntityData.defineId(GarholdEntity.class, EntityDataSerializers.BOOLEAN);


    private static final UUID CAPTURE_KB_UUID = UUID.fromString("c84d8d2a-1a5f-4b0c-9a4c-6c2c4d9b6a21");
    private static final AttributeModifier CAPTURE_KB_MOD = new AttributeModifier(CAPTURE_KB_UUID, "Garhold capture knockback", 1.0, AttributeModifier.Operation.ADDITION);

    private static final UUID CHAIN_KB_UUID = UUID.fromString("8c3a1b6e-2d78-4b1d-9c3b-7f7a6a3c2d11");
    private static final AttributeModifier CHAIN_KB_MOD = new AttributeModifier(CHAIN_KB_UUID, "Garhold chain lock knockback", 1.0, AttributeModifier.Operation.ADDITION);

    private AnimationChainer<GarholdAnimationState> animationChainer = new AnimationChainer<>();
    private AnimationChainer<GarholdSideGateAnimationState> sideGateAnimationChainer = new AnimationChainer<>();

    public final AnimationState detachAnimationState = new AnimationState();
    public final AnimationState forceDetachAnimationState = new AnimationState();
    public final AnimationState chargeDiveAnimationState = new AnimationState();
    public final AnimationState landDiveAnimationState = new AnimationState();
    public final AnimationState closingGateAnimationState = new AnimationState();
    public final AnimationState sideCaptureAnimationState = new AnimationState();
    public final AnimationState ascendAnimationState = new AnimationState();
    public final AnimationState idleHangAnimationState = new AnimationState();
    public final AnimationState reattachAnimationState = new AnimationState();

    public final AnimationState sideWideToOpenedAnimationState = new AnimationState();

    // --- Bottom Door Animation ---
    public float clientBottomOpen = 0f;
    public float clientBottomOpenO = 0f;
    private float bottomOpenTarget = 0f;

    private static final float BOTTOM_OPEN_IN = 0.18f;
    private static final float BOTTOM_OPEN_OUT = 0.10f;

    // --- Flying Animation ---
    public float clientFlyBlend = 0.0f;
    public float clientFlyBlendO = 0f;
    private float flyTarget = 0.0f;

    private static final float FLY_IN  = 0.18f;
    private static final float FLY_OUT = 0.10f;

    // --- Side Animation ---
    public float clientSideOpen = 0.0f;
    public float clientSideOpenO = 0f;
    private float sideOpenTarget = 0.0f;

    private static final float SIDE_OPEN_IN  = 0.18f;
    private static final float SIDE_OPEN_OUT = 0.10f;

    // --- Ascend movement (server tick based) ---
    private boolean ascendLiftActive = false;
    private int ascendLiftTicksLeft = 0;

    // --- Others ---
    public int sideCaptureCooldownTicks = 0;

    private int chainedPassengerHoldTicks = 0;
    private int chainedDetachLockoutTicks = 0;

    private int flapSoundCooldown = 0;

    private boolean missedCapture = false;

    private static final float ASCEND_LIFT_START_PROGRESS = 0.25f;
    private static final int ASCEND_LIFT_TICKS = 10;
    private static final double ASCEND_LIFT_BLOCKS = 3.0;
    private static final double ASCEND_LIFT_PER_TICK = ASCEND_LIFT_BLOCKS / ASCEND_LIFT_TICKS;

    private static final float GROUND_SMASH_HIT_AT = 0.75f;
    private static final float GROUND_SMASH_RADIUS = 2.5f;
    private static final float GROUND_SMASH_RADIUS_SQR = GROUND_SMASH_RADIUS * GROUND_SMASH_RADIUS;
    private static final float GROUND_SMASH_KB_H = 1.1f;     // horizontal knockback
    private static final float GROUND_SMASH_KB_Y = 0.35f;

    public static final float CAPTURE_MAX_WIDTH_ADD  = 0.6f;
    public static final float CAPTURE_MAX_HEIGHT_ADD = 0.3f;

    public GarholdEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.moveControl = new FlyingMoveControl(this, 16, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GarholdDiveCaptureGoal(this));
        this.goalSelector.addGoal(2, new GarholdSideCaptureGoal(this));
        this.goalSelector.addGoal(3, new GarholdHoverAboveTargetGoal(this, 1.2F));
        this.goalSelector.addGoal(5, new GarholdReturnToChainGoal(this));
        this.goalSelector.addGoal(8, new GarholdWanderGoal(this, 1.0, 12, 6));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                        this,
                        LivingEntity.class,
                        10,
                        true,
                        false,
                        this::isValidGarholdTarget
                )
        );
        super.registerGoals();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 35.0)
                .add(Attributes.FLYING_SPEED, 0.15)
                .add(Attributes.MOVEMENT_SPEED, 0.2) // Required for slowing down the initial speed on ground.
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ARMOR, 15.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, GarholdState.FLYING);
        this.entityData.define(ANIMATION_STATE, GarholdAnimationState.NONE);
        //this.entityData.define(BOTTOM_GATE_ANIMATION_STATE, GarholdBottomGateAnimationState.BOTTOM_CLOSED);
        this.entityData.define(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.NONE);
        this.entityData.define(BOTTOM_OPENESS, false);
        this.entityData.define(SIDE_OPENESS, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Chained", this.isGarholdState(GarholdState.CHAINED));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setGarholdState(compoundTag.getBoolean("Chained") ? GarholdState.CHAINED : GarholdState.FLYING);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    public boolean onGround() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVec) {
        // Only simulate movement on the authoritative side.
        if (!this.isControlledByLocalInstance()) {
            this.calculateEntityAnimation(false);
            return;
        }

        // Keep vanilla fluid handling (this is what tends to keep edge cases sane)
        if (this.isInWater()) {
            // If you want true vanilla water behavior, call super.travel(travelVec) here instead.
            this.moveRelative(0.02F, travelVec);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
            this.calculateEntityAnimation(false);
            return;
        }

        if (this.isInLava()) {
            // If you want true vanilla lava behavior, call super.travel(travelVec) here instead.
            this.moveRelative(0.02F, travelVec);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            this.calculateEntityAnimation(false);
            return;
        }

        // --- AIR / FLYING BRANCH (vanilla-like) ---

        // Your knobs:
        // - horizontalScale: changes only strafe/forward response (pathing-friendly)
        // - verticalScale: changes only ascend/descend response
        final float horizontalScale = this.getGarholdHorizontalFlightScale(); // <-- you implement (can be 0.5f..2.0f etc)
        final float verticalScale   = this.getGarholdVerticalFlightScale();   // <-- you implement (often 1.0f)

        // IMPORTANT: keep vanilla "speed" usage for AI/pathfinding compatibility
        final float speed = this.getSpeed();

        // Scale the *input* (xxa/yya/zza), not the base speed.
        // This preserves MoveControl/pathfinder expectations much better than custom steering.
        Vec3 scaledInput = new Vec3(
                travelVec.x * horizontalScale,
                travelVec.y * verticalScale,
                travelVec.z * horizontalScale
        );

        // Vanilla-ish: accelerate based on input and current yaw
        this.moveRelative(speed, scaledInput);

        // Apply gravity if you ever want it (Garhold usually shouldn’t have gravity)
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.08, 0.0));
        }

        // Actually move
        this.move(MoverType.SELF, this.getDeltaMovement());

        final double dampingXZ = 0.91;
        final double dampingY  = 0.91;
        Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(v.x * dampingXZ, v.y * dampingY, v.z * dampingXZ);

        this.calculateEntityAnimation(false);
    }

    private float getGarholdHorizontalFlightScale() {
        return 1.0f;
    }

    private float getGarholdVerticalFlightScale() {
        return 1.0f;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.bottomDoorAnimation();
            this.sideDoorAnimation();
            this.flyAnimation();
            return;
        }

        // --- Gravity policy ---
        // Carrying = has passenger (captured), and NOT chained => should fall.
        boolean carrying = this.hasCapturedPlayer();
        boolean chained  = this.isGarholdState(GarholdState.CHAINED);

        if (carrying && !chained) {
            this.setNoGravity(false);
        } else {
            this.setNoGravity(true);
        }

        animationChainer.tick(this::transitionTo);
        sideGateAnimationChainer.tick(this::transitionTo);

        if (this.entityData.get(ANIMATION_STATE).equals(GarholdAnimationState.NONE)) {
            if (this.entityData.get(STATE).equals(GarholdState.CHAINED)) {
                this.playIdleHanging();
                this.closeSide();
                this.closeBottom();
            } else {
                this.openBottom();
                this.openSide();
            }
        }
    }

    public void flyAnimation() {
        boolean wantsFly = this.isGarholdState(GarholdState.FLYING)
                        || this.entityData.get(ANIMATION_STATE).equals(GarholdAnimationState.ASCEND)
                        || this.isGarholdState(GarholdState.LOCKING_ON_CHAIN);

        clientFlyBlendO   = clientFlyBlend;

        flyTarget = wantsFly ? 1.0f : 0.0f;

        float rate = (flyTarget > clientFlyBlend) ? FLY_IN : FLY_OUT;
        clientFlyBlend += (flyTarget - clientFlyBlend) * rate;
        clientFlyBlend = Mth.clamp(clientFlyBlend, 0.0f, 1.0f);
    }

    public void bottomDoorAnimation() {
        boolean wantsOpen = this.entityData.get(BOTTOM_OPENESS);

        clientBottomOpenO = clientBottomOpen;

        bottomOpenTarget = wantsOpen ? 1.0f : 0.0f;

        float rate = (bottomOpenTarget > clientBottomOpen) ? BOTTOM_OPEN_IN : BOTTOM_OPEN_OUT;
        clientBottomOpen += (bottomOpenTarget - clientBottomOpen) * rate;
        clientBottomOpen = Mth.clamp(clientBottomOpen, 0f, 1f);
    }

    public void sideDoorAnimation() {
        boolean wantsOpen = this.isGarholdState(GarholdState.FLYING);

        clientSideOpenO = clientSideOpen;

        sideOpenTarget = wantsOpen ? 1.0f : 0.0f;

        float rate = (sideOpenTarget > clientSideOpen) ? SIDE_OPEN_IN : SIDE_OPEN_OUT;
        clientSideOpen += (sideOpenTarget - clientSideOpen) * rate;
        clientSideOpen = Mth.clamp(clientSideOpen, 0f, 1f);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (sideCaptureCooldownTicks > 0) sideCaptureCooldownTicks--;

        boolean chainLocked = isPushableState();

        var inst = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (inst != null) {
            boolean has = inst.getModifier(CHAIN_KB_UUID) != null;

            if (chainLocked && !has) {
                inst.addTransientModifier(CHAIN_KB_MOD);
            } else if (!chainLocked && has) {
                inst.removeModifier(CHAIN_KB_UUID);
            }
        }

        if (chainedPassengerHoldTicks > 0) chainedPassengerHoldTicks--;
        if (chainedDetachLockoutTicks > 0) chainedDetachLockoutTicks--;

        if (this.isGarholdState(GarholdState.CHAINED)) {
            if (chainedPassengerHoldTicks == 1) {
                // about to expire next tick -> dismount now
                forceDismountPassengerUnderSelf();
                this.playSound(DNLSounds.GARHOLD_RELEASE.get());
                chainedDetachLockoutTicks = 100; // 5 seconds
            }

            // Forced detach ALWAYS wins
            if (!hasChainAboveSelf()) {
                startDetaching(true);
            } else {
                // Normal detach only if not protected
                boolean protectedFromNormalDetach = (chainedPassengerHoldTicks > 0) || (chainedDetachLockoutTicks > 0);
                if (!protectedFromNormalDetach && this.getTarget() != null/*seesPlayerInRange()*/) {
                    startDetaching(false);
                }
            }
        }

        if (ascendLiftTicksLeft > 0) {
            // keep it stable during the lift
            this.setDeltaMovement(0.0, 0.0, 0.0);

            // move up smoothly
            double step = ASCEND_LIFT_PER_TICK;

            // Try to move up, but stop if we'd collide
            AABB next = this.getBoundingBox().move(0.0, step, 0.0);
            if (this.level().noCollision(this, next)) {
                this.setPos(this.getX(), this.getY() + step, this.getZ());
            } else {
                // find the largest safe step (binary search)
                double lo = 0.0;
                double hi = step;

                for (int i = 0; i < 8; i++) { // 8 iterations is plenty
                    double mid = (lo + hi) * 0.5;
                    AABB midBox = this.getBoundingBox().move(0.0, mid, 0.0);
                    if (this.level().noCollision(this, midBox)) lo = mid;
                    else hi = mid;
                }

                if (lo > 1.0e-4) {
                    this.setPos(this.getX(), this.getY() + lo, this.getZ());
                }

                // Stop lifting once blocked
                ascendLiftTicksLeft = 0;
                ascendLiftActive = false;
            }

            ascendLiftTicksLeft--;

            if (ascendLiftTicksLeft <= 0) {
                ascendLiftTicksLeft = 0;
                ascendLiftActive = false;
            }
        }

        tickFlapSound();
    }

    public boolean hasCapturedPlayer() {
        return !this.getPassengers().isEmpty();
    }

    public boolean beginCapture(@Nullable LivingEntity target) {
        if (target == null) return false;
        if (!isValidCaptureTarget(target)) return false;

        if (target.isPassenger()) {
            target.stopRiding();
        }

        this.applyCaptureAttributes();
        this.clearNearbyTargets(target);

        boolean mounted = target.startRiding(this, true);

        if (!mounted) {
            this.clearCaptureAttributes();
            return false;
        }

        return true;
    }

    private void teleportRiderToChainedGarhold() {
        if (this.level().isClientSide) return;
        if (this.getPassengers().isEmpty()) return;

        Entity rider = this.getPassengers().get(0);
        if (!(rider instanceof LivingEntity player)) return;

        Entity target = findNearestChainedGarhold(128.0);
        if (target == null) return;

        ServerLevel level = (ServerLevel) this.level();

        spawnTeleportSpawnerFx(level, this);
        this.playSound(DNLSounds.GARHOLD_TELEPORT.get());

        player.teleportTo(target.getX(), target.getY(), target.getZ());
        player.stopRiding();
        player.startRiding(target, true);

        spawnTeleportSpawnerFx(level, target);
        target.playSound(DNLSounds.GARHOLD_TELEPORT.get());

        if (target instanceof GarholdEntity g) {
            g.chainedPassengerHoldTicks = 60;
        }
    }


    private Entity findNearestChainedGarhold(double range) {
        AABB box = this.getBoundingBox().inflate(range);

        Entity best = null;
        double bestDist = Double.MAX_VALUE;

        for (GarholdEntity g : this.level().getEntitiesOfClass(GarholdEntity.class, box)) {
            if (g == this) continue;
            if (!g.isAlive()) continue;
            if (!g.isGarholdState(GarholdState.CHAINED)) continue;
            if (!g.hasChainAboveSelf()) continue; // must really be chained
            if (!g.getPassengers().isEmpty()) continue; // skip if already holding someone (optional)

            double d = this.distanceToSqr(g);
            if (d < bestDist) {
                bestDist = d;
                best = g;
            }
        }

        for (BrokenGarholdEntity b : this.level().getEntitiesOfClass(BrokenGarholdEntity.class, box)) {
            if (!b.isAlive()) continue;
            if (!b.getPassengers().isEmpty()) continue;

            double d = this.distanceToSqr(b);
            if (d < bestDist) {
                bestDist = d;
                best = b;
            }
        }
        return best;
    }

    private void forceDismountPassengerUnderSelf() {
        if (this.getPassengers().isEmpty()) return;

        Entity rider = this.getPassengers().get(0);
        rider.stopRiding();

        // Place them underneath (try to find a safe-ish spot)
        double x = this.getX();
        double z = this.getZ();
        double y = this.getBoundingBox().minY;

        if (rider instanceof LivingEntity p) {
            p.teleportTo(x, y, z);
        } else {
            rider.teleportTo(x, y, z);
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        if (!this.level().isClientSide) {
            if (this.getPassengers().isEmpty()) {
                this.clearCaptureAttributes();
            }
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) return;

        float seatY;

        if (this.isGarholdState(GarholdState.CHAINED)) {
            seatY = 0.5f;
        } else {
            seatY = 0.1f; // default
        }

        Vec3 seat = new Vec3(this.getX(), this.getY() + seatY, this.getZ());
        moveFunction.accept(passenger, seat.x, seat.y, seat.z);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isPushable() {
        if (isPushableState()) {
            return false;
        }
        return super.isPushable();
    }

    @Override
    public void push(double x, double y, double z) {
        if (isPushableState()) {
            return;
        }
        super.push(x, y, z);
    }

    private boolean isPushableState() {
        return isGarholdState(GarholdState.LOCKING_ON_CHAIN) || isGarholdState(GarholdState.ATTACHING) || isGarholdState(GarholdState.CHAINED);
    }


    private void applyCaptureAttributes() {
        AttributeInstance kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null && !kb.hasModifier(CAPTURE_KB_MOD)) {
            kb.addTransientModifier(CAPTURE_KB_MOD);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.level().isClientSide) {
            if (player.getAbilities().instabuild) {

                boolean usePickaxe = stack.getItem() instanceof PickaxeItem;
                boolean useHoe = stack.getItem() instanceof HoeItem;

                if (usePickaxe || useHoe) {
                    ServerLevel level = (ServerLevel) this.level();
                    BrokenGarholdEntity broken = DNLEntityTypes.BROKEN_GARHOLD.get().create(level);

                    if (broken != null) {
                        broken.moveTo(
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                this.getYRot(),
                                this.getXRot()
                        );

                        broken.setYHeadRot(this.getYHeadRot());
                        broken.setYBodyRot(this.yBodyRot);

                        // 👇 If right-clicked with hoe → open & release instead of drop
                        if (useHoe) {
                            broken.setReleaseInsteadOfDrop(true);
                        }

                        level.addFreshEntity(broken);
                    }

                    this.discard();
                    return InteractionResult.CONSUME;
                }
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        if (attacker instanceof LivingEntity p && this.hasPassenger(p)) {
            amount *= 0.5f;
        }

        boolean ok = super.hurt(source, amount);

        if (ok && this.isGarholdState(GarholdState.CHAINED)) {
            boolean protectedFromNormalDetach = (chainedPassengerHoldTicks > 0) || (chainedDetachLockoutTicks > 0);
            if (!protectedFromNormalDetach) {
                startDetaching(false);
            }
        }

        return ok;
    }


    private void clearCaptureAttributes() {
        AttributeInstance kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null && kb.hasModifier(CAPTURE_KB_MOD)) {
            kb.removeModifier(CAPTURE_KB_UUID);
        }
    }

    private void clearNearbyTargets(LivingEntity player) {
        double r = 48.0; // tweak radius
        AABB box = player.getBoundingBox().inflate(r);
        for (Mob mob : this.level().getEntitiesOfClass(Mob.class, box, m -> m.getTarget() == player)) {
            mob.setTarget(null);
        }
    }

    private boolean hasChainAboveSelf() {
        AABB box = this.getBoundingBox();

        // center XZ, bottom Y
        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        double y0 = box.minY; // bottom of hitbox

        // Build a blockpos from that point, then go up
        BlockPos base = BlockPos.containing(cx, y0, cz);
        return isChainBlock(base.above(3));
    }

    private void startDetaching(boolean forced) {
        // Don’t stomp other states
        if (!isGarholdState(GarholdState.CHAINED)) return;

        this.setGarholdState(forced ? GarholdState.FORCE_DETACHING :GarholdState.DETACHING);

        if (forced) {
            playForceDetach();
        } else {
            playDetach();
        }
        //if (forced) playForceDetach();

        // Stop any chain-lock motion immediately
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    // Ground Smash

    @Nullable
    private LivingEntity getCapturedVictimSafe() {
        if (this.getPassengers().isEmpty()) return null;
        Entity e = this.getPassengers().get(0);
        return (e instanceof LivingEntity le) ? le : null;
    }

    public void doGroundSmashHitExcludeCaptured() {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!this.isAlive()) return;

        LivingEntity excluded = getCapturedVictimSafe();

        AABB box = this.getBoundingBox().inflate(GROUND_SMASH_RADIUS, 1.5, GROUND_SMASH_RADIUS);

        // particles (copy your spawner-carrier style)
        ParticleOptions ringA = new ScalableAxisParticleType.ScalableAxisParticleData(
                DNLParticleTypes.WHITE_SHOCKWAVE_MEDIUM_PARTICLE.get(), 0, 90, (float)(GROUND_SMASH_RADIUS * 2 + 1)
        );
        level.sendParticles(ringA, this.getX(), this.getY() + 0.01F, this.getZ(), 1, 0, 0, 0, 0);

        ParticleOptions ringB = new ScalableAxisParticleType.ScalableAxisParticleData(
                DNLParticleTypes.WHITE_SHOCKWAVE_MEDIUM_PARTICLE.get(), 0, 90, (float)(GROUND_SMASH_RADIUS * 1 + 2)
        );
        level.sendParticles(ringB, this.getX(), this.getY() + 0.01F, this.getZ(), 1, 0, 0, 0, 0);


        DamageSource src = this.damageSources().mobAttack(this);
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);

        final LivingEntity currentTarget = this.getTarget();

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, victim -> {
            if (victim == this) return false;
            if (!victim.isAlive()) return false;
            if (victim.isSpectator()) return false;

            if (!isPlayerOrPlayerOwned(victim)) return false;
            if (excluded != null && victim == excluded) return false;
            if (victim.isPassengerOfSameVehicle(this) || this.isPassengerOfSameVehicle(victim)) return false;
            if (victim instanceof Player p && p.isCreative()) return false;
            boolean allowed =
                    (victim instanceof Player)
                            || isPlayerOrPlayerOwned(victim)
                            || (currentTarget != null && victim == currentTarget);

            return allowed;
        })) {

            boolean blockedByShield = (e instanceof Player p) && p.isBlocking() && p.isDamageSourceBlocked(src);

            e.hurt(src, dmg);

            if (blockedByShield) {
                Player p = (Player) e;
                p.stopUsingItem();
                p.getCooldowns().addCooldown(Items.SHIELD, 100);

                level.playSound(null, p.getX(), p.getY(), p.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);

                level.broadcastEntityEvent(this, (byte) 30);
            }

            applySmashKnockback(e);
        }
    }

    private void applySmashKnockback(LivingEntity entity) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        double len = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
        dx /= len; dz /= len;

        var v = entity.getDeltaMovement();
        entity.setDeltaMovement(
                v.x + dx * GROUND_SMASH_KB_H,
                Math.max(v.y, GROUND_SMASH_KB_Y),
                v.z + dz * GROUND_SMASH_KB_H
        );

        entity.hurtMarked = true;
        entity.hasImpulse = true;
        entity.setOnGround(false);
    }

    private static boolean isPlayerOrPlayerOwned(LivingEntity e) {
        if (e instanceof Player) return true;

        // Vanilla pets
        if (e instanceof net.minecraft.world.entity.TamableAnimal ta) {
            UUID owner = ta.getOwnerUUID();
            return owner != null; // owned by *some* player
        }

        // Generic owner interface used by some entities (and many mods)
        if (e instanceof net.minecraft.world.entity.OwnableEntity ownable) {
            UUID owner = ownable.getOwnerUUID();
            return owner != null;
        }

        return false;
    }

    private boolean isValidGarholdTarget(LivingEntity target) {

        if (target.getMobType() == MobType.UNDEAD) return false;

        return isValidCaptureTarget(target);
    }

    public boolean isValidCaptureTarget(@Nullable LivingEntity e) {
        if (e == null) return false;

        if (e == this) return false;
        if (e instanceof GarholdEntity || e instanceof BrokenGarholdEntity) return false;
        if (isRidingAnyGarhold(e)) return false;

        if (!e.isAlive()) return false;
        if (e.isSpectator()) return false;

        if (e instanceof Player p) {
            if (p.isCreative() || p.isSpectator()) return false;
        }

        return canCaptureSize(e);
    }

    private boolean canCaptureSize(LivingEntity target) {
        if (target == null) return false;

        EntityDimensions self = this.getDimensions(this.getPose());
        EntityDimensions t    = target.getDimensions(target.getPose());

        float maxW = self.width  + CAPTURE_MAX_WIDTH_ADD;
        float maxH = self.height + CAPTURE_MAX_HEIGHT_ADD;

        return t.width <= maxW && t.height <= maxH;
    }

    private static boolean isRidingAnyGarhold(@Nullable LivingEntity target) {
        if (target == null) return false;
        Entity v = target.getVehicle();
        return v instanceof GarholdEntity || v instanceof BrokenGarholdEntity;
    }

    @Override
    public void die(DamageSource $$0) {
        super.die($$0);
        if (!this.level().isClientSide) {
            this.stopGarholdSounds();
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return DNLSounds.GARHOLD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.GARHOLD_DEATH.get();
    }

    private static final int FLAP_INTERVAL_TICKS = 5;
    private static final float FLAP_VOL = 0.5f;
    private static final float FLAP_PITCH_MIN = 0.95f;
    private static final float FLAP_PITCH_MAX = 1.05f;

    private void tickFlapSound() {
        if (!(this.level() instanceof ServerLevel level)) return;

        GarholdAnimationState anim = this.entityData.get(ANIMATION_STATE);

        // only flap during flap-capable animations
        if (!isFlapAnimation(anim) && !this.isGarholdState(GarholdState.FLYING) && !this.isGarholdState(GarholdState.LOCKING_ON_CHAIN)) {
            flapSoundCooldown = 0; // so the next time we enter a flap anim, it can play immediately
            return;
        }

        // optional: different cadence per animation
        int interval = getFlapInterval(anim);

        if (flapSoundCooldown-- > 0) return;
        flapSoundCooldown = interval;

        float pitch = FLAP_PITCH_MIN + this.random.nextFloat() * (FLAP_PITCH_MAX - FLAP_PITCH_MIN);

        level.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                DNLSounds.GARHOLD_FLAP.get(),
                this.getSoundSource(),
                FLAP_VOL,
                pitch
        );
    }

    private boolean isFlapAnimation(GarholdAnimationState anim) {
        return anim == GarholdAnimationState.ASCEND
                || anim == GarholdAnimationState.REATTACH
                || anim == GarholdAnimationState.FORCE_DETACH;
    }

    // Optional: tune cadence by animation
    private int getFlapInterval(GarholdAnimationState anim) {
        return switch (anim) {
            case FORCE_DETACH -> Math.max(6, FLAP_INTERVAL_TICKS - 4); // faster
            case ASCEND       -> FLAP_INTERVAL_TICKS + 4;              // slower
            case REATTACH     -> FLAP_INTERVAL_TICKS + 2;              // slightly slower
            default           -> FLAP_INTERVAL_TICKS;                  // FLY / FLY_2
        };
    }

    public void playGarholdSound(SoundEvent soundEvent) {
        float radius = 32.0f;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        for (ServerPlayer player : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(this.getId(), soundEvent.getLocation(), SoundSource.HOSTILE), player);
        }
    }

    public void stopGarholdSounds() {
        float radius = 32.0f;
        AABB detectionBox = this.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        List<ResourceLocation> soundsToStop = new ArrayList<>(List.of());
        soundsToStop.add(DNLSounds.GARHOLD_CLOSING.get().getLocation());
        for (ServerPlayer player : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStop) {
                Services.NETWORK.sendToPlayer(new S2CStopTickingSoundPacket(this.getId(), sound, 20, true), player);
            }
        }
    }

    private void openBottom() {
        this.entityData.set(BOTTOM_OPENESS, true);
    }

    private void closeBottom() {
        this.entityData.set(BOTTOM_OPENESS, false);
    }

    private void openSide() {
        this.entityData.set(SIDE_OPENESS, true);
    }

    private void closeSide() {
        this.entityData.set(SIDE_OPENESS, false);
    }

    public boolean isChainBlock(BlockPos pos) {
        return this.level().getBlockState(pos).is(Blocks.CHAIN);
    }

    public GarholdState getGarholdState() {
        return this.entityData.get(STATE);
    }

    public void setGarholdState(GarholdState state) {
        this.entityData.set(STATE, state);
    }

    public boolean isGarholdState(GarholdState state) {
        return this.entityData.get(STATE).equals(state);
    }

    public boolean isNoAnimation() {
        return this.entityData.get(ANIMATION_STATE).equals(GarholdAnimationState.NONE);
    }

    public static void spawnTeleportSpawnerFx(ServerLevel level, Entity e) {
        AABB box = e.getBoundingBox();

        double centerX = box.getCenter().x;
        double centerY = box.getCenter().y;
        double centerZ = box.getCenter().z;

        double halfWidth = box.getXsize() * 0.5;
        double halfHeight = box.getYsize() * 0.5;
        double halfDepth = box.getZsize() * 0.5;

        level.sendParticles(
                ParticleTypes.FLAME,
                centerX, centerY, centerZ,
                30,
                halfWidth, halfHeight, halfDepth,
                0.001
        );

        level.sendParticles(
                ParticleTypes.POOF,
                centerX, centerY, centerZ,
                30,
                halfWidth, halfHeight, halfDepth,
                0.001
        );
    }

    public void playReattachAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                GarholdAnimationState.REATTACH,
                GarholdAnimationDuration.REATTACH,
                null,
                () -> {
                    this.setGarholdState(GarholdState.CHAINED);
                    this.playIdleHanging();
                    this.closeBottom();
                    this.closeSide();
                }
        ));
    }

    private void playForceDetach() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdAnimationState.FORCE_DETACH, GarholdAnimationDuration.FORCE_DETACH, null, () -> {
            this.setGarholdState(GarholdState.FLYING);
            this.openSide();
            this.openBottom();
        }));
    }

    private void playDetach() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(GarholdAnimationState.DETACH, GarholdAnimationDuration.DETACH, null, () -> {
            this.setGarholdState(GarholdState.FLYING);
            this.openSide();
            this.openBottom();
        }));

    }

    private void playIdleHanging() {
        if (this.isGarholdState(GarholdState.CHAINED)) {
            this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                    GarholdAnimationState.IDLE_HANGING,
                    GarholdAnimationDuration.IDLE_HANGING,
                    null,
                    this::playIdleHanging
            ));
        }
    }

    public void playChargeDiveWithProgress(BiConsumer<GarholdAnimationState, Float> onProgress, Runnable onComplete) {

        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                GarholdAnimationState.CHARGE_DIVE,
                GarholdAnimationDuration.CHARGE_DIVE,
                () -> {
                    this.closeBottom();
                    this.closeSide();
                    this.playSound(DNLSounds.GARHOLD_CHARGE_DIVE.get());
                },
                onComplete,
                onProgress
        ));
    }

    public void playLandDiveAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                GarholdAnimationState.LAND_DIVE,
                GarholdAnimationDuration.LAND_DIVE,
                () -> {
                    this.playSound(DNLSounds.GARHOLD_LAND_DIVE.get());
                },
                () -> {
                    if (this.hasCapturedPlayer()) {
                        this.playClosingGateAnimation();
                    } else {
                        this.playAscendAnimation(true);
                    }
                }
        ));
    }

    private boolean playedSideBarOpenSound;

    public void playSideCaptureAnimation() {
        this.animationChainer.reset();

        this.animationChainer.enqueue(
                AnimationChainer.AnimationStep.of(
                        GarholdAnimationState.SIDE_CAPTURE,
                        GarholdAnimationDuration.SIDE_CAPTURE,
                        () -> {
                            this.playSound(DNLSounds.GARHOLD_SIDE_BAR_CLOSE.get());
                            this.playedSideBarOpenSound = false; // reset for this animation
                        },
                        () -> { // onComplete
                            if (this.hasCapturedPlayer()) {
                                this.playSound(DNLSounds.GARHOLD_LAND_DIVE.get());
                                this.playClosingGateAnimation();
                                this.closeBottom();
                            } else {
                                this.playAscendAnimation(true);
                            }
                        },
                        (state, progress) -> {
                            if (!playedSideBarOpenSound && progress >= 0.75f) {
                                playedSideBarOpenSound = true;
                                this.playSound(DNLSounds.GARHOLD_SIDE_BAR_OPEN.get());
                            }
                        }
                )
        );
    }

    public void playClosingGateAnimation() {
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                GarholdAnimationState.CLOSING_GATE,
                GarholdAnimationDuration.CLOSING_GATE,
                () -> this.playGarholdSound(DNLSounds.GARHOLD_CLOSING.get()),
                () -> {
                    teleportRiderToChainedGarhold();

                    this.playAscendAnimation(false);
                }
        ));
    }

    public void playAscendAnimation(boolean missed) {
        this.missedCapture = missed;

        this.animationChainer.enqueue(
                AnimationChainer.AnimationStep.of(
                        GarholdAnimationState.ASCEND,
                        GarholdAnimationDuration.ASCEND,
                        () -> { // onStart
                            if (missedCapture) {
                                this.playWideToOpenedAnimation();
                            }
                            this.openSide();
                            ascendLiftActive = false;
                            ascendLiftTicksLeft = 0;
                            openBottom();
                        },
                        () -> { // onComplete
                            ascendLiftActive = false;
                            ascendLiftTicksLeft = 0;

                            this.setGarholdState(GarholdState.FLYING);
                        },
                        (anim, progress) -> {
                            if (this.hasCapturedPlayer()) this.forceDismountPassengerUnderSelf();
                            if (!ascendLiftActive && progress >= ASCEND_LIFT_START_PROGRESS) {
                                this.playSound(DNLSounds.GARHOLD_FLAP.get(), FLAP_VOL, 1.0F);
                                ascendLiftActive = true;
                                ascendLiftTicksLeft = ASCEND_LIFT_TICKS;
                            }
                        }
                )
        );
    }

    // Side Animations

    public void playWideToOpenedAnimation() {
        this.sideGateAnimationChainer.reset();
        this.sideGateAnimationChainer.enqueue(AnimationChainer.AnimationStep.of(
                GarholdSideGateAnimationState.SIDE_WIDE_TO_OPENED,
                GarholdAnimationDuration.SIDE_WIDE_TO_OPENED,
                null,
                () -> this.entityData.set(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.NONE)
        ));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            GarholdAnimationState animationState = this.entityData.get(ANIMATION_STATE);
            this.resetAnimation();
            switch (animationState) {
                case DETACH -> this.detachAnimationState.startIfStopped(this.tickCount);
                case FORCE_DETACH -> this.forceDetachAnimationState.startIfStopped(this.tickCount);
                case CHARGE_DIVE -> this.chargeDiveAnimationState.startIfStopped(this.tickCount);
                case LAND_DIVE -> this.landDiveAnimationState.startIfStopped(this.tickCount);
                case CLOSING_GATE -> this.closingGateAnimationState.startIfStopped(this.tickCount);
                case SIDE_CAPTURE -> this.sideCaptureAnimationState.startIfStopped(this.tickCount);
                case ASCEND -> this.ascendAnimationState.startIfStopped(this.tickCount);
                case IDLE_HANGING -> this.idleHangAnimationState.startIfStopped(this.tickCount);
                case REATTACH -> this.reattachAnimationState.startIfStopped(this.tickCount);
            }
        }

        if (SIDE_GATE_ANIMATION_STATE.equals(entityDataAccessor)) {
            GarholdSideGateAnimationState sideGateAnimationState = this.entityData.get(SIDE_GATE_ANIMATION_STATE);
            this.resetSideGateAnimation();
            if (sideGateAnimationState == GarholdSideGateAnimationState.SIDE_WIDE_TO_OPENED) {
                this.sideWideToOpenedAnimationState.startIfStopped(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetAnimation() {
        this.detachAnimationState.stop();
        this.forceDetachAnimationState.stop();
        this.chargeDiveAnimationState.stop();
        this.landDiveAnimationState.stop();
        this.closingGateAnimationState.stop();
        this.sideCaptureAnimationState.stop();
        this.ascendAnimationState.stop();
        this.idleHangAnimationState.stop();
        this.reattachAnimationState.stop();
    }

    private void resetSideGateAnimation() {
        this.sideWideToOpenedAnimationState.stop();
    }

    public GarholdEntity transitionTo(GarholdAnimationState state) {
        switch (state) {
            case DETACH -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.DETACH);
            case FORCE_DETACH -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.FORCE_DETACH);
            case CHARGE_DIVE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.CHARGE_DIVE);
            case LAND_DIVE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.LAND_DIVE);
            case CLOSING_GATE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.CLOSING_GATE);
            case SIDE_CAPTURE -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.SIDE_CAPTURE);
            case ASCEND -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.ASCEND);
            case IDLE_HANGING -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.IDLE_HANGING);
            case REATTACH -> this.entityData.set(ANIMATION_STATE, GarholdAnimationState.REATTACH);
        }
        return this;
    }

    public GarholdEntity transitionTo(GarholdSideGateAnimationState state) {
        switch (state) {
            case NONE -> this.entityData.set(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.NONE);
            case SIDE_WIDE_TO_OPENED -> this.entityData.set(SIDE_GATE_ANIMATION_STATE, GarholdSideGateAnimationState.SIDE_WIDE_TO_OPENED);
        }
        return this;
    }

    public enum GarholdState {
        CHAINED,
        LOCKING_ON_CHAIN,
        ATTACHING,
        FORCE_DETACHING,
        DETACHING,
        FLYING,
        DIVE,
        SIDE_CAPTURE
    }

    public enum GarholdSideGateAnimationState {
        NONE,
        SIDE_WIDE_TO_OPENED
    }

    public enum GarholdAnimationState {
        NONE,
        DETACH,
        FORCE_DETACH,
        CHARGE_DIVE,
        LAND_DIVE,
        CLOSING_GATE,
        SIDE_CAPTURE,
        ASCEND,
        IDLE_HANGING,
        REATTACH
    }
}
