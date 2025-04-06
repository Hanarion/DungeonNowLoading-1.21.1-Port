package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import net.minecraft.sounds.SoundEvents;

import java.util.Set;
import java.util.UUID;

public class FairkeeperOurosShootVertexDomainGoal extends StoppableGoal {

    private final FairkeeperOurosEntity ouros;
    private FairkeeperOurosEntity.FairkeeperOurosState state;

    private FairkeeperSerpentCallerEntity caller;
    private Set<UUID> playerUUIDs;
    private int attackTicks;
    private int playerCount;

    private final int START_UP_DELAY = 40;

    public FairkeeperOurosShootVertexDomainGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros) {
        this.ouros = ouros;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        return this.ouros.getTarget() != null && this.ouros.getTarget().isAlive() && this.ouros.isState(state);
    }

    @Override
    public void start() {
        super.start();
        this.attackTicks = reducedTickDelay(START_UP_DELAY);
        this.playerCount = 1;
        this.caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        if (caller != null) {
            this.playerCount = caller.getParticipatingPlayerCount();
            this.playerUUIDs = caller.getParticipatingPlayerUUIDs();
        }
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

        float maxHealth = (playerCount + 1) * 25F;

        VertexDomainProjectileEntity vertexDomainProjectileEntity = new VertexDomainProjectileEntity(this.ouros.level(), this.ouros, maxHealth);
        vertexDomainProjectileEntity.shoot(this.ouros.getX(), this.ouros.getY() - 1, this.ouros.getZ(), this.caller.getX(), this.caller.getY() - this.caller.getArenaSize(), this.caller.getZ(), 0.3F, 0.5F);
        this.ouros.level().addFreshEntity(vertexDomainProjectileEntity);

        this.caller.addMinion(vertexDomainProjectileEntity.getUUID());

        this.ouros.level().playSound(null, this.ouros.getX(), this.ouros.getY() - 1, this.ouros.getZ(), SoundEvents.WITHER_SHOOT, this.ouros.getSoundSource(), 3.0F, 1.0F + (this.ouros.getRandom().nextFloat() - this.ouros.getRandom().nextFloat()) * 0.2F);

        this.stopGoal();
    }
}
