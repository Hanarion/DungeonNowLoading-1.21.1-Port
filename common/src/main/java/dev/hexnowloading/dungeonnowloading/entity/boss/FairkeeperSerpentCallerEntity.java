package dev.hexnowloading.dungeonnowloading.entity.boss;

import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class FairkeeperSerpentCallerEntity extends Entity {

    private static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> BOROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> OUROS_UUID = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> HORIZONTAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VERTICAL_OFFSET = SynchedEntityData.defineId(FairkeeperSerpentCallerEntity.class, EntityDataSerializers.INT);

    private int activationTick;

    public FairkeeperSerpentCallerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ACTIVATED, false);
        this.entityData.define(BOROS_UUID, Optional.empty());
        this.entityData.define(OUROS_UUID, Optional.empty());
        this.entityData.define(HORIZONTAL_OFFSET, 0);
        this.entityData.define(VERTICAL_OFFSET, 0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("Activated", isActivated());
        if (this.getBorosId() != null) {
            compoundTag.putUUID("BorosUUID", this.getBorosId());
        }
        if (this.getOurosId() != null) {
            compoundTag.putUUID("OurosUUID", this.getOurosId());
        }
        compoundTag.putInt("HorizontalOffset", this.getHorizontalOffset());
        compoundTag.putInt("VerticalOffset", this.getVerticalOffset());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.entityData.set(ACTIVATED, compoundTag.getBoolean("Activated"));
        if (compoundTag.hasUUID("BorosUUID")) {
            this.setBorosId(compoundTag.getUUID("BorosUUID"));
        }
        if (compoundTag.hasUUID("OurosUUID")) {
            this.setOurosId(compoundTag.getUUID("OurosUUID"));
        }
        this.entityData.set(HORIZONTAL_OFFSET, compoundTag.getInt("HorizontalOffset"));
        this.entityData.set(VERTICAL_OFFSET, compoundTag.getInt("VerticalOffset"));
    }

    public void startBossFight() {
        this.activationTick = 60;
        this.setActivated(true);
        this.setOffsets(5, 10);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isActivated()) {
            if (this.activationTick > 0) {
                this.activationTick--;
                return;
            }
            this.summonBosses();
            this.setActivated(false);
        }
    }

    private void summonBosses() {
        // Get the current position of the entity
        BlockPos currentPosition = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());

        // Get the facing direction and the direction to the right
        Direction direction = Direction.fromYRot(this.getYRot());
        Direction clockWiseDirection = direction.getClockWise();
        Direction counterClockWiseDirection = direction.getCounterClockWise();

        // Calculate the target position
        BlockPos clockWiseTargetPosition = currentPosition
                .relative(clockWiseDirection, this.getHorizontalOffset())
                .below(this.getVerticalOffset());

        BlockPos counterClockWiseTargetPosition = currentPosition
                .relative(counterClockWiseDirection, this.getHorizontalOffset())
                .above(this.getVerticalOffset());

        // Set the block to red wool at the target position
        FairkeeperBorosEntity boros = new FairkeeperBorosEntity(DNLEntityTypes.FAIRKEEPER.get(), this.level());
        if (boros != null) {
            boros.moveTo(counterClockWiseTargetPosition.getX(), counterClockWiseTargetPosition.getY(), counterClockWiseTargetPosition.getZ());
            boros.setCallerId(this.getUUID());
            boros.setState(FairkeeperBorosEntity.FairkeeperState.AWAKENING);
            boros.setSpawnPoint(counterClockWiseTargetPosition);
            boros.setYRot(clockWiseDirection.toYRot());
            boros.yBodyRot = boros.getYRot();
            boros.yHeadRot = boros.getYRot();
            this.level().addFreshEntity(boros);
            this.setBorosId(boros.getUUID());
        }

        FairkeeperOurosEntity ouros = new FairkeeperOurosEntity(DNLEntityTypes.FAIRKEEPER_OUROS.get(), this.level());
        if (ouros != null) {
            ouros.moveTo(clockWiseTargetPosition.getX(), clockWiseTargetPosition.getY(), clockWiseTargetPosition.getZ());
            ouros.setCallerId(this.getUUID());
            ouros.setState(FairkeeperOurosEntity.FairkeeperOurosState.AWAKENING);
            ouros.setSpawnPoint(clockWiseTargetPosition);
            ouros.setYRot(counterClockWiseDirection.toYRot());
            ouros.yBodyRot = ouros.getYRot();
            ouros.yHeadRot = ouros.getYRot();
            this.level().addFreshEntity(ouros);
            this.setOurosId(ouros.getUUID());
        }
        //this.level().setBlock(clockWiseTargetPosition, Blocks.RED_WOOL.defaultBlockState(), Block.UPDATE_ALL);
        //this.level().setBlock(counterClockWiseTargetPosition, Blocks.BLUE_WOOL.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Nullable
    public UUID getBorosId() { return this.entityData.get(BOROS_UUID).orElse(null); }

    public void setBorosId(@Nullable UUID uniqueId) {
        this.entityData.set(BOROS_UUID, Optional.ofNullable(uniqueId));
    }

    @Nullable
    public UUID getOurosId() { return this.entityData.get(OUROS_UUID).orElse(null); }

    public void setOurosId(@Nullable UUID uniqueId) {
        this.entityData.set(OUROS_UUID, Optional.ofNullable(uniqueId));
    }

    public void setActivated(boolean activated) { this.entityData.set(ACTIVATED, activated); }

    public boolean isActivated() { return this.entityData.get(ACTIVATED); }

    public void setOffsets(int x, int y) {
        this.entityData.set(HORIZONTAL_OFFSET, x);
        this.entityData.set(VERTICAL_OFFSET, y);
    }

    public int getHorizontalOffset() { return this.entityData.get(HORIZONTAL_OFFSET); }

    public int getVerticalOffset() { return this.entityData.get(VERTICAL_OFFSET); }

}
