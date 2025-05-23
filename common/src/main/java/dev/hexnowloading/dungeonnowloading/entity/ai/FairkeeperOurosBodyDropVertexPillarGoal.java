package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexPillarProjectileEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FairkeeperOurosBodyDropVertexPillarGoal extends StoppableGoal {

    private final FairkeeperOurosPartEntity part;
    private final FairkeeperOurosPartEntity.FairkeeperOurosPartState state;

    private int progress;
    private boolean animationEnded;

    public FairkeeperOurosBodyDropVertexPillarGoal(FairkeeperOurosPartEntity part, FairkeeperOurosPartEntity.FairkeeperOurosPartState state) {
        this.part = part;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        return this.part.isState(this.state) && this.part.getDropPosition() != null;
    }

    @Override
    public void start() {
        super.start();
        this.progress = 0;
    }

    @Override
    public void stop() {
        this.part.setState(FairkeeperOurosPartEntity.FairkeeperOurosPartState.IDLE);
        this.part.setDropPosition(null);
    }

    @Override
    public void tick() {
        if (this.progress == 0 || this.progress == 1) {
            this.droppingLogic();
        } else if (this.progress == 2) {
            if (this.part.playDoorCloseAnimation()) {
                this.progress++;
            } else {
                this.stopGoal();
            }
        } else if (this.progress == 3) {
            if (this.part.getAnimationChainer().isFinished()) {
                this.stopGoal();
            }
        }
    }

    private void droppingLogic() {
        Vec3 dropPosition = new Vec3(this.part.getDropPosition().getX(), this.part.getDropPosition().getY(), this.part.getDropPosition().getZ());
        double distance = this.part.position().distanceTo(dropPosition);

        if (this.progress == 0 && distance < 3.0F) {
            this.part.playVertexPillarDoorOpenAnimation(null);
            //this.part.transitionTo(FairkeeperOurosPartEntity.FairkeeperOurosPartAnimationState.SCUTTLE_OPEN);
            this.progress++;
        }
        if (this.progress <= 1 && distance < 1.0F) {
            Level level = this.part.level();
            VertexPillarProjectileEntity stonePillar = new VertexPillarProjectileEntity(level, (LivingEntity) this.part.getHead(), 0.8F, true);
            stonePillar.setPos(dropPosition.x + 0.5, dropPosition.y, dropPosition.z + 0.5);
            level.addFreshEntity(stonePillar);

            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(dropPosition.x, dropPosition.y, dropPosition.z);
            while (mutableBlockPos.getY() > level.getMinBuildHeight() && !level.getBlockState(mutableBlockPos).blocksMotion()) {
                mutableBlockPos.move(Direction.DOWN);
            }

            BlockState blockState = level.getBlockState(mutableBlockPos);
            if (blockState.blocksMotion()) {
                ((ServerLevel) level).sendParticles(new ScalableAxisParticleType.ScalableAxisParticleData(DNLParticleTypes.REDSTONE_HAZARD_INDICATOR_PARTICLE.get(), 0, 90, 1.0F), mutableBlockPos.getX() + 0.5F, mutableBlockPos.getY() + 1.05F, mutableBlockPos.getZ() + 0.5F, 1, 0, 0, 0, 0);
            }

            this.progress++;
        }
    }
}
