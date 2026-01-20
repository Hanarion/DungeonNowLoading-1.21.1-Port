package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.*;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class DispelBlockEntity extends BlockEntity {

    private static final float RETURN_PARTICLE_SPEED = 0.18f; // blocks per tick
    private static final double MAX_SPAWN_DISTANCE = 32.0;

    // Stored "structure-local" corners (offsets) + the facing they were authored in
    private BlockPos cornerA = BlockPos.ZERO;
    private BlockPos cornerB = BlockPos.ZERO;
    private Direction nbtFacing = Direction.NORTH;

    private boolean wasPowered = false;

    private static final int DELAY_TICKS = 20 * 5;
    private int delayTicks = 0;
    private boolean pending = false;

    private final LongList cachedPreservers = new LongArrayList();

    private static final int SPAWN_TICKS_TOTAL = 20 * 5; // 5 seconds

    // we compute per particle ticks, but this is a cap / safety
    private static final int MAX_WAIT_TICKS = 40;

    private int spawnTicksLeft = 0;
    private int waitTicksLeft = 0;

    private enum Phase { NONE, SPAWNING, WAITING }
    private Phase phase = Phase.NONE;

    private long triggerGameTime = 0L;
    private int firstArrivalTick = 0; // relative to triggerGameTime
    private int lastArrivalTick = 0;  // relative to triggerGameTime

    private int growthTotal = 0;      // G = preserverCount + 3
    private int nextGrowthIndex = 0;  // 0..growthTotal-1


    public DispelBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.DISPEL_BLOCK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DispelBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;
        if (!be.pending) return;

        be.tryDoGrowth(server);

        if (be.phase == Phase.SPAWNING) {
            boolean isLastSpawnTick = (be.spawnTicksLeft == 1);

            // Spawn the inward-travel particles every tick
            be.spawnReturnParticlesBatch(server);

            // On the LAST spawn tick: pop + replace preservers NOW
            if (isLastSpawnTick) {
                be.popAndReplacePreservers(server);
                // After this, cached preservers are no longer preservers (but we keep the list for visuals if needed)
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

        if (relTick < firstArrivalTick) return;
        if (relTick > lastArrivalTick) return; // after window, stop

        // If only 1 growth event, schedule it at lastArrival (also equals firstArrival if window=0)
        int denom = Math.max(1, growthTotal - 1);
        int window = Math.max(0, lastArrivalTick - firstArrivalTick);

        // Catch up in case of lag (do multiple growth steps if needed)
        while (nextGrowthIndex < growthTotal) {
            int scheduledTick = firstArrivalTick + (nextGrowthIndex * window) / denom;
            if (relTick < scheduledTick) break;

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
        var sound = DNLSounds.MENDING_TABLE_MEND.get(); // adjust to your registry accessor

        if (state.isAir() || state.canBeReplaced()) {
            level.setBlock(startPos, small.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
            return;
        }

        if (state.is(small)) {
            level.setBlock(startPos, medium.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
            return;
        }

        if (state.is(medium)) {
            level.setBlock(startPos, large.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
            return;
        }

        if (state.is(large)) {
            level.setBlock(startPos, cluster.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        if (state.is(cluster)) {
            // drop durite item (keep cluster)
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5,
                    new net.minecraft.world.item.ItemStack(DNLItems.DURITE.get()) // adjust
            ));
            level.playSound(null, startPos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
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


    private void spawnReturnParticlesBatch(ServerLevel level) {
        if (cachedPreservers.isEmpty()) return;

        Vec3 start = Vec3.atCenterOf(this.getBlockPos().above());

        final int fadeIn = 10;
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
            int travelTicks = (int) Math.ceil(travelDist / 0.18);
            travelTicks = Math.max(1, travelTicks);

            // Velocity magnitude chosen so it arrives exactly in travelTicks
            float vMag = (float) (travelDist / (double) travelTicks);

            // Velocity points back toward start
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

        var breakSound = DNLSounds.MENDSTONE_CHALK_MARK_BREAK.get(); // adjust accessor

        for (int i = 0; i < cachedPreservers.size(); i++) {
            BlockPos p = BlockPos.of(cachedPreservers.getLong(i));

            if (!level.getBlockState(p).is(preserver)) continue;

            spawnPopBurst(level, p);

            level.setBlock(p, replaceState, Block.UPDATE_ALL);

            // play at that block position
            level.playSound(null, p, breakSound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
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


    private int computeDelayTicks(ServerLevel level) {
        if (cachedPreservers.isEmpty()) return 0;

        Vec3 start = Vec3.atCenterOf(this.getBlockPos().above());

        double maxTravel = 0.0;

        for (int i = 0; i < cachedPreservers.size(); i++) {
            BlockPos p = BlockPos.of(cachedPreservers.getLong(i));
            double dist = start.distanceTo(Vec3.atCenterOf(p));
            double travel = Math.min(MAX_SPAWN_DISTANCE, dist); // because you spawn at min(5, dist)
            if (travel > maxTravel) maxTravel = travel;
        }

        // delay so that the farthest particle reaches start exactly when we execute
        int ticks = (int) Math.ceil(maxTravel / (double) RETURN_PARTICLE_SPEED);

        // keep it at least 1 tick if we’re doing anything
        return Math.max(1, ticks);
    }

    // Call from structure placement code or processor
    public void setRegion(BlockPos cornerA, BlockPos cornerB, Direction authoredFacing) {
        this.cornerA = cornerA;
        this.cornerB = cornerB;
        this.nbtFacing = authoredFacing;
        setChanged();
    }

    public void onRedstone(boolean powered) {
        if (this.level == null || this.level.isClientSide) return;

        if (powered && !wasPowered) {
            if (this.level instanceof ServerLevel server) {
                cachePreservers(server);

                this.triggerGameTime = server.getGameTime();

                int preserverCount = cachedPreservers.size();
                this.growthTotal = preserverCount + 3; // P + 3
                this.nextGrowthIndex = 0;

                int minTravel = computeMinTravelTicks(server);
                int maxTravel = computeMaxTravelTicks(server);

// arrivals are relative to trigger tick
                this.firstArrivalTick = minTravel;
                this.lastArrivalTick  = (SPAWN_TICKS_TOTAL - 1) + maxTravel;

// keep your 5s spawning phase
                this.spawnTicksLeft = SPAWN_TICKS_TOTAL;
                this.phase = Phase.SPAWNING;
                this.pending = true;

// wait phase length = time from end of spawning to last arrival
                this.waitTicksLeft = maxTravel;


                setChanged();
            }
        }

        wasPowered = powered;
    }


    private void runDispel(ServerLevel level) {
        // Place bud 1 block above dispel
        BlockPos budPos = this.getBlockPos().above();
        BlockState budState = DNLBlocks.LARGE_DURITE_BUD.get().defaultBlockState();

        if (level.getBlockState(budPos).canBeReplaced()) {
            level.setBlock(budPos, budState, Block.UPDATE_ALL);
        }

        cachedPreservers.clear();
    }

    private int travelTicksForDistance(double travelDist) {
        int t = (int) Math.ceil(travelDist / 0.18); // your chosen “desired speed”
        return Math.max(1, t);
    }

    private int computeMinTravelTicks(ServerLevel level) {
        if (cachedPreservers.isEmpty()) return 0;
        Vec3 start = Vec3.atCenterOf(this.getBlockPos().above());

        int min = Integer.MAX_VALUE;
        for (int i = 0; i < cachedPreservers.size(); i++) {
            BlockPos p = BlockPos.of(cachedPreservers.getLong(i));
            double dist = start.distanceTo(Vec3.atCenterOf(p));
            double travelDist = Math.min(MAX_SPAWN_DISTANCE, dist);
            min = Math.min(min, travelTicksForDistance(travelDist));
        }
        return (min == Integer.MAX_VALUE) ? 0 : min;
    }

    private int computeMaxTravelTicks(ServerLevel level) {
        if (cachedPreservers.isEmpty()) return 0;
        Vec3 start = Vec3.atCenterOf(this.getBlockPos().above());

        int max = 0;
        for (int i = 0; i < cachedPreservers.size(); i++) {
            BlockPos p = BlockPos.of(cachedPreservers.getLong(i));
            double dist = start.distanceTo(Vec3.atCenterOf(p));
            double travelDist = Math.min(MAX_SPAWN_DISTANCE, dist);
            max = Math.max(max, travelTicksForDistance(travelDist));
        }
        return max;
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
    }
}
