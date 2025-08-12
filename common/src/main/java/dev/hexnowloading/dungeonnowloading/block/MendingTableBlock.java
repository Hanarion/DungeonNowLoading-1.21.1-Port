package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.world.item.context.BlockPlaceContext;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MendingTableBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public MendingTableBlock(Properties p){ super(p); registerDefaultState(stateDefinition.any().setValue(WATERLOGGED,false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b){ b.add(WATERLOGGED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){
        boolean water = c.getLevel().getFluidState(c.getClickedPos()).is(Fluids.WATER);
        return defaultBlockState().setValue(WATERLOGGED, water);
    }
    @Override public FluidState getFluidState(BlockState s){
        return s.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(s);
    }

    // The only method you MUST implement:
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MendingTableBlockEntity(pos, state); // a do-nothing BE is fine for now
    }
    // No ticker yet -> BE won’t tick, which is fine.
}
