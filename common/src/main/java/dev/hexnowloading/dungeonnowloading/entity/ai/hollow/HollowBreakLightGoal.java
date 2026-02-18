package dev.hexnowloading.dungeonnowloading.entity.ai.hollow;

import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class HollowBreakLightGoal extends Goal {

    private final PathfinderMob mob;
    private final int radius;
    private final int interval;
    private final double speed;

    private BlockPos target;
    private int cooldown;

    private int timeTrying;          // ticks spent trying current target
    private int nextRepathTick;      // to avoid repathing every tick
    private static final int GIVE_UP_TICKS = 200; // 10 seconds

    public HollowBreakLightGoal(PathfinderMob mob, int radius, int interval, double speed) {
        this.mob = mob;
        this.radius = radius;
        this.interval = Math.max(1, interval);
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.noPhysics) return false;

        boolean chargeCooldown = (mob instanceof HollowEntity h) && h.isChargeOnCooldown();

        // Only block break-light when we can see player AND we're NOT in charge cooldown
        if (!chargeCooldown) {
            if (mob.getTarget() instanceof Player p && mob.getSensing().hasLineOfSight(p)) return false;
        }

        if (cooldown-- > 0) return false;
        cooldown = interval;

        target = findTargetLight();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null) return false;
        if (mob.noPhysics) return false;

        boolean chargeCooldown = (mob instanceof HollowEntity h) && h.isChargeOnCooldown();

        if (!chargeCooldown) {
            if (mob.getTarget() instanceof Player p && mob.getSensing().hasLineOfSight(p)) return false;
        }

        return isValidLight(mob.level(), target);
    }

    @Override
    public void start() {
        timeTrying = 0;
        nextRepathTick = 0;
        moveToTarget();
    }

    @Override
    public void tick() {
        if (target == null) return;

        timeTrying++;
        if (timeTrying >= GIVE_UP_TICKS) {
            // Give up after 10s
            target = null;
            mob.getNavigation().stop();
            return;
        }

        // Repath occasionally (flying mobs can drift / get bumped)
        if (nextRepathTick-- <= 0) {
            nextRepathTick = 10; // repath twice per second
            moveToTarget();
        }

        // Reach check: within ~1 block of the light
        if (mob.blockPosition().closerThan(target, 2.0)) {
            interact(mob.level(), target);
            target = null;
            mob.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        target = null;
        timeTrying = 0;
        nextRepathTick = 0;
        mob.getNavigation().stop();
    }

    private void moveToTarget() {
        if (target == null) return;

        Path path = mob.getNavigation().createPath(target, 0);
        if (path == null || !path.canReach()) {
            timeTrying += 20; // punish: +1 second
            return;
        }

        mob.getNavigation().moveTo(path, speed);
    }

    private BlockPos findTargetLight() {
        Level level = mob.level();
        BlockPos origin = mob.blockPosition();

        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);

                    double dSq = pos.distSqr(origin);
                    if (dSq >= bestDistSq) continue;

                    if (!isValidLight(level, pos)) continue;

                    Path path = mob.getNavigation().createPath(pos, 0);
                    if (path == null || !path.canReach()) continue;

                    bestDistSq = dSq;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    private boolean isValidLight(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        Block b = state.getBlock();

        // Candles & Campfires: valid if LIT
        if (b instanceof CandleBlock || b instanceof CandleCakeBlock || b instanceof CampfireBlock) {
            return state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
        }

        // Other blocks: must emit light
        if (state.getLightEmission() <= 0) return false;

        // Must be instant break (your original rule)
        return state.getDestroySpeed(level, pos) == 0.0F;
    }


    private void interact(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // Extinguish candles/campfires (don't break)
        if ((block instanceof CandleBlock || block instanceof CandleCakeBlock || block instanceof CampfireBlock)
                && state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT)) {

            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), Block.UPDATE_ALL);
            playExtinguishFx((ServerLevel) level, pos);
            return;
        }

        if (block instanceof FireBlock) {
            playExtinguishFx((ServerLevel) level, pos);
        }

        // Break other light sources AND drop the item
        level.destroyBlock(pos, true);
    }

    private void playExtinguishFx(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.0F);
        level.sendParticles(ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                8, 0.12, 0.08, 0.12, 0.0
        );
    }
}
