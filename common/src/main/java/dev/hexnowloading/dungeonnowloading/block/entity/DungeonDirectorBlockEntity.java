package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.block.DungeonDirectorBlock;
import dev.hexnowloading.dungeonnowloading.block.ZoneReceiverBlockEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.spawn_node.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class DungeonDirectorBlockEntity extends BlockEntity implements ZoneReceiverBlockEntity {

    private static final int CHECK_INTERVAL = 1;

    private BlockPos cornerAOffset = BlockPos.ZERO;
    private BlockPos cornerBOffset = BlockPos.ZERO;
    private Direction authoredFacing = Direction.NORTH;
    private boolean regionSet = false;

    // Authoring: baked data stored inside director
    private boolean baked = false;
    private final List<StoredSpawnNode> storedNodes = new ArrayList<>();

    // Runtime encounter state
    private boolean triggered = false;
    private boolean cleared = false;
    private final Set<UUID> spawnedMobs = new HashSet<>();
    private int tickCounter = 0;

    private final List<SpawnTask> pendingTasks = new ArrayList<>();
    private boolean spawnsScheduled = false;

    public DungeonDirectorBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.DUNGEON_DIRECTOR.get(), pos, state);
    }

    // =========================
    // Tick / Trigger
    // =========================
    public static void serverTick(Level level, BlockPos pos, BlockState state, DungeonDirectorBlockEntity be) {
        if (be.cleared) return;
        if (!be.hasRegion()) return;

        // 1) Trigger check (rate-limited)
        if (!be.triggered) {
            be.tickCounter++;
            if (be.tickCounter % CHECK_INTERVAL != 0) return;

            if (be.isAnySurvivalPlayerInsideRegion()) {
                be.triggered = true;

                int scheduled = be.scheduleFromStoredNodes((ServerLevel) level);
                if (scheduled <= 0) {
                    be.triggered = false; // don't lock
                    return;
                }

                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        // 2) Triggered: tick spawn tasks EVERY TICK
        ServerLevel server = (ServerLevel) level;

        be.tickSpawnTasks(server);
        be.pruneDeadSpawnedMobs(server);

        // 3) Clear ONLY when tasks done AND mobs dead
        if (be.pendingTasks.isEmpty() && be.spawnsScheduled) {
            be.cleared = true;
            be.setChanged();

            if (state.getValue(DungeonDirectorBlock.REMOVE_AFTER_SUMMON)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }

            level.sendBlockUpdated(pos, state, state, 3);
        }
    }


    private boolean isAnySurvivalPlayerInsideRegion() {
        if (!(level instanceof ServerLevel server)) return false;
        AABB box = getRegionAabbInflated(0.0);
        return !server.getEntitiesOfClass(Player.class, box,
                p -> !p.isSpectator() && !p.getAbilities().instabuild
        ).isEmpty();
    }

    private AABB getRegionAabbInflated(double inflate) {
        BlockPos a = worldPosition.offset(rotateOffset(cornerAOffset));
        BlockPos b = worldPosition.offset(rotateOffset(cornerBOffset));

        BlockPos min = new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(a.getX(), b.getX()) + 1,
                Math.max(a.getY(), b.getY()) + 1,
                Math.max(a.getZ(), b.getZ()) + 1
        );

        return new AABB(min, max).inflate(inflate);
    }

    private void tickSpawnTasks(ServerLevel server) {
        if (pendingTasks.isEmpty()) return;
        pendingTasks.removeIf(task -> task.tick(server, this));
        setChanged();
    }

    private int scheduleFromStoredNodes(ServerLevel server) {
        if (storedNodes.isEmpty()) return 0;
        if (!baked) return 0;

        pendingTasks.clear();
        int scheduled = 0;

        for (StoredSpawnNode entry : storedNodes) {
            BlockPos basePos = worldPosition.offset(rotateOffset(entry.relPos));

            ResourceLocation poolId;
            try {
                poolId = new ResourceLocation(entry.poolId);
            } catch (Exception e) {
                continue;
            }

            SpawnPool pool = SpawnPools.get(poolId);
            if (pool == null) continue;

            ResourceLocation nodeId = pool.pickNodeId(server.random);
            if (nodeId == null) continue;

            SpawnNode nodeDef = SpawnNodes.get(nodeId);
            if (nodeDef == null) continue;

            SpawnEntry picked = nodeDef.pickEntry(server.random);

            CompoundTag patch = picked.combinedPatchCopy();

            // Create a resolved single-mode node for the spawn task pipeline
            SpawnNode resolved = new SpawnNode(
                    nodeDef.id,
                    picked.entityType,
                    picked.count,
                    picked.chance,
                    picked.spawnEffect,
                    picked.nbtPatch,
                    picked.snbtPatch
            );

            SpawnRequest req = new SpawnRequest(resolved, patch, basePos);
            SpawnTask task = DNLSpawnEffects.createTask(resolved.spawnEffect, req);
            pendingTasks.add(task);

            scheduled++;
        }

        spawnsScheduled = true;
        setChanged();
        return scheduled;
    }



    public boolean spawnOne(ServerLevel server, SpawnNode def, CompoundTag patch, BlockPos pos) {
        Entity entity = def.entityType.create(server);
        if (!(entity instanceof Mob mob)) {
            if (entity != null) entity.discard();
            return false;
        }

        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, mob.getYRot(), mob.getXRot());

        // If you want "structure" behavior globally, use STRUCTURE here.
        mob.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);

        // Apply NBT (this is where Passengers gets created)
        CompoundTag full = mob.saveWithoutId(new CompoundTag());
        NbtMerge.mergeCompound(full, patch);
        mob.load(full);

        server.addFreshEntity(mob);

        // Track root + passengers
        trackSpawnTree(mob);

        // Apply scaling to root + passengers (config can disable inside EntityScale or you gate it here)
        scaleSpawnTree(mob);

        return true;
    }

    private void trackSpawnTree(Entity root) {
        spawnedMobs.add(root.getUUID());
        for (Entity p : root.getPassengers()) {
            trackSpawnTree(p);
        }
    }

    private void scaleSpawnTree(Entity root) {
        if (root instanceof LivingEntity living) {
            EntityScale.scaleMobAttributes(living);
        }
        for (Entity p : root.getPassengers()) {
            scaleSpawnTree(p);
        }
    }



    private boolean rollChance(ServerLevel level, double chance) {
        if (chance >= 1.0) return true;
        if (chance <= 0.0) return false;
        return level.random.nextDouble() < chance;
    }

    private void pruneDeadSpawnedMobs(ServerLevel server) {
        spawnedMobs.removeIf(uuid -> {
            Entity e = server.getEntity(uuid);
            return e == null || !e.isAlive();
        });
    }

    // =========================
    // Bake / Unbake (authoring)
    // =========================
    public boolean isBaked() { return baked; }

    public int bakeFromWorldSpawnNodes() {
        if (!(level instanceof ServerLevel server)) return 0;
        if (!hasRegion()) return 0;

        AABB box = getRegionAabbInflated(0.0);

// Convert AABB to integer block bounds
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.ceil(box.maxX) - 1;
        int maxY = (int) Math.ceil(box.maxY) - 1;
        int maxZ = (int) Math.ceil(box.maxZ) - 1;

        storedNodes.clear();

        int count = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);

                    if (!server.getBlockState(p).is(DNLBlocks.SPAWN_NODE.get())) continue;

                    BlockEntity be = server.getBlockEntity(p);
                    if (be instanceof SpawnNodeBlockEntity nodeBe) {
                        // IMPORTANT: store UNROTATED local offset, relative to the director
                        BlockPos rel = p.subtract(worldPosition);

                        storedNodes.add(new StoredSpawnNode(rel, nodeBe.getSpawnPool()));
                        count++;

                        server.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        baked = true;
        resetEncounterState();
        setChanged();
        return count;
    }

    public int restoreSpawnNodesToWorld() {
        if (!(level instanceof ServerLevel server)) return 0;

        int placed = 0;
        for (StoredSpawnNode entry : storedNodes) {
            BlockPos p = worldPosition.offset(rotateOffset(entry.relPos));

            if (!server.getBlockState(p).isAir()) continue;

            server.setBlock(p, DNLBlocks.SPAWN_NODE.get().defaultBlockState(), 3);

            BlockEntity be = server.getBlockEntity(p);
            if (be instanceof SpawnNodeBlockEntity nodeBe) {
                nodeBe.setSpawnPool(entry.poolId);
                nodeBe.setChanged();

                BlockState st = server.getBlockState(p);
                server.sendBlockUpdated(p, st, st, 3);
                placed++;
            }
        }

        baked = false;
        resetEncounterState();
        setChanged();
        return placed;
    }

    public void resetEncounterState() {
        triggered = false;
        cleared = false;
        spawnedMobs.clear();
        tickCounter = 0;
        pendingTasks.clear();
        spawnsScheduled = false;
    }


    // =========================
    // ZoneReceiverBlockEntity
    // =========================
    @Override
    public void setRegion(BlockPos cornerAWorld, BlockPos cornerBWorld, Direction authoredFacing) {
        this.regionSet = true;
        // store offsets relative to director position
        this.cornerAOffset = cornerAWorld.subtract(this.worldPosition);
        this.cornerBOffset = cornerBWorld.subtract(this.worldPosition);

        // store the facing used during authoring (usually the block’s facing at the time)
        this.authoredFacing = authoredFacing == null ? Direction.NORTH : authoredFacing;

        // if region changes, reset baked/encounter state (recommended)
        this.baked = false;
        this.storedNodes.clear();
        resetEncounterState();

        setChanged();
    }

    public boolean hasRegion() { return regionSet; }

    private BlockPos rotateOffset(BlockPos offset) {
        if (level == null) return offset;

        Direction currentFacing = getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);

        int currentIndex = switch (currentFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int authoredIndex = switch (authoredFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int diff = currentIndex - authoredIndex;

        return switch (diff) {
            default -> offset;
            case 1, -3 -> offset.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
            case -1, 3 -> offset.rotate(net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90);
            case -2, 2 -> offset.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_180);
        };
    }

    public void setAuthoredFacing(Direction facing) {
        this.authoredFacing = (facing == null) ? Direction.NORTH : facing;
        setChanged();
    }

    // =========================
    // NBT Save/Load
    // =========================
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("CornerA", writePos(cornerAOffset));
        tag.put("CornerB", writePos(cornerBOffset));
        tag.putInt("AuthoredFacing", authoredFacing.get3DDataValue());

        tag.putBoolean("RegionSet", regionSet);
        tag.putBoolean("Baked", baked);

        ListTag stored = new ListTag();
        for (StoredSpawnNode e : storedNodes) {
            CompoundTag t = new CompoundTag();
            t.putInt("dx", e.relPos.getX());
            t.putInt("dy", e.relPos.getY());
            t.putInt("dz", e.relPos.getZ());
            t.putString("PoolId", e.poolId);
            stored.add(t);
        }
        tag.put("StoredNodes", stored);

        tag.putBoolean("Triggered", triggered);
        tag.putBoolean("Cleared", cleared);

        ListTag uuids = new ListTag();
        for (UUID id : spawnedMobs) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Id", id);
            uuids.add(t);
        }
        tag.put("SpawnedMobs", uuids);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.cornerAOffset = tag.contains("CornerA") ? readPos(tag.getCompound("CornerA")) : BlockPos.ZERO;
        this.cornerBOffset = tag.contains("CornerB") ? readPos(tag.getCompound("CornerB")) : BlockPos.ZERO;
        this.authoredFacing = tag.contains("AuthoredFacing")
                ? Direction.from3DDataValue(tag.getInt("AuthoredFacing"))
                : Direction.NORTH;

        this.regionSet = tag.getBoolean("RegionSet");
        this.baked = tag.getBoolean("Baked");

        this.storedNodes.clear();
        if (tag.contains("StoredNodes")) {
            ListTag list = tag.getList("StoredNodes", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                BlockPos rel = new BlockPos(t.getInt("dx"), t.getInt("dy"), t.getInt("dz"));
                String poolId = t.getString("PoolId");
                storedNodes.add(new StoredSpawnNode(rel, poolId));
            }
        }

        this.triggered = tag.getBoolean("Triggered");
        this.cleared = tag.getBoolean("Cleared");

        this.spawnedMobs.clear();
        if (tag.contains("SpawnedMobs")) {
            ListTag list = tag.getList("SpawnedMobs", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                if (t.hasUUID("Id")) spawnedMobs.add(t.getUUID("Id"));
            }
        }
    }

    private static CompoundTag writePos(BlockPos pos) {
        CompoundTag t = new CompoundTag();
        t.putInt("X", pos.getX());
        t.putInt("Y", pos.getY());
        t.putInt("Z", pos.getZ());
        return t;
    }

    private static BlockPos readPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }

    public static class StoredSpawnNode {
        public final BlockPos relPos;
        public final String poolId;

        public StoredSpawnNode(BlockPos relPos, String poolId) {
            this.relPos = relPos;
            this.poolId = poolId;
        }
    }
}
