package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WispBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D),
            Block.box(5.5D, 5.5D, 5.5D, 10.5D, 10.5D, 10.5D)
    );

    public WispBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        if (random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 0.5D;
            double cz = pos.getZ() + 0.5D;
            if (level instanceof ServerLevel serverLevel) {
                spawnBurstParticles(serverLevel, cx, cy, cz, ParticleTypes.FLAME, 12, 0.18D, 0.18D);
                spawnBurstParticles(serverLevel, cx, cy, cz, ParticleTypes.SMOKE, 6, 0.18D, 0.18D);
            }
            level.playSound(null, cx, cy, cz, DNLSounds.WISP_DEATH.get(), SoundSource.BLOCKS, 0.9F, 2.0F);
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static void playPlacementEffects(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.5D;
        double cz = pos.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.POOF, cx, cy, cz, 5, 0.2D, 0.2D, 0.2D, 0.01D);
        level.playSound(null, cx, cy, cz, DNLSounds.WISP_FLARE_UP.get(), SoundSource.BLOCKS, 0.9F, 1.2F);
    }

    private static void spawnBurstParticles(ServerLevel level, double x, double y, double z, net.minecraft.core.particles.ParticleOptions particle, int count, double horizontalSpeed, double verticalSpeed) {
        for (int i = 0; i < count; i++) {
            double vx = (level.random.nextDouble() - 0.5D) * horizontalSpeed;
            double vy = (level.random.nextDouble() - 0.5D) * verticalSpeed;
            double vz = (level.random.nextDouble() - 0.5D) * horizontalSpeed;
            level.sendParticles(particle, x, y, z, 0, vx, vy, vz, 1.0D);
        }
    }
}
