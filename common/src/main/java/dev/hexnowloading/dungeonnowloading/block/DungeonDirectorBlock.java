package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class DungeonDirectorBlock extends Block implements EntityBlock {

    public DungeonDirectorBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getHorizontalDirection().getOpposite());
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonDirectorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide) return InteractionResult.SUCCESS;

        // If holding zone wand, let item handle click (sets region)
        if (player.getItemInHand(hand).is(DNLItems.ZONE_WAND.get())) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DungeonDirectorBlockEntity director)) return InteractionResult.PASS;

        // Authoring actions are creative-only
        if (!player.getAbilities().instabuild) {
            return InteractionResult.PASS; // don’t spam survival players
        }

        // Shift-right-click: toggle bake/unbake
        if (player.isCrouching()) {
            int n;
            if (!director.isBaked()) {
                n = director.bakeFromWorldSpawnNodes(); // <- rename if you kept old name
                player.displayClientMessage(Component.literal("Baked " + n + " Spawn Nodes into Director."), true);
            } else {
                n = director.restoreSpawnNodesToWorld(); // <- rename if you kept old name
                player.displayClientMessage(Component.literal("Restored " + n + " Spawn Nodes from Director."), true);
            }

            director.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }

        // Optional: hint on normal right-click (creative only)
        player.displayClientMessage(Component.literal("Shift-right-click to bake/unbake Spawn Nodes."), true);
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;

        return (lvl, p, st, be) -> {
            if (be instanceof DungeonDirectorBlockEntity director) {
                DungeonDirectorBlockEntity.serverTick(lvl, p, st, director);
            }
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof DungeonDirectorBlockEntity director) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            director.setAuthoredFacing(facing); // add setter
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }
}
