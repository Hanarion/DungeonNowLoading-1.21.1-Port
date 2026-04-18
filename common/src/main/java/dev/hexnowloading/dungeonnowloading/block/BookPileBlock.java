package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.BookPileBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class BookPileBlock extends PileBlock implements EntityBlock {
    protected static final VoxelShape ONE_AABB = Block.box(0, 0, 0, 16, 3,16);
    protected static final VoxelShape TWO_AABB = Block.box(0, 0, 0, 16, 8,16);
    protected static final VoxelShape THREE_AABB = Block.box(0, 0, 0, 16, 10,16);
    protected static final VoxelShape FOUR_AABB = Block.box(0, 0, 0, 16, 10,16);

    public BookPileBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(WATERLOGGED, PILE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        switch (state.getValue(PILE)) {
            case 1:
            default:
                return ONE_AABB;
            case 2:
                return TWO_AABB;
            case 3:
                return THREE_AABB;
            case 4:
                return FOUR_AABB;
        }
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockState.is(this)) {
            return blockState.setValue(PILE, Integer.valueOf(Math.min(4, blockState.getValue(PILE) + 1)));
        } else {
            FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            return super.getStateForPlacement(ctx).setValue(WATERLOGGED, Boolean.valueOf(flag));
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) && state.getValue(PILE) < 4 ? true : super.canBeReplaced(state, ctx);
    }

    @Override
    public boolean isPathfindable(BlockState p_154341_, BlockGetter p_154342_, BlockPos p_154343_, PathComputationType p_154344_) {
        return false;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BookPileBlockEntity pileBe)) return;

        // Read from item BlockEntityTag (same pattern as chests/shulkers)
        CompoundTag bet = stack.getTagElement("BlockEntityTag");
        if (bet != null && bet.contains("LootTable", Tag.TAG_STRING)) {
            ResourceLocation id = new ResourceLocation(bet.getString("LootTable"));
            long seed = bet.contains("LootTableSeed", Tag.TAG_LONG) ? bet.getLong("LootTableSeed") : level.getRandom().nextLong();
            pileBe.setLootTable(id, seed);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && blockEntity instanceof BookPileBlockEntity pileBe) {
            ResourceLocation tableId = pileBe.getLootTable();

            if (tableId != null) {
                LootTable table = serverLevel.getServer().getLootData().getLootTable(tableId);

                LootParams.Builder builder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.BLOCK_STATE, state)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                        .withOptionalParameter(LootContextParams.TOOL, tool)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, player);

                for (ItemStack drop : table.getRandomItems(builder.create(LootContextParamSets.BLOCK))) {
                    popResource(serverLevel, pos, drop);
                }

                // Prevent vanilla block loot from ALSO dropping (avoid double drops)
                // Make sure your normal block loot table (blocks/book_pile.json) drops nothing.
            }
        }

        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // If we have a BE and it has a stored loot table -> use it (chest-style)
        if (builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY)
                instanceof BookPileBlockEntity be) {

            ResourceLocation tableId = be.getLootTable();
            Level level = builder.getLevel();
            if (tableId != null) {
                var lootTable = level.getServer().getLootData().getLootTable(tableId);

                // Run the stored loot table
                return lootTable.getRandomItems(builder.create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK));
            }
        }

        // fallback to normal block loot table behavior if none stored
        return super.getDrops(state, builder);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BookPileBlockEntity(pos, state);
    }
}
