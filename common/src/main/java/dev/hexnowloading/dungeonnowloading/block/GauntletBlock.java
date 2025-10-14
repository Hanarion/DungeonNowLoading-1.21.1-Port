package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.GauntletBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CGauntletOpenEditorPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GauntletBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    // 14x6x14 box: (1..15, 0..6, 1..15)
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 6, 15);

    public GauntletBlock(Properties props) {
        super(props.noOcclusion()); // lets the BER handle visuals + avoids full-cube occlusion
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ACTIVE, FACING);
    }

    // Face the player on placement
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // Auto-placement of pedestal moved to GauntletBlockItem; no action needed here now.
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    // Rotation / mirror support for structure blocks, etc.
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // Selection outline (what you see) and collision (what you bump into)
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    // Let light pass like a non-full cube (nice for a brazier)
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GauntletBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        if (!player.getAbilities().instabuild) return InteractionResult.PASS; // creative only
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS; // empty hand to avoid conflicts

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GauntletBlockEntity g) {
            Services.NETWORK.sendToPlayer(new S2CGauntletOpenEditorPacket(
                    pos,
                    g.getWavesTotal(), g.getWavesCurrent(), g.isActive(),
                    g.getRelX(), g.getRelY(), g.getRelZ(),
                    g.getSizeX(), g.getSizeY(), g.getSizeZ(),
                    g.getActivationRange(),
                    g.getLootTable() == null ? "" : g.getLootTable().toString(),
                    g.getTestWave()
            ), sp);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    // When removed, also remove the pedestal below (without drops) if present.
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.is(DNLBlocks.GAUNTLET_VAULT.get())) {
                level.destroyBlock(below, false); // don't drop pedestal separately
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, DNLBlockEntityTypes.GAUNTLET.get(), GauntletBlockEntity::clientTick) : null;
    }
}
