package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WispLanternLightGoal extends Goal {
    private static final int INTERVAL_TICKS = reducedTickDelay(80);
    private static final int SUMMON_TRAIL_START_TICKS = reducedTickDelay(26);
    private static final int SUMMON_ANIMATION_TICKS = reducedTickDelay((int) Math.ceil(2.125D * 20.0D));
    private static final double CANDLE_TIP_Y_OFFSET = 0.92D;

    private final WispLanternEntity lantern;

    private int cooldown = INTERVAL_TICKS;
    private boolean summoning;
    private int summonTicks;
    @Nullable
    private BlockPos pendingSpawnPos;

    public WispLanternLightGoal(WispLanternEntity lantern) {
        this.lantern = lantern;
    }

    @Override
    public boolean canUse() {
        return this.lantern.isAlive()
                && this.lantern.getTarget() == null
                && this.lantern.findNearestVisibleLightTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.lantern.isAlive()
                && this.lantern.getTarget() == null
                && (this.summoning || this.lantern.findNearestVisibleLightTarget() != null);
    }

    @Override
    public void start() {
        this.cooldown = INTERVAL_TICKS;
        this.summoning = false;
        this.summonTicks = 0;
        this.pendingSpawnPos = null;
    }

    @Override
    public void stop() {
        this.summoning = false;
        this.summonTicks = 0;
        this.pendingSpawnPos = null;
        this.lantern.setSummoningWisp(false);
    }

    @Override
    public void tick() {
        if (this.summoning) {
            this.tickSummon();
            return;
        }

        if (--this.cooldown > 0) {
            return;
        }

        if (this.lantern.findNearestVisibleLightTarget() == null) {
            this.cooldown = INTERVAL_TICKS;
            return;
        }

        BlockPos spawnPos = this.lantern.findSummonPos();
        if (spawnPos == null) {
            this.cooldown = INTERVAL_TICKS;
            return;
        }

        this.summoning = true;
        this.summonTicks = 0;
        this.pendingSpawnPos = spawnPos;
        this.lantern.setSummoningWisp(true);
    }

    private void tickSummon() {
        this.summonTicks++;
        this.lantern.getMoveControl().setWantedPosition(this.lantern.getX(), this.lantern.getY(), this.lantern.getZ(), 0.0D);
        this.lantern.setDeltaMovement(Vec3.ZERO);

        if (this.summonTicks >= SUMMON_TRAIL_START_TICKS) {
            this.spawnSummonTrailParticles();
        }

        if (this.summonTicks < SUMMON_ANIMATION_TICKS) {
            return;
        }

        if (this.pendingSpawnPos != null) {
            this.lantern.spawnSummonedWisp(this.pendingSpawnPos, null);
        }

        this.summoning = false;
        this.summonTicks = 0;
        this.pendingSpawnPos = null;
        this.cooldown = INTERVAL_TICKS;
        this.lantern.setSummoningWisp(false);
    }

    private void spawnSummonTrailParticles() {
        if (!(this.lantern.level() instanceof ServerLevel server) || this.pendingSpawnPos == null) {
            return;
        }

        double trailTicks = Math.max(1.0D, SUMMON_ANIMATION_TICKS - SUMMON_TRAIL_START_TICKS);
        double progress = Mth.clamp((this.summonTicks - SUMMON_TRAIL_START_TICKS) / trailTicks, 0.0D, 1.0D);
        Vec3 start = this.lantern.position().add(0.0D, CANDLE_TIP_Y_OFFSET, 0.0D);
        Vec3 end = Vec3.atCenterOf(this.pendingSpawnPos).add(0.0D, -0.1D, 0.0D);
        Vec3 particlePos = start.lerp(end, progress);

        server.sendParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 3, 0.08D, 0.08D, 0.08D, 0.02D);
        if (this.summonTicks % 3 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 1, 0.04D, 0.04D, 0.04D, 0.005D);
        }
    }
}
