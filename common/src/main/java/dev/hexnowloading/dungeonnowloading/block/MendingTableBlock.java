package dev.hexnowloading.dungeonnowloading.block;

import com.mojang.serialization.MapCodec;

import dev.hexnowloading.dungeonnowloading.block.entity.MendingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
//add implements menuprovider
public class MendingTableBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<MendingTableBlock> CODEC = simpleCodec(MendingTableBlock::new);

    @Override
    public MapCodec<MendingTableBlock> codec() {
        return CODEC;
    }
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Tight hitbox matching the model: base slab (0-6), central pillar (3-13 from y 4-10), and top slab (10-16)
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 6, 16),        // base slab
            Block.box(3, 4, 3, 13, 10, 13),       // central pillar
            Block.box(0, 10, 0, 16, 16, 16)       // top slab
    );

    public MendingTableBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public void onRemove(BlockState state, Level lvl, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = lvl.getBlockEntity(pos);
            if (be instanceof MendingTableBlockEntity mending) {
                NonNullList<ItemStack> drops = NonNullList.create();
                for (int i = 0; i < 3; i++) {
                    ItemStack stack = mending.getItem(i);
                    if (!stack.isEmpty()) {
                        drops.add(stack.copy());
                    }
                }
                for (ItemStack stack : drops) {
                    Containers.dropItemStack(lvl, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
                mending.clearContent();
                lvl.updateNeighbourForOutputSignal(pos, this);
            } else if (be instanceof Container container) {
                Containers.dropContents(lvl, pos, container);
                lvl.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, lvl, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());

        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }


    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MendingTableBlockEntity mending) {
                player.openMenu(mending);
            }
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MendingTableBlockEntity(pos, state);
    }

}
