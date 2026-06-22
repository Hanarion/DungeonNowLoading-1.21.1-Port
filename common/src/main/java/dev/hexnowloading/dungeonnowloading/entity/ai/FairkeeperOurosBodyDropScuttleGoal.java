package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
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

public class FairkeeperOurosBodyDropScuttleGoal extends StoppableGoal{
    private final FairkeeperOurosPartEntity part;
    private final FairkeeperOurosPartEntity.FairkeeperOurosPartState state;

    private int progress;

    public FairkeeperOurosBodyDropScuttleGoal(FairkeeperOurosPartEntity part, FairkeeperOurosPartEntity.FairkeeperOurosPartState state) {
        this.part = part;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        return this.part.isState(this.state);
    }

    @Override
    public void start() {
        super.start();
        this.progress = 0;
    }

    @Override
    public void stop() {
        this.part.setState(FairkeeperOurosPartEntity.FairkeeperOurosPartState.IDLE);
    }

    @Override
    public void tick() {
        if (this.progress == 0) {
            this.part.playScuttleDoorOpenAnimation(this::printAndAdd);
            this.progress++;
        } else if (this.progress == 2) {
            this.droppingLogic();
        } else if (this.progress == 3) {
            this.part.playScuttleDoorCloseAnimation(this::stopGoal);
            this.progress++;
        }
    }

    private void printAndAdd() {
        this.progress++;
    }

    private void droppingLogic() {
        Level level = this.part.level();

        ScuttleEntity scuttle = DNLEntityTypes.SCUTTLE.get().create(level);
        scuttle = (ScuttleEntity) SpawnMobUtil.spawnEntityWithRot(scuttle, this.part.getX(), this.part.getY() - 2.0F, this.part.getZ(), this.part.getYRot(), 0.0F, level);
        scuttle.setYBodyRot(this.part.getYRot());
        scuttle.setYHeadRot(this.part.getYRot());
        scuttle.lootTable = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "empty");
        scuttle.skipDropExperience();
        level.addFreshEntity(scuttle);

        ((FairkeeperOurosEntity) this.part.getHead()).getCaller().addMinion(scuttle.getUUID());

        level.playSound(null, this.part.getX(), this.part.getY() - 0.5F, this.part.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.8F);

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.part.getX(), this.part.getY(), part.getZ());

        while (mutableBlockPos.getY() > level.getMinBuildHeight() && !level.getBlockState(mutableBlockPos).blocksMotion()) {
            mutableBlockPos.move(Direction.DOWN);
        }

        BlockState blockState = level.getBlockState(mutableBlockPos);
        if (blockState.blocksMotion()) {
            ((ServerLevel) level).sendParticles(new ScalableAxisParticleType.ScalableAxisParticleData(DNLParticleTypes.REDSTONE_HAZARD_INDICATOR_PARTICLE.get(), 0, 90, 1.25F), mutableBlockPos.getX() + 0.5F, mutableBlockPos.getY() + 1.05F, mutableBlockPos.getZ() + 0.5F, 1, 0, 0, 0, 0);
        }

        this.progress++;
    }
}
