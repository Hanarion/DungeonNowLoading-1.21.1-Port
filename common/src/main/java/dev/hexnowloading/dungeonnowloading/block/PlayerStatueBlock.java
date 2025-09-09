package dev.hexnowloading.dungeonnowloading.block;

import com.mojang.authlib.GameProfile;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class PlayerStatueBlock extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Slim pedestal footprint (entities won’t get stuck as easily); render can extend higher.
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 16, 13); // 10x10, 1 block tall

    // Match this to however many poses your Blockbench export supports (or make it configurable)
    public static final int MAX_POSES = 4;

    public PlayerStatueBlock(Properties props) {
        super(props);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    // ---- placement / states ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    // ---- shapes / render ----

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        // Use BER (BlockEntityRenderer) to draw the full-body model
        return net.minecraft.world.level.block.RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // ---- block entity ----

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DNLBlockEntityTypes.PLAYER_STATUE.get().create(pos, state);
    }

    // ---- placement: feed owner & optional pose from item NBT ----

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;

        var be = level.getBlockEntity(pos);
        if (be instanceof PlayerStatueBlockEntity statue) {
            GameProfile gp = null;

            // Prefer Owner from item NBT if present
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Owner", 10)) {
                gp = NbtUtils.readGameProfile(tag.getCompound("Owner"));
            }

            // Otherwise use the placer’s profile
            if (gp == null && placer instanceof Player p) {
                gp = p.getGameProfile();
            }

            if (gp != null) {
                statue.setOwner(gp); // calls sendBlockUpdated
            }
        }
    }

    // ---- interaction: Brush to cycle pose ----

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(level.getBlockEntity(pos) instanceof PlayerStatueBlockEntity statue)) return InteractionResult.PASS;

        boolean isBrush = held.is(Items.BRUSH);
        if (isBrush) {
            if (!level.isClientSide) {
                int dir = player.isShiftKeyDown() ? -1 : +1;
                int next = Math.floorMod(statue.getPoseVariant() + dir, MAX_POSES);
                statue.setPoseVariant(next);
                statue.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);

                // tiny feedback
                level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.6f, 1.2f);
                level.levelEvent(2001, pos, Block.getId(state)); // particles
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    // ---- pick-block: preserve owner & pose on the item ----

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(world, pos, state);
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PlayerStatueBlockEntity statue) {
            // Prefer simple string SkullOwner in the item (your renderer resolves profile later)
            GameProfile gp = statue.getOwner();
            if (gp != null && gp.getName() != null && !gp.getName().isEmpty()) {
                stack.getOrCreateTag().putString("SkullOwner", gp.getName());
            } else if (gp != null) {
                // fallback to compound if name unknown
                stack.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gp));
            }
            stack.getOrCreateTag().putInt("DNL_Pose", statue.getPoseVariant());
        }
        return stack;
    }
}
