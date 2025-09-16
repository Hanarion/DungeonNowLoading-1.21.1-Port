package dev.hexnowloading.dungeonnowloading.block;

import com.mojang.authlib.GameProfile;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CPedestalOpenEditorPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Arrays;

public class PlayerStatueBlock extends BaseEntityBlock implements EntityBlock, SimpleWaterloggedBlock {

    //public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Slim pedestal footprint (entities won’t get stuck as easily); render can extend higher.
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0); // 10x10, 1 block tall

    // Match this to however many poses your Blockbench export supports (or make it configurable)
    public static final int MAX_POSES = 4;
    public static final boolean ENABLE_SIGN_EDIT = false;

    public PlayerStatueBlock(Properties props) {
        super(props);
        registerDefaultState(this.stateDefinition.any()
                .setValue(ROTATION, 0)
                .setValue(WATERLOGGED, false));
    }

    // ---- placement / states ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        int rot = (Mth.floor((ctx.getRotation() * 16.0F / 360.0F) + 0.5F)) & 15;
        return this.defaultBlockState()
                .setValue(ROTATION, rot)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(ROTATION, rot.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ROTATION, WATERLOGGED);
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
    public RenderShape getRenderShape(BlockState state) {
        // Use BER (BlockEntityRenderer) to draw the full-body model
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
        if (!(be instanceof PlayerStatueBlockEntity statue)) return;

        GameProfile gp = null;
        CompoundTag tag = stack.getTag();

        if (tag != null) {
            if (tag.contains("Owner", 10)) gp = NbtUtils.readGameProfile(tag.getCompound("Owner"));
            else if (tag.contains("SkullOwner", 10)) gp = NbtUtils.readGameProfile(tag.getCompound("SkullOwner"));
            else if (tag.contains("SkullOwner", 8))  gp = new GameProfile(null, tag.getString("SkullOwner"));
        }

        // Owner / SkullOwner
        /*if (tag != null) {
            if (tag.contains("Owner", 10)) {
                gp = NbtUtils.readGameProfile(tag.getCompound("Owner"));
            } else if (tag.contains("SkullOwner", 10)) {
                gp = NbtUtils.readGameProfile(tag.getCompound("SkullOwner"));
            } else if (tag.contains("SkullOwner", 8)) {
                gp = new GameProfile(null, tag.getString("SkullOwner"));
            }
        }*/
        if (gp == null) gp = new GameProfile(null, "MHF_Alex");
        statue.setOwner(gp);

        // Restore offering if present in the item NBT
        if (tag != null && tag.contains("Offering", 10)) {
            ItemStack off = ItemStack.of(tag.getCompound("Offering"));
            if (!off.isEmpty()) statue.placeOffering(off);
        } else if (tag != null && tag.contains("DNL_Notch", 8)) {
            // Rebuild offering from tier if item only stored the tier
            var tier = PlayerStatueBlockEntity.NotchTier.fromString(tag.getString("DNL_Notch"));
            if (tier != PlayerStatueBlockEntity.NotchTier.NONE && !statue.hasOffering()) {
                ItemStack off = PlayerStatueBlockEntity.defaultItemForTier(tier);
                if (!off.isEmpty()) statue.placeOffering(off);
            }
        }
    }

    // ---- interaction: Brush to cycle pose ----

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        ItemStack held = player.getItemInHand(hand);
        var be = level.getBlockEntity(pos);
        if (!(be instanceof PlayerStatueBlockEntity statue)) return InteractionResult.PASS;

        // ---- PEDESTAL MATERIAL INTERACTION (server) ----
        if (!level.isClientSide) {
            // Waxed statues block material edits
            if (statue.isWaxed()) {
                level.playSound(null, pos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.PASS;
            }

            // (A) TAKE offering: allowed even if the player's hand is NOT empty
            if (statue.hasOffering()) {
                ItemStack out = statue.takeOffering();
                if (!out.isEmpty()) {
                    boolean added = player.addItem(out);
                    if (!added) player.drop(out, false);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);

                    // "In the Name of (Player Name)"
                    String ownerName = "Someone";
                    var gp = statue.getOwner();
                    if (gp != null && gp.getName() != null && !gp.getName().isEmpty()) ownerName = gp.getName();
                    Component msg = Component.translatable(
                            "block.dungeonnowloading.player_statue.message",
                            Component.literal(ownerName).withStyle(ChatFormatting.GOLD)
                    );
                    player.displayClientMessage(msg, true);
                }
                return InteractionResult.CONSUME;
            }

            // (B) PLACE offering: only when empty; don't consume in Creative
            var tier = PlayerStatueBlockEntity.NotchTier.NONE;
            tier = PlayerStatueBlockEntity.tierFromItem(held);
            if (tier != PlayerStatueBlockEntity.NotchTier.NONE) {
                // give BE a single copy
                ItemStack one = held.copy();
                one.setCount(1);
                if (statue.placeOffering(one)) {
                    if (!player.getAbilities().instabuild) { // survival/adventure only
                        held.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, 1.2f);
                    return InteractionResult.CONSUME;
                }
            }
        } else {
            // client: optimistic success for either take or place
            if (statue.hasOffering() || PlayerStatueBlockEntity.tierFromItem(held) != PlayerStatueBlockEntity.NotchTier.NONE) {
                return InteractionResult.SUCCESS;
            }
        }

        // Keep your sign-editing behind the flag, unchanged
        if (!ENABLE_SIGN_EDIT) return InteractionResult.PASS;

        // --- BRUSH: cycle pose (like you had) ---
        if (held.is(Items.BRUSH)) {
            if (!level.isClientSide) {
                int dir = player.isShiftKeyDown() ? -1 : +1;
                int next = Math.floorMod(statue.getPoseVariant() + dir, MAX_POSES);
                statue.setPoseVariant(next);
                statue.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.6f, 1.2f);
                level.levelEvent(2001, pos, Block.getId(state));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // --- CLIENT: wait for server to decide (just like SignBlock) ---
        if (level.isClientSide) {
            // If you want to mimic SignBlock exactly, return CONSUME here
            // so the client waits for the server to send the open-editor screen.
            return statue.isWaxed() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        // --- SERVER below ---

        // If waxed: block edits (mirror sign behavior)
        if (statue.isWaxed()) {
            level.playSound(null, pos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.PASS;
        }

        // If another player is editing, block (mirror sign)
        if (otherPlayerIsEditing(player, statue)) {
            return InteractionResult.PASS;
        }

        // --- DYE / GLOW / UNGLOW like signs (only if not waxed and editable) ---
        if (held.getItem() instanceof DyeItem dye) {
            statue.setAllText(Arrays.asList(statue.getLines()), dye.getDyeColor(), statue.isGlowingText());
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
            if (!player.isCreative()) held.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.GLOW_INK_SAC)) {
            statue.setAllText(Arrays.asList(statue.getLines()), statue.getTextColor(), true);
            level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
            if (!player.isCreative()) held.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.INK_SAC)) {
            statue.setAllText(Arrays.asList(statue.getLines()), statue.getTextColor(), false);
            level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
            if (!player.isCreative()) held.shrink(1);
            return InteractionResult.SUCCESS;
        }

        // (Optional) Honeycomb/Axe for wax toggle like signs:
        if (held.is(Items.HONEYCOMB)) {
            if (statue.setWaxed(true)) {
                level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!player.isCreative()) held.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        if (held.is(Items.IRON_AXE) || held.is(Items.DIAMOND_AXE) || held.is(Items.NETHERITE_AXE) || held.is(Items.GOLDEN_AXE) || held.is(Items.STONE_AXE) || held.is(Items.WOODEN_AXE)) {
            if (statue.setWaxed(false)) {
                level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // Server: empty hand opens editor
        if (!level.isClientSide && held.isEmpty() && player instanceof ServerPlayer sp) {
            if (statue.isWaxed()) return InteractionResult.PASS;

            statue.setAllowedEditor(sp.getUUID());
            statue.setChanged();

            // ✅ send S2C packet; the client handler will open the screen on the render thread
            Services.NETWORK.sendToPlayer(
                    new S2CPedestalOpenEditorPacket(
                            pos,
                            java.util.Arrays.asList(statue.getLines()),
                            statue.getTextColor(),
                            statue.isGlowingText()
                    ),
                    sp
            );
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static boolean otherPlayerIsEditing(Player player, PlayerStatueBlockEntity be) {
        var lock = be.getAllowedEditor();
        return lock != null && !lock.equals(player.getUUID());
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide
                ? createTickerHelper(type, DNLBlockEntityTypes.PLAYER_STATUE.get(), PlayerStatueBlockEntity::serverTick)
                : null;
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
            stack.getOrCreateTag().putString("DNL_Notch", statue.getNotchTier().name());
        }
        return stack;
    }
}
