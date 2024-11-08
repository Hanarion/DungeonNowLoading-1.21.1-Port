package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.block.BallistaGolemStatueBlock;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
        world.addFreshEntity(golem);
        level.playSound(null, x, y, z, SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.8F);

    }

    public void alert(BlockPos blockPos, BallistaGolemStatueBlockEntity ballistaGolemStatueBlockEntity) {
        BallistaGolemStatueBlock ballistaGolemStatueBlock = (BallistaGolemStatueBlock) ballistaGolemStatueBlockEntity.getBlockState().getBlock();

        summonBallistaGolemEntity(level, blockPos, BallistaGolemStatueBlock.getDirection(ballistaGolemStatueBlockEntity.getBlockState()));
        BallistaGolemStatueBlock.destroyAllBlocks(level, blockPos);
    }
}
