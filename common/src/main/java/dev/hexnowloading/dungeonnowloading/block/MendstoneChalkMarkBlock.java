package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.MendstoneChalkMarkBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;

public class MendstoneChalkMarkBlock extends PreserverBlock implements SimpleWaterloggedBlock {

    public static final IntegerProperty OUTLINE = IntegerProperty.create("outline", 0, 4);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape UP_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape DOWN_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WEST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);

    private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Map.of(
            Direction.UP, UP_AABB,
            Direction.DOWN, DOWN_AABB,
            Direction.WEST, WEST_AABB,
            Direction.EAST, EAST_AABB,
            Direction.NORTH, NORTH_AABB,
            Direction.SOUTH, SOUTH_AABB
    );

    public MendstoneChalkMarkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(OUTLINE, 0).setValue(LIT, false).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(OUTLINE);
        builder.add(LIT);
        builder.add(WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace();
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(direction.getOpposite()));
        FluidState fluidstate = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        return blockState.is(this) && blockState.getValue(FACING) == direction ? this.defaultBlockState().setValue(FACING, direction.getOpposite()).setValue(WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8) : this.defaultBlockState().setValue(FACING, direction).setValue(WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return stateIn;
    }



    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return switch (state.getValue(FACING)) {
            case UP -> UP_AABB;
            case NORTH -> NORTH_AABB;
            case EAST -> EAST_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            default -> DOWN_AABB;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    public void setLitPreserverBlock(ServerLevel level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (be instanceof MendstoneChalkMarkBlockEntity mark) {
            // 1) increment damage like vanilla durability
            int newDamage = mark.getDamage() + 1;
            mark.setDamage(newDamage);

            // 2) break block if fully consumed
            if (newDamage >= MendstoneChalkMarkBlockEntity.MAX_USES) {
                // drop as item if desired (true), or just vanish (false)
                level.getServer().execute(() -> {
                    // Unregister listener (if you have one) and then destroy the block
                    level.playSound(null, pos, DNLSounds.MENDSTONE_CHALK_MARK_BREAK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                });
                return; // stop here, don't call super
            }

            // 3) compute outline stage based on damage
            int outlineStage = newDamage / 32;

            BlockState cur = level.getBlockState(pos);

            int previousOutlineStage = cur.getValue(OUTLINE);

            if (previousOutlineStage != outlineStage) {
                DustColorTransitionOptions dust2 = new DustColorTransitionOptions(
                        new Vector3f(0.45f, 0.80f, 1.0f),   // from
                        new Vector3f(0.90f, 0.95f, 1.0f),   // to
                        1.0f                                 // scale
                );
                level.sendParticles(dust2, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.0, 0.5, 0.5, 0.0);
                level.playSound(null, pos, DNLSounds.MENDSTONE_CHALK_MARK_CRACK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            level.setBlock(pos, cur.setValue(LIT, true).setValue(OUTLINE, outlineStage), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, 20);

            mark.setChanged();
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!serverLevel.getBlockTicks().hasScheduledTick(blockPos, this)) {
            BlockState cur = serverLevel.getBlockState(blockPos);
            serverLevel.setBlock(blockPos, cur.setValue(LIT, false), Block.UPDATE_ALL);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MendstoneChalkMarkBlockEntity(blockPos, blockState);
    }

    @Override
    protected boolean canPlayerDestroy() {
        return true;
    }
}