package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MendingAuraBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final SoundType SOUND_TYPE = new SoundType(1.0F, 1.0F, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, DNLSounds.MENDING_AURA_POP.get(), SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);

    public MendingAuraBlock(Properties $$0) {
        super($$0);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean skipRendering(BlockState p_53972_, BlockState p_53973_, Direction p_53974_) {
        return p_53973_.is(this) ? true : super.skipRendering(p_53972_, p_53973_, p_53974_);
    }

    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

    public VoxelShape getVisualShape(BlockState p_48735_, BlockGetter p_48736_, BlockPos p_48737_, CollisionContext p_48738_) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MendingAuraBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public void startRestoration(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            level.scheduleTick(pos, this, mendingAuraBlockEntity.getRestoreTime());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            mendingAuraBlockEntity.restoreBlock(level, pos, state);
        }
    }

    /*@org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, DNLBlockEntityTypes.MENDING_AURA.get(), MendingAuraBlockEntity::tick);
    }*/

    /*@Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);

        if (blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            MendingAuraBlockEntity.tick(serverLevel, blockPos, blockState, mendingAuraBlockEntity);
        }
    }*/

    @Override
    public SoundType getSoundType(BlockState blockState) {
        return SOUND_TYPE;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        super.animateTick(blockState, level, blockPos, randomSource);

        if (randomSource.nextInt(60) == 0) {
            List<Vec3> possibleDirections = new ArrayList<>();

            if (level.isEmptyBlock(blockPos.north())) possibleDirections.add(new Vec3(0.0, 0.0, -1.0)); // Move North (-Z)
            if (level.isEmptyBlock(blockPos.south())) possibleDirections.add(new Vec3(0.0, 0.0, 1.0));  // Move South (+Z)
            if (level.isEmptyBlock(blockPos.east())) possibleDirections.add(new Vec3(1.0, 0.0, 0.0));   // Move East (+X)
            if (level.isEmptyBlock(blockPos.west())) possibleDirections.add(new Vec3(-1.0, 0.0, 0.0));  // Move West (-X)
            if (level.isEmptyBlock(blockPos.above())) possibleDirections.add(new Vec3(0.0, 1.0, 0.0));  // Move Up (+Y)
            if (level.isEmptyBlock(blockPos.below())) possibleDirections.add(new Vec3(0.0, -1.0, 0.0)); // Move Down (-Y)

            Vec3 velocity = new Vec3(0.0, 0.0, 0.0);
            Vec3 spawnOffset = new Vec3(0.0, 0.0, 0.0);

            if (!possibleDirections.isEmpty()) {
                Vec3 chosenDirection = possibleDirections.get(randomSource.nextInt(possibleDirections.size()));
                velocity = chosenDirection.scale(0.06);
                spawnOffset = chosenDirection;
            }

            double x = blockPos.getX() + randomSource.nextDouble() + spawnOffset.x * (0.5F + randomSource.nextDouble() * 1.0F);
            double y = blockPos.getY() + randomSource.nextDouble() + spawnOffset.y * (0.5F + randomSource.nextDouble() * 1.0F);
            double z = blockPos.getZ() + randomSource.nextDouble() + spawnOffset.z * (0.5F + randomSource.nextDouble() * 1.0F);

            level.addParticle(DNLParticleTypes.MENDING_POP_AND_RUNE_PARTICLE.get(), true, x, y, z, -velocity.x, -velocity.y, -velocity.z);
        }
    }
}
