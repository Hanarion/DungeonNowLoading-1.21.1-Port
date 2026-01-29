package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.SpawnerCarrierAttackGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SpawnerCarrierEntity extends Monster {
    public final AnimationState attackAnimationState = new AnimationState();
    private static final EntityDataAccessor<String> STORED_ENTITY_ID = SynchedEntityData.defineId(SpawnerCarrierEntity.class, EntityDataSerializers.STRING);
    private int summonTick = 200;
    @Nullable public transient Entity previewEntity;
    @Nullable public transient String previewEntityId;
    public float previewSpin = 0.0F;
    public float previewOSpin = 0.0F;

    private final float SPAWN_RANGE = 5;
    private CompoundTag storedEntityNbt = null;

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
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putString("StoredEntityId", this.entityData.get(STORED_ENTITY_ID));
        if (this.storedEntityNbt != null) {
            tag.put("StoredEntityNbt", this.storedEntityNbt);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.entityData.set(STORED_ENTITY_ID, tag.getString("StoredEntityId"));
        this.storedEntityNbt = tag.contains("StoredEntityNbt", CompoundTag.TAG_COMPOUND)
                ? tag.getCompound("StoredEntityNbt")
                : null;
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
    }

    public boolean storeMobFrom(LivingEntity mob) {
        // Don’t store players, bosses, or itself, etc.
        if (mob instanceof Player) return false;
        if (mob.getType() == this.getType()) return false;

        // Type id
        var key = EntityType.getKey(mob.getType());
        if (key == null) return false;

        this.entityData.set(STORED_ENTITY_ID, key.toString());

        // NBT snapshot
        CompoundTag nbt = new CompoundTag();
        mob.saveWithoutId(nbt);

        // Strip / sanitize things you *usually* don’t want to clone
        nbt.remove("UUID");
        nbt.remove("Pos");
        nbt.remove("Motion");
        nbt.remove("Rotation");
        nbt.remove("Dimension");
        nbt.remove("Passengers");
        nbt.remove("Leash");

        // Optional: avoid copying active aggression / targets
        nbt.remove("Brain");
        nbt.remove("HurtTime");
        nbt.remove("HurtByTimestamp");

        this.storedEntityNbt = nbt;
        return true;
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
    public boolean doHurtTarget(Entity entity) {
        this.level().broadcastEntityEvent(this, (byte) 4);
        return super.doHurtTarget(entity);
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 4) {
            this.attackAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(b);
        }
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
    protected SoundEvent getHurtSound(DamageSource $$0) { return SoundEvents.IRON_GOLEM_HURT; }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 2.0F);
    }
}
