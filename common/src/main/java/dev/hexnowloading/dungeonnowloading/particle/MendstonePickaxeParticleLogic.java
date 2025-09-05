package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class MendstonePickaxeParticleLogic {
    // Radius (in blocks) to search around player
    private static final int RADIUS = 10;
    // How many ticks between scans (20 ticks = 1 second)
    private static final int INTERVAL_TICKS = 10;
    // Maximum number of ore blocks to process per interval to cap cost
    private static final int MAX_BLOCKS_PER_SCAN = 100;
    // Particles per exposed face
    private static final int PARTICLES_PER_FACE = 1;

    public static void handleClientTick(Minecraft mc) {
        if (mc == null || mc.player == null || mc.level == null) return;
        if (!mc.player.getMainHandItem().is(DNLItems.MENDSTONE_PICKAXE.get())) return;


        if (mc.level.getGameTime() % INTERVAL_TICKS != 0) return; // throttle (if game running slow skip)

        BlockPos playerPos = mc.player.blockPosition();

        List<BlockPos> targets = findNearbyDiamondOres(mc.level, playerPos, RADIUS, MAX_BLOCKS_PER_SCAN);
        for (BlockPos pos : targets) {
            spawnParticlesOnExposedFaces(mc.level, pos, PARTICLES_PER_FACE);
        }
    }

    private static List<BlockPos> findNearbyDiamondOres(Level level, BlockPos center, int radius, int limit) {
        List<BlockPos> list = new ArrayList<>();
        int r2 = radius * radius;

        // Sphere Check
        for (int dx = -radius; dx <= radius && list.size() < limit; dx++) {
            for (int dy = -radius; dy <= radius && list.size() < limit; dy++) {
                for (int dz = -radius; dz <= radius && list.size() < limit; dz++) {
                    if (dx*dx + dy*dy + dz*dz > r2) continue; // Skip if block outside of sphere.
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
                        list.add(pos);
                    }
                }
            }
        }
        return list;
    }

    private static void spawnParticlesOnExposedFaces(Level level, BlockPos pos, int perFace) {
        BlockState state = level.getBlockState(pos);
        if (!(state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE))) return;

        for (Direction dir : Direction.values()) {
            if (isFaceExposed(level, pos, dir)) {
                for (int i = 0; i < perFace; i++) {
                    spawnFaceParticle(level, pos, dir);
                }
            }
        }
    }

    private static boolean isFaceExposed(Level level, BlockPos pos, Direction face) {
        BlockPos adjacent = pos.relative(face);
        BlockState neighbor = level.getBlockState(adjacent);

        return neighbor.isAir() || !neighbor.isSolidRender(level, adjacent);
    }

    // Spawns one particle somewhere on the given face
    private static void spawnFaceParticle(Level level, BlockPos pos, Direction face) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        double u = level.random.nextDouble();
        double v = level.random.nextDouble();
        final double OUT = 0.02D; // push outward a bit more so particles are clearly outside
        double px = 0;
        double py = 0;
        double pz = 0;
        switch (face) {
            case UP -> { px = x + u; py = y + 1 + OUT; pz = z + v; }
            case DOWN -> { px = x + u; py = y - OUT; pz = z + v; }
            case NORTH -> { px = x + u; py = y + v; pz = z - OUT; }
            case SOUTH -> { px = x + u; py = y + v; pz = z + 1 + OUT; }
            case WEST -> { px = x - OUT; py = y + u; pz = z + v; }
            case EAST -> { px = x + 1 + OUT; py = y + u; pz = z + v; }
        }

        level.addParticle(DNLParticleTypes.MENDING_RUNE_SHORT_PARTICLE.get(), px, py, pz, 0, 0, 0);
    }
}
