package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.block.entity.WispwardChestBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.WispwardLanternBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CWispwardLanternCartOpenConfigPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class WispwardLanternCartEntity extends AbstractMinecart {
    private static final float DERAILED_BREAK_DAMAGE = 80.0F;
    private static final float INCLINE_TRANSITION_TICKS = 6.0F;
    private static final EntityDataAccessor<Boolean> LIT = SynchedEntityData.defineId(WispwardLanternCartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TIMED = SynchedEntityData.defineId(WispwardLanternCartEntity.class, EntityDataSerializers.BOOLEAN);

    private float inclineAnimationProgress;
    private float inclineAnimationProgressO;
    private int timerSeconds = WispwardLanternBlockEntity.MIN_TIMER_SECONDS;
    private long litUntilGameTime = 0L;
    private boolean lockedLit = false;

    public WispwardLanternCartEntity(EntityType<? extends WispwardLanternCartEntity> type, Level level) {
        super(type, level);
    }

    public WispwardLanternCartEntity(Level level, double x, double y, double z) {
        super(DNLEntityTypes.WISPWARD_LANTERN_CART.get(), level, x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LIT, false);
        builder.define(TIMED, false);
    }

    public boolean isLit() {
        return this.entityData.get(LIT);
    }

    public boolean isTimed() {
        return this.entityData.get(TIMED);
    }

    public void setTimed(boolean timed) {
        this.entityData.set(TIMED, timed);
    }

    public int getTimerSeconds() {
        return this.timerSeconds;
    }

    public void setTimerSeconds(int timerSeconds) {
        this.timerSeconds = Math.max(WispwardLanternBlockEntity.MIN_TIMER_SECONDS, Math.min(WispwardLanternBlockEntity.MAX_TIMER_SECONDS, timerSeconds));
    }

    public boolean lightFromWisp() {
        if (this.isLit()) {
            return false;
        }

        this.entityData.set(LIT, true);
        if (this.level() instanceof ServerLevel server) {
            if (this.isTimed()) {
                this.markLit(server.getGameTime());
            }
            WispwardChestBlockEntity.notifyLanternChanged(server, this.blockPosition());

            Vec3 core = this.getLightPosition();
            server.sendParticles(ParticleTypes.FLAME, core.x, core.y, core.z, 12, 0.22D, 0.28D, 0.22D, 0.03D);
            server.sendParticles(ParticleTypes.SMOKE, core.x, core.y, core.z, 4, 0.18D, 0.20D, 0.18D, 0.01D);
            server.playSound(null, core.x, core.y, core.z, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.65F, 1.2F);
        }
        return true;
    }

    private void markLit(long gameTime) {
        this.litUntilGameTime = gameTime + this.timerSeconds * 20L;
    }

    public void refreshTimedLight(long gameTime) {
        if (this.isTimed() && this.isLit() && !this.lockedLit) {
            this.markLit(gameTime);
        }
    }

    public void lockLit() {
        if (!this.isTimed() || this.lockedLit) {
            return;
        }

        this.lockedLit = true;
        this.litUntilGameTime = Long.MAX_VALUE;
    }

    public Vec3 getLightPosition() {
        return this.position().add(0.0D, 1.55D, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        this.tickInclineAnimation();
        this.tickTimedLight();
    }

    private void tickTimedLight() {
        if (this.level().isClientSide || !this.isTimed() || !this.isLit() || this.lockedLit) {
            return;
        }

        if (this.level() instanceof ServerLevel server && (this.litUntilGameTime <= 0L || server.getGameTime() >= this.litUntilGameTime)) {
            this.entityData.set(LIT, false);
            WispwardChestBlockEntity.notifyLanternChanged(server, this.blockPosition());
        }
    }

    private void tickInclineAnimation() {
        this.inclineAnimationProgressO = this.inclineAnimationProgress;
        float step = 1.0F / INCLINE_TRANSITION_TICKS;
        if (this.isOnAscendingRail()) {
            this.inclineAnimationProgress = Math.min(1.0F, this.inclineAnimationProgress + step);
        } else {
            this.inclineAnimationProgress = Math.max(0.0F, this.inclineAnimationProgress - step);
        }
    }

    private boolean isOnAscendingRail() {
        BlockPos pos = this.blockPosition();
        BlockState state = this.level().getBlockState(pos);
        if (!state.is(BlockTags.RAILS)) {
            pos = pos.below();
            state = this.level().getBlockState(pos);
        }

        if (!state.is(BlockTags.RAILS) || !(state.getBlock() instanceof BaseRailBlock railBlock)) {
            return false;
        }

        RailShape shape = state.getValue(railBlock.getShapeProperty());
        return shape.isAscending();
    }

    public float getInclineAnimationProgress(float partialTick) {
        return Mth.lerp(partialTick, this.inclineAnimationProgressO, this.inclineAnimationProgress);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(DNLItems.WISPLIGHT_ROD.get())) {
            if (!this.level().isClientSide) {
                this.lightFromWisp();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTimed() && player.getAbilities().instabuild) {
            if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                Services.NETWORK.sendToPlayer(new S2CWispwardLanternCartOpenConfigPacket(this.getId(), this.getTimerSeconds()), serverPlayer);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        }

        if (this.isInvulnerableTo(source)) {
            return false;
        }

        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

        boolean creativePlayerDamage = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
        float hitDamage = amount * 10.0F;

        if (this.isOnRails() && !creativePlayerDamage) {
            this.setDamage(Math.min(DERAILED_BREAK_DAMAGE - 1.0F, Math.max(this.getDamage(), hitDamage)));
            this.playSound(SoundEvents.SHIELD_BLOCK, 0.6F, 1.2F);
            return true;
        }

        if (creativePlayerDamage) {
            if (this.level() instanceof ServerLevel server) {
                WispwardChestBlockEntity.notifyLanternChanged(server, this.blockPosition());
            }
            this.discard();
            return true;
        }

        this.setDamage(this.getDamage() + hitDamage);
        if (this.getDamage() > DERAILED_BREAK_DAMAGE) {
            this.destroy(source);
        }

        return true;
    }

    @Override
    public Item getDropItem() {
        return this.isTimed() ? DNLItems.TIMED_WISPWARD_LANTERN_CART.get() : DNLItems.WISPWARD_LANTERN_CART.get();
    }

    @Override
    public Type getMinecartType() {
        return Type.RIDEABLE;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.isTimed() ? DNLItems.TIMED_WISPWARD_LANTERN_CART.get() : DNLItems.WISPWARD_LANTERN_CART.get());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Lit", this.isLit());
        tag.putBoolean("Timed", this.isTimed());
        tag.putInt("TimerSeconds", this.timerSeconds);
        tag.putLong("LitUntilGameTime", this.litUntilGameTime);
        tag.putBoolean("LockedLit", this.lockedLit);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(LIT, tag.getBoolean("Lit"));
        this.entityData.set(TIMED, tag.getBoolean("Timed"));
        this.setTimerSeconds(tag.getInt("TimerSeconds"));
        this.litUntilGameTime = tag.getLong("LitUntilGameTime");
        this.lockedLit = tag.getBoolean("LockedLit");
    }
}
