package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.block.BallistaGolemStatueBlock;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BallistaGolemStatueBlockEntity extends BlockEntity {
    public BallistaGolemStatueBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.BALLISTA_GOLEM_STATUE.get(), blockPos, blockState);
    }

    public void summonBallistaGolemEntity(Level world, BlockPos pos, Direction facing) {

        double x = pos.getX() + 0.5D, y = pos.getY(), z = pos.getZ() + 0.5D;

        BallistaGolemEntity golem = DNLEntityTypes.BALLISTA_GOLEM.get().create(world);
        golem.setPos(x, y, z); // Center the entity on the block
        golem.setYRot(facing.toYRot()); // Set the entity's rotation based on the facing direction
        golem.setYHeadRot(facing.toYRot());
        golem.setPersistenceRequired();
        world.addFreshEntity(golem);
        level.playSound(null, x, y, z, SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.8F);

    }

    public void alert(BlockPos blockPos, BallistaGolemStatueBlockEntity ballistaGolemStatueBlockEntity) {
        if (level.isClientSide) {
            return;
        }
        BallistaGolemStatueBlock ballistaGolemStatueBlock = (BallistaGolemStatueBlock) ballistaGolemStatueBlockEntity.getBlockState().getBlock();

        summonBallistaGolemEntity(level, blockPos, BallistaGolemStatueBlock.getDirection(ballistaGolemStatueBlockEntity.getBlockState()));
        BallistaGolemStatueBlock.destroyAllBlocks(level, blockPos);
        BallistaGolemStatueBlock.destroyBlocksAbove(level, blockPos);
        SoundEvent soundType = level.getBlockState(blockPos).getSoundType().getBreakSound();
        RandomSource random = this.getLevel().random;
        this.level.playSound(null, blockPos, soundType, SoundSource.BLOCKS, 1.0f, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
    }
}
