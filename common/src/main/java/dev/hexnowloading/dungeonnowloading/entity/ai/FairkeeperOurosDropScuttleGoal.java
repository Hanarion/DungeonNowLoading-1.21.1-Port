package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.SpawnMobUtil;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FairkeeperOurosDropScuttleGoal extends StoppableGoal {
    private final FairkeeperOurosEntity ouros;
    private final FairkeeperOurosEntity.FairkeeperOurosState state;
    private final int scuttleCount;

    private int attackTicks;
    private int loopCount;
    private FairkeeperOurosPartEntity currentPart;
    private FairkeeperSerpentCallerEntity caller;
    private int playerCount;

    private final int START_UP_DELAY = 40;
    private final int SUMMON_INTERVAL = 20;
    private final int MAX_SCUTTLE_COUNT = 5;


    public FairkeeperOurosDropScuttleGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros, int scuttleCount) {
        this.ouros = ouros;
        this.state = state;
        this.scuttleCount = scuttleCount;
    }

    @Override
    public boolean canUse() {
        return this.ouros.getTarget() != null && this.ouros.getTarget().isAlive() && this.ouros.isState(state);
    }

    @Override
    public void start() {
        super.start();
        this.attackTicks = reducedTickDelay(START_UP_DELAY);
        this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        this.playerCount = 1;
        this.caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        if (caller != null) {
            this.playerCount = caller.getParticipatingPlayerCount();
        }
        this.loopCount = Math.min(this.playerCount + this.scuttleCount - 1, MAX_SCUTTLE_COUNT);
    }

    @Override
    public void stop() {
        this.ouros.stopAttacking(20);
    }

    @Override
    public void tick() {
        if (this.attackTicks > 0) {
            this.attackTicks--;
            return;
        }

        if (this.currentPart == null) {
            this.stopGoal();
            return;
        }

        this.currentPart.triggerDropScuttleAnimation();
        this.summonScuttle();

        loopCount--;

        if (this.loopCount <= 0) {
            this.stopGoal();
            return;
        }

        this.attackTicks = SUMMON_INTERVAL;

        for (int i = 0; i < 4; i++) {
            if (this.currentPart.getChild() instanceof FairkeeperOurosPartEntity fairkeeperOurosPartEntity) {
                this.currentPart = fairkeeperOurosPartEntity;
            } else {
                this.stopGoal();
            }
        }
    }

    private void summonScuttle() {

        Level level = this.ouros.level();

        ScuttleEntity scuttle = DNLEntityTypes.SCUTTLE.get().create(level);
        scuttle = (ScuttleEntity) SpawnMobUtil.spawnEntityWithRot(scuttle, this.currentPart.getX(), this.currentPart.getY() - 0.5F, this.currentPart.getZ(), this.currentPart.getYRot(), 0.0F, this.ouros.level());
        scuttle.setYBodyRot(this.currentPart.getYRot());
        scuttle.setYHeadRot(this.currentPart.getYRot());
        scuttle.lootTable = new ResourceLocation(DungeonNowLoading.MOD_ID, "empty");
        scuttle.skipDropExperience();
        level.addFreshEntity(scuttle);

        this.caller.addMinion(scuttle.getUUID());

        level.playSound(null, this.currentPart.getX(), this.currentPart.getY() - 0.5F, this.currentPart.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 1.0F, this.ouros.level().random.nextFloat() * 0.2F + 0.8F);

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.currentPart.getX(), this.currentPart.getY(), currentPart.getZ());

        while (mutableBlockPos.getY() > level.getMinBuildHeight() && !level.getBlockState(mutableBlockPos).blocksMotion()) {
            mutableBlockPos.move(Direction.DOWN);
        }

        BlockState blockState = level.getBlockState(mutableBlockPos);
        if (blockState.blocksMotion()) {
            ((ServerLevel) level).sendParticles(new ScalableAxisParticleType.ScalableAxisParticleData(DNLParticleTypes.REDSTONE_HAZARD_INDICATOR_PARTICLE.get(), 0, 90, 1.25F), mutableBlockPos.getX() + 0.5F, mutableBlockPos.getY() + 1.05F, mutableBlockPos.getZ() + 0.5F, 1, 0, 0, 0, 0);
        }
    }
}
