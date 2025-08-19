package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;

public class HangingAzuroLeavesBlock extends GrowingPlantBodyBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public HangingAzuroLeavesBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader lvl, BlockPos pos) {
        return this.canAttachTo(lvl.getBlockState(pos.above()));
    }

//    @Override public ItemStack getCloneItemStack(BlockGetter lvl, BlockPos pos, BlockState s) {
//        return new ItemStack(DNLBlocks.AZURO_HANGING_LEAVES_TIP.get().asItem());
//    }


    @Override
    protected boolean canAttachTo(BlockState state) {
        return state.is(DNLBlocks.AZURO_LEAVES.get())
                || state.is(DNLBlocks.AZURO_OAK_LOG.get())
                || state.is(DNLBlocks.AZURO_OAK_PLANKS.get())
                || state.is(DNLBlocks.AZURO_HANGING_LEAVES.get());
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock) DNLBlocks.AZURO_HANGING_LEAVES_TIP.get();
    }

}
