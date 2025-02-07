package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public class FairkeeperOurosShootVertexArrowGoal extends Goal {

    private final FairkeeperOurosEntity ouros;
    private FairkeeperOurosEntity.FairkeeperOurosState state;
    private int attackTicks;
    private FairkeeperOurosPartEntity currentPart;
    private int loopCount;

    private final int SHOOTING_INTERVAL = 15;
    private final int START_UP_DELAY = 40;
    private final int TOTAL_LOOP = 1;

    private final float ARROW_SPEED = 2.5F;

    public FairkeeperOurosShootVertexArrowGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros) {
        this.ouros = ouros;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        return this.ouros.getTarget() != null && this.ouros.getTarget().isAlive() && this.ouros.isState(state);
    }

    @Override
    public void start() {
        this.attackTicks = reducedTickDelay(SHOOTING_INTERVAL + START_UP_DELAY);
        this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        this.loopCount = 0;
    }

    @Override
    public void tick() {
        if (this.currentPart == null || this.currentPart.isTail()) {
            loopCount++;
            if (loopCount == TOTAL_LOOP) {
                this.ouros.stopAttacking(0);
            } else {
                this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
            }
            return;
        }

        if (this.attackTicks > 0) {
            this.attackTicks--;
            return;
        }

        this.attackTicks = reducedTickDelay(SHOOTING_INTERVAL);

        if (this.currentPart == null) {
            this.ouros.stopAttacking(0);
            return;
        }

        this.shootArrow();

        this.currentPart = (FairkeeperOurosPartEntity) this.currentPart.getChild();
        if (this.currentPart != null) {
            this.currentPart = (FairkeeperOurosPartEntity) this.currentPart.getChild();
        }
    }

    private void shootArrow() {
        Level level = this.ouros.level();
        VertexArrowProjectileEntity vertexArrow = new VertexArrowProjectileEntity(level, this.ouros);
        LivingEntity target = this.ouros.getTarget();
        double dx = target.getX() - this.currentPart.getX();
        double dy = target.getY() - this.currentPart.getY();
        double dz = target.getZ() - this.currentPart.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        dx /= distance;
        dy /= distance;
        dz /= distance;

        vertexArrow.setDeltaMovement(dx * ARROW_SPEED, dy * ARROW_SPEED, dz * ARROW_SPEED);
        vertexArrow.setPos(this.currentPart.getX(), this.currentPart.getY() - 0.1F, this.currentPart.getZ());
        level.addFreshEntity(vertexArrow);
    }
}
