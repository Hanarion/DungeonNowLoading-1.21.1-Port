package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.ZoneReceiverBlockEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.registry.*;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockDestructionManager;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class DuriteQuellerBlockEntity extends BlockEntity implements ZoneReceiverBlockEntity {

    private static final float RETURN_PARTICLE_SPEED = 0.18f; // blocks per tick
    private static final double MAX_SPAWN_DISTANCE = 32.0;
    private static final int GROWTH_INTERVAL_TICKS = 20; // 1 second


    // Stored "structure-local" corners (offsets) + the facing they were authored in
    private BlockPos cornerA = BlockPos.ZERO;
    private BlockPos cornerB = BlockPos.ZERO;
    private Direction nbtFacing = Direction.NORTH;

    private boolean wasPowered = false;
    private int spawnTicksTotal = 0;
    private int delayTicks = 0;
    private boolean pending = false;

    private final LongList cachedPreservers = new LongArrayList();

    private static final int SPAWN_TICKS_TOTAL = 20 * 5; // 5 seconds
    private static final int EARLY_MENDING_POP_TICKS = 16;

    // we compute per particle ticks, but this is a cap / safety
    private static final int RETURN_TRAVEL_TICKS = 40;

    private int spawnTicksLeft = 0;
    private int waitTicksLeft = 0;

    private enum Phase { NONE, SPAWNING, WAITING }
    private Phase phase = Phase.NONE;

    private long triggerGameTime = 0L;
    private int firstArrivalTick = 0; // relative to triggerGameTime
    private int lastArrivalTick = 0;  // relative to triggerGameTime

    private int growthTotal = 0;      // G = preserverCount + 3
    private int nextEarlyIndex = 0;   // 0..growthTotal-1
    private int nextGrowthIndex = 0;  // 0..growthTotal-1

    private int particleFirstArrivalTick = 0; // relative to triggerGameTime

    public DuriteQuellerBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.DURITE_QUELLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DuriteQuellerBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;

        // Nothing to do if not running
        if (!be.pending || be.phase == Phase.NONE) return;

        // Only re-cache while we still expect preservers to exist
        if (be.phase == Phase.SPAWNING) {
            be.ensureCachedPreservers(server);

            if (be.cachedPreservers.isEmpty()) {
                be.pending = false;
                be.phase = Phase.NONE;

                be.spawnTicksLeft = 0;
                be.waitTicksLeft = 0;
                be.growthTotal = 0;
                be.nextGrowthIndex = 0;
                be.nextEarlyIndex = 0;

                be.setChanged();
                return;
            }
        }

        be.tryDoGrowth(server);

        if (be.phase == Phase.SPAWNING) {
            boolean isLastSpawnTick = (be.spawnTicksLeft == 1);

            be.spawnReturnParticlesBatch(server);

            if (isLastSpawnTick) {
                be.popAndReplacePreservers(server);
            }

            be.spawnTicksLeft--;
            be.setChanged();

            if (be.spawnTicksLeft <= 0) {
                be.phase = Phase.WAITING;
                be.setChanged();
            }
            return;
        }

        if (be.phase == Phase.WAITING) {
            be.waitTicksLeft--;
            be.setChanged();

            if (be.waitTicksLeft <= 0) {
                be.pending = false;
                be.phase = Phase.NONE;
                be.setChanged();

                be.runDispel(server);
            }
        }
    }


    private void tryDoGrowth(ServerLevel level) {
        if (!pending || growthTotal <= 0) return;

        int relTick = (int) (level.getGameTime() - triggerGameTime);

        // ----- EARLY POPS (16 ticks before each growth) -----
        while (nextEarlyIndex < growthTotal) {
            int growthTick = getScheduledGrowthTick(nextEarlyIndex);
            int earlyTick = Math.max(particleFirstArrivalTick, growthTick - EARLY_MENDING_POP_TICKS);

            if (relTick < earlyTick) break;

            BlockPos startPos = this.getBlockPos().above();
            float scale = getScaleForIndex(level, nextEarlyIndex);
            spawnMendingPop(level, startPos, scale);

            nextEarlyIndex++;
        }

        // ----- ACTUAL GROWTH + POP BURST -----
        while (nextGrowthIndex < growthTotal) {
            int growthTick = getScheduledGrowthTick(nextGrowthIndex);
            if (relTick < growthTick) break;

            doOneGrowth(level);
            nextGrowthIndex++;
        }
    }



    private void doOneGrowth(ServerLevel level) {
        BlockPos startPos = this.getBlockPos().above();
        BlockState state = level.getBlockState(startPos);

        spawnPopBurst(level, startPos);

        Block small   = DNLBlocks.SMALL_DURITE_BUD.get();
        Block medium  = DNLBlocks.MEDIUM_DURITE_BUD.get();
        Block large   = DNLBlocks.LARGE_DURITE_BUD.get();
        Block cluster = DNLBlocks.DURITE_CLUSTER.get();

        // helper
        var sound = DNLSounds.DURITE_QUELLER_CRYSTAL_GROW.get(); // adjust to your registry accessor
        var soundPop = DNLSounds.MENDING_AURA_POP.get();

        if (state.isAir() || state.canBeReplaced()) {
            level.setBlock(startPos, small.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, startPos, soundPop, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
            return;
        }

        if (state.is(small)) {
            level.setBlock(startPos, medium.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, startPos, soundPop, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
            return;
        }

        if (state.is(medium)) {
            level.setBlock(startPos, large.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, startPos, soundPop, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
            return;
        }

        if (state.is(large)) {
            level.setBlock(startPos, cluster.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.7F);
            level.playSound(null, startPos, soundPop, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        if (state.is(cluster)) {
            // drop durite item (keep cluster)
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5,
                    new net.minecraft.world.item.ItemStack(DNLItems.DURITE.get())
            ));
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.7F);
            level.playSound(null, startPos, soundPop, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.7F);
        level.playSound(null, startPos, soundPop, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level.random.nextFloat() < 0.25f) {
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5,
                    new net.minecraft.world.item.ItemStack(DNLItems.DURITE.get())
            ));
        }
    }


    private void cachePreservers(ServerLevel level) {
        cachedPreservers.clear();

        BlockPos center = this.getBlockPos();
        BlockPos absCornerA = center.offset(rotateOffset(level, center, cornerA, nbtFacing));
        BlockPos absCornerB = center.offset(rotateOffset(level, center, cornerB, nbtFacing));

        int minX = Math.min(absCornerA.getX(), absCornerB.getX());
        int minY = Math.min(absCornerA.getY(), absCornerB.getY());
        int minZ = Math.min(absCornerA.getZ(), absCornerB.getZ());
        int maxX = Math.max(absCornerA.getX(), absCornerB.getX());
        int maxY = Math.max(absCornerA.getY(), absCornerB.getY());
        int maxZ = Math.max(absCornerA.getZ(), absCornerB.getZ());

        Block preserver = DNLBlocks.STONE_PRESERVER.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(preserver)) {
                        cachedPreservers.add(cursor.asLong());
                    }
                }
            }
        }
    }

    private void spawnMendingPop(ServerLevel level, BlockPos blockPos, float scale) {
        double x = blockPos.getX() + 0.5;
        double y = blockPos.getY() + 0.5; // slightly above the bottom face
        double z = blockPos.getZ() + 0.5;

        var data = new ScalableParticleType.ScalableParticleData(
                DNLParticleTypes.MENDING_POP_PARTICLE.get(),
                scale
        );

        level.sendParticles(data, x, y, z, 1, 0, 0, 0, 0);
    }



    private void spawnReturnParticlesBatch(ServerLevel level) {
        if (cachedPreservers.isEmpty()) return;

        Vec3 start = Vec3.atCenterOf(this.getBlockPos().above());

        final int fadeIn = 4;
        final int fadeOut = 10;
        final double jitter = 0.10;

        for (int i = 0; i < cachedPreservers.size(); i++) {
            BlockPos preserverPos = BlockPos.of(cachedPreservers.getLong(i));
            Vec3 targetCenter = Vec3.atCenterOf(preserverPos);

            Vec3 toTarget = targetCenter.subtract(start);
            double distToTarget = toTarget.length();
            if (distToTarget < 1.0e-6) continue;

            Vec3 dirToTarget = toTarget.scale(1.0 / distToTarget); // normalize

            // Spawn at 5 blocks out toward the preserver, unless preserver is closer (<5)
            double travelDist = Math.min(MAX_SPAWN_DISTANCE, distToTarget);
            Vec3 spawn = start.add(dirToTarget.scale(travelDist));

            // Decide travel ticks (same rule as computeMaxTravelTicks)
            int travelTicks = RETURN_TRAVEL_TICKS;

// speed becomes distance / fixedTime (farther => faster)
            float vMag = (float) (travelDist / (double) travelTicks);

            Vec3 vel = start.subtract(spawn);
            if (vel.lengthSqr() < 1.0e-6) continue;
            vel = vel.normalize().scale(vMag);

            double px = spawn.x + (level.random.nextDouble() - 0.5) * jitter;
            double py = spawn.y + (level.random.nextDouble() - 0.5) * jitter;
            double pz = spawn.z + (level.random.nextDouble() - 0.5) * jitter;

            var data = new dev.hexnowloading.dungeonnowloading.particle.type.MendingFadeParticleType.Data(
                    DNLParticleTypes.MENDING_FADE_PARTICLE.get(),
                    (float) vel.x, (float) vel.y, (float) vel.z,
                    fadeIn, fadeOut, travelTicks
            );

            level.sendParticles(data, px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    private void popAndReplacePreservers(ServerLevel level) {
        if (cachedPreservers.isEmpty()) return;

        Block preserver = DNLBlocks.STONE_PRESERVER.get();
        BlockState replaceState = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();

        var breakSound = DNLSounds.DURITE_QUELLER_REPLACE_PRESERVER.get(); // adjust accessor

        for (int i = 0; i < cachedPreservers.size(); i++) {
            BlockPos p = BlockPos.of(cachedPreservers.getLong(i));

            if (!level.getBlockState(p).is(preserver)) continue;

            spawnPopBurst(level, p);

            level.setBlock(p, replaceState, Block.UPDATE_ALL);

            // play at that block position
            level.playSound(null, p, breakSound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.5F);
        }
    }


    private void spawnPopBurst(ServerLevel level, BlockPos pos) {
        Vec3 c = Vec3.atCenterOf(pos);

        final int count = 8;
        final int fadeIn = 0;
        final int fadeOut = 8;
        final int lifetime = 12;
        final float speed = 0.22f;

        for (int i = 0; i < count; i++) {
            // random direction
            double rx = (level.random.nextDouble() * 2.0 - 1.0);
            double ry = (level.random.nextDouble() * 2.0 - 1.0);
            double rz = (level.random.nextDouble() * 2.0 - 1.0);
            Vec3 dir = new Vec3(rx, ry, rz);
            if (dir.lengthSqr() < 1.0e-6) {
                dir = new Vec3(0, 1, 0);
            }
            dir = dir.normalize().scale(speed);

            var data = new dev.hexnowloading.dungeonnowloading.particle.type.MendingFadeParticleType.Data(
                    DNLParticleTypes.MENDING_FADE_PARTICLE.get(),
                    (float) dir.x, (float) dir.y, (float) dir.z,
                    fadeIn, fadeOut, lifetime
            );

            // tiny jitter so it looks like a pop, not a point
            double px = c.x + (level.random.nextDouble() - 0.5) * 0.25;
            double py = c.y + (level.random.nextDouble() - 0.5) * 0.25;
            double pz = c.z + (level.random.nextDouble() - 0.5) * 0.25;

            level.sendParticles(data, px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    public void tryReplaceSelfWithMendingAura(ServerLevel level) {
        if (!hasAnyPreserverInRegion(level)) return;

        BlockPos pos = this.getBlockPos();
        BlockState originalState = level.getBlockState(pos);

        // snapshot BEFORE we touch the block
        CompoundTag tag = this.saveWithFullMetadata(); // or saveWithoutMetadata()

        // mirror your working mending placement behavior
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);

        BlockState auraState = MendingAuraBlock.configureForStoredBlock(DNLBlocks.MENDING_AURA.get().defaultBlockState(), originalState);
        level.setBlock(pos, auraState, Block.UPDATE_CLIENTS);

        BlockEntity newBe = level.getBlockEntity(pos);
        if (newBe instanceof MendingAuraBlockEntity auraBe) {
            BlockState storedState = MendingAuraBlock.refreshStoredConnections(originalState, level, pos);
            auraBe.setStoredBlock(storedState, tag);
            MendingAuraBlock.refreshNeighboringStoredConnections(level, pos);
            auraBe.syncToClients(level, level.getBlockState(pos));
            auraBe.setChanged();
        }

        // IMPORTANT: start restoration (your preserver system does this)
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof MendingAuraBlock auraBlock) {
            auraBlock.startRestoration(level, pos);
        }

        // IMPORTANT: cancel AFTER all setBlock calls (same reason as your comment)
        BlockDestructionManager.cancel();
    }

    public boolean hasAnyPreserverInRegion(ServerLevel level) {
        BlockPos center = this.getBlockPos();
        BlockPos absCornerA = center.offset(rotateOffset(level, center, cornerA, nbtFacing));
        BlockPos absCornerB = center.offset(rotateOffset(level, center, cornerB, nbtFacing));

        int minX = Math.min(absCornerA.getX(), absCornerB.getX());
        int minY = Math.min(absCornerA.getY(), absCornerB.getY());
        int minZ = Math.min(absCornerA.getZ(), absCornerB.getZ());
        int maxX = Math.max(absCornerA.getX(), absCornerB.getX());
        int maxY = Math.max(absCornerA.getY(), absCornerB.getY());
        int maxZ = Math.max(absCornerA.getZ(), absCornerB.getZ());

        Block preserver = DNLBlocks.STONE_PRESERVER.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(preserver)) return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setRegion(BlockPos cornerAWorld, BlockPos cornerBWorld, Direction authoredFacing) {
        // If your BE expects STRUCTURE-LOCAL offsets, store offsets relative to this block:
        BlockPos center = this.getBlockPos();
        this.cornerA = cornerAWorld.subtract(center);
        this.cornerB = cornerBWorld.subtract(center);
        this.nbtFacing = authoredFacing;
        setChanged();
    }

    public void setNbtFacing(Direction direction) {
        this.nbtFacing = direction;
    }

    public void onRedstone(boolean powered) {
        if (this.level == null || this.level.isClientSide) return;

        if (powered && !wasPowered) {

            // already running -> ignore
            if (this.pending || this.phase != Phase.NONE) {
                wasPowered = powered;
                return;
            }

            if (this.level instanceof ServerLevel server) {
                cachePreservers(server);

                if (cachedPreservers.isEmpty()) {
                    wasPowered = powered;
                    return;
                }

                this.triggerGameTime = server.getGameTime();

                int preserverCount = cachedPreservers.size();
                this.growthTotal = preserverCount + 3;
                this.nextGrowthIndex = 0;
                this.nextEarlyIndex = 0;

                int travelTicksAll = RETURN_TRAVEL_TICKS;

// first particle can arrive at this tick (because you spawn continuously starting immediately)
                this.particleFirstArrivalTick = travelTicksAll;

// First growth happens 16 ticks AFTER first arrival
                int firstGrowthTick = this.particleFirstArrivalTick + EARLY_MENDING_POP_TICKS;

// Last growth tick (fixed 1s interval)
                int lastGrowthTick = firstGrowthTick + (this.growthTotal - 1) * GROWTH_INTERVAL_TICKS;

// Sync: lastArrival = (spawnTicksTotal - 1) + travelTicksAll
                this.spawnTicksTotal = Math.max(1, (lastGrowthTick - travelTicksAll) + 1);

                this.firstArrivalTick = firstGrowthTick;
                this.lastArrivalTick  = lastGrowthTick;

                this.spawnTicksLeft = this.spawnTicksTotal;
                this.waitTicksLeft = travelTicksAll;


                this.phase = Phase.SPAWNING;
                this.pending = true;

                level.playSound(null, this.getBlockPos(), DNLSounds.DURITE_QUELLER_ACTIVATE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

                setChanged();
            }
        }

        wasPowered = powered;
    }

    private void runDispel(ServerLevel level) {
        cachedPreservers.clear();
    }

    private int travelTicksForDistance(double travelDist) {
        int t = (int) Math.ceil(travelDist / 0.18);
        return Math.max(1, t);
    }

    private int getScheduledGrowthTick(int index) {
        return firstArrivalTick + index * GROWTH_INTERVAL_TICKS;
    }

    private float getScaleForIndex(ServerLevel level, int index) {
        BlockPos startPos = this.getBlockPos().above();
        BlockState state = level.getBlockState(startPos);

        Block small   = DNLBlocks.SMALL_DURITE_BUD.get();
        Block medium  = DNLBlocks.MEDIUM_DURITE_BUD.get();
        Block large   = DNLBlocks.LARGE_DURITE_BUD.get();
        Block cluster = DNLBlocks.DURITE_CLUSTER.get();

        if (state.isAir() || state.canBeReplaced()) return 1.5f;
        if (state.is(small)) return 2.0f;
        if (state.is(medium)) return 2.5f;
        if (state.is(large)) return 3.0f;
        if (state.is(cluster)) return 3.5f;

        // blocked case
        return 1.5f;
    }

    // === Rotation helper (same idea as your Preserver code) ===
    public static BlockPos rotateOffset(Level level, BlockPos pos, BlockPos offset, Direction nbtFacing) {
        Direction propertyDirection = level.getBlockState(pos).getValue(BlockStateProperties.FACING);

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

        return switch (facingDifference) {
            default -> offset;
            case 1, -3 -> offset.rotate(Rotation.CLOCKWISE_90);
            case -1, 3 -> offset.rotate(Rotation.COUNTERCLOCKWISE_90);
            case -2, 2 -> offset.rotate(Rotation.CLOCKWISE_180);
        };
    }

    private void ensureCachedPreservers(ServerLevel level) {
        if (!this.cachedPreservers.isEmpty()) return;
        cachePreservers(level);
    }

    // === Save/load ===
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("cornerAx", cornerA.getX());
        tag.putInt("cornerAy", cornerA.getY());
        tag.putInt("cornerAz", cornerA.getZ());
        tag.putInt("cornerBx", cornerB.getX());
        tag.putInt("cornerBy", cornerB.getY());
        tag.putInt("cornerBz", cornerB.getZ());
        tag.putString("nbtFacing", nbtFacing.getName());
        tag.putInt("delayTicks", delayTicks);
        tag.putBoolean("pending", pending);
        tag.putBoolean("wasPowered", wasPowered);
        tag.putString("phase", this.phase.name());
        tag.putInt("spawnTicksLeft", this.spawnTicksLeft);
        tag.putInt("waitTicksLeft", this.waitTicksLeft);

        tag.putLong("triggerGameTime", this.triggerGameTime);
        tag.putInt("firstArrivalTick", this.firstArrivalTick);
        tag.putInt("lastArrivalTick", this.lastArrivalTick);

        tag.putInt("growthTotal", this.growthTotal);
        tag.putInt("nextGrowthIndex", this.nextGrowthIndex);
        tag.putInt("nextEarlyIndex", this.nextEarlyIndex);

        tag.putInt("spawnTicksTotal", this.spawnTicksTotal);
        tag.putInt("particleFirstArrivalTick", this.particleFirstArrivalTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cornerA = new BlockPos(tag.getInt("cornerAx"), tag.getInt("cornerAy"), tag.getInt("cornerAz"));
        cornerB = new BlockPos(tag.getInt("cornerBx"), tag.getInt("cornerBy"), tag.getInt("cornerBz"));
        nbtFacing = Direction.byName(tag.getString("nbtFacing"));
        if (nbtFacing == null) nbtFacing = Direction.NORTH;
        delayTicks = tag.getInt("delayTicks");
        pending = tag.getBoolean("pending");
        wasPowered = tag.getBoolean("wasPowered");
        this.phase = Phase.valueOf(tag.getString("phase"));
        this.spawnTicksLeft = tag.getInt("spawnTicksLeft");
        this.waitTicksLeft = tag.getInt("waitTicksLeft");

        this.triggerGameTime = tag.getLong("triggerGameTime");
        this.firstArrivalTick = tag.getInt("firstArrivalTick");
        this.lastArrivalTick = tag.getInt("lastArrivalTick");

        this.growthTotal = tag.getInt("growthTotal");
        this.nextGrowthIndex = tag.getInt("nextGrowthIndex");
        this.nextEarlyIndex = tag.getInt("nextEarlyIndex");

        this.spawnTicksTotal = tag.getInt("spawnTicksTotal");
        this.particleFirstArrivalTick = tag.getInt("particleFirstArrivalTick");
    }
}
