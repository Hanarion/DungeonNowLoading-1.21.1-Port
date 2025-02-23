package dev.hexnowloading.dungeonnowloading.block;

import com.google.common.collect.ImmutableList;
import dev.hexnowloading.dungeonnowloading.block.property.MendingRunes;
import dev.hexnowloading.dungeonnowloading.registry.DNLProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class PreserverBlock extends Block {

    public static final EnumProperty<MendingRunes> RUNES = DNLProperties.MENDING_RUNES;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final static ImmutableList<MendingRunes> RANDOM_RUNE = ImmutableList.of(
            MendingRunes.M_OFF,
            MendingRunes.E_OFF,
            MendingRunes.N_OFF,
            MendingRunes.D_OFF,
            MendingRunes.I_OFF,
            MendingRunes.G_OFF
    );
    private boolean hasBeenRemovedByCreativePlayer;

    public PreserverBlock(Properties $$0) {
        super($$0);
        hasBeenRemovedByCreativePlayer = false;
        this.registerDefaultState(this.defaultBlockState().setValue(RUNES, MendingRunes.M_OFF));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNES);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        RandomSource randomSource = level.getRandom();
        return this.defaultBlockState().setValue(RUNES, RANDOM_RUNE.get(randomSource.nextInt(RANDOM_RUNE.size())));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, world, pos, newState, isMoving);

        if (!state.is(newState.getBlock()) && !hasBeenRemovedByCreativePlayer) {
            placeRuneBlock(world, pos, state);
        }
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(world, player, pos, state, blockEntity, tool);
        hasBeenRemovedByCreativePlayer = false;
        if (player.isCreative()) {
            hasBeenRemovedByCreativePlayer = true;
        } else {
            placeRuneBlock(world, pos, state);
        }
    }

    private void placeRuneBlock(Level level, BlockPos blockPos, BlockState blockState) {
        MendingRunes rune = blockState.getValue(RUNES);
        MendingRunes newRune = switch (rune) {
            case M_OFF, M_ON -> MendingRunes.M_ON;
            case E_OFF, E_ON -> MendingRunes.E_ON;
            case N_OFF, N_ON -> MendingRunes.N_ON;
            case D_OFF, D_ON -> MendingRunes.D_ON;
            case I_OFF, I_ON -> MendingRunes.I_ON;
            case G_OFF, G_ON -> MendingRunes.G_ON;

        };
        level.setBlock(blockPos, blockState.setValue(RUNES, newRune), Block.UPDATE_CLIENTS);
        if (!level.isClientSide) {
            level.scheduleTick(blockPos, this, 20);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!serverLevel.getBlockTicks().hasScheduledTick(blockPos, this)) {
            MendingRunes rune = blockState.getValue(RUNES);
            MendingRunes newRune = switch (rune) {
                case M_OFF, M_ON -> MendingRunes.M_OFF;
                case E_OFF, E_ON -> MendingRunes.E_OFF;
                case N_OFF, N_ON -> MendingRunes.N_OFF;
                case D_OFF, D_ON -> MendingRunes.D_OFF;
                case I_OFF, I_ON -> MendingRunes.I_OFF;
                case G_OFF, G_ON -> MendingRunes.G_OFF;
            };
            serverLevel.setBlock(blockPos, blockState.setValue(RUNES, newRune), Block.UPDATE_CLIENTS);
        }
    }
}
