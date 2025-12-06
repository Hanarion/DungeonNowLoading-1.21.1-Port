package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PayloadEntity extends Entity {
    public enum Kind {
        CRYO(0), OIL(1), CALTROP(2);
        public final int id;
        Kind(int id) { this.id = id; }
        public static Kind byId(int id) {
            return id == 0 ? CRYO : id == 1 ? OIL : CALTROP;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_KIND = SynchedEntityData.defineId(PayloadEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STUCK = SynchedEntityData.defineId(PayloadEntity.class, EntityDataSerializers.BOOLEAN);

    private double gravity = 0.04D; // similar to snowball
    private double drag = 0.98D;
    private int life = 200; // ticks until auto-remove safety

    private int hitsToBreak = 2; // reduced to two punches

    public PayloadEntity(EntityType<? extends PayloadEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public PayloadEntity(Level level, double x, double y, double z, Kind kind) {
        super(DNLEntityTypes.PAYLOAD.get(), level);
        this.setPos(x, y, z);
        setKind(kind);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_KIND, Kind.CRYO.id);
        this.entityData.define(DATA_STUCK, Boolean.FALSE);
    }

    public void setKind(Kind kind) {
        this.entityData.set(DATA_KIND, kind.id);
    }

    public Kind getKind() {
        return Kind.byId(this.entityData.get(DATA_KIND));
    }

    private boolean isStuck() {
        return this.entityData.get(DATA_STUCK);
    }

    private void setStuck(boolean stuck) {
        this.entityData.set(DATA_STUCK, stuck);
    }

    public void setPhysics(double gravity, double drag) {
        this.gravity = gravity;
        this.drag = drag;
    }

    @Override
    public void tick() {
        super.tick();
        if (isStuck() && getKind() == Kind.CALTROP) {
            // If no longer touching any block, drop again (restore gravity)
            if (!isTouchingAnyBlock()) {
                this.noPhysics = false;
                setStuck(false);
            } else {
                // extend despawn timer when stuck (increase a lot)
                if (this.life < 2400) this.life = 2400; // ~120 seconds
                applyCaltropEffects();
                if (--life <= 0) this.discard();
                return;
            }
        }

        // Apply gravity
        Vec3 motion = this.getDeltaMovement();
        if (!this.isNoGravity()) {
            motion = motion.add(0.0D, -gravity, 0.0D);
        }
        // Apply drag
        motion = motion.scale(drag);
        // Move
        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, motion);

        // Impact check: if on ground or collided with block, trigger effect and maybe persist
        if (this.onGround() || this.horizontalCollision || this.verticalCollision) {
            this.onImpact();
            return;
        }

        if (--life <= 0) {
            this.discard();
        }
    }

    protected void onImpact() {
        BlockPos pos = this.blockPosition();
        switch (getKind()) {
            case CRYO -> {
                BlockState snow = Blocks.POWDER_SNOW.defaultBlockState();
                if (snow.canSurvive(level(), pos)) {
                    if (level().isEmptyBlock(pos)) {
                        level().setBlock(pos, snow, 11);
                    } else if (level().isEmptyBlock(pos.above())) {
                        level().setBlock(pos.above(), snow, 11);
                    }
                }
                this.discard();
            }
            case OIL -> {
                if (DNLBlocks.blocksRegistered && DNLBlocks.OIL_SPILL != null) {
                    Block spill = DNLBlocks.OIL_SPILL.get();
                    BlockState spillState = spill.defaultBlockState();
                    BlockState landed = level().getBlockState(pos);
                    if (!landed.is(spill) && (level().isEmptyBlock(pos) || landed.canBeReplaced())) {
                        level().setBlock(pos, spillState, 11);
                    }
                }
                this.discard();
            }
            case CALTROP -> {
                // Stick to surface: stop moving, persist, apply effects in tick
                setDeltaMovement(Vec3.ZERO);
                this.noPhysics = true;
                setStuck(true);
                // Nudge into the block face to avoid z-fighting
                this.setPos(getX(), getY(), getZ());
            }
        }
    }

    private boolean isTouchingAnyBlock() {
        AABB bb = this.getBoundingBox().inflate(0.02D);
        BlockPos min = new BlockPos((int)Math.floor(bb.minX), (int)Math.floor(bb.minY), (int)Math.floor(bb.minZ));
        BlockPos max = new BlockPos((int)Math.floor(bb.maxX), (int)Math.floor(bb.maxY), (int)Math.floor(bb.maxZ));
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockState s = level().getBlockState(new BlockPos(x, y, z));
                    if (!s.isAir()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyCaltropEffects() {
        // Larger contact box; gentle slow; damage like sweet berry bush when moving and not crouching
        AABB box = this.getBoundingBox().inflate(0.3D, 0.2D, 0.3D);
        for (Entity e : level().getEntities(this, box)) {
            if (!(e instanceof LivingEntity living)) continue;

            // Compute horizontal movement based on position delta (server-reliable)
            double dx = living.getX() - living.xOld;
            double dz = living.getZ() - living.zOld;
            double hMove = Math.abs(dx) + Math.abs(dz);

            // Gentle slow if moving even slightly
            if (hMove > 1.0E-4D) {
                Vec3 motion = living.getDeltaMovement();
                living.setDeltaMovement(motion.x * 0.85D, motion.y, motion.z * 0.85D);
            }

            boolean crouching = (living instanceof Player p) && p.isShiftKeyDown();
            boolean moving = hMove > 0.005D; // walking threshold

            // Player-specific: consider sprint state and inputs
            if (living instanceof Player p) {
                if (p.isSprinting()) {
                    moving = true;
                } else if (!moving) {
                    // if providing movement input, treat as moving even if server delta is small
                    if (p.zza != 0.0F || p.xxa != 0.0F) {
                        moving = true;
                    }
                }
            }

            if (!level().isClientSide && moving && !crouching) {
                // Damage tick cadence
                if (level().getGameTime() % 5 == 0) {
                    living.hurt(level().damageSources().sweetBerryBush(), 1.0F);
                }
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_KIND, tag.getInt("Kind"));
        this.entityData.set(DATA_STUCK, tag.getBoolean("Stuck"));
        this.gravity = tag.contains("Gravity") ? tag.getDouble("Gravity") : this.gravity;
        this.drag = tag.contains("Drag") ? tag.getDouble("Drag") : this.drag;
        this.life = tag.contains("Life") ? tag.getInt("Life") : this.life;
        this.hitsToBreak = tag.contains("CaltropHits") ? tag.getInt("CaltropHits") : this.hitsToBreak;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Kind", this.entityData.get(DATA_KIND));
        tag.putBoolean("Stuck", this.entityData.get(DATA_STUCK));
        tag.putDouble("Gravity", this.gravity);
        tag.putDouble("Drag", this.drag);
        tag.putInt("Life", this.life);
        tag.putInt("CaltropHits", this.hitsToBreak);
    }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        // Allow players to punch caltrops off surfaces; other damage types can be ignored
        if (getKind() == Kind.CALTROP) {
            if (src.getEntity() instanceof Player) {
                if (--hitsToBreak <= 0) {
                    this.discard();
                }
                return true;
            }
            return false;
        }
        if (src.is(DamageTypeTags.IS_FIRE)) return false;
        return super.hurt(src, amount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // Remove right-click breaking; only punching allowed
        return InteractionResult.PASS;
    }
}
