package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeepsteelMountedRailBlock extends RailBlock {
    protected static final VoxelShape NORTH_SOUTH_PLATFORM_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 2, 8, 8),
            Block.box(14, 0, 0, 16, 8, 8),
            Block.box(0, 8, 8, 2, 16, 16),
            Block.box(14, 8, 8, 16, 16, 16)
    ).optimize();
    protected static final VoxelShape EAST_WEST_PLATFORM_SHAPE = rotateY(NORTH_SOUTH_PLATFORM_SHAPE);

    private final Item railDrop;

    public DeepsteelMountedRailBlock(Properties properties, Item railDrop) {
        super(properties);
        this.railDrop = railDrop;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(super.getShape(state, level, pos, context), platformShape(state)).optimize();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return platformShape(state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!level.isClientSide && newState.isAir()) {
            level.setBlock(pos, platformStateFromRail(state), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateDir(Level level, BlockPos pos, BlockState state, boolean forceUpdate) {
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide && level.getBlockState(pos).is(this)) {
            this.updateState(state, level, pos, block);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(railDrop);
    }

    public Item getRailDrop() {
        return railDrop;
    }

    protected static VoxelShape platformShape(BlockState state) {
        RailShape shape = state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty());
        return shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST ? EAST_WEST_PLATFORM_SHAPE : NORTH_SOUTH_PLATFORM_SHAPE;
    }

    public static BlockState platformStateFromRail(BlockState railState) {
        return DNLBlocks.DEEPSTEEL_SLOPED_PLATFORM_FLOATING_RAIL.get().defaultBlockState()
                .setValue(DeepsteelPlatformBlock.FACING, DeepsteelRailMounts.facingFromRailShape(railState.getValue(((BaseRailBlock) railState.getBlock()).getShapeProperty())))
                .setValue(DeepsteelPlatformBlock.WATERLOGGED, railState.hasProperty(WATERLOGGED) && railState.getValue(WATERLOGGED));
    }

    private static VoxelShape rotateY(VoxelShape shape) {
        VoxelShape rotated = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            rotated = Shapes.or(rotated, Shapes.create(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX));
        }
        return rotated.optimize();
    }

    public static Properties properties() {
        return Properties.of().noOcclusion().strength(0.7F).sound(net.minecraft.world.level.block.SoundType.METAL);
    }

    public static Item defaultDrop() {
        return Items.RAIL;
    }
}
