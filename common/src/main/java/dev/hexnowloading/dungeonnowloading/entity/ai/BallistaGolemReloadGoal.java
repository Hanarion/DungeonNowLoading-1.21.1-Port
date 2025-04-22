package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BallistaGolemReloadGoal extends Goal {

    private final BallistaGolemEntity ballistaGolemEntity;
    private int nextScanTick;
    private int attackTicks;

    public BallistaGolemReloadGoal(BallistaGolemEntity ballistaGolemEntity) {
        this.ballistaGolemEntity = ballistaGolemEntity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private int nextStartTick() { return reducedTickDelay(10); }

    private int nextCooldownStartTick() { return reducedTickDelay(60 + ballistaGolemEntity.getRandom().nextInt(40)); }

    @Override
    public boolean canUse() {
        if (nextScanTick > 0) {
            --this.nextScanTick;
            return false;
        } else {
            this.nextScanTick = this.nextStartTick();
            return this.ballistaGolemEntity.isState(BallistaGolemEntity.BallistaGolemState.IDLE) && this.ballistaGolemEntity.getBallistaArrowCount() <= 0;
        }
    }

    @Override
    public void start() {
        this.ballistaGolemEntity.setState(BallistaGolemEntity.BallistaGolemState.RELOAD);
        this.attackTicks = reducedTickDelay(200);
        this.ballistaGolemEntity.triggerReloadAnimation();
        //this.ballistaGolemEntity.playSound(DNLSounds.BALLISTA_GOLEM_RELOAD.get(), 1.5F, 1.0F);
    }

    @Override
    public boolean canContinueToUse() {
        return this.ballistaGolemEntity.isState(BallistaGolemEntity.BallistaGolemState.RELOAD);
    }

    @Override
    public void tick() {
        if (this.attackTicks > 0) {
            this.attackTicks--;
        } else {
            this.ballistaGolemEntity.setState(BallistaGolemEntity.BallistaGolemState.IDLE);
            this.ballistaGolemEntity.triggerIdleAnimation();
            this.nextScanTick = this.nextCooldownStartTick();
            return;
        }
        if (this.attackTicks == reducedTickDelay(200 - 10)) {
            this.ballistaGolemEntity.playBallsitaGolemSound(DNLSounds.BALLISTA_GOLEM_RELOAD.get());
        }
        if (this.attackTicks == reducedTickDelay(200 - 180)) {
            this.ballistaGolemEntity.setBallistaArrowCount(6);
        }
    }
}
