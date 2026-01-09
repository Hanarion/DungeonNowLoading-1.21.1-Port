package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.seeping_soul.SeepingSoulAnimationDuration;
import dev.hexnowloading.dungeonnowloading.entity.util.AnimationChainer;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityStates;
import dev.hexnowloading.dungeonnowloading.entity.util.EventAnimationSystem;
import dev.hexnowloading.dungeonnowloading.entity.util.RecallableDef;
import dev.hexnowloading.dungeonnowloading.registry.DNLRecallables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SeepingSoulEntity extends Entity {

    public AnimationState spawnAnimation = new AnimationState();
    public AnimationState idleAnimation = new AnimationState();
    public AnimationState idleBreakAnimation = new AnimationState();
    public AnimationState recallingAnimation = new AnimationState();
    public AnimationState hurtLeftAnimation = new AnimationState();
    public AnimationState hurtRightAnimation = new AnimationState();

    private AnimationChainer<SeepingSoulAnimationState> animationChainer = new AnimationChainer<>();
    private final EventAnimationSystem eventAnimations = new EventAnimationSystem();


    private static final byte EVENT_HURT_LEFT = 70;
    private static final byte EVENT_HURT_RIGHT = 71;

    private static final int MAX_HP = 10;
    private static final int SPAWN_DELAY_TICKS = 20 * 3; // 3 sec
    private static final int HEAL_DELAY_TICKS = 20 * 5;    // 5 sec
    private static final int HEAL_INTERVAL_TICKS = 20;     // 1 sec
    private static final int CHANNEL_TOTAL_TICKS = 20 * 5; // 5 sec
    private static final int HURT_FLASH_TICKS = 10;

    private static final EntityDataAccessor<Integer> DATA_SPAWN_DELAY = SynchedEntityData.defineId(SeepingSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HP = SynchedEntityData.defineId(SeepingSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DEFEATED_COUNT = SynchedEntityData.defineId(SeepingSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHANNEL_TICKS = SynchedEntityData.defineId(SeepingSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HURT_TICKS = SynchedEntityData.defineId(SeepingSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<SeepingSoulAnimationState> ANIMATION_STATE = SynchedEntityData.defineId(SeepingSoulEntity.class, EntityStates.SEEPING_SOUL_ANIMATION_STATE);

    // Stored as string in NBT
    private ResourceLocation bossId = new ResourceLocation("minecraft", "pig"); // placeholder
    private int lastHitTick = -999999;

    public SeepingSoulEntity(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    private RecallableDef def() {
        return DNLRecallables.get(this.bossId);
    }

    // --- Hitbox 1x1 ---
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(1.0F, 1.0F);
    }

    @Override
    public boolean isPickable() {
        return !isInSpawnDelay();
    }

    @Override
    public boolean isAttackable() {
        return !isInSpawnDelay();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SPAWN_DELAY, SPAWN_DELAY_TICKS);
        this.entityData.define(DATA_HP, MAX_HP);
        this.entityData.define(DATA_DEFEATED_COUNT, 0);
        this.entityData.define(DATA_CHANNEL_TICKS, 0);
        this.entityData.define(DATA_HURT_TICKS, 0);
        this.entityData.define(ANIMATION_STATE, SeepingSoulAnimationState.NONE);
    }

    @Override
    public void tick() {
        super.tick();

        int ht = getHurtTicks();
        if (ht > 0) setHurtTicks(ht - 1);

        if (this.level().isClientSide) {
            eventAnimations.tick();
            spawnVisibleSoulParticles();
            return;
        }

        // --- Spawn delay gate ---
        int delay = getSpawnDelayTicks();
        if (delay > 0) {

            delay--;
            setSpawnDelayTicks(delay);

            if (delay == 0) {
                playSpawnAnimation();
            }

            return;
        }

        // Channeling...
        int channel = getChannelTicks();
        if (channel > 0) {
            channel++;
            if (channel >= CHANNEL_TOTAL_TICKS) {
                setChannelTicks(0);
                summonBoss();
                this.discard();
                return;
            }
            setChannelTicks(channel);
        }

        // Healing...
        int ticksSinceHit = this.tickCount - this.lastHitTick;
        if (ticksSinceHit >= HEAL_DELAY_TICKS) {
            if ((this.tickCount % HEAL_INTERVAL_TICKS) == 0) {
                int hp = getHp();
                if (hp < MAX_HP) setHp(hp + 1);
            }
        }

        animationChainer.tick(this::transitionTo);

        if (this.entityData.get(ANIMATION_STATE).equals(SeepingSoulAnimationState.NONE)) {
            playIdleAnimation();
        }
    }

    private void spawnVisibleSoulParticles() {
        // Only when visible (not in the 5s spawn-delay invis)
        if (this.isInSpawnDelay()) return;

        // 🔽 75% chance to do nothing → quarter particle count
        if (this.random.nextInt(4) != 0) return;

        double x = this.getX();
        double y = this.getY() + 0.55D;
        double z = this.getZ();

        double vx = (this.random.nextDouble() - 0.5D) * 0.02D;
        double vy = 0.02D + this.random.nextDouble() * 0.02D;
        double vz = (this.random.nextDouble() - 0.5D) * 0.02D;

        double rangeXZ = 0.8D;
        double rangeY  = 0.3D;

        double ox = (this.random.nextDouble() - 0.5D) * rangeXZ * 2.0D;
        double oy = (this.random.nextDouble() - 0.5D) * rangeY  * 2.0D;
        double oz = (this.random.nextDouble() - 0.5D) * rangeXZ * 2.0D;

        this.level().addParticle(ParticleTypes.SOUL,
                x + ox, y + oy, z + oz,
                vx, vy, vz
        );

        if (this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    x + ox * 0.8, y + oy * 0.8, z + oz * 0.8,
                    vx * 0.5, vy * 0.5, vz * 0.5
            );
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) return true;

        setHurtTicks(HURT_FLASH_TICKS);

        int hp = getHp() - 1;
        setHp(hp);
        this.lastHitTick = this.tickCount;

        if (hp <= 0) {
            if (this.level() instanceof ServerLevel sl) {
                playDeathFx(sl);
            }
            disperse();
            this.discard();
            return true;
        }

        playHurtAnimation();

        Player player = source.getEntity() instanceof Player p ? p : null;

        if (player == null) return true;

        RecallableDef def = def();
        Component bossName = (def != null) ? def.displayName() : Component.literal("???");
        Component itemName = (def != null) ? def.recallItem().get().getDefaultInstance().getHoverName() : Component.literal("???");

        player.displayClientMessage(Component.translatable("entity.dungeonnowloading.seeping_soul.right_click", itemName, bossName), true);

        return true;
    }

    private void playDeathFx(ServerLevel level) {
        double x0 = this.getX();
        double y0 = this.getY() + 0.5D;
        double z0 = this.getZ();

        // Tight-ish burst radius
        double radius = 0.8D;

        // Poof burst
        for (int i = 0; i < 28; i++) {
            double x = x0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;
            double y = y0 + (level.random.nextDouble() * 2.0 - 1.0) * 0.6;
            double z = z0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;

            double vx = (level.random.nextDouble() * 2.0 - 1.0) * 0.12;
            double vy = 0.05 + level.random.nextDouble() * 0.18;
            double vz = (level.random.nextDouble() * 2.0 - 1.0) * 0.12;

            level.sendParticles(ParticleTypes.POOF, x, y, z, 1, vx, vy, vz, 0.0);
        }

        // Soul burst
        for (int i = 0; i < 22; i++) {
            double x = x0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;
            double y = y0 + level.random.nextDouble() * 0.9;
            double z = z0 + (level.random.nextDouble() * 2.0 - 1.0) * radius;

            double vx = (level.random.nextDouble() * 2.0 - 1.0) * 0.05;
            double vy = 0.03 + level.random.nextDouble() * 0.06;
            double vz = (level.random.nextDouble() * 2.0 - 1.0) * 0.05;

            level.sendParticles(ParticleTypes.SOUL, x, y, z, 1, vx, vy, vz, 0.0);

            if (level.random.nextInt(3) == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, vx * 0.7, vy * 0.7, vz * 0.7, 1.0);
            }
        }
    }

    // --- Right-click interaction + summoning item use ---
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.level().isClientSide) {

            if (isInSpawnDelay()) return InteractionResult.FAIL;

            if (tryStartChanneling(player, stack)) {
                return InteractionResult.CONSUME;
            }

            // Otherwise: show hint
            RecallableDef def = def();
            Component bossName = (def != null) ? def.displayName() : Component.literal("???");
            Component itemName = (def != null)
                    ? def.recallItem().get().getDefaultInstance().getHoverName()
                    : Component.literal("???");

            player.displayClientMessage(
                    Component.translatable(
                            "entity.dungeonnowloading.seeping_soul.right_click",
                            itemName,
                            bossName
                    ),
                    true
            );
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public boolean tryStartChanneling(Player player, ItemStack stack) {
        if (this.level().isClientSide) return false;

        if (isInSpawnDelay()) return false;

        RecallableDef def = def();
        if (def == null) return false;

        if (getChannelTicks() > 0) return false;

        playRecallingAnimation();

        setChannelTicks(1);
        return true;
    }


    private void summonBoss() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        RecallableDef def = def();
        if (def == null) return;

        def.spawner().spawn(sl, this, getDefeatedCount());
    }

    private void disperse() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        RecallableDef def = def();
        if (def == null) return;

        def.disperseHandler().handle(sl, this, getDefeatedCount());
    }

    // --- NBT ---
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Boss", CompoundTag.TAG_STRING)) {
            this.bossId = new ResourceLocation(tag.getString("Boss"));
        }

        setSpawnDelayTicks(Mth.clamp(tag.getInt("SpawnDelayTicks"), 0, SPAWN_DELAY_TICKS));
        setHp(Mth.clamp(tag.getInt("Health"), 0, MAX_HP));
        setDefeatedCount(Mth.clamp(tag.getInt("DefeatedCount"), 0, 100));
        setChannelTicks(Mth.clamp(tag.getInt("ChannelTicks"), 0, CHANNEL_TOTAL_TICKS));
        this.lastHitTick = tag.getInt("LastHitTick");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Boss", this.bossId.toString());
        tag.putInt("Health", getHp());
        tag.putInt("DefeatedCount", getDefeatedCount());
        tag.putInt("ChannelTicks", getChannelTicks());
        tag.putInt("LastHitTick", this.lastHitTick);
        tag.putInt("SpawnDelayTicks", getSpawnDelayTicks());

    }

    // --- Getters/Setters ---
    public int getSpawnDelayTicks() { return this.entityData.get(DATA_SPAWN_DELAY); }
    public void setSpawnDelayTicks(int v) { this.entityData.set(DATA_SPAWN_DELAY, Math.max(0, v)); }

    public boolean isInSpawnDelay() { return getSpawnDelayTicks() > 0; }

    public int getMaxHp() { return MAX_HP; }
    public int getHp() { return this.entityData.get(DATA_HP); }
    public void setHp(int v) { this.entityData.set(DATA_HP, Mth.clamp(v, 0, MAX_HP)); }

    public int getDefeatedCount() { return this.entityData.get(DATA_DEFEATED_COUNT); }
    public void setDefeatedCount(int v) { this.entityData.set(DATA_DEFEATED_COUNT, Mth.clamp(v, 0, 100)); }

    public int getChannelTicks() { return this.entityData.get(DATA_CHANNEL_TICKS); }
    public void setChannelTicks(int v) { this.entityData.set(DATA_CHANNEL_TICKS, Mth.clamp(v, 0, CHANNEL_TOTAL_TICKS)); }

    public ResourceLocation getBossId() { return bossId; }
    public void setBossId(ResourceLocation bossId) { this.bossId = bossId; }

    public int getHurtTicks() { return this.entityData.get(DATA_HURT_TICKS); }
    public void setHurtTicks(int v) { this.entityData.set(DATA_HURT_TICKS, Math.max(0, v)); }

    public boolean isNoAnimation() {
        return this.entityData.get(ANIMATION_STATE).equals(SeepingSoulAnimationState.NONE);
    }

    private void playIdleOrIdleBreak() {
        if (this.random.nextInt(3) == 0) {
            this.animationChainer.enqueue(
                    AnimationChainer.AnimationStep.of(
                            SeepingSoulAnimationState.IDLE_BREAK,
                            SeepingSoulAnimationDuration.IDLE_BREAK,
                            null,
                            () -> this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(SeepingSoulAnimationState.IDLE, SeepingSoulAnimationDuration.IDLE, null, this::playIdleOrIdleBreak))
                    )
            );
        } else {
            this.animationChainer.enqueue(
                    AnimationChainer.AnimationStep.of(
                            SeepingSoulAnimationState.IDLE,
                            SeepingSoulAnimationDuration.IDLE,
                            null,
                            this::playIdleOrIdleBreak
                    )
            );
        }
    }

    private void playIdleAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(SeepingSoulAnimationState.IDLE, SeepingSoulAnimationDuration.IDLE, null, this::playIdleOrIdleBreak));
    }

    private void playHurtAnimation() {
        byte evt = this.random.nextBoolean() ? EVENT_HURT_LEFT : EVENT_HURT_RIGHT;
        this.level().broadcastEntityEvent(this, evt);
    }

    private void playRecallingAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(SeepingSoulAnimationState.RECALLING, SeepingSoulAnimationDuration.RECALLING));
    }

    public void playSpawnAnimation() {
        this.animationChainer.reset();
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(SeepingSoulAnimationState.SPAWN, SeepingSoulAnimationDuration.SPAWN));
        this.animationChainer.enqueue(AnimationChainer.AnimationStep.of(SeepingSoulAnimationState.IDLE, SeepingSoulAnimationDuration.IDLE, null, this::playIdleOrIdleBreak));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ANIMATION_STATE.equals(entityDataAccessor)) {
            SeepingSoulAnimationState animationState = this.entityData.get(ANIMATION_STATE);
            this.resetAnimation();
            switch (animationState) {
                case IDLE -> this.idleAnimation.startIfStopped(this.tickCount);
                case IDLE_BREAK -> {
                    this.idleAnimation.stop();
                    this.idleBreakAnimation.startIfStopped(this.tickCount);
                }
                case SPAWN -> this.spawnAnimation.startIfStopped(this.tickCount);
                case RECALLING -> {
                    this.idleAnimation.stop();
                    this.recallingAnimation.startIfStopped(this.tickCount);
                }
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public void handleEntityEvent(byte b) {
        switch (b) {
            case EVENT_HURT_LEFT -> eventAnimations.playSeconds("hurt", SeepingSoulAnimationDuration.HURT, () -> hurtLeftAnimation.start(this.tickCount), () -> hurtLeftAnimation.stop());
            case EVENT_HURT_RIGHT -> eventAnimations.playSeconds("hurt", SeepingSoulAnimationDuration.HURT, () -> hurtRightAnimation.start(this.tickCount), () -> hurtRightAnimation.stop());
            default -> super.handleEntityEvent(b);
        }
    }

    private void resetAnimation() {
        //this.idleAnimation.stop();
        this.idleBreakAnimation.stop();
        this.spawnAnimation.stop();
        this.hurtLeftAnimation.stop();
        this.hurtRightAnimation.stop();
        this.recallingAnimation.stop();
    }

    public SeepingSoulEntity transitionTo(SeepingSoulAnimationState state) {
        switch (state) {
            case IDLE:
                this.entityData.set(ANIMATION_STATE, SeepingSoulAnimationState.IDLE);
                break;
            case IDLE_BREAK:
                this.entityData.set(ANIMATION_STATE, SeepingSoulAnimationState.IDLE_BREAK);
                break;
            case SPAWN:
                this.entityData.set(ANIMATION_STATE, SeepingSoulAnimationState.SPAWN);
                break;
            case HURT_LEFT:
                this.entityData.set(ANIMATION_STATE, SeepingSoulAnimationState.HURT_LEFT);
                break;
            case HURT_RIGHT:
                this.entityData.set(ANIMATION_STATE, SeepingSoulAnimationState.HURT_RIGHT);
                break;
            case RECALLING:
                this.entityData.set(ANIMATION_STATE, SeepingSoulAnimationState.RECALLING);
                break;
        }
        return this;
    }

    public enum SeepingSoulAnimationState {
        NONE,
        IDLE,
        IDLE_BREAK,
        SPAWN,
        HURT_LEFT,
        HURT_RIGHT,
        RECALLING
    }
}
