package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.WispwardChestBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CWispwardChestOpenConfigPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WispwardChestBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public WispwardChestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof WispwardChestBlockEntity chest) {
            chest.initializeLootTable(server);
        }
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!oldState.is(newState.getBlock())) {
            super.onRemove(oldState, level, pos, newState, moved);
        }
    }

    public static boolean isChestBlockedByBlock(BlockGetter level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isRedstoneConductor(level, above);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.getMainHandItem().is(DNLItems.ZONE_WAND.get()) || player.getOffhandItem().is(DNLItems.ZONE_WAND.get())) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        if (!(level.getBlockEntity(pos) instanceof WispwardChestBlockEntity chest)) {
            return locked(level, player, pos, 1);
        }

        if (player.getAbilities().instabuild && level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            chest.refreshLanternCache(server);
            Services.NETWORK.sendToPlayer(new S2CWispwardChestOpenConfigPacket(pos, chest.getConfiguredLootTable(), Math.max(1, chest.getEffectiveRequiredLitLanterns())), serverPlayer);
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel server) {
            chest.refreshLanternCache(server);
        }

        if (!chest.hasRequiredLanternsLit()) {
            return locked(level, player, pos, Math.max(1, chest.getEffectiveRequiredLitLanterns()));
        }

        player.displayClientMessage(Component.translatable("warning.dungeonnowloading.cannot_open_wispward_chest"), true);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult locked(Level level, Player player, BlockPos pos, int requiredLitLanterns) {
        player.displayClientMessage(Component.translatable("warning.dungeonnowloading.cannot_open_wispward_chest", requiredLitLanterns), true);
        playSound(level, pos, SoundEvents.CHEST_LOCKED);
        return InteractionResult.SUCCESS;
    }

    private static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WispwardChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, DNLBlockEntityTypes.WISPWARD_CHEST.get(), WispwardChestBlockEntity::serverTick);
    }
}
