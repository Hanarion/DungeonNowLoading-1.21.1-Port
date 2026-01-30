package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.SpawnerCarrierAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SpawnerCarrierEntity extends Monster {
    private static final EntityDataAccessor<String> STORED_ENTITY_ID = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> PICKAXE_ACCUM_DAMAGE = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<SpawnerCarrierAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityStates.SPAWNER_CARRIER_ANIMATION_STATE);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState groundSmashAnimationState = new AnimationState();
    public final AnimationState summonAnimationState = new AnimationState();
    public final AnimationState summonEndAnimationState = new AnimationState();
    private AnimationChainer<SpawnerCarrierAnimationState> animationChainer = new AnimationChainer<>();

    private int summonTick = 200;
    @Nullable public transient Entity previewEntity;
    @Nullable public transient String previewEntityId;
    public float previewSpin = 0.0F;
    public float previewOSpin = 0.0F;
    private CompoundTag storedEntityNbt = null;

    private boolean wasMoving = false;
    private boolean locomotionLocked = false;

    private final float SPAWN_RANGE = 5;

    private static final float FRAG_THRESHOLD = 5.0F;
    private static final float FRAG_MAX = 40.0F;
    private static final float FRAG_SPAWN_HEIGHT = 1.0F;

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
        this.goalSelector.addGoal(1, new SpawnerCarrierAttackGoal(this, 1.0F, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, SpawnerCarrierEntity.class)).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STORED_ENTITY_ID, "");
        this.entityData.define(PICKAXE_ACCUM_DAMAGE, 0.0F);
        this.entityData.define(ANIMATION_STATE, SpawnerCarrierAnimationState.IDLE);
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

            // mirror BaseSpawner: (1000 / (delay + 200)) degrees per tick
            // you can use summonTick or a client-synced delay value (see note below)
            float delay = Math.max(0, this.summonTick); // this field is server-only right now unless you sync it
            float add = 1000.0F / (delay + 200.0F);
            this.previewSpin = (this.previewSpin + add) % 360.0F;
        }

        if (this.level().isClientSide) return;

        animationChainer.tick(this::transitionTo);
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
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (!(this.level() instanceof ServerLevel level)) return;
        if (!(this.getTarget() instanceof Player)) return;
        if (this.isSpawnerBroken()) return;

        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.75D, this.getZ(),
                1, 0.25D, 0.15D, 0.25D, 0.0D);

        if (summonTick-- > 0) return;

        ensureStoredMob();

        int spawnCount = this.getRandom().nextInt(1, 4);
        BlockPos centerPos = this.getOnPos();

        for (int i = 0; i < spawnCount; i++) {
            RandomSource r = level.getRandom();

            double x = centerPos.getX() + (r.nextDouble() - r.nextDouble()) * this.SPAWN_RANGE + 0.5;
            double y = centerPos.getY() + r.nextInt(3) - 1;
            double z = centerPos.getZ() + (r.nextDouble() - r.nextDouble()) * this.SPAWN_RANGE + 0.5;

            if (!canSpawnAt(level, x, y, z)) continue;

            level.sendParticles(ParticleTypes.POOF, x, y + 0.5, z, 20, 0.3D, 0.3D, 0.3D, 0.0D);
            level.sendParticles(ParticleTypes.FLAME, x, y + 0.5, z, 10, 0.3D, 0.3D, 0.3D, 0.0D);

            this.spawnStoredMob(level, x, y, z);
        }

        summonTick = 100 + this.getRandom().nextInt(0, 5) * 20;
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

    public boolean hasStoredMob() {
        return !this.entityData.get(STORED_ENTITY_ID).isEmpty() && this.storedEntityNbt != null;
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
        this.playSound(SoundEvents.GLASS_BREAK, 1.0F, 1.0F); //TODO: replace with your actual break sound

        this.entityData.set(STORED_ENTITY_ID, "");
        this.storedEntityNbt = null;
        this.previewEntity = null;
        this.previewEntityId = null;
    }


    @Override
    public boolean doHurtTarget(Entity entity) {
        this.level().broadcastEntityEvent(this, (byte) 4);
        return super.doHurtTarget(entity);
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
    protected SoundEvent getHurtSound(DamageSource $$0) { return SoundEvents.IRON_GOLEM_HURT; }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 2.0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            SpawnerCarrierAnimationState animationState = this.entityData.get(ANIMATION_STATE);
            this.resetAnimation();
            switch (animationState) {
                case GROUND_SMASH -> this.groundSmashAnimationState.startIfStopped(this.tickCount);
                case SUMMON -> this.summonAnimationState.startIfStopped(this.tickCount);
                case SUMMON_END -> this.summonEndAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetAnimation() {
        this.idleAnimationState.stop();
        this.walkAnimationState.stop();
        this.groundSmashAnimationState.stop();
        this.summonAnimationState.stop();
        this.summonEndAnimationState.stop();
    }


    public SpawnerCarrierEntity transitionTo(SpawnerCarrierAnimationState state) {
        switch (state) {
            case IDLE -> this.entityData.set(ANIMATION_STATE, SpawnerCarrierAnimationState.IDLE);
            case GROUND_SMASH -> this.entityData.set(ANIMATION_STATE, SpawnerCarrierAnimationState.GROUND_SMASH);
            case SUMMON -> this.entityData.set(ANIMATION_STATE, SpawnerCarrierAnimationState.SUMMON);
            case SUMMON_END -> this.entityData.set(ANIMATION_STATE, SpawnerCarrierAnimationState.SUMMON_END);
        }
        return this;
    }

    public enum SpawnerCarrierAnimationState {
        IDLE,
        GROUND_SMASH,
        SUMMON,
        SUMMON_END,
    }
}
