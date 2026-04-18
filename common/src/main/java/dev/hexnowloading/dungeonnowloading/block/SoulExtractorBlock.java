package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.SoulExtractorBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
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
import org.jetbrains.annotations.Nullable;

public class SoulExtractorBlock extends BaseEntityBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SoulExtractorBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    /* ---------------- State ---------------- */

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    /* ---------------- Redstone pause + edge handling ---------------- */

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SoulExtractorBlockEntity se) {
                    se.setPaused(powered);
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    /* ---------------- Interactions ---------------- */

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SoulExtractorBlockEntity se)) return InteractionResult.PASS;

        // Locked: prevent changes (still allow comparator reads, etc.)
        if (se.isLocked()) {
            return InteractionResult.CONSUME;
        }

        // Apply Dungeon Locker
        if (!held.isEmpty() && held.is(Items.STICK)) {
            se.setLocked(true);
            if (!player.isCreative()) held.shrink(1);
            player.displayClientMessage(Component.translatable("block.dnl.soul_extractor.locked"), true);
            return InteractionResult.CONSUME;
        }

        // Set filter from Spawn Egg (prototype)
        if (!held.isEmpty() && held.getItem() instanceof SpawnEggItem egg) {
            // 1.20.1 mappings: this variant is common; adjust if your mappings differ
            EntityType<?> type = egg.getType(held.getTag());
            if (type != null) {
                se.setFilter(type);
                player.displayClientMessage(Component.translatable("block.dnl.soul_extractor.filtered", type.toShortString()), true);
            } else {
                player.displayClientMessage(Component.translatable("block.dnl.soul_extractor.spawn_egg_invalid"), true);
            }
            return InteractionResult.CONSUME;
        }

        // Clear filter with shift + empty hand (QoL)
        if (held.isEmpty() && player.isShiftKeyDown()) {
            se.clearFilter();
            player.displayClientMessage(Component.translatable("block.dnl.soul_extractor.filter_cleared"), true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    /* ---------------- Comparator ---------------- */

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SoulExtractorBlockEntity se) {
            return Math.max(0, Math.min(15, se.getSouls()));
        }
        return 0;
    }

    /* ---------------- BlockEntity ---------------- */

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DNLBlockEntityTypes.SOUL_EXTRACTOR.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, DNLBlockEntityTypes.SOUL_EXTRACTOR.get(), SoulExtractorBlockEntity::serverTick);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SoulExtractorBlockEntity se) {
                se.setPaused(powered);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState $blockState0) {
        return RenderShape.MODEL;
    }
}
