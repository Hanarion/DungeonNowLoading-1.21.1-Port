package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.spawner_carrier.SpawnerCarrierApproachAndSmashGoal;
import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.SpawnerCarrierAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class SpawnerCarrierEntity extends Monster {
    private static final EntityDataAccessor<String> STORED_ENTITY_ID = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> PICKAXE_ACCUM_DAMAGE = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<SpawnerCarrierAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityStates.SPAWNER_CARRIER_ANIMATION_STATE);
    private static final EntityDataAccessor<Integer> ANIMATION_SEQ = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState groundSmashAnimationState = new AnimationState();
    public final AnimationState summonAnimationState = new AnimationState();
    public final AnimationState summonEndAnimationState = new AnimationState();
    private AnimationChainer<SpawnerCarrierAnimationState> animationChainer = new AnimationChainer<>();

    @Nullable public transient Entity previewEntity;
    @Nullable public transient String previewEntityId;
    public float previewSpin = 0.0F;
    public float previewOSpin = 0.0F;
    private CompoundTag storedEntityNbt = null;
    private boolean locomotionLocked = false;
    private int summonLoopsRemaining = 0;
    private boolean summonTriggeredThisLoop = false;


    private final float SPAWN_RANGE = 5;
    private static final int SUMMON_TRIES_PER_LOOP = 10;
    private static final float FRAG_THRESHOLD = 5.0F;
    private static final float FRAG_MAX = 40.0F;
    private static final float FRAG_SPAWN_HEIGHT = 1.0F;

    // --- Minion upkeep config ---
    private static final int MINIONS_DESIRED = 3;

    private static final int CHARGE_MAX = 3;
    private static final int CHARGE_RECHARGE_TICKS = 20 * 10; // 5 seconds

    private static final int SUMMON_TRIES_PER_CHARGE = 10;

    // --- Runtime state ---
    private int spawnCharges = CHARGE_MAX;
    private int rechargeTicks = CHARGE_RECHARGE_TICKS;

    // Track spawned mobs so we know how many are still alive
    private final ArrayList<UUID> spawnedMinions;

    // prevents re-triggering the whole chain every tick
    private int upkeepCooldownTicks = 0;

    private boolean groundSmashHitTriggered = false;

    private static final float GROUND_SMASH_HIT_AT = 0.75f;
    private static final float GROUND_SMASH_RADIUS = 3.5f;
    private static final float GROUND_SMASH_RADIUS_SQR = GROUND_SMASH_RADIUS * GROUND_SMASH_RADIUS;
    private static final float GROUND_SMASH_KB_H = 1.1f;     // horizontal knockback
    private static final float GROUND_SMASH_KB_Y = 0.35f;

    private static final String[] DEFAULT_POOL = {
            "minecraft:zombie",
            "minecraft:skeleton",
            "minecraft:spider",
            "minecraft:creeper",
            "minecraft:cave_spider"
    };

    public SpawnerCarrierEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.0F);
        this.xpReward = 20;
        this.spawnedMinions = new ArrayList<>();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.25)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SpawnerCarrierApproachAndSmashGoal(this, 1.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        //this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, SpawnerCarrierEntity.class)).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STORED_ENTITY_ID, "");
        this.entityData.define(PICKAXE_ACCUM_DAMAGE, 0.0F);
        this.entityData.define(ANIMATION_STATE, SpawnerCarrierAnimationState.IDLE);
        this.entityData.define(ANIMATION_SEQ, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putString("StoredEntityId", this.entityData.get(STORED_ENTITY_ID));
        if (this.storedEntityNbt != null) {
            tag.put("StoredEntityNbt", this.storedEntityNbt);
        }
        tag.putFloat("PickaxeAccumDamage", this.getPickaxeAccumDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.entityData.set(STORED_ENTITY_ID, tag.getString("StoredEntityId"));
        this.storedEntityNbt = tag.contains("StoredEntityNbt", CompoundTag.TAG_COMPOUND)
                ? tag.getCompound("StoredEntityNbt")
                : null;
        if (tag.contains("PickaxeAccumDamage")) {
            this.entityData.set(PICKAXE_ACCUM_DAMAGE, tag.getFloat("PickaxeAccumDamage"));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.previewOSpin = this.previewSpin;

            SpawnerCarrierAnimationState s = this.entityData.get(ANIMATION_STATE);

            // degrees per tick (tune these)
            float add = switch (s) {
                case SUMMON -> 12.0F;       // fast spin while summoning
                case GROUND_SMASH -> 4.0F;  // medium spin during windup/smash
                case SUMMON_END -> 2.0F;    // slow down
                default -> 1.0F;            // idle / walking
            };

            this.previewSpin = (this.previewSpin + add) % 360.0F;
        }

        if (this.level().isClientSide) return;

        pruneSpawnedMinions();

        // recharge every 5 seconds
        if (spawnCharges < CHARGE_MAX) {
            if (--rechargeTicks <= 0) {
                spawnCharges++;
                rechargeTicks = CHARGE_RECHARGE_TICKS;
            }
        } else {
            rechargeTicks = CHARGE_RECHARGE_TICKS;
        }

        if (upkeepCooldownTicks > 0) upkeepCooldownTicks--;

        // auto-upkeep during combat
        tryTriggerUpkeepSummon();

        animationChainer.tick(this::transitionTo);
    }

    private void pruneSpawnedMinions() {
        if (!(this.level() instanceof ServerLevel level)) return;

        spawnedMinions.removeIf(uuid -> {
            Entity e = level.getEntity(uuid);
            return e == null || !e.isAlive();
        });
    }

    private int getAliveMinionCount() {
        return spawnedMinions.size();
    }


    @Nullable
    public Entity spawnStoredMob(ServerLevel level, double x, double y, double z) {
        if (this.entityData.get(STORED_ENTITY_ID).isEmpty()) return null;

        CompoundTag tag = (this.storedEntityNbt == null ? new CompoundTag() : this.storedEntityNbt.copy());
        tag.putString("id", this.getStoredEntityId());

        Entity spawned = EntityType.loadEntityRecursive(tag, level, (entity) -> {
            entity.moveTo(x, y, z, entity.getYRot(), entity.getXRot());
            return entity;
        });

        if (spawned == null) return null;

        if (spawned instanceof Mob mob) {
            BlockPos pos = BlockPos.containing(x, y, z);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
            mob.setTarget(this.getTarget());

        }

        level.addFreshEntity(spawned);
        return spawned;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide && locomotionLocked) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            this.hasImpulse = true; // helps syncing sometimes
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.isSpawnerBroken()) return;

        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.5D, this.getZ(), 1, 0.25D, 0.45D, 0.25D, 0.0D);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Creative-only behavior
        if (!player.getAbilities().instabuild) {
            return super.mobInteract(player, hand);
        }

        // Only works with spawn eggs
        if (!(stack.getItem() instanceof SpawnEggItem egg)) {
            return super.mobInteract(player, hand);
        }

        // Server-side only for state changes
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Don't allow changing if spawner is broken
        if (this.isSpawnerBroken()) {
            return InteractionResult.CONSUME;
        }

        // Determine entity type from the spawn egg (handles NBT overrides)
        EntityType<?> type = egg.getType(stack.getTag());
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        if (key == null) {
            return InteractionResult.CONSUME;
        }

        // Store the id
        this.entityData.set(STORED_ENTITY_ID, key.toString());

        // Store the egg's EntityTag (variants, custom name, etc.)
        CompoundTag newStored = new CompoundTag();
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains("EntityTag", CompoundTag.TAG_COMPOUND)) {
            newStored = stack.getTag().getCompound("EntityTag").copy();
        }
        this.storedEntityNbt = newStored;

        // Refresh preview entity (client will recreate)
        this.previewEntity = null;
        this.previewEntityId = null;

        // Nice feedback (optional)
        ((ServerLevel)this.level()).sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.2, this.getZ(),
                12, 0.25, 0.25, 0.25, 0.0);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                DNLSounds.SPAWNER_CARRIER_CHARGE.get(), SoundSource.HOSTILE, 1.0F, 1.2F);

        return InteractionResult.CONSUME; // consumes the interaction (doesn't spawn the egg mob)
    }


    private boolean trySummonSingleMobWithRetries(int tries) {
        if (!(this.level() instanceof ServerLevel level)) return false;
        if (this.isSpawnerBroken()) return false;

        String id = this.entityData.get(STORED_ENTITY_ID);
        if (id.isEmpty()) return false; // empty spawner allowed

        BlockPos centerPos = this.getOnPos();
        RandomSource r = level.getRandom();

        for (int attempt = 0; attempt < tries; attempt++) {
            double x = centerPos.getX() + (r.nextDouble() - r.nextDouble()) * this.SPAWN_RANGE + 0.5;
            double y = centerPos.getY() + r.nextInt(3) - 1;
            double z = centerPos.getZ() + (r.nextDouble() - r.nextDouble()) * this.SPAWN_RANGE + 0.5;

            if (!canSpawnAt(level, x, y, z)) continue;

            level.sendParticles(ParticleTypes.POOF, x, y + 0.5, z, 12, 0.25D, 0.25D, 0.25D, 0.0D);
            level.sendParticles(ParticleTypes.FLAME, x, y + 0.5, z, 6, 0.25D, 0.25D, 0.25D, 0.0D);

            level.playSound(null, x, y, z, DNLSounds.SPAWNER_CARRIER_SPAWN_MOB.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

            Entity spawned = this.spawnStoredMob(level, x, y, z);
            if (spawned != null) {
                spawnedMinions.add(spawned.getUUID());
                return true;
            }
        }

        return false;
    }

    private void tryTriggerUpkeepSummon() {
        if (this.level().isClientSide) return;

        // only during combat
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        // no spawner / broken spawner => no upkeep
        if (this.isSpawnerBroken()) return;
        if (this.entityData.get(STORED_ENTITY_ID).isEmpty()) return; // empty spawner allowed

        // don’t interrupt other attacks
        if (this.isBusyAttacking() || this.isLocomotionLocked()) return;

        // don’t spam trigger
        if (upkeepCooldownTicks > 0) return;

        int alive = getAliveMinionCount();
        if (alive >= MINIONS_DESIRED) return;

        int needed = MINIONS_DESIRED - alive;
        if (spawnCharges <= 0) return;

        // how many summons we can actually do right now
        int loops = Math.min(needed, spawnCharges);

        startUpkeepSummonChain(loops);

        // small cooldown so we don’t re-start instantly if something fails
        upkeepCooldownTicks = 10;
    }

    private void startUpkeepSummonChain(int loops) {
        this.animationChainer.reset();
        this.setLocomotionLocked(true);

        // store a max cap if you want, but don't force it here
        this.summonLoopsRemaining = loops;

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                SpawnerCarrierAnimationState.GROUND_SMASH,
                SpawnerCarrierAnimationDuration.GROUND_SMASH,
                () -> {
                    this.groundSmashHitTriggered = false;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), DNLSounds.SPAWNER_CARRIER_SMASH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                },
                this::queueNextSummonStep,
                (anim, progress) -> {
                    if (!this.groundSmashHitTriggered && progress >= GROUND_SMASH_HIT_AT) {
                        this.groundSmashHitTriggered = true;
                        doGroundSmashHit();
                    }
                }
        ));
    }



    private void ensureStoredMob() {
        if (!this.entityData.get(STORED_ENTITY_ID).isEmpty()) return;

        String id = DEFAULT_POOL[this.random.nextInt(DEFAULT_POOL.length)];
        this.entityData.set(STORED_ENTITY_ID, id);

        // Empty tag = default variant. (Still valid with loadEntityRecursive if id is set)
        this.storedEntityNbt = new CompoundTag();
    }

    private boolean canSpawnAt(ServerLevel level, double x, double y, double z) {
        var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                new net.minecraft.resources.ResourceLocation(this.getStoredEntityId())
        ).orElse(null);

        if (type == null) return false;

        Entity temp = type.create(level);
        if (temp == null) return false;

        temp.moveTo(x, y, z, 0, 0);
        return level.noCollision(temp);
    }

    public String getStoredEntityId() {
        return this.entityData.get(STORED_ENTITY_ID);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);

        // Only choose randomly when it "naturally" appears in the world
        if (spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION || spawnType == MobSpawnType.SPAWN_EGG) {
            if (this.entityData.get(STORED_ENTITY_ID).isEmpty()) {
                String id = DEFAULT_POOL[this.random.nextInt(DEFAULT_POOL.length)];
                this.entityData.set(STORED_ENTITY_ID, id);
                this.storedEntityNbt = new CompoundTag(); // default variant
            }
        }

        return data;
    }


    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.95F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean didHurt = super.hurt(source, amount);
        if (!didHurt) return false;

        // Server only
        if (this.level().isClientSide) return true;

        // Already broken? ignore pickaxe mining
        if (this.isSpawnerBroken()) return true;

        // Must be a player using a pickaxe as the direct hit
        Entity attacker = source.getEntity();
        if (!(attacker instanceof Player player)) return true;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof PickaxeItem)) return true;

        // Accumulate the actual damage dealt by this hit
        float before = this.getPickaxeAccumDamage();
        float after = Mth.clamp(before + amount, 0.0F, FRAG_MAX);

        int beforeSteps = (int)(before / FRAG_THRESHOLD);
        int afterSteps  = (int)(after  / FRAG_THRESHOLD);

        // Drop 1 fragment per crossed 5-damage threshold
        int toDrop = Math.max(0, afterSteps - beforeSteps);
        if (toDrop > 0) {

            int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, held);
            float height = this.getBbHeight();

            for (int i = 0; i < toDrop; i++) {
                int count = fortuneLikeCount(this.getRandom(), fortune);

                this.spawnAtLocation(new ItemStack(DNLItems.SPAWNER_FRAGMENT.get(), count), height);

                spawnSpawnerChipParticles((ServerLevel) this.level(), 10);
            }
        }

        this.playSound(DNLSounds.SPAWNER_CARRIER_HURT.get(), 1.0F, 1.0F);

        this.entityData.set(PICKAXE_ACCUM_DAMAGE, after);

        // When it reaches 40, "break" the spawner (stop future spawns)
        if (after >= FRAG_MAX) {
            onSpawnerBreak((ServerLevel) this.level());
        }

        return true;
    }

    private void onSpawnerBreak(ServerLevel level) {
        // Visual/audio feedback (tweak as you like)
        spawnSpawnerChipParticles(level, 40);
        level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.5, this.getZ(),
                20, 0.3D, 0.3D, 0.3D, 0.0D);
        this.playSound(DNLSounds.SPAWNER_CARRIER_SPAWNER_BREAK.get(), 1.0F, 1.0F);

        this.entityData.set(STORED_ENTITY_ID, "");
        this.storedEntityNbt = null;
        this.previewEntity = null;
        this.previewEntityId = null;
    }


    @Override
    public boolean doHurtTarget(Entity entity) {
        return false;
    }

    @Override
    protected void updateWalkAnimation(float v) {
        float w;
        if (this.getPose() == Pose.STANDING) {
            w = Math.min(v * 6.0F, 1.0F);
        } else {
            w = 0.0F;
        }

        this.walkAnimation.update(w, 1.0F);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        int base = this.getDeathFragmentValue(); // your 0..4 value based on spawner damage
        if (base <= 0) return;

        float height = this.getBbHeight();

        // Base payout: either 1 frame (value 4) or fragments (1..3)
        if (base == 4) {
            this.spawnAtLocation(new ItemStack(DNLItems.SPAWNER_FRAME.get(), 1), height);
        } else {
            this.spawnAtLocation(new ItemStack(DNLItems.SPAWNER_FRAGMENT.get(), base), height);
        }

        // Looting bonus: fragments on top (also works "on top of the frame")
        int extra = lootingExtraCount(this.getRandom(), looting);
        if (extra > 0) {
            this.spawnAtLocation(new ItemStack(DNLItems.SPAWNER_FRAGMENT.get(), extra), height);
        }
    }

    public float getSpawnerDamageProgress() {
        return Mth.clamp(this.getPickaxeAccumDamage() / FRAG_MAX, 0.0F, 1.0F);
    }

    // Death reward in "fragment value" (0..4)
    public int getDeathFragmentValue() {
        float dmg = Mth.clamp(this.getPickaxeAccumDamage(), 0.0F, FRAG_MAX);
        int lost = (int)(dmg / 10.0F);      // 0..4
        return Mth.clamp(4 - lost, 0, 4);
    }

    public float getPickaxeAccumDamage() {
        return this.entityData.get(PICKAXE_ACCUM_DAMAGE);
    }

    public int getSpawnerCrackStage() {
        // -1 means "no crack overlay"
        if (this.isSpawnerBroken()) return -1;

        float p = Mth.clamp(this.getPickaxeAccumDamage() / FRAG_MAX, 0.0F, 1.0F);
        int stage = (int)(p * 10.0F); // 0..10
        return Mth.clamp(stage, 0, 9); // vanilla has 0..9
    }


    public boolean isSpawnerBroken() {
        return this.getPickaxeAccumDamage() >= FRAG_MAX;
    }

    private static int fortuneLikeCount(net.minecraft.util.RandomSource random, int fortuneLevel) {
        if (fortuneLevel <= 0) return 1;

        int i = random.nextInt(fortuneLevel + 2) - 1;
        if (i < 0) i = 0;
        return 1 * (i + 1); // base is 1 fragment per threshold
    }

    private static int lootingExtraCount(net.minecraft.util.RandomSource random, int lootingLevel) {
        if (lootingLevel <= 0) return 0;
        return random.nextInt(lootingLevel + 1); // 0..looting
    }

// ...

    private void doGroundSmashHit() {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!this.isAlive()) return;

        AABB box = this.getBoundingBox().inflate(GROUND_SMASH_RADIUS, 1.5, GROUND_SMASH_RADIUS);

        ParticleOptions particleData = new ScalableAxisParticleType.ScalableAxisParticleData(DNLParticleTypes.WHITE_SHOCKWAVE_MEDIUM_PARTICLE.get(), 0, 90, GROUND_SMASH_RADIUS * 2 + 1);
        level.sendParticles(particleData, this.getX(), this.getY() + 0.01F, this.getZ(), 1, 0.0d, 0.0d, 0.0d, 0);

        ParticleOptions particleDataSmall = new ScalableAxisParticleType.ScalableAxisParticleData(DNLParticleTypes.WHITE_SHOCKWAVE_MEDIUM_PARTICLE.get(), 0, 90, GROUND_SMASH_RADIUS * 1 + 2);
        level.sendParticles(particleDataSmall, this.getX(), this.getY() + 0.01F, this.getZ(), 1, 0.0d, 0.0d, 0.0d, 0);

        for (Player player : level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator() && !p.getAbilities().invulnerable)) {

            DamageSource src = this.damageSources().mobAttack(this);

            // Was this smash blocked by the player's shield?
            boolean blockedByShield = player.isBlocking() && player.isDamageSourceBlocked(src);

            // Deal damage (shield may fully/partially block depending on vanilla behavior)
            player.hurt(src, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));

            // If blocked, disable shield (axe-like)
            if (blockedByShield) {
                // immediately stop blocking
                player.stopUsingItem();

                // apply cooldown so they can't raise it again
                player.getCooldowns().addCooldown(Items.SHIELD, 100);

                // optional: play the "shield disabled" feel (pick whichever sound you like)
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);

                this.level().broadcastEntityEvent(this, (byte)30);
            }

            // Knockback: apply even if they blocked (or reduce it if you prefer)
            applySmashKnockback(player);
        }
    }


    private void applySmashKnockback(Player player) {
        double dx = player.getX() - this.getX();
        double dz = player.getZ() - this.getZ();
        double len = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
        dx /= len; dz /= len;

        // use delta movement instead of push() (more reliable)
        var v = player.getDeltaMovement();
        player.setDeltaMovement(
                v.x + dx * GROUND_SMASH_KB_H,
                Math.max(v.y, GROUND_SMASH_KB_Y),
                v.z + dz * GROUND_SMASH_KB_H
        );

        player.hurtMarked = true;
        player.hasImpulse = true;
        player.setOnGround(false);
    }


    public boolean isLocomotionLocked() { return locomotionLocked; }

    private void setLocomotionLocked(boolean locked) {
        this.locomotionLocked = locked;
    }

    public boolean isBusyAttacking() {
        if (locomotionLocked) return true;
        var s = this.entityData.get(ANIMATION_STATE);
        return s == SpawnerCarrierAnimationState.GROUND_SMASH
                || s == SpawnerCarrierAnimationState.SUMMON
                || s == SpawnerCarrierAnimationState.SUMMON_END;
    }


    public void playGroundSmashFromGoal() {
        if (this.level().isClientSide) return;
        if (isBusyAttacking()) return;
        playGroundSmashAnimation(); // your private method
    }

    private void spawnSpawnerChipParticles(ServerLevel level, int count) {
        BlockState state = Blocks.SPAWNER.defaultBlockState();

        // Spawn around the spawner area (near top looks best since you're dropping from top)
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                this.getX(),
                this.getY() + this.getBbHeight() * 0.85,
                this.getZ(),
                count,
                0.5, 0.5, 0.5,   // spread
                0.05                // speed
        );
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return DNLSounds.SPAWNER_CARRIER_IDLE.get();
    }


    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.SPAWNER_CARRIER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(DNLSounds.SCUTTLE_STEP.get(), 1.0F, 2.0F);
    }

    private void playGroundSmashAnimation() {
        this.animationChainer.reset();
        this.setLocomotionLocked(true);

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                SpawnerCarrierAnimationState.GROUND_SMASH,
                SpawnerCarrierAnimationDuration.GROUND_SMASH,
                () -> {
                    this.groundSmashHitTriggered = false;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), DNLSounds.SPAWNER_CARRIER_SMASH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                },
                this::afterGroundSmashDecideSummon,
                (anim, progress) -> {
                    if (!this.groundSmashHitTriggered && progress >= GROUND_SMASH_HIT_AT) {
                        this.groundSmashHitTriggered = true;
                        doGroundSmashHit();
                    }
                }
        ));
    }


    private void afterGroundSmashDecideSummon() {
        // If spawner broken, just stand up
        if (this.isSpawnerBroken()) {
            queueSummonEnd();
            return;
        }

        // If we already have enough minions, don't summon
        if (getAliveMinionCount() >= MINIONS_DESIRED) {
            queueSummonEnd();
            return;
        }

        // If no charges, don't summon
        if (spawnCharges <= 0) {
            queueSummonEnd();
            return;
        }

        // Determine how many loops we should do THIS chain
        int needed = MINIONS_DESIRED - getAliveMinionCount();
        int loops = Math.min(needed, spawnCharges);

        // Important: set loopsRemaining to the real number
        this.summonLoopsRemaining = loops;

        // If loops ended up 0 (edge case), just end
        if (this.summonLoopsRemaining <= 0) {
            queueSummonEnd();
            return;
        }

        // Start looping summons
        queueNextSummonStep();
    }


    private void queueNextSummonStep() {
        if (this.isSpawnerBroken()) { queueSummonEnd(); return; }

        if (!needsMoreMinions()) { queueSummonEnd(); return; }

        if (this.summonLoopsRemaining <= 0) { queueSummonEnd(); return; }
        if (spawnCharges <= 0) { queueSummonEnd(); return; } // optional but consistent

        this.summonLoopsRemaining--;

        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                SpawnerCarrierAnimationState.SUMMON,
                SpawnerCarrierAnimationDuration.SUMMON,
                () -> {
                    this.summonTriggeredThisLoop = false;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), DNLSounds.SPAWNER_CARRIER_CHARGE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                },
                () -> {
                    if (!needsMoreMinions()) {
                        this.summonLoopsRemaining = 0;
                        queueSummonEnd();
                        return;
                    }
                    queueNextSummonStep();
                },
                (anim, progress) -> {
                    if (!this.summonTriggeredThisLoop && progress >= 0.75f) {
                        this.summonTriggeredThisLoop = true;
                        if (spawnCharges > 0) spawnCharges--;
                        trySummonSingleMobWithRetries(SUMMON_TRIES_PER_CHARGE);
                    }
                }
        ));
    }

    private void queueSummonEnd() {
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(
                SpawnerCarrierAnimationState.SUMMON_END,
                SpawnerCarrierAnimationDuration.SUMMON_END,
                null,
                () -> {
                    this.setLocomotionLocked(false);
                    this.transitionTo(SpawnerCarrierAnimationState.IDLE);
                }
        ));
    }

    private boolean needsMoreMinions() {
        return getAliveMinionCount() < MINIONS_DESIRED;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (ANIMATION_STATE.equals(key) || ANIMATION_SEQ.equals(key)) {
            SpawnerCarrierAnimationState s = this.entityData.get(ANIMATION_STATE);
            this.resetAnimation();

            switch (s) {
                case GROUND_SMASH -> this.groundSmashAnimationState.start(this.tickCount);
                case SUMMON       -> this.summonAnimationState.start(this.tickCount);
                case SUMMON_END   -> this.summonEndAnimationState.start(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(key);
    }


    private void resetAnimation() {
        this.groundSmashAnimationState.stop();
        this.summonAnimationState.stop();
        this.summonEndAnimationState.stop();
    }


    public SpawnerCarrierEntity transitionTo(SpawnerCarrierAnimationState state) {
        this.entityData.set(ANIMATION_STATE, state);

        int seq = this.entityData.get(ANIMATION_SEQ);
        this.entityData.set(ANIMATION_SEQ, seq + 1);

        return this;
    }


    public enum SpawnerCarrierAnimationState {
        IDLE,
        GROUND_SMASH,
        SUMMON,
        SUMMON_END,
    }
}
