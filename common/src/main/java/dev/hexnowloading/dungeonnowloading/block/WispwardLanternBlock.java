package dev.hexnowloading.dungeonnowloading.block;

import com.mojang.serialization.MapCodec;

import dev.hexnowloading.dungeonnowloading.block.entity.WispwardChestBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.WispwardLanternBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CWispwardLanternOpenConfigPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WispwardLanternBlock extends BaseEntityBlock {

    public static final MapCodec<WispwardLanternBlock> CODEC = simpleCodec(WispwardLanternBlock::new);

    @Override
    public MapCodec<WispwardLanternBlock> codec() {
        return CODEC;
    }
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final int TIMED_UNLIGHT_DELAY_TICKS = 20;
    private static final VoxelShape SHAPE = Block.box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);

    private final boolean timed;

    public WispwardLanternBlock(Properties properties) {
        this(properties, false);
    }

    public WispwardLanternBlock(Properties properties, boolean timed) {
        super(properties);
        this.timed = timed;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.FALSE));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WispwardLanternBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!this.timed || !player.getAbilities().instabuild) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        if (level.getBlockEntity(pos) instanceof WispwardLanternBlockEntity lantern && player instanceof ServerPlayer serverPlayer) {
            Services.NETWORK.sendToPlayer(new S2CWispwardLanternOpenConfigPacket(pos, lantern.getTimerSeconds()), serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(state.getBlock()) && level instanceof ServerLevel server) {
            WispwardChestBlockEntity.notifyLanternPlaced(server, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (this.timed && state.getValue(LIT) && level instanceof ServerLevel server) {
            server.scheduleTick(pos, this, getTimedDelayTicks(server, pos));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            WispwardChestBlockEntity.notifyLanternRemoved(server, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.timed && state.getValue(LIT)) {
            if (level.getBlockEntity(pos) instanceof WispwardLanternBlockEntity lantern) {
                if (lantern.isLockedLit()) {
                    return;
                }

                if (!lantern.shouldTurnOff(level.getGameTime())) {
                    level.scheduleTick(pos, this, lantern.getRemainingLitTicks(level.getGameTime()));
                    return;
                }
            }
            level.setBlock(pos, state.setValue(LIT, Boolean.FALSE), Block.UPDATE_ALL);
            WispwardChestBlockEntity.notifyLanternChanged(level, pos);
        }
    }

    public static boolean lightFromWisp(ServerLevel level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof WispwardLanternBlock) || state.getValue(LIT)) {
            return false;
        }

        level.setBlock(pos, state.setValue(LIT, Boolean.TRUE), Block.UPDATE_ALL);
        if (((WispwardLanternBlock) state.getBlock()).timed) {
            if (level.getBlockEntity(pos) instanceof WispwardLanternBlockEntity lantern) {
                lantern.markLit(level.getGameTime());
            }
            level.scheduleTick(pos, state.getBlock(), getTimedDelayTicks(level, pos));
        }
        WispwardChestBlockEntity.notifyLanternChanged(level, pos);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.55D;
        double z = pos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.FLAME, x, y, z, 12, 0.22D, 0.28D, 0.22D, 0.03D);
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 4, 0.18D, 0.20D, 0.18D, 0.01D);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.65F, 1.2F);
        return true;
    }

    public static int lightEmission(BlockState state) {
        return state.getValue(LIT) ? 14 : 0;
    }

    private static int getTimedDelayTicks(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof WispwardLanternBlockEntity lantern) {
            return lantern.getTimerSeconds() * 20;
        }
        return TIMED_UNLIGHT_DELAY_TICKS;
    }
}
