package dev.hexnowloading.dungeonnowloading.spawn_node.effect;

import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import dev.hexnowloading.dungeonnowloading.spawn_node.SpawnDefinition;
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

        // Scale poof based on entity size
        PoofParams poof = computePoof(level, req.def(), pos);

        // One poof burst per "spawn attempt" feels good (count times)
        int count = Math.max(1, req.def().count);
        for (int i = 0; i < count; i++) {
            // Visual first (even if chance fails, you might prefer poof only on success)
            // If you only want poof when something actually spawns, move this after spawnOne() returns true.
            level.sendParticles(
                    ParticleTypes.POOF,
                    pos.getX() + 0.5, pos.getY() + poof.yCenter, pos.getZ() + 0.5,
                    poof.particles,
                    poof.spreadXZ, poof.spreadY, poof.spreadXZ,
                    poof.speed
            );

            director.spawnOne(level, req.def(), req.patch(), pos);
        }

        done = true;
        return true;
    }

    private static PoofParams computePoof(ServerLevel level, SpawnDefinition def, BlockPos pos) {
        // Default values if entity can't be constructed
        float bbW = 0.6f;
        float bbH = 1.8f;

        try {
            Entity probe = def.entityType.create(level);
            if (probe != null) {
                // Position affects some entities' bounding boxes less often, but harmless
                probe.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                bbW = Math.max(0.1f, probe.getBbWidth());
                bbH = Math.max(0.1f, probe.getBbHeight());
                probe.discard(); // not added to world, but safe
            }
        } catch (Throwable ignored) {
        }

        // Particle scaling:
        // - width affects XZ spread + count
        // - height affects Y spread + yCenter
        int particles = clampInt((int) (12 + (bbW * bbH) * 18), 8, 60);

        double spreadXZ = clampDouble(0.15 + (bbW * 0.6), 0.15, 1.25);
        double spreadY  = clampDouble(0.15 + (bbH * 0.35), 0.15, 1.25);

        // Center poof around mid-body
        double yCenter = clampDouble(bbH * 0.5, 0.3, 2.0);

        // Particle motion speed
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
