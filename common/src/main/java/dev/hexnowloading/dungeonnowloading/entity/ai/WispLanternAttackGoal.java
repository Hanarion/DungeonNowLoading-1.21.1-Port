package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.HoveringFlyingMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WispLanternAttackGoal extends Goal {
    private static final int INTERVAL_TICKS = reducedTickDelay(80);   // 5 seconds
    private static final int SUMMON_TRAIL_START_TICKS = reducedTickDelay(26);
    private static final int SUMMON_ANIMATION_TICKS = reducedTickDelay((int)Math.ceil(2.125D * 20.0D));
    private static final double CANDLE_TIP_Y_OFFSET = 0.92D;
    private static final int H_RADIUS = 2;           // horizontal radius
    private static final int V_RANGE = 1;            // vertical +/- range
    private static final int MAX_ATTEMPTS = 12;

    private final Mob owner;
    private final Level level;
    private final RandomSource rng;

    private int cooldown = INTERVAL_TICKS;
    private boolean summoning;
    private int summonTicks;
    private BlockPos pendingSpawnPos;

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
        summoning = false;
        summonTicks = 0;
        pendingSpawnPos = null;
    }

    @Override
    public void stop() {
        summoning = false;
        summonTicks = 0;
        pendingSpawnPos = null;
        this.setSummoningAnimation(false);
    }

    @Override
    public void tick() {
        if (summoning) {
            tickSummon();
            return;
        }

        if (cooldown == reducedTickDelay(20)) {
            ((HoveringFlyingMoveControl)owner.getMoveControl()).setWantedPosition();
        }
        if (--cooldown > 0) return;

        BlockPos spawnPos = tryFindSpawnPos(this.level);
        if (spawnPos == null) {
            cooldown = INTERVAL_TICKS;
            return;
        }

        beginSummon(spawnPos);
    }

    private void beginSummon(BlockPos spawnPos) {
        summoning = true;
        summonTicks = 0;
        pendingSpawnPos = spawnPos;
        this.setSummoningAnimation(true);
    }

    private void tickSummon() {
        summonTicks++;

        if (summonTicks >= SUMMON_TRAIL_START_TICKS) {
            spawnSummonTrailParticles();
        }

        if (summonTicks < SUMMON_ANIMATION_TICKS) {
            return;
        }

        if (pendingSpawnPos != null) {
            spawnWisp(pendingSpawnPos);
        }

        summoning = false;
        summonTicks = 0;
        pendingSpawnPos = null;
        cooldown = INTERVAL_TICKS;
        this.setSummoningAnimation(false);
    }

    private void spawnWisp(BlockPos spawnPos) {
        WispEntity wisp = DNLEntityTypes.WISP.get().create(level);
        if (wisp == null) return;
        wisp.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        wisp.setOwner(owner);
        if (owner.getTarget() != null) {
            LivingEntity target = owner.getTarget();
            wisp.setTarget(target);

            // --- Rotate Wisp to face target immediately ---
            double dx = target.getX() - wisp.getX();
            double dz = target.getZ() - wisp.getZ();
            float yaw = (float)(Mth.atan2(dz, dx) * (180F / Math.PI)) - 90F;  // convert to Minecraft yaw

            wisp.setYRot(yaw);
            wisp.setYHeadRot(yaw);
            wisp.setYBodyRot(yaw);
        }
        if (level.noCollision(wisp)) {
            if (level instanceof ServerLevel server) {
                server.sendParticles(
                        ParticleTypes.POOF,
                        wisp.getX(), wisp.getY(), wisp.getZ(),
                        10, 0.2, 0.2, 0.2, 0.01
                );
            }
            level.addFreshEntity(wisp);
        }
    }

    private void setSummoningAnimation(boolean summoningWisp) {
        if (owner instanceof WispLanternEntity wispLantern) {
            wispLantern.setSummoningWisp(summoningWisp);
        }
    }

    private void spawnSummonTrailParticles() {
        if (!(level instanceof ServerLevel server) || pendingSpawnPos == null) {
            return;
        }

        double trailTicks = Math.max(1.0D, SUMMON_ANIMATION_TICKS - SUMMON_TRAIL_START_TICKS);
        double progress = Mth.clamp((summonTicks - SUMMON_TRAIL_START_TICKS) / trailTicks, 0.0D, 1.0D);
        Vec3 start = this.getCandleTipParticlePos();
        Vec3 end = Vec3.atCenterOf(pendingSpawnPos).add(0.0D, -0.1D, 0.0D);
        Vec3 particlePos = start.lerp(end, progress);

        server.sendParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 3, 0.08D, 0.08D, 0.08D, 0.02D);
        if (summonTicks % 3 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 1, 0.04D, 0.04D, 0.04D, 0.005D);
        }
    }

    private Vec3 getCandleTipParticlePos() {
        return owner.position().add(0.0D, CANDLE_TIP_Y_OFFSET, 0.0D);
    }

    private BlockPos tryFindSpawnPos(Level server) {
        BlockPos base = owner.blockPosition();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int dx = rng.nextInt(-H_RADIUS, H_RADIUS + 1);
            int dz = rng.nextInt(-H_RADIUS, H_RADIUS + 1);
            if (dx == 0 && dz == 0) {
                dx = rng.nextBoolean() ? H_RADIUS : -H_RADIUS;
            }

            int dy = rng.nextFloat() < 0.7F ? 0 : rng.nextInt(-V_RANGE, V_RANGE + 1);

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
