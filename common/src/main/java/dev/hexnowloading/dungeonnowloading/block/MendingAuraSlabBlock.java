package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class MendingAuraSlabBlock extends MendingAuraBlock {
    public static final EnumProperty<SlabType> TYPE;
    public static final BooleanProperty WATERLOGGED;
    public static final VoxelShape BOTTOM_AABB;
    public static final VoxelShape TOP_AABB;

    public MendingAuraSlabBlock(BlockBehaviour.Properties $$0) {
        super($$0);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM)).setValue(WATERLOGGED, false));
    }

    public boolean useShapeForLightOcclusion(BlockState $$0) {
        return $$0.getValue(TYPE) != SlabType.DOUBLE;
    }

    @Override
    public boolean skipRendering(BlockState blockState, BlockState blockState1, Direction direction) {
        return blockState1.is(this) && blockState1.getValue(TYPE) == SlabType.DOUBLE && blockState.getValue(TYPE) == SlabType.DOUBLE;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{TYPE, WATERLOGGED});
    }

    public VoxelShape getShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        SlabType $$4 = (SlabType)$$0.getValue(TYPE);
        switch ($$4) {
            case DOUBLE -> {
                return Shapes.block();
            }
            case TOP -> {
                return TOP_AABB;
            }
            default -> {
                return BOTTOM_AABB;
            }
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos $$1 = context.getClickedPos();
        BlockState $$2 = context.getLevel().getBlockState($$1);
        if ($$2.is(this)) {
            return (BlockState)((BlockState)$$2.setValue(TYPE, SlabType.DOUBLE)).setValue(WATERLOGGED, false);
        } else {
            FluidState $$3 = context.getLevel().getFluidState($$1);
            BlockState $$4 = (BlockState)((BlockState)this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM)).setValue(WATERLOGGED, $$3.getType() == Fluids.WATER);
            Direction $$5 = context.getClickedFace();
            return $$5 != Direction.DOWN && ($$5 == Direction.UP || !(context.getClickLocation().y - (double)$$1.getY() > 0.5)) ? $$4 : (BlockState)$$4.setValue(TYPE, SlabType.TOP);
        }
    }

    public boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
        ItemStack $$2 = $$1.getItemInHand();
        SlabType $$3 = (SlabType)$$0.getValue(TYPE);
        if ($$3 != SlabType.DOUBLE && $$2.is(this.asItem())) {
            if ($$1.replacingClickedOnBlock()) {
                boolean $$4 = $$1.getClickLocation().y - (double)$$1.getClickedPos().getY() > 0.5;
                Direction $$5 = $$1.getClickedFace();
                if ($$3 == SlabType.BOTTOM) {
                    return $$5 == Direction.UP || $$4 && $$5.getAxis().isHorizontal();
                } else {
                    return $$5 == Direction.DOWN || !$$4 && $$5.getAxis().isHorizontal();
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public FluidState getFluidState(BlockState $$0) {
        return (Boolean)$$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
    }

    public boolean placeLiquid(LevelAccessor $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
        return $$2.getValue(TYPE) != SlabType.DOUBLE ? super.placeLiquid($$0, $$1, $$2, $$3) : false;
    }

    public boolean canPlaceLiquid(BlockGetter $$0, BlockPos $$1, BlockState $$2, Fluid $$3) {
        return $$2.getValue(TYPE) != SlabType.DOUBLE ? super.canPlaceLiquid($$0, $$1, $$2, $$3) : false;
    }

    public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
        if ((Boolean)$$0.getValue(WATERLOGGED)) {
            $$3.scheduleTick($$4, Fluids.WATER, Fluids.WATER.getTickDelay($$3));
        }

        return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5);
    }

    public boolean isPathfindable(BlockState $$0, BlockGetter $$1, BlockPos $$2, PathComputationType $$3) {
        switch ($$3) {
            case LAND -> {
                return false;
            }
            case WATER -> {
                return $$1.getFluidState($$2).is(FluidTags.WATER);
            }
            case AIR -> {
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    static {
        TYPE = BlockStateProperties.SLAB_TYPE;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
        TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    }
}
