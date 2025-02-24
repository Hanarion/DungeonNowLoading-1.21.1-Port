package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.PreserverBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class PreserverBlock extends BaseEntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private final Block blockType;

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

   /* @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PreserverBlockEntity preserver) {
                ServerLevel serverLevel = (ServerLevel) level;
                preserver.registerListener(serverLevel);
            }
        }
    }*/

    /*@Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onPlace(state, level, pos, newState, isMoving);
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PreserverBlockEntity preserver) {
                ServerLevel serverLevel = (ServerLevel) level;
                preserver.registerListener(serverLevel);
            }
        }
    }*/

    /*@Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PreserverBlockEntity preserver) {
                ServerLevel serverLevel = (ServerLevel) level;
                preserver.unregisterListener(serverLevel);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }*/

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(world, player, pos, state, blockEntity, tool);
        placeRuneBlock(world, pos, state);

    }

    @Override
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        if (level.isClientSide) {
            return;
        }
        placeRuneBlock(level, blockPos, level.getBlockState(blockPos));
    }

    private void placeRuneBlock(Level level, BlockPos blockPos, BlockState blockState) {
        level.setBlock(blockPos, this.blockType.defaultBlockState().setValue(LIT, true), Block.UPDATE_CLIENTS);
        if (!level.isClientSide) {
            level.scheduleTick(blockPos, this, 20);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!serverLevel.getBlockTicks().hasScheduledTick(blockPos, this)) {
            /*PreserverBlockEntity oldBlockEntity = (PreserverBlockEntity) serverLevel.getBlockEntity(blockPos);
            if (oldBlockEntity != null) {
                System.out.println(oldBlockEntity);
                oldBlockEntity.unregisterListener(serverLevel);
            }*/
            serverLevel.setBlock(blockPos, this.blockType.defaultBlockState().setValue(LIT, false), Block.UPDATE_CLIENTS);
           /* PreserverBlockEntity newBlockEntity = (PreserverBlockEntity) serverLevel.getBlockEntity(blockPos);
            if (newBlockEntity != null) {
                System.out.println(newBlockEntity);
                newBlockEntity.registerListener(serverLevel);
            }*/
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, DNLBlockEntityTypes.PRESERVER_BLOCK.get(), PreserverBlockEntity::tick);
    }
}
