package dev.hexnowloading.dungeonnowloading.components.spawn_node.spawn_effect;

import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.SpawnRequest;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.SpawnTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class PoofDelaySpreadSpawnEffect implements SpawnTask {
    private final SpawnRequest req;
    private int delay;
    private final int interval;
    private final int radius;
    private int remaining;
    private int t;

    public PoofDelaySpreadSpawnEffect(SpawnRequest req, int delay, int interval, int radius) {
        this.req = req;
        this.delay = Math.max(0, delay);
        this.interval = Math.max(1, interval);
        this.radius = Math.max(0, radius);
        this.remaining = Math.max(1, req.node().count);
    }

    @Override
    public boolean tick(ServerLevel level, DungeonDirectorBlockEntity director) {
        if (delay-- > 0) return false;
        if (remaining <= 0) return true;
        if ((t++ % interval) != 0) return false;

        // consume one "slot" each time
        remaining--;

        // roll chance for this slot
        double chance = req.node().chance;
        boolean willSpawn = chance >= 1.0 || (chance > 0.0 && level.random.nextDouble() < chance);

        BlockPos pos = pickSpread(level, req.basePos(), radius);

        // If you want: poof only when spawning, or always.
        // I'd recommend "only when spawning" so poof implies something happened:
        if (willSpawn) {
            level.sendParticles(ParticleTypes.POOF,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    20, 0.25, 0.25, 0.25, 0.02);

            director.spawnOne(level, req.node(), req.patch(), pos);
        } else {
            // Optional: small fizzle effect when it "fails"
            // level.sendParticles(ParticleTypes.SMOKE, ...);
        }

        return remaining <= 0;
    }

    private static BlockPos pickSpread(ServerLevel level, BlockPos base, int r) {
        if (r <= 0) return base;
        for (int i = 0; i < 8; i++) {
            int dx = level.random.nextInt(r * 2 + 1) - r;
            int dz = level.random.nextInt(r * 2 + 1) - r;
            BlockPos p = base.offset(dx, 0, dz);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolid()) return p;
        }
        return base;
    }
}
