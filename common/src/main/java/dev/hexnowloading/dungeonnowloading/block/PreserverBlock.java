package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.PreserverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class PreserverBlock extends BaseEntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private final Block blockType;
    private int radius;
    private int thickness;

    public PreserverBlock(Properties $$0) {
        super($$0);
        this.blockType = this.asBlock();
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState().setValue(LIT, false);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            BlockEntity blockEntityOld = level.getBlockEntity(blockPos);

            System.out.println("Old Block Entity : " + blockEntityOld);
            if (blockEntityOld instanceof PreserverBlockEntity preserverBlockEntity) {
                radius = preserverBlockEntity.getUser().getListenerRadius();
                thickness = preserverBlockEntity.getUser().getThickness();
                System.out.println("Old Radius : " + preserverBlockEntity.getUser().getListenerRadius());
                System.out.println("Old Thickness : " + preserverBlockEntity.getUser().getThickness());
            }
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, blockPos, state, blockEntity, tool);
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            level.setBlock(blockPos, this.blockType.defaultBlockState().setValue(LIT, true), Block.UPDATE_CLIENTS);

            BlockEntity blockEntityNew = level.getBlockEntity(blockPos);

            if (blockEntityNew instanceof PreserverBlockEntity preserverBlockEntity) {
                preserverBlockEntity.getUser().setListenerRadius(radius);
                preserverBlockEntity.getUser().setThickness(thickness);
                System.out.println("New Radius : " + preserverBlockEntity.getUser().getListenerRadius());
                System.out.println("New Thickness : " + preserverBlockEntity.getUser().getThickness());

            }

            if (!level.isClientSide) {
                level.scheduleTick(blockPos, this, 20);
            }
        }


    }

    @Override
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        if (level.isClientSide) {
            return;
        }
        BlockEntity blockEntityOld = level.getBlockEntity(blockPos);

        System.out.println("Old Block Entity : " + blockEntityOld);
        if (blockEntityOld instanceof PreserverBlockEntity preserverBlockEntity) {
            radius = preserverBlockEntity.getUser().getListenerRadius();
            thickness = preserverBlockEntity.getUser().getThickness();
            System.out.println("Old Radius : " + preserverBlockEntity.getUser().getListenerRadius());
            System.out.println("Old Thickness : " + preserverBlockEntity.getUser().getThickness());
        }

    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!serverLevel.getBlockTicks().hasScheduledTick(blockPos, this)) {
            serverLevel.setBlock(blockPos, this.blockType.defaultBlockState().setValue(LIT, false), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PreserverBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public void setLitPreserverBlock(ServerLevel serverLevel, BlockPos blockPos) {
        serverLevel.setBlock(blockPos, this.blockType.defaultBlockState().setValue(LIT, true), Block.UPDATE_CLIENTS);
        serverLevel.scheduleTick(blockPos, this, 20);
    }
}
