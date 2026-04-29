package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MimiclingFallingBlockEntity extends FallingBlockEntity {
    private static final float MIN_IMPACT_RANGE = 1.0F;
    private static final float MAX_IMPACT_RANGE = 5.0F;

    private BlockState mimiclingBlockState = Blocks.SAND.defaultBlockState();
    private float impactDamage = 1.0F;
    private NonNullList<ItemStack> impactDrops = NonNullList.create();

    public MimiclingFallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);
    }

    public MimiclingFallingBlockEntity(Level level, BlockPos pos, BlockState state, float impactDamage, Iterable<ItemStack> impactDrops) {
        this(DNLEntityTypes.MIMICLING_FALLING_BLOCK.get(), level);
        this.mimiclingBlockState = state;
        this.impactDamage = Math.max(0.0F, impactDamage);
        for (ItemStack stack : impactDrops) {
            if (!stack.isEmpty()) {
                this.impactDrops.add(stack.copy());
            }
        }
        this.blocksBuilding = true;
        this.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.resolveSpawnOverlap();
        this.setStartPos(this.blockPosition());
    }

    @Override
    public void tick() {
        if (this.mimiclingBlockState.isAir()) {
            this.discard();
            return;
        }

        ++this.time;
        if (!this.level().isClientSide && this.time == 1) {
            this.resolveSpawnOverlap();
        }
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level().isClientSide) {
            LivingEntity impactEntity = this.getImpactEntity();
            if (impactEntity != null || this.onGround()) {
                this.impactAndDiscard(impactEntity);
                return;
            }
        }

        if (!this.level().isClientSide && (this.time > 600 || this.getY() < this.level().getMinBuildHeight() - 64)) {
            this.discard();
            return;
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
    }

    private LivingEntity getImpactEntity() {
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.05D), entity -> entity.isAlive() && !entity.isSpectator())) {
            return target;
        }
        return null;
    }

    private void resolveSpawnOverlap() {
        if (!this.hasFallingBlockOverlap(this.getBoundingBox())) {
            return;
        }

        Vec3 original = this.position();
        for (int attempts = 0; attempts < 12; attempts++) {
            int distance = 1 + this.random.nextInt(3);
            double x = this.random.nextBoolean() ? distance : -distance;
            double z = this.random.nextBoolean() ? distance : -distance;
            double y = this.random.nextInt(3);
            Vec3 moved = original.add(x, y, z);
            AABB movedBounds = this.getBoundingBox().move(moved.subtract(this.position()));
            BlockPos movedPos = BlockPos.containing(moved);
            if (this.level().getBlockState(movedPos).isAir()
                    && this.level().noCollision(this, movedBounds)
                    && !this.hasFallingBlockOverlap(movedBounds)) {
                this.setPos(moved.x, moved.y, moved.z);
                this.xo = this.getX();
                this.yo = this.getY();
                this.zo = this.getZ();
                this.setStartPos(this.blockPosition());
                return;
            }
        }
    }

    private boolean hasFallingBlockOverlap(AABB area) {
        return !this.level().getEntitiesOfClass(MimiclingFallingBlockEntity.class, area.inflate(0.1D), entity -> entity != this && entity.isAlive()).isEmpty();
    }

    private void impactAndDiscard(LivingEntity directHit) {
        this.playImpactSound(directHit);
        this.playImpactEffects();
        this.hurtEntitiesInImpactRange();
        if (!this.level().isClientSide) {
            for (ItemStack stack : this.impactDrops) {
                if (!this.isMatchingFallingBlockDrop(stack)) {
                    this.spawnAtLocation(stack.copy());
                }
            }
        }
        this.discard();
    }

    private void playImpactSound(LivingEntity directHit) {
        SoundType soundType = this.mimiclingBlockState.getSoundType();
        BlockPos soundPos = directHit != null ? directHit.blockPosition() : this.blockPosition();
        this.level().playSound(null, soundPos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) * 0.5F, soundType.getPitch() * 0.8F);
    }

    private boolean isMatchingFallingBlockDrop(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == this.mimiclingBlockState.getBlock();
    }

    private void hurtEntitiesInImpactRange() {
        float range = this.getImpactRange();
        double rangeSqr = range * range;
        AABB area = new AABB(
                this.getX() - range,
                this.getY() - 1.0D,
                this.getZ() - range,
                this.getX() + range,
                this.getY() + 2.0D,
                this.getZ() + range
        );
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && !entity.isSpectator())) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            if (dx * dx + dz * dz <= rangeSqr) {
                target.hurt(this.damageSources().fallingBlock(this), this.impactDamage);
            }
        }
    }

    private void playImpactEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float range = this.getImpactRange();
        int count = 8 + (int)(range * 6.0F);
        BlockParticleOption particle = new BlockParticleOption(DNLParticleTypes.MIMICLING_IMPACT_BLOCK_PARTICLE.get(), this.mimiclingBlockState);
        Vec3 center = Vec3.atCenterOf(this.blockPosition());
        for (int i = 0; i < count; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = range * (0.88D + this.random.nextDouble() * 0.12D);
            double targetX = Math.cos(angle) * distance;
            double targetZ = Math.sin(angle) * distance;
            double targetY = this.random.nextDouble() < 0.35D
                    ? 0.55D + this.random.nextDouble() * 0.85D
                    : -0.15D + this.random.nextDouble() * 0.45D;
            serverLevel.sendParticles(particle, center.x, center.y, center.z, 0, targetX, targetY, targetZ, 1.0D);
        }
    }

    private float getImpactRange() {
        return Math.max(MIN_IMPACT_RANGE, Math.min(MAX_IMPACT_RANGE, this.impactDamage));
    }

    @Override
    public BlockState getBlockState() {
        return this.mimiclingBlockState;
    }

    public float getImpactDamage() {
        return this.impactDamage;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("MimiclingBlockState", NbtUtils.writeBlockState(this.mimiclingBlockState));
        tag.putFloat("MimiclingImpactDamage", this.impactDamage);
        ListTag drops = new ListTag();
        for (ItemStack stack : this.impactDrops) {
            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);
            drops.add(stackTag);
        }
        tag.put("MimiclingImpactDrops", drops);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MimiclingBlockState", 10)) {
            this.mimiclingBlockState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompound("MimiclingBlockState"));
        }
        this.impactDamage = tag.contains("MimiclingImpactDamage", 99) ? Math.max(0.0F, tag.getFloat("MimiclingImpactDamage")) : 1.0F;
        this.impactDrops = NonNullList.create();
        ListTag drops = tag.getList("MimiclingImpactDrops", 10);
        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = ItemStack.of(drops.getCompound(i));
            if (!stack.isEmpty()) {
                this.impactDrops.add(stack);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.mimiclingBlockState = Block.stateById(packet.getData());
        this.blocksBuilding = true;
        this.setStartPos(BlockPos.containing(packet.getX(), packet.getY(), packet.getZ()));
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource damageSource) {
        return false;
    }
}
