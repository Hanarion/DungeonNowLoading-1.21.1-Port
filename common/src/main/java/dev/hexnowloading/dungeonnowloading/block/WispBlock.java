package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.WispBlockEntity;
import dev.hexnowloading.dungeonnowloading.item.WisplightRodItem;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.world.WispBlockTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WispBlock extends Block implements EntityBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final int ROTATIONS = RotationSegment.getMaxSegmentIndex() + 1;
    private static final double AMBIENT_PARTICLE_RANGE = 4.0D / 16.0D;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D),
            Block.box(5.5D, 5.5D, 5.5D, 10.5D, 10.5D, 10.5D)
    );

    public WispBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), ROTATIONS));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), ROTATIONS));
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WispBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == DNLBlockEntityTypes.WISP_BLOCK.get()
                ? (tickerLevel, pos, tickerState, blockEntity) -> WispBlockEntity.serverTick(tickerLevel, pos, tickerState, (WispBlockEntity) blockEntity)
                : null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D + (random.nextDouble() * 2.0D - 1.0D) * AMBIENT_PARTICLE_RANGE;
        double y = pos.getY() + 0.5D + (random.nextDouble() * 2.0D - 1.0D) * AMBIENT_PARTICLE_RANGE;
        double z = pos.getZ() + 0.5D + (random.nextDouble() * 2.0D - 1.0D) * AMBIENT_PARTICLE_RANGE;

        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        if (random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Mob mob) || !mob.isAlive()) {
            return;
        }

        this.applyWispImpact(serverLevel, pos, mob);
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.5D;
        double cz = pos.getZ() + 0.5D;
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.gameEvent(entity, GameEvent.ENTITY_DIE, pos);
        serverLevel.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof WispBlockEntity blockEntity && blockEntity.getOwner() != null) {
            WispBlockTracker.get(serverLevel).unregister(serverLevel, pos, blockEntity.getOwner());
        }

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

    private void applyWispImpact(ServerLevel level, BlockPos pos, Mob mob) {
        if (mob.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            MobEffectInstance effect = mob.getEffect(MobEffects.FIRE_RESISTANCE);
            int newDuration = Math.max(0, effect.getDuration() - 1200);
            mob.removeEffect(MobEffects.FIRE_RESISTANCE);
            if (newDuration > 0) {
                mob.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        newDuration,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
            }
            return;
        }

        mob.setSecondsOnFire(4);
        if (level.getBlockEntity(pos) instanceof WispBlockEntity blockEntity && blockEntity.getOwner() != null) {
            Player owner = level.getServer().getPlayerList().getPlayer(blockEntity.getOwner());
            if (owner != null) {
                mob.hurt(level.damageSources().playerAttack(owner), WisplightRodItem.SMALL_WISP_DAMAGE);
                return;
            }
        }

        mob.hurt(level.damageSources().generic(), WisplightRodItem.SMALL_WISP_DAMAGE);
    }
}
