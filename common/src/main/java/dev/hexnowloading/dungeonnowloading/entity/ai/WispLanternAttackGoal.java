package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.HoveringFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class WispLanternAttackGoal extends Goal {
    private static final int INTERVAL_TICKS = reducedTickDelay(80);   // 5 seconds
    private static final int H_RADIUS = 2;           // horizontal radius
    private static final int V_RANGE = 1;            // vertical +/- range
    private static final int MAX_ATTEMPTS = 12;

    private final Mob owner;
    private final Level level;
    private final RandomSource rng;

    private int cooldown = INTERVAL_TICKS;

    public WispLanternAttackGoal(Mob owner) {
        this.owner = owner;
        this.level = owner.level();
        this.rng = owner.getRandom();
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return owner.getTarget() != null && owner.isAlive();
    }

    @Override
    public void start() {
        cooldown = INTERVAL_TICKS;
    }

    @Override
    public void stop() {
    }

    @Override
    public void tick() {
        if (cooldown == reducedTickDelay(60)) {
            ((HoveringFlyingMoveControl)owner.getMoveControl()).setWantedPosition();
        }
        if (--cooldown > 0) return;
        cooldown = INTERVAL_TICKS;

        BlockPos spawnPos = tryFindSpawnPos(this.level);
        if (spawnPos == null) return;

        WispEntity wisp = DNLEntityTypes.WISP.get().create(level);
        if (wisp == null) return;
        wisp.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        wisp.setOwner(owner);
        if (owner.getTarget() != null) {
            wisp.setTarget(owner.getTarget());
        }
        if (level.noCollision(wisp)) {
            level.addFreshEntity(wisp);
        }
    }

    private BlockPos tryFindSpawnPos(Level server) {
        BlockPos base = owner.blockPosition();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int dx = rng.nextInt(-H_RADIUS, H_RADIUS + 1);
            int dz = rng.nextInt(-H_RADIUS, H_RADIUS + 1);
            int dy = rng.nextInt(-V_RANGE, V_RANGE + 1);

            BlockPos pos = base.offset(dx, dy, dz);
            if (isAirLikeAndSafe(server, pos)) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Accept air/tall-grass/replaceable blocks, but reject fluids and solid collisions.
     */
    private boolean isAirLikeAndSafe(Level server, BlockPos pos) {
        BlockState state = server.getBlockState(pos);

        // Must not be fluid
        if (!state.getFluidState().isEmpty()) return false;

        // Collision shape empty: allows air, tall grass, flowers, etc.
        if (!state.getCollisionShape(server, pos).isEmpty()) return false;

        // Also ensure the position won’t suffocate entities (extra safety)
        return server.isEmptyBlock(pos);
    }
}
