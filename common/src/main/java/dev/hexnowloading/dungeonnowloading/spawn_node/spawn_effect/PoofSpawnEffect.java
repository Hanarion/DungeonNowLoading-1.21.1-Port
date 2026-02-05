package dev.hexnowloading.dungeonnowloading.spawn_node.spawn_effect;

import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import dev.hexnowloading.dungeonnowloading.spawn_node.SpawnNode;
import dev.hexnowloading.dungeonnowloading.spawn_node.SpawnRequest;
import dev.hexnowloading.dungeonnowloading.spawn_node.SpawnTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class PoofSpawnEffect implements SpawnTask {

    private final SpawnRequest req;
    private boolean done = false;

    public PoofSpawnEffect(SpawnRequest req) {
        this.req = req;
    }

    @Override
    public boolean tick(ServerLevel level, DungeonDirectorBlockEntity director) {
        if (done) return true;

        BlockPos pos = req.basePos();
        SpawnNode def = req.node();

        // Precompute poof params once
        PoofParams poof = computePoof(level, def, pos);

        int slots = Math.max(1, def.count);
        double chance = def.chance;

        for (int i = 0; i < slots; i++) {
            if (!roll(level, chance)) continue;

            // Spawn first (so poof means "a mob spawned")
            boolean spawned = director.spawnOne(level, def, req.patch(), pos);
            if (!spawned) continue;

            level.sendParticles(
                    ParticleTypes.POOF,
                    pos.getX() + 0.5, pos.getY() + poof.yCenter, pos.getZ() + 0.5,
                    poof.particles,
                    poof.spreadXZ, poof.spreadY, poof.spreadXZ,
                    poof.speed
            );
        }

        done = true;
        return true;
    }

    private static boolean roll(ServerLevel level, double chance) {
        if (chance >= 1.0) return true;
        if (chance <= 0.0) return false;
        return level.random.nextDouble() < chance;
    }

    private static PoofParams computePoof(ServerLevel level, SpawnNode def, BlockPos pos) {
        float bbW = 0.6f;
        float bbH = 1.8f;

        try {
            Entity probe = def.entityType.create(level);
            if (probe != null) {
                probe.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                bbW = Math.max(0.1f, probe.getBbWidth());
                bbH = Math.max(0.1f, probe.getBbHeight());
                probe.discard();
            }
        } catch (Throwable ignored) {}

        int particles = clampInt((int) (12 + (bbW * bbH) * 18), 8, 60);

        double spreadXZ = clampDouble(0.15 + (bbW * 0.6), 0.15, 1.25);
        double spreadY  = clampDouble(0.15 + (bbH * 0.35), 0.15, 1.25);

        double yCenter = clampDouble(bbH * 0.5, 0.3, 2.0);

        double speed = clampDouble(0.01 + (bbW * 0.01) + (bbH * 0.005), 0.01, 0.08);

        return new PoofParams(particles, spreadXZ, spreadY, yCenter, speed);
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double clampDouble(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private record PoofParams(int particles, double spreadXZ, double spreadY, double yCenter, double speed) {}
}
