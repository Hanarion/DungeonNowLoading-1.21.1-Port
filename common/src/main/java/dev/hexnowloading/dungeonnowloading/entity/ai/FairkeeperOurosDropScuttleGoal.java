package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.ScuttleEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.SpawnMobUtil;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;

public class FairkeeperOurosDropScuttleGoal extends Goal {
    private final FairkeeperOurosEntity ouros;
    private final FairkeeperOurosEntity.FairkeeperOurosState state;

    private int attackTicks;
    private int loopCount;
    private FairkeeperOurosPartEntity currentPart;
    private FairkeeperSerpentCallerEntity caller;
    private int playerCount;

    private final int START_UP_DELAY = 40;
    private final int SUMMON_INTERVAL = 20;
    private final int MAX_SCUTTLE_COUNT = 3;


    public FairkeeperOurosDropScuttleGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros) {
        this.ouros = ouros;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        return this.ouros.getTarget() != null && this.ouros.getTarget().isAlive() && this.ouros.isState(state);
    }

    @Override
    public void start() {
        this.attackTicks = reducedTickDelay(START_UP_DELAY);
        this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        this.playerCount = 1;
        this.caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        if (caller != null) {
            this.playerCount = caller.getParticipatingPlayerCount();
        }
        this.loopCount = Math.min(this.playerCount, MAX_SCUTTLE_COUNT);
    }

    @Override
    public void tick() {
        if (this.attackTicks > 0) {
            this.attackTicks--;
            return;
        }

        if (this.currentPart == null) {
            this.ouros.stopAttacking(0);
            return;
        }

        this.summonScuttle();

        loopCount--;

        if (this.loopCount <= 0) {
            this.ouros.stopAttacking(0);
            return;
        }

        this.attackTicks = SUMMON_INTERVAL;

        for (int i = 0; i < 4; i++) {
            this.currentPart = (FairkeeperOurosPartEntity) this.currentPart.getChild();
            if (this.currentPart == null) {
                this.ouros.stopAttacking(0);
            }
        }
    }

    private void summonScuttle() {

        ScuttleEntity scuttle = DNLEntityTypes.SCUTTLE.get().create(this.ouros.level());
        scuttle = (ScuttleEntity) SpawnMobUtil.spawnEntityWithRot(scuttle, this.currentPart.getX(), this.currentPart.getY() - 0.5F, this.currentPart.getZ(), this.currentPart.getYRot(), 0.0F, this.ouros.level());
        scuttle.setYBodyRot(this.currentPart.getYRot());
        scuttle.setYHeadRot(this.currentPart.getYRot());
        scuttle.lootTable = new ResourceLocation(DungeonNowLoading.MOD_ID, "empty");
        scuttle.skipDropExperience();
        this.ouros.level().addFreshEntity(scuttle);

        this.caller.addMinion(scuttle.getUUID());

        this.ouros.level().playSound(null, this.currentPart.getX(), this.currentPart.getY() - 0.5F, this.currentPart.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 1.0F, this.ouros.level().random.nextFloat() * 0.2F + 0.8F);

    }
}
