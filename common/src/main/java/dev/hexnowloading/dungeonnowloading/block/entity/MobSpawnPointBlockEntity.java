package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.entity.misc.MobSpawnEffectEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.util.SpawnEffectType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MobSpawnPointBlockEntity extends BlockEntity {

    private boolean triggered = false;

    // Config
    private ResourceLocation mobId = null;
    private CompoundTag mobData = null;

    public double spawnOffsetX = 0.5D;
    public double spawnOffsetY = 0.5D;
    public double spawnOffsetZ = 0.5D;

    public double detectMinOffsetX = 0.0D;
    public double detectMinOffsetY = 0.0D;
    public double detectMinOffsetZ = 0.0D;

    public double detectMaxOffsetX = 1.0D;
    public double detectMaxOffsetY = 1.0D;
    public double detectMaxOffsetZ = 1.0D;

    private Direction baseFacing = Direction.NORTH;
    private SpawnEffectType spawnEffectType = SpawnEffectType.NONE;

    public MobSpawnPointBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.MOB_SPAWN_POINT.get(), pos, state);
    }

    public void setBaseFacing(Direction facing) {
        this.baseFacing = facing;
        setChanged();
    }

    public boolean isTriggered() {
        return triggered;
    }

    // --------- Server tick: detection + handoff ---------

    public static void serverTick(Level level, BlockPos pos, BlockState state, MobSpawnPointBlockEntity be) {
        if (level.isClientSide) return;
        if (be.triggered) return;

        // Check every 5 ticks
        if (level.getGameTime() % 5L != 0L) {
            return;
        }

        // Local-space corners (in baseFacing space)
        Vec3 localA = new Vec3(be.detectMinOffsetX, be.detectMinOffsetY, be.detectMinOffsetZ);
        Vec3 localB = new Vec3(be.detectMaxOffsetX, be.detectMaxOffsetY, be.detectMaxOffsetZ);

        // Rotate both into world-space offsets
        Vec3 rotA = be.rotateOffsetVec(level, state, localA);
        Vec3 rotB = be.rotateOffsetVec(level, state, localB);

        double minX = pos.getX() + Math.min(rotA.x, rotB.x);
        double minY = pos.getY() + Math.min(rotA.y, rotB.y);
        double minZ = pos.getZ() + Math.min(rotA.z, rotB.z);

        double maxX = pos.getX() + Math.max(rotA.x, rotB.x);
        double maxY = pos.getY() + Math.max(rotA.y, rotB.y);
        double maxZ = pos.getZ() + Math.max(rotA.z, rotB.z);

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        var players = level.getEntitiesOfClass(
                Player.class,
                box,
                p -> !p.getAbilities().instabuild
        );

        if (players.isEmpty()) {
            return;
        }

        be.triggerAndHandoff((ServerLevel) level, state);
    }

    public void setDetectionFromWorldCorners(BlockState state, BlockPos cornerA, BlockPos cornerB) {
        // Center of block and corners
        Vec3 originCenter = Vec3.atCenterOf(this.worldPosition);
        Vec3 aWorld = Vec3.atCenterOf(cornerA).subtract(originCenter);
        Vec3 bWorld = Vec3.atCenterOf(cornerB).subtract(originCenter);

        // Convert world-space deltas to local offsets (inverse of rotateOffset)
        Vec3 aLocal = inverseRotateOffsetVec(state, aWorld);
        Vec3 bLocal = inverseRotateOffsetVec(state, bWorld);

        this.detectMinOffsetX = Math.min(aLocal.x, bLocal.x);
        this.detectMinOffsetY = Math.min(aLocal.y, bLocal.y);
        this.detectMinOffsetZ = Math.min(aLocal.z, bLocal.z);

        this.detectMaxOffsetX = Math.max(aLocal.x, bLocal.x);
        this.detectMaxOffsetY = Math.max(aLocal.y, bLocal.y);
        this.detectMaxOffsetZ = Math.max(aLocal.z, bLocal.z);

        setChanged();
    }

    private Vec3 inverseRotateOffsetVec(BlockState state, Vec3 worldDelta) {
        if (!state.hasProperty(BlockStateProperties.FACING)) {
            return worldDelta;
        }

        Direction propertyDirection = state.getValue(BlockStateProperties.FACING);
        Direction nbtFacing = this.baseFacing;

        int propertyFacingIndex = switch (propertyDirection) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int nbtFacingIndex = switch (nbtFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int facingDifference = propertyFacingIndex - nbtFacingIndex;

        // Inverse of rotateOffsetVec
        return switch (facingDifference) {
            default -> worldDelta;
            // forward was CW90: (-z, y, x) → inverse is CCW90: (z, y, -x)
            case 1, -3 -> new Vec3(worldDelta.z, worldDelta.y, -worldDelta.x);
            // forward was CCW90: (z, y, -x) → inverse is CW90: (-z, y, x)
            case -1, 3 -> new Vec3(-worldDelta.z, worldDelta.y, worldDelta.x);
            // 180 is its own inverse
            case -2, 2 -> new Vec3(-worldDelta.x, worldDelta.y, -worldDelta.z);
        };
    }




    // Called from detection OR from block's canBeReplaced when player places on it
    public void triggerAndHandoff(ServerLevel level, BlockState state) {
        if (triggered) return;
        triggered = true;

        if (mobId == null) {
            // no mob configured, just remove the block
            level.removeBlock(worldPosition, false);
            return;
        }

        // Compute final spawn position from offsets + rotation
        Vec3 localSpawn = new Vec3(spawnOffsetX, spawnOffsetY, spawnOffsetZ);
        Vec3 rotatedSpawn = rotateOffsetVec(level, state, localSpawn);

        double x = this.worldPosition.getX() + rotatedSpawn.x;
        double y = this.worldPosition.getY() + rotatedSpawn.y;
        double z = this.worldPosition.getZ() + rotatedSpawn.z;

        MobSpawnEffectEntity effect = DNLEntityTypes.MOB_SPAWN_EFFECT.get().create(level);
        if (effect != null) {
            effect.moveTo(x, y, z, 0.0F, 0.0F);
            effect.configure(mobId, mobData, spawnEffectType);
            level.addFreshEntity(effect);
        }

        // Now that the effect entity owns the logic, remove the block
        level.removeBlock(worldPosition, false);
        setChanged();
    }

    // --------- Rotation helper (same logic as your Preserver) ---------

    private Vec3 rotateOffsetVec(Level level, BlockState state, Vec3 offset) {
        if (!state.hasProperty(BlockStateProperties.FACING)) {
            return offset;
        }

        Direction propertyDirection = state.getValue(BlockStateProperties.FACING);
        Direction nbtFacing = this.baseFacing;

        int propertyFacingIndex = switch (propertyDirection) {
            default -> 0;          // NORTH
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int nbtFacingIndex = switch (nbtFacing) {
            default -> 0;          // NORTH
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int facingDifference = propertyFacingIndex - nbtFacingIndex;

        return switch (facingDifference) {
            default -> offset;
            case 1, -3 -> new Vec3(-offset.z, offset.y, offset.x);          // CLOCKWISE_90
            case -1, 3 -> new Vec3(offset.z, offset.y, -offset.x);          // COUNTERCLOCKWISE_90
            case -2, 2 -> new Vec3(-offset.x, offset.y, -offset.z);         // 180
        };
    }

    // --------- Save/load ---------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Triggered", this.triggered);

        if (mobId != null) tag.putString("MobId", mobId.toString());
        if (mobData != null && !mobData.isEmpty()) tag.put("MobData", mobData.copy());

        tag.putDouble("SpawnOffsetX", spawnOffsetX);
        tag.putDouble("SpawnOffsetY", spawnOffsetY);
        tag.putDouble("SpawnOffsetZ", spawnOffsetZ);

        tag.putDouble("DetectMinOffsetX", detectMinOffsetX);
        tag.putDouble("DetectMinOffsetY", detectMinOffsetY);
        tag.putDouble("DetectMinOffsetZ", detectMinOffsetZ);

        tag.putDouble("DetectMaxOffsetX", detectMaxOffsetX);
        tag.putDouble("DetectMaxOffsetY", detectMaxOffsetY);
        tag.putDouble("DetectMaxOffsetZ", detectMaxOffsetZ);

        tag.putString("BaseFacing", baseFacing.getName());
        tag.putString("SpawnEffect", spawnEffectType.getId());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.triggered = tag.getBoolean("Triggered");

        if (tag.contains("MobId"))
            this.mobId = new ResourceLocation(tag.getString("MobId"));

        if (tag.contains("MobData"))
            this.mobData = tag.getCompound("MobData");

        if (tag.contains("SpawnOffsetX")) this.spawnOffsetX = tag.getDouble("SpawnOffsetX");
        if (tag.contains("SpawnOffsetY")) this.spawnOffsetY = tag.getDouble("SpawnOffsetY");
        if (tag.contains("SpawnOffsetZ")) this.spawnOffsetZ = tag.getDouble("SpawnOffsetZ");

        if (tag.contains("DetectMinOffsetX")) this.detectMinOffsetX = tag.getDouble("DetectMinOffsetX");
        if (tag.contains("DetectMinOffsetY")) this.detectMinOffsetY = tag.getDouble("DetectMinOffsetY");
        if (tag.contains("DetectMinOffsetZ")) this.detectMinOffsetZ = tag.getDouble("DetectMinOffsetZ");

        if (tag.contains("DetectMaxOffsetX")) this.detectMaxOffsetX = tag.getDouble("DetectMaxOffsetX");
        if (tag.contains("DetectMaxOffsetY")) this.detectMaxOffsetY = tag.getDouble("DetectMaxOffsetY");
        if (tag.contains("DetectMaxOffsetZ")) this.detectMaxOffsetZ = tag.getDouble("DetectMaxOffsetZ");

        if (tag.contains("BaseFacing")) {
            Direction loaded = Direction.byName(tag.getString("BaseFacing"));
            this.baseFacing = loaded != null ? loaded : Direction.NORTH;
        } else {
            this.baseFacing = Direction.NORTH;
        }

        if (tag.contains("SpawnEffect")) {
            this.spawnEffectType = SpawnEffectType.fromString(tag.getString("SpawnEffect"));
        } else {
            this.spawnEffectType = SpawnEffectType.NONE;
        }
    }

    // Optional helpers to configure from code
    public void setMobId(ResourceLocation id) {
        this.mobId = id;
        setChanged();
    }

    public void setMobData(CompoundTag data) {
        this.mobData = data;
        setChanged();
    }

    public void setSpawnEffectType(SpawnEffectType type) {
        this.spawnEffectType = type;
        setChanged();
    }
}
