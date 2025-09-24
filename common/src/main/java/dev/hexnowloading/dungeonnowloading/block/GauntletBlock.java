package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class GauntletBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public GauntletBlock(Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ACTIVE);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GauntletBlockEntity(pos, state);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; } // base model (no horns)
}
}
